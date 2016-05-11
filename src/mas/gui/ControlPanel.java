package mas.gui;

import java.awt.GridLayout;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;

import mas.io.Reader;

/*
 * Displays information about the time and score
 */
@SuppressWarnings("serial")
public class ControlPanel extends JPanel
{
	JLabel intervalLabel = new JLabel("Interval"); //Total simulation time
	JLabel intervalValueLabel = new JLabel();

	JLabel timerLabel = new JLabel("Timer"); //Timer updated by the interval
	JLabel timerValueLabel = new JLabel("0");

	JLabel playerLabel = new JLabel("Player"); //All players and score
	JLabel scoreLabel = new JLabel("Score");

	Hashtable<String,JLabel> players = new Hashtable<String, JLabel>(); //to be initialized

	public ControlPanel(Reader reader)
	{
		super();
		int agentNr = reader.GlobalVariables.get(Reader.AGENT_NR); //get agent number

		setLayout(new GridLayout(3 + agentNr, 2)); //set the gird
		add(intervalLabel);
		intervalValueLabel.setText("" + reader.GlobalVariables.get(Reader.TOTAL_SIMULATION_TIME));
		add(intervalValueLabel);
		add(timerLabel);
		add(timerValueLabel);
		add(playerLabel);
		add(scoreLabel);
		
		//Initialy all agents have score 0
		for (String name : reader.AGENTS)
		{
			add(new JLabel(name));
			players.put(name, new JLabel("0"));
			add(players.get(name));
		}
	}

	public int getTime()
	{
		return Integer.parseInt(timerValueLabel.getText());
	}

	/*
	 * Update the time
	 */
	public void updateTime(int step)
	{
		timerValueLabel.setText(getTime() + step + "");
	}
	
	/*
	 * Update the score
	 */
	public void updateScore(int score, String color)
	{
		players.get(color).setText(score + Integer.parseInt(players.get(color).getText()) + "");
	}
	
	/*
	 * Transfer points
	 */
	public void transferPoints(int points, String sender, String receiver)
	{
		players.get(sender).setText(Integer.parseInt(players.get(sender).getText()) - points + "");
		players.get(receiver).setText(Integer.parseInt(players.get(receiver).getText()) + points + "");
	}
}