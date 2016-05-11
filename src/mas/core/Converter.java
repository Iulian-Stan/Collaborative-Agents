package mas.core;

import mas.io.Obj;
import mas.io.Pair;
import mas.io.Reader;

/*
 * Converts the data read from the system file into a grid of cells
 */
public class Converter {
	public static int height, width; //grid base prameters

	public static Cell[][] convert(Reader reader)
	{
		Pair<Integer, Integer> pair;
		height = reader.GlobalVariables.get(Reader.GRID_HEIGHT);
		width = reader.GlobalVariables.get(Reader.GRID_WIDTH);
		Cell[][] cells = new Cell[height][width];
		for (int h = 0; h < height; ++h)
			for (int w = 0; w < width; ++w)
			{
				cells[h][w] = new Cell(); 
			}
		for (String agent : reader.Agents.keySet()) //Put all agents
		{
			pair = reader.Agents.get(agent);
			cells[pair.getFirst()][pair.getSecond()].addAgent(agent, ""); //Initially it does not have any tile ""
		}
		for (Obj object : reader.Objects.keySet()) //Put all other objects tiles, holes and obstacles
		{
			for(Pair<Integer, Pair<String, Pair<Integer, Integer>>> data : reader.Objects.get(object))
			{
				pair = data.getSecond().getSecond(); //pair that stores coordinates of the cell
				switch (object) 
				{
				case TILES : //color and number
					cells[pair.getFirst()][pair.getSecond()].addTile(
							data.getSecond().getFirst(),
							data.getFirst());
					break;
				case OBSTACLES: //just position
					cells[pair.getFirst()][pair.getSecond()].setObstacle();
					break;
				case HOLES : //color and depth
					cells[pair.getFirst()][pair.getSecond()].setHole(
							data.getSecond().getFirst(),
							data.getFirst());
					break;
				}
			}
		}
		return cells;
	}
}
