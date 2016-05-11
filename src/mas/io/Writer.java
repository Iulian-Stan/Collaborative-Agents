package mas.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * Logger class
 */
public class Writer 
{
	private final static String LOG_FILE = "resources/log.txt";

	private PrintWriter _out;
	private boolean _console = false; 
	private long time = System.currentTimeMillis();

	public Writer(boolean console) throws IOException
	{
		File file = new File(LOG_FILE);
		if(!file.exists()){
			file.createNewFile();
		}
		FileWriter fileWritter = new FileWriter(file.getPath(),true);
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		_out = new PrintWriter(bufferWritter);   
		_console = console;
	}
	
	/*
	 * Close the file for the information to be saved
	 */
	public void Clear()
	{
		_out.close();
	}

	/*
	 * Write tol file and to console if specified
	 */
	public void write(String line)
	{
		line = processLine(line);
		_out.println(line);
		if (_console)
			log(line);
	}

	/*
	 * Add time to log
	 */
	private String processLine(String line)
	{
		return "["+(System.currentTimeMillis() - time)/1000.+"] " + line;
	}
	
	/*
	 * Write to file
	 */
	private void log(String message)
	{
		System.out.println(message);
	}
}