package mas.core;

import java.io.IOException;

import mas.gui.MyGuiImpl;
import mas.io.Reader;
import mas.io.Writer;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/*
 * Mediator agent
 * all messages are managed 
 * by this class instance
 */
@SuppressWarnings("serial")
public class EnvironmentAgent extends Agent {

	private MyGuiImpl _myGui;
	private Reader _reader;
	private Writer _writer;
	private Cell[][] _cells;

	protected void setup() {
		try
		{
			_reader = new Reader(); //read data from file
			_cells = Converter.convert(_reader); //convert it to table format
			_myGui = new MyGuiImpl(_cells, _reader); //initialize the gui

			addBehaviour(new InformBehaviour()); //start the informer server 
			addBehaviour(new ExecutorBehaviour()); //start the executor of action requests
			addBehaviour(new MediatorBehaviour()); //start the negotiation mediator server

			CreateAgents(_reader.AGENTS); //creates the agents

			_writer = new Writer(true); //creates the logger
			addBehaviour(new UpdateBehaviour(this)); //starts the timer
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		//log initial data
		_writer.write(_reader.GlobalVariables.toString());
		_writer.write(_reader.Agents.toString());
		_writer.write(_reader.Objects.toString());

		//Display a system ready message
		System.out.println("Environment agent - " + this.getLocalName() + " ready .");
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		_reader.Clear();
		_writer.Clear();
		// Printout a dismissal message
		System.out.println("Environment " + getAID().getName() + " terminating.");
	}

	//Create agent method 1
	/*
	 * Creates the agents and starts them
	 * Each agent will receive the mediator AID
	 * and the update interval 
	 */
	private void CreateAgents(String[] names)
	{
		ContainerController cc = getContainerController();
		try {
			for (String name : names)
			{
				AgentController ac = cc.createNewAgent(
						name, 
						"mas.core.WorkerAgent", 
						new Object[] {getAID(), _reader.GlobalVariables.get(Reader.SINGLE_OPERATION_TIME)});
				ac.start();
			}
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Gui grid update
	 */
	public void updateCell(int row, int column, Cell cell)
	{
		_myGui.updateCell(row, column, cell);
	}

	/*
	 * Gui score update
	 */
	public void updateScore(int score, String color)
	{
		_myGui.updateScore(score, color);
	}

	/*
	 * Gui transfer points
	 */
	public void transferPoints(int points, String sender, String receiver)
	{
		_myGui.transferPoints(points, sender, receiver);
	}

	/*
	 * Ticker behavior that updates the gui timer
	 * at the specified intervals of time
	 */
	class UpdateBehaviour extends TickerBehaviour
	{
		public UpdateBehaviour(Agent a) 
		{
			super(a, _reader.GlobalVariables.get(Reader.SINGLE_OPERATION_TIME));
		}

		/*
		 * If the global time elapsed stop the simulation
		 * otherwise update the gui timer
		 */
		@Override
		protected void onTick() {
			if (timeElapsed())
			{
				stop();
				removeBehaviour(this); //Removes this behavior
				addBehaviour(new ShutDownBehaviour()); //Tells AMS to stop all agents
			}
			else
			{
				_myGui.updateTime(_reader.GlobalVariables.get(Reader.SINGLE_OPERATION_TIME));
			}
		}

		/*
		 * Get elapsed time from the simulation start point
		 */
		public boolean timeElapsed()
		{
			return _myGui.getTime() >= _reader.GlobalVariables.get(Reader.TOTAL_SIMULATION_TIME);
		}
	}

	/*
	 * AMS stop all registered agents
	 */
	class ShutDownBehaviour extends OneShotBehaviour
	{
		@Override
		public void action() 
		{
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("shutDown");
			template.addServices(sd);
			DFAgentDescription[] result;
			KillAgent ka = new KillAgent();
			Action action = new Action(getAMS(), ka);
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.setLanguage(new SLCodec().getName());
			request.setOntology(JADEManagementOntology.NAME);
			request.addReceiver(action.getActor());
			getContentManager().registerLanguage(new SLCodec());
			getContentManager().registerOntology(JADEManagementOntology.getInstance());

			try {
				result = DFService.search(myAgent, template);

				for (int i = 0; i < result.length; ++i)
				{
					ka.setAgent(result[i].getName()); // AID of the agent you want to kill
					getContentManager().fillContent(request, action);
					send(request);
				}
			} catch (Exception e){//CodecException | OntologyException | FIPAException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Server that responds to information request about the environment
	 * map with agents, tiles, holes and obstacles positions
	 */
	class InformBehaviour extends CyclicBehaviour
	{
		MessageTemplate mt = MessageTemplate.MatchConversationId("information_request");

		public void action()
		{
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null)  //if an agent requests for information
			{
				try 
				{
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM); 
					reply.setContentObject(_cells); //send the map as a serialized object
					myAgent.send(reply);
				} catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			else
				block();
		}
	}

	/*
	 * Action request executor behavior
	 */
	class ExecutorBehaviour extends CyclicBehaviour
	{
		MessageTemplate mt = MessageTemplate.MatchConversationId("request_execution");

		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null)  //if a request is received
			{
				ACLMessage reply = msg.createReply();
				try {
					IAction action = (IAction)msg.getContentObject(); //Get the serialized action
					if (action.Verify(_cells)) //if it is possible
					{
						action.Execute(_cells, (EnvironmentAgent) myAgent); //execute it
						_writer.write(action.toString()); //log it
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL); //respond with success
					}
					else
					{
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL); //otherwise reject request
					}
					myAgent.send(reply);	
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			}
			else
				block();
		}
	}


	/*
	 * Action request executor behavior
	 */
	class MediatorBehaviour extends CyclicBehaviour
	{
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		MessageTemplate mt = MessageTemplate.MatchConversationId("negotiation");
		DFAgentDescription[] result;
		String agent;
		AID sender;

		public MediatorBehaviour()
		{
			sd.setType("shutDown");
			template.addServices(sd);
		}

		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null)  //if a message is received
			{
				msg.clearAllReceiver(); //clears the previous receiver
				sender = msg.getSender(); 
				//try sending to all other agents
				//further they will communicate alone
				try 
				{
					//msg.setSender(sender);
					result = DFService.search(myAgent, template);
					for (int i = 0; i < result.length; ++i)
						if (!sender.toString().equals(result[i].getName().toString()))
							msg.addReceiver(result[i].getName());
					send(msg);
				} 
				catch (FIPAException e) 
				{
					e.printStackTrace();
				}
			}
			else
				block();
		}
	}


	//Create agent method 2
	/*
		private void CreateAgents()
		{
			CreateAgent ca = new CreateAgent();
			ca.setAgentName("john");
			ca.setClassName("mas.core.WorkerAgent");
			ca.setContainer(new ContainerID("Main-Container", null));
			Action actExpr = new Action(getAMS(), ca);
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.addReceiver(getAMS());
			request.setOntology(JADEManagementOntology.getInstance().getName());
			request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			try {
				getContentManager().registerLanguage(new SLCodec());
				getContentManager().registerOntology(JADEManagementOntology.getInstance());
				getContentManager().fillContent(request, actExpr);
				addBehaviour(new AchieveREInitiator(this, request) {
					protected void handleInform(ACLMessage inform) {
						System.out.println("Agent successfully created");
					}
					protected void handleFailure(ACLMessage failure) {
						System.out.println("Error creating agent.");
						System.out.println(failure.getContent());
					}
				});
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	 */
}