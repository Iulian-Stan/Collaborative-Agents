package mas.core;

import jade.util.leap.Serializable;

import java.util.LinkedList;

/*
 * Similar to java Point adjusted t my needs
 */
@SuppressWarnings("serial")
public class Point implements Serializable
{
	private int _row; //x coordinate
	private int _column; //y coordinate

	/*
	 * Default constructor (outside of the grid)
	 */
	public Point()
	{
		setPoint(-1,-1);
	}

	/*
	 * Set row and column
	 */
	public Point(int row, int column) 
	{
		setPoint(row,column);
	}

	/*
	 * Coping constructor
	 */
	public Point(Point point)
	{
		setPoint(point._row, point._column);
	}

	public int getRow()
	{
		return _row;
	}

	public int getColumn()
	{
		return _column;
	}

	public void setPoint(int row, int column)
	{
		_row = row;
		_column = column;
	}

	/*
	 * Manhattan distance between two positions
	 */
	public int getDistance(Point point)
	{
		return Math.abs(_row - point._row) + Math.abs(_column - point._column);
	}

	/*
	 * Get the closest point from a list
	 */
	public Point getClosest(LinkedList<Point> points)
	{
		Point closest = null;
		int d1 = -1, d2;
		for (int index = 0; index < points.size(); ++index)
		{
			d2 = getDistance(points.get(index));
			if (d1 == -1 || d1 > d2)
			{
				closest = points.get(index);
				d1 = d2;
			}
		}
		return closest;
	}
	
	/*
	 * Get the farest point frm the list
	 */
	public Point getFarest(LinkedList<Point> points)
	{
		Point farest = null;
		int d1 = -1, d2;
		for (int index = 0; index < points.size(); ++index)
		{
			d2 = getDistance(points.get(index));
			if (d1 == -1 || d1 < d2)
			{
				farest = points.get(index);
				d1 = d2;
			}
		}
		return farest;
	}
	
	/*
	 * Get direction by position of the next point
	 */
	public Direction getDirection(Point point)
	{
		if (_row == point._row)
		{
			if (_column > point._column)
				return Direction.West;
			else
				return Direction.East;
		}
		else
		{
			if (_row > point._row)
				return Direction.North;
			else
				return Direction.South;
		}
	}

	@Override
	public String toString()
	{
		return "("+_row+","+_column+")";
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}

		Point point = (Point) obj;
		return _row == point._row && _column == point._column;
	}
}
/*
@SuppressWarnings("rawtypes")
class PointComparator implements Comparator
{
	private Point _toCompare;

	public PointComparator(Point toCompare)
	{
		_toCompare = toCompare;
	}

	@Override
	public int compare(Object arg0, Object arg1) 
	{
		return _toCompare.getDistance((Point)arg0) - _toCompare.getDistance((Point)arg1);
	}
}
 */