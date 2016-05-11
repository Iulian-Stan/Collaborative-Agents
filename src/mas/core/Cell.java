package mas.core;

import jade.util.leap.Serializable;

import java.util.Hashtable;
import java.util.Set;

/*
 * This class objects store the information of the cells
 * from the grid
 */
@SuppressWarnings("serial")
public class Cell implements Serializable
{
	private static int PRIZE = 10;	//base prize for putting a tile in the hole
	private static int BONUS = 50;	//bonus prize for filling a hole
	
	private boolean _isObstacle = false; //Obstacle flag, if set others are ignored
	
	private int _depth = -1; //Depth of the hole (hole-1 - not hole) (0 - former hole) (other - actual hole)
	private String _color;	//The color of the hole
	
	private Hashtable<String, String> _agents; //Agents for the cell name and carrying tile color
	private Hashtable<String, Integer> _tiles; //Tiles from the cell color and number

	public Cell()
	{
		_agents = new Hashtable<String, String>();
		_tiles = new Hashtable<String, Integer>();
	}

	public void setObstacle()
	{
		_isObstacle = true;
	}

	public void setHole(String color, int depth)
	{
		_color = color;
		_depth = depth;
	}

	/*
	 * Get the color of the hole
	 */
	public String getColor() 
	{
		return _color;
	}

	public int getDepth()
	{
		return _depth;
	}

	public boolean isObstacle()
	{
		return _isObstacle;
	}

	public boolean isHole()
	{
		return _depth > 0;
	}

	/*
	 * It's not an obstacle and not an actual hole
	 */
	public boolean isPassable() 
	{
		return !isObstacle() && !isHole();
	}

	/*
	 * Move the agent and the carrying tie into the cell
	 */
	public void addAgent(String agent,String tile) 
	{
		_agents.put(agent, tile);
	}

	/*
	 * Removes the agent from the cell
	 */
	public void removeAgent(String agent) 
	{
		_agents.remove(agent);
	}

	/*
	 * Put a specified number of tiles in the cell (initialization)
	 */
	public void addTile(String color,int number) 
	{
		_tiles.put(color, number);
	}

	/*
	 * Moves the agent to a new cell and removes from the former one
	 */
	public void moveAgent(String agent, Cell to) 
	{
		to.addAgent(agent,_agents.get(agent));
		_agents.remove(agent);
	}

	/*
	 * Gets all the agents from the cell (agents may coexist in the same cell)
	 */
	public Set<String> getAgents() 
	{
		return _agents.keySet();
	}

	/*
	 * Get the tile color of a specified agent (will return "" if it doesn't carry any)
	 */
	public String getAgentTile(String agent) 
	{
		return _agents.get(agent);
	}

	/*
	 * Gets the number of different 
	 */
	public int getTilesNr() 
	{
		return _tiles.size();
	}

	/*
	 * Get all tiles different colors
	 */
	public Set<String> getTiles()
	{
		return _tiles.keySet();
	}

	/*
	 * Get the number of tiles of the specified color
	 */
	public int getTilesNr(String tile)
	{
		return _tiles.get(tile);
	}

	/*
	 * "Put a specific tile in the agent's hands" for the specific action
	 */
	public void agentPickTile(String agent, String tile)
	{
		_agents.put(agent, tile);
		if (_tiles.get(tile) - 1 == 0)
			_tiles.remove(tile);
		else
			_tiles.put(tile, _tiles.get(tile) - 1);
	}

	/*
	 * "Drop the tile" for the specific action
	 */
	public void agentPutTile(String agent)
	{
		String tile = _agents.get(agent);
		_agents.put(agent, "");
		if (_tiles.containsKey(tile))
			_tiles.put(tile, _tiles.get(tile) + 1);
		else
			_tiles.put(tile, 1);
	}

	/*
	 * "Use tile" for the specific action, returns the points
	 */
	public int fillHole()
	{
		if (--_depth > 0)
			return PRIZE;
		else
			return BONUS;
	}

	/*
	 * "Remove tile from agent's hands" it may be used or simply disapear
	 */
	public void agentRemoveTile(String agent)
	{
		_agents.put(agent, "");
	}
}