package mas.core;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Observable;

/*
 * The thread that computes the path for movement
 * Receives just the initial position and the final one
 * and is observer by the behavior
 */
public class Planner extends Observable implements Runnable {

	private boolean _life;  //thread specific parameter telling that it is alive
	private boolean _running; //thread specific parameter telling that it's busy

	private Point _start; //start point
	private Point _finish; //final point

	private Deque<Point> _plan = new LinkedList<Point>(); //Route 
	private LinkedList<Point> visited = new LinkedList<Point>(); //Already visited positions
	private boolean _inclusive; //Tells that the final position is an actual one or just adjacent
	private Cell[][] _cells; //The map

	public Planner(Cell[][] cells)
	{
		_life = true;
		_running = false;
		_cells = cells;
	}

	public boolean isRunning()
	{
		return _running;
	}

	/*
	 * Tells the state of the agent for running again
	 * It must have no plan and actually be in an idle state (not running)
	 */
	public boolean canWork()
	{
		return _plan.isEmpty() && _running == false; 
	}

	/*
	 * Returns the plan
	 */
	public Deque<Point> getPlan()
	{
		return _plan;
	}

	/*
	 * Set the parameters for planning
	 * Inclusive = true for pick up tile
	 * Inclusive = false for use tile
	 */
	public void Initialize(Point start, Point finish, boolean inclusive)
	{
		_start = new Point(start);
		_finish = new Point(finish);
		_inclusive = inclusive;
		_plan.clear(); //clear the old plan
		_plan.push(_start); //set the start == current position
		visited.clear(); //clear memory
	}

	public void Stop()
	{
		_running = false;
	}

	synchronized public void Run()
	{ 
		_running = true;
		notify();
	}

	public void Finish()
	{ 
		_life = false;
	}

	public void run()
	{
		while(_life) 
		{ 
			try 
			{ 
				synchronized(this) 
				{ 
					if(!_running) 
						wait() ;
				}
				//if there is no possible plan, or it was achieved
				//it's inside the cell for inclusive = true
				//or near for inclusive = false
				if (_plan.isEmpty() ||
						_inclusive && _plan.peek().getDistance(_finish) == 0 ||
						!_inclusive && _plan.peek().getDistance(_finish) == 1)
				{
					if (_plan.isEmpty())
						_plan.pollLast();
					Stop();
					setChanged(); //Update state
					notifyObservers(); //Notify the WorkingBehaviour to wake up 
				}
				else
				{
					Point currentPoint = _plan.peek(), previousPoint, nextPoint = null;
					int row = currentPoint.getRow(), column = currentPoint.getColumn();
					//computes the coordinates for all posible directions
					//similar for move action test
					for (Direction direction : Direction.values())
					{
						previousPoint = _plan.peek();
						switch (direction) //computes the coordinates for all posible directions
						{
						case East:
							if (column + 1 < _cells[row].length && _cells[row][column+1].isPassable())
								nextPoint = new Point(row,column + 1);
							break;
						case North:
							if (row - 1 > -1 && _cells[row-1][column].isPassable())
								nextPoint = new Point(row - 1,column);
							break;
						case West:
							if (column - 1 > -1 && _cells[row][column-1].isPassable())
								nextPoint = new Point(row,column - 1);
							break;
						case South:
							if (row + 1 < _cells.length && _cells[row+1][column].isPassable())
								nextPoint = new Point(row + 1,column);
							break;
						}
						//if there is a possible movement position, that wasn't visited
						//and is not in the current path
						if (nextPoint != null && !visited.contains(nextPoint) && !_plan.contains(nextPoint))
						{
							//if this is the first option add it
							if (previousPoint.equals(currentPoint))
								_plan.push(nextPoint);
							//if it's not the first option
							//check if it is a better one
							//if so the replace similar to A*
							else if (_finish.getDistance(previousPoint) >  _finish.getDistance(nextPoint))
							{
								visited.add(_plan.pop());
								_plan.push(nextPoint);
							}
						}
					}
					//if it is a dead end then go back
					//add the bad position to visited list
					if (nextPoint == null)
						visited.add(_plan.pop());
				}
			} 
			catch(InterruptedException e){} 
		}
	}
}
