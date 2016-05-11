package mas.core;

public enum Direction {	
	East(0),
	North(1),
	West(2), 
	South(3) ;

	private final int _index; 

	private Direction (int index)
	{
		_index = index;
	}

	public int getIndex()
	{
		return _index;
	}
}
