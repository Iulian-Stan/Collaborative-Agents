package mas.core;

import jade.util.leap.Serializable;

/*
 * Action interface that will be used to initialize 
 * different actions and request environment for
 * execution. 
 */
interface IAction
{
	public boolean getAgentPosition(Cell[][] cells); //As well the environment as the agent must now the agent position
	public boolean Verify(Cell[][] cells); //Verifies the possibility of execution of the action
	public void Execute(Cell[][] cells, EnvironmentAgent agent); //The execution
}

/*
 * Abstract class that initialize the common fields
 * for all kinds of action
 */
@SuppressWarnings("serial")
abstract class Action implements IAction, Serializable
{
	protected String _agent;					//Agent name == color
	protected Point _position = new Point(0,0); //Agent's position

	public Action(String agent)
	{
		_agent = agent;
	}

	public String getAgent()
	{
		return _agent;
	}

	public boolean getAgentPosition(Cell[][] cells)
	{
		for (int x = 0; x < cells.length; ++x)
			for (int y = 0; y < cells[x].length; ++y)
				if (cells[x][y].getAgents().contains(_agent))
				{
					_position.setPoint(x, y);
					return true;
				}
		return false;
	}
}

@SuppressWarnings("serial")
class Pick extends Action
{
	private String _color; //Color of the tile that must be picked

	public Pick (String agent, String color)
	{
		super(agent);
		_color = color;
	}

	public boolean Verify(Cell[][] cells) 
	{
		if (getAgentPosition(cells))
		{
			int x = _position.getRow(), y = _position.getColumn();
			return cells[x][y].getAgentTile(_agent).contains("") && //The agent must not be carrying any tile
					cells[x][y].getTiles().contains(_color) && //The desired tile color must exist in the cell
					cells[x][y].getTilesNr(_color) > 0; //Additionally if it was not removed the number is checked to not be null
		}
		return false;
	}

	public void Execute(Cell[][] cells, EnvironmentAgent agent) 
	{
		if (getAgentPosition(cells))
		{
			int x = _position.getRow(), y = _position.getColumn();
			cells[x][y].agentPickTile(_agent, _color); //Pick the tile
			agent.updateCell(x, y, cells[x][y]); //Update the interface
			return;
		}
	}

	public String toString()
	{
		return "["+_agent+"] pick from " + _position.toString() + " one " +  _color; //For the logger
	}
}


@SuppressWarnings("serial")
class Drop extends Action
{
	public Drop(String agent) 
	{
		super(agent);
	}

	public boolean Verify(Cell[][] cells) 
	{
		if (getAgentPosition(cells))
			return (!cells[_position.getRow()][_position.getColumn()].getAgentTile(_agent).equals("")); //The agent must be carrying something
		return false;
	}

	public void Execute(Cell[][] cells, EnvironmentAgent agent) 
	{
		if (getAgentPosition(cells))
		{
			int x = _position.getRow(), y = _position.getColumn();
			cells[x][y].agentPutTile(_agent);	//Drop the tile
			agent.updateCell(x, y, cells[x][y]); //Update the interface
		}
	}

	public String toString()
	{
		return "["+_agent+"] drops " + _position.toString(); //For the logger
	}
}

@SuppressWarnings("serial")
class Move extends Action
{
	private Direction _direction; //Direction in wich the aggent wants to move

	public Move(String agent, Direction direction) 
	{
		super(agent);
		_direction = direction;
	}

	/*
	 * The position in which the agent wants to move must be on the grid
	 * and it have to be a passable cell (reference - Cell.java)
	 */
	public boolean Verify(Cell[][] cells) 
	{
		if (getAgentPosition(cells))
		{
			int x = _position.getRow(), y = _position.getColumn();
			switch (_direction)
			{
			case East:
				return (y + 1 < cells[x].length && cells[x][y+1].isPassable());
			case North:
				return (x - 1 > -1 && cells[x-1][y].isPassable());
			case West:
				return (y - 1 > -1 && cells[x][y-1].isPassable());
			case South:
				return (x + 1 < cells.length && cells[x+1][y].isPassable());
			}
		}
		return false;
	}

	/*
	 * Get next position from direction
	 * Update both cells
	 */
	public void Execute(Cell[][] cells, EnvironmentAgent agent) 
	{
		if (getAgentPosition(cells))
		{
			int x = _position.getRow(), y = _position.getColumn(), x1 = x, y1 = y;
			switch (_direction)
			{
			case East:
				++y1; 
				break;
			case North:
				--x1;
				break;
			case West:
				--y1;
				break;
			case South:
				++x1;
				break;
			}
			cells[x1][y1].addAgent(_agent, cells[x][y].getAgentTile(_agent));
			cells[x][y].removeAgent(_agent);
			agent.updateCell(x, y, cells[x][y]);
			agent.updateCell(x1, y1, cells[x1][y1]);
		}
	}

	public String toString()
	{
		return "["+_agent+"] move from " + _position.toString() + " to " +  _direction;
	}
}

@SuppressWarnings("serial")
class Use extends Action
{
	private Direction _direction; //Direction in which the tile must be put

	public Use(String agent, Direction directon) 
	{
		super(agent);
		_direction = directon;
	}

	/*
	 * The cell must be on the grid and it has to be a hole
	 */
	public boolean Verify(Cell[][] cells) 
	{
		if (getAgentPosition(cells))
		{
			int x = _position.getRow(), y = _position.getColumn();
			if (!cells[x][y].getAgentTile(_agent).equals(""))
				switch (_direction)
				{
				case East:
					return (y + 1 < cells[x].length && cells[x][y+1].isHole());
				case North:
					return (x - 1 > -1 && cells[x-1][y].isHole());
				case West:
					return (y - 1 > -1 && cells[x][y-1].isHole());
				case South:
					return (x + 1 < cells.length && cells[x+1][y].isHole());
				}			
		}
		return false;
	}

	/*
	 * Execute the action , updtes the interface and the score (reference - Cell.java)
	 */
	public void Execute(Cell[][] cells, EnvironmentAgent agent) 
	{
		if (getAgentPosition(cells))
		{
			int x = _position.getRow(), y = _position.getColumn(), x1 = x, y1 = y;
			switch (_direction)
			{
			case East:
				++y1; 
				break;
			case North:
				--x1;
				break;
			case West:
				--y1;
				break;
			case South:
				++x1;
				break;
			}
			agent.updateScore(cells[x1][y1].fillHole(), cells[x][y].getAgentTile(_agent));
			cells[x][y].agentRemoveTile(_agent);
			agent.updateCell(x, y, cells[x][y]);
			agent.updateCell(x1, y1, cells[x1][y1]);
		}
	}

	public String toString()
	{
		return "["+_agent+"] use from " + _position.toString() + " to " +  _direction;
	}
}

@SuppressWarnings("serial")
class Transfer extends Action
{
	private int _points;	//Number of points to transfer
	private String _receiver;	//Receiver agent

	public Transfer(String agent, int points, String receiver) 
	{
		super(agent);
		_points = points;
		_receiver = receiver;
	}

	public boolean Verify(Cell[][] cells) //Always true
	{
		return true;
	}

	public void Execute(Cell[][] cells, EnvironmentAgent agent) 
	{
		agent.transferPoints(_points, _agent, _receiver);//Transfer the points
	}

	public String toString()
	{
		return "["+_agent+"] transfers " + _points + " to " +  _receiver;
	}
}