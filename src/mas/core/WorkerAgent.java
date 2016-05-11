package mas.core;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

enum State {sending_request, waiting_for_response, finishing}
enum Act {move, pick}

/*
 * The worker agent
 */
@SuppressWarnings("serial")
public class WorkerAgent extends Agent 
{
	private Cell[][] map = null; //internal map
	private AID environment;	//parent agent = mediator
	private String name;		//agent's name == color
	private int wait = 0;		//wait interval
	private double transfer = 0;	//estimated cost per movement

	private int canAccept = 0; //the price of the accepted proposal
	private boolean canPropose = true; //can make one proposal at a time and waits for status report
	private NegotiationBehaviour nBehaviour = null;
	private WorkingBehaviour wBehaviour = null;

	private Point from, to; //tile and hole position received through negotiation
	private String color; //color of the tile
	private AID proposer; //the other agent

	protected void setup() 
	{
		Object[] args = getArguments();
		name = getAID().getName().split("@")[0]; //get it's name == color
		if (args != null && args.length == 2)
		{
			environment = (AID) args[0]; // get mediator AID
			wait = (Integer) args[1];	// get step interval

			//Register a service for shutdown operation
			//also used for all agent discovery
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("shutDown");
			sd.setName("JADE-mangement");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}

			addBehaviour(new InformationRequest()); //first time request information
			//display agent ready message
			System.out.println("Worker agent - " + this.getLocalName() + " is ready .");
		}
		else
			System.out.println("Worker agent - " + this.getLocalName() + " not ehough info.");
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
			removeBehaviour(wBehaviour);
			if (nBehaviour != null)
				removeBehaviour(nBehaviour);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Worker "+getAID().getName()+" terminating.");
	}

	//Information request behavior after execution it
	//starts the working behavior and is called again 
	//when something is wrong
	class InformationRequest extends Behaviour
	{
		MessageTemplate mt;
		State state = State.sending_request;
		public void action() {
			switch (state) {
			case sending_request: //sending the request 
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
				request.setConversationId("information_request");
				request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
				request.addReceiver(environment);
				myAgent.send(request);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("information_request"),
						MessageTemplate.MatchInReplyTo(request.getReplyWith()));
				state = State.waiting_for_response;
				break;
			case waiting_for_response: //waiting foe response
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.INFORM)
					{
						try 
						{
							map = (Cell[][]) reply.getContentObject();
						} 
						catch (UnreadableException e) 
						{
							e.printStackTrace();
						}
						finally
						{
							state = State.finishing; 
						}
					}
				}		
				else
					block();
				break;
			case finishing:
				break;
			}
		}

		public boolean done() {
			if (state == State.finishing)
			{
				myAgent.addBehaviour(wBehaviour = new WorkingBehaviour()); //start the work
				if (transfer > 0) //if it hase the base to negotiate
					myAgent.addBehaviour(nBehaviour = new NegotiationBehaviour()); //start the work
			}
			return state == State.finishing;
		}
	}

	//First sends an proposal to the mediator
	//then communicates with any one that replies
	class NegotiationBehaviour extends CyclicBehaviour
	{
		private MessageTemplate negotiation = MessageTemplate.MatchConversationId("negotiation");
		private String[] content;
		private Point _tilePosition, _holePosition;
		private boolean canNegotiate = true;

		public void sendRequest(Point tilePosition, Point holePosition) 
		{
			_tilePosition = new Point(tilePosition);
			_holePosition = new Point(holePosition);
			ACLMessage proposal = new ACLMessage(ACLMessage.PROPOSE);
			proposal.setContent(Math.round(transfer) + " " + tilePosition.getRow() + " " + 
					tilePosition.getColumn() + " " + holePosition.getRow() + 
					" " + holePosition.getColumn() + " " + name);
			proposal.setConversationId("negotiation");
			proposal.addReceiver(environment);
			send(proposal);
			System.out.println(name + " to all " + tilePosition.toString() + " -> " + 
					_holePosition.toString() + " for " + Math.round(transfer));
			canPropose = false;
		}

		public void action() 
		{
			ACLMessage msg = receive(negotiation);
			if (msg != null) //receives proposals or status updates
				//first come - first served
			{
				if (msg.getContent() != null)
				{
					content = msg.getContent().split(" ");
				}
				switch (msg.getPerformative())
				{
				case ACLMessage.PROPOSE:
					if (canAccept == 0)
					{
						ACLMessage reply = msg.createReply();
						if (Integer.parseInt(content[0]) >= Math.round(transfer))
						{
							reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							reply.setContent(content[0]+ " " + name);
							System.out.println(name + " to " + content[5] + " ACCEPT");
						}
						else
						{
							reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
							reply.setContent(String.valueOf(Math.round(transfer)));
							System.out.println(name + " to " + content[5] + " ACCEPT FOR " + Math.round(transfer));
						}
						send(reply);
					}
					break;
				case ACLMessage.ACCEPT_PROPOSAL:
					if (canNegotiate && _tilePosition != null && _holePosition != null)
					{
						canNegotiate = false;
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						int i = Integer.parseInt(content[0]);
						reply.setContent(i + " " + _tilePosition.getRow() + 
								" " + _tilePosition.getColumn() + " " + _holePosition.getRow() +
								" " + _holePosition.getColumn() + " " + name);
						send(reply);
					}
					break;
				case ACLMessage.REJECT_PROPOSAL:
					if (canNegotiate && Integer.parseInt(content[0]) <= Math.round(transfer))
					{
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(Integer.parseInt(content[0]) + " " + _tilePosition.getRow() + 
								" " + _tilePosition.getColumn() + " " + _holePosition.getRow() +
								" " + _holePosition.getColumn() + " " + name);
						send(reply);
					}
					break;
				case ACLMessage.INFORM:
					if (canAccept == 0)
					{
						proposer = msg.getSender();
						from = new Point(Integer.parseInt(content[1]),Integer.parseInt(content[2]));
						to = new Point(Integer.parseInt(content[3]),Integer.parseInt(content[4]));
						color = content[5];
						canAccept = Integer.parseInt(content[0]);
						System.out.println(color + " to " + " name " + from.toString() + " -> " +
								to.toString() + " for " + content[0]);
					}
					else
					{
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.FAILURE);
						send(reply);
					}
					break;
				case ACLMessage.REQUEST:
					try 
					{
						ACLMessage request = new ACLMessage(ACLMessage.CFP);
						request.setConversationId("request_execution");
						request.addReceiver(environment);
						request.setContentObject(new Transfer(name, Integer.parseInt(content[0]), content[1]));
						send(request);
					} 
					catch (Exception e) //NumberFormatException | IOException e) 
					{
						e.printStackTrace();
					}
					finally
					{
						canPropose = true;
						canNegotiate = true;
					}
					break;
				case ACLMessage.FAILURE:
					canPropose = true;
					canNegotiate = true;
					break;
				}
			}
			else
				block();
		}
	}

	/*
	 * Working behaviour includes planning and action request execution
	 */
	class WorkingBehaviour extends CyclicBehaviour implements Observer
	{
		MessageTemplate mt;
		State state = State.sending_request;
		State stateN = State.sending_request;
		Point position = new Point(), aux;
		LinkedList<Point> tiles = new LinkedList<Point>();
		LinkedList<Point> holes = new LinkedList<Point>();
		String holding;
		Planner planner = new Planner(map);
		IAction nextAction = null;
		int steps = 0;

		public WorkingBehaviour() 
		{
			planner.addObserver(this); //makes the planner able to wake up the behavior if blocked
			(new Thread(planner)).start(); //starts the planner
		}

		@Override
		public void action()
		{	
			//if the planner is idle and no action were programmed
			if (planner.canWork() && nextAction == null)
			{
				//update the data
				tiles.clear();
				holes.clear();
				for(int row = 0; row < map.length; ++row)
					for (int column = 0; column < map[row].length; ++column)
					{
						if (map[row][column].getAgents().contains(name))
						{
							position.setPoint(row, column);
							holding = map[row][column].getAgentTile(name);
						}
						if (map[row][column].getTiles().contains(name))
							tiles.add(new Point(row, column));
						if (map[row][column].isHole() && map[row][column].getColor().equals(name))
							holes.add(new Point(row,column));
					}
				if (holding.equals("")) // if it is not holding any tile search for the nearest tile
				{
					if (canAccept > 0)
					{
						planner.Initialize(position, position = from,true);
						planner.Run(); //start the planner's work
					}
					else
						if (!tiles.isEmpty()) //if there are actually tiles
						{
							aux = position.getClosest(tiles);
							steps += position.getDistance(aux); //get the distance to the tile
							planner.Initialize(position, position = aux,true);
							planner.Run(); //start the planner's work
						}

				}
				else //otherwise to for a hole
				{
					if (canAccept > 0)
					{
						if (holding.equals(color))
						{
							planner.Initialize(position, position = to, false);
							planner.Run(); //start the planner's work
						}
						else
							nextAction = new Drop(name);
					}
					else
						if (!holes.isEmpty())
						{
							aux = position.getClosest(holes);
							steps += position.getDistance(aux); //get the distance to the hole
							planner.Initialize(position, position = aux,false);
							planner.Run();
						}
				}
				if (nBehaviour != null && canPropose && transfer > 0 && tiles.size() > 0 && holes.size() > 0)
				{
					aux = position.getClosest(tiles);
					{
						if (aux.getClosest(holes) != null)
							nBehaviour.sendRequest(aux, aux.getClosest(holes));
					}
				}
				block();
			}
			else //otherwise if there are elements in the plan or there are some programmed actions
			{
				switch (state)
				{
				case sending_request:	//send request to execute the action				
					ACLMessage request = new ACLMessage(ACLMessage.CFP);
					request.setConversationId("request_execution");
					request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
					request.addReceiver(environment);
					try {
						if (planner.getPlan().size() > 1) //if there is a route make a move
							request.setContentObject(new Move(name, planner.getPlan().pollLast().getDirection(planner.getPlan().peekLast())));
						else
						{
							if (nextAction != null)
							{
								request.setContentObject(new Drop(name));
							}
							else
								if (holding.equals("")) //if it's not holding anything get the tile
								{
									planner.getPlan().pollLast();
									if (canAccept > 0)
										request.setContentObject(new Pick(name, color));
									else
										request.setContentObject(new Pick(name, name));
								}
								else //if the tile is in the hads use it
								{
									request.setContentObject(new Use(name, 
											planner.getPlan().pollLast().getDirection(position)));

									//make some computations based on which the agent will make proposals 
									if (transfer == 0)
										transfer = 10.0 / steps;
									else
										transfer = (transfer + 10.0 / steps) / 2;
									if (canAccept > 0)
									{
										ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
										msg.setConversationId("negotiation");
										msg.addReceiver(proposer);
										msg.setContent(canAccept + " " + name);
										send(msg);
										canAccept = 0;
									}
									steps = 0;
								}
							nextAction = null;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					myAgent.send(request);
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("request_execution"),
							MessageTemplate.MatchInReplyTo(request.getReplyWith()));
					state = State.waiting_for_response;
					break;
				case waiting_for_response: //wait for response
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) 
					{
						//if everything went well wait the interval
						if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) 
						{
							try {
								Thread.sleep(wait);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						//if there was a problem update the map
						if (reply.getPerformative() == ACLMessage.REJECT_PROPOSAL)
						{
							{
								removeBehaviour(this);
								if (nBehaviour != null)
									removeBehaviour(nBehaviour);
								addBehaviour(new InformationRequest());
							}
						}

						if (nextAction == null)
							state = State.finishing;
						else
							state = State.sending_request;
					}
					else
						block();
					break;
				case finishing:
					state = State.sending_request;
					restart();
					break;
				}
			}
		}

		//wake up operation used by the planner
		@Override
		public void update(Observable arg0, Object arg1) 
		{
			restart();
		}
	}
}