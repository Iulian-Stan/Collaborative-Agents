package mas.io;

public class Pair<FirstType, SecondType> 
{
	private FirstType _first;
	private SecondType _second;

	public Pair(FirstType first, SecondType second) 
	{
		_first = first;
		_second = second;
	}

	public FirstType getFirst()
	{
		return _first;
	}
	
	public void setFirst(FirstType first)
	{
		_first = first;
	}

	public SecondType getSecond()
	{
		return _second;
	}
	
	public void setSecond(SecondType second)
	{
		_second = second;
	}

	@Override
	public String toString() {
		return "(" + _first + ", " + _second + ")";
	}
}