package mas.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

/*
 * Reader class
 */
public class Reader 
{
	private final static String INPUT_FILE = "resources/system.txt";
	private final static Charset ENCODING = Charset.forName("UTF8");// StandardCharsets.UTF_8;

	public final static String AGENT_NR = "AgentNumber";
	public final static String SINGLE_OPERATION_TIME = "SingleOperationTime";
	public final static String TOTAL_SIMULATION_TIME = "TotalSimulationTime";
	public final static String GRID_WIDTH = "GridWidth";
	public final static String GRID_HEIGHT = "GridHeight";
	public final static String[] GLOBAL_VARS = new String[] {
		AGENT_NR, 
		SINGLE_OPERATION_TIME,
		TOTAL_SIMULATION_TIME,
		GRID_WIDTH,
		GRID_HEIGHT}; 
	
	public Hashtable<String, Integer> GlobalVariables;
	public String[] AGENTS;
	public Hashtable<String, Pair<Integer, Integer>> Agents;
	public Hashtable<Obj, ArrayList<Pair<Integer, Pair<String, Pair<Integer, Integer>>>>> Objects;
	private int state;

	public Reader() throws IOException
	{
		GlobalVariables = new Hashtable<String, Integer>();
		Agents = new Hashtable<String, Pair<Integer, Integer>>();
		Objects = new Hashtable<Obj, ArrayList<Pair<Integer, Pair<String, Pair<Integer, Integer>>>>>();
		for (Obj object : Obj.values())
			Objects.put(object, new ArrayList<Pair<Integer, Pair<String, Pair<Integer, Integer>>>>()); 
		state = 0;
		readFile();
	}

	private void readFile() throws IOException
	{
		//Path path = Paths.get(INPUT_FILE).toAbsolutePath();
		Scanner scanner = new Scanner(new FileInputStream(INPUT_FILE), ENCODING.name());
		try 
		{
			while (scanner.hasNextLine())
			{
				processLine(scanner.nextLine());
			}
		}
		finally
		{
			scanner.close();
		}
	}

	private void processLine(String line)
	{
		if (line.startsWith(Obj.OBSTACLES.toString()))// OBSTACLES))
		{
			state = 2;
			line = line.replaceFirst(Obj.OBSTACLES.toString(), "");
		}
		else if (line.startsWith(Obj.TILES.toString()))
		{
			state = 3;
			line = line.replaceFirst(Obj.TILES.toString(), "");
		}
		else if (line.startsWith(Obj.HOLES.toString()))
		{
			state = 4;
			line = line.replaceFirst(Obj.HOLES.toString(), "");
		}
		Scanner scanner = new Scanner(line);
		scanner.useDelimiter("[\\s,.;]+");
		if (scanner.hasNext())
			processScanner(scanner);
	}

	private void processScanner(Scanner scanner)
	{
		switch (state)
		{
		case 0:
			InitGlobalVariables(scanner);
			break;
		case 1:
			InitAgents(scanner);
			break;
		case 2:
			InitObjects(Obj.OBSTACLES, scanner);
			break;
		case 3:
			InitObjects(Obj.TILES, scanner);
			break;
		case 4:
			InitObjects(Obj.HOLES, scanner);
			break;
		default:
			return;
		}
		if (scanner.hasNext())
			processScanner(scanner);
	}

	private void InitGlobalVariables(Scanner scanner)
	{
		for (String var : GLOBAL_VARS)
		{
			GlobalVariables.put(var, scanner.nextInt());
		}
		state = 1;
	}

	private void InitAgents(Scanner scanner)
	{
		if (AGENTS == null)
		{
			AGENTS = new String[GlobalVariables.get(AGENT_NR)];
			for (int iter = 0; iter < GlobalVariables.get(AGENT_NR); ++iter)
				AGENTS[iter] = scanner.next();
		}
		else
		{
			for (int iter = 0; iter < GlobalVariables.get(AGENT_NR); ++iter)
				Agents.put(AGENTS[iter], new Pair<Integer, Integer>(scanner.nextInt(), scanner.nextInt()));
			state = -1;
		}
	}

	private void InitObjects(Obj object, Scanner scanner)
	{
		switch (object)
		{
		case OBSTACLES:
			Objects.get(object).add(new Pair<Integer, Pair<String, Pair<Integer, Integer>>>(1,
					new Pair<String, Pair<Integer, Integer>>("", 
							new Pair<Integer, Integer>(scanner.nextInt(), scanner.nextInt()))));
			break;
		default :
			Objects.get(object).add(new Pair<Integer, Pair<String, Pair<Integer, Integer>>>(scanner.nextInt(),
					new Pair<String, Pair<Integer, Integer>>(scanner.next(), 
							new Pair<Integer, Integer>(scanner.nextInt(), scanner.nextInt()))));
			break;
		}
	}

	public void Clear()
	{
		GlobalVariables.clear();
		AGENTS = null;
		Agents.clear();
		Objects.clear();
	}
}
