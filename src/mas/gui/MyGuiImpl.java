package mas.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import mas.core.Cell;
import mas.core.Converter;
import mas.io.Reader;

@SuppressWarnings("serial")
public class MyGuiImpl extends JFrame implements MyGui {

	private ControlPanel control;
	private GridPanel grid;
	
	public MyGuiImpl(Cell[][] cells, Reader reader)
	{
		super();
		setLayout(new BorderLayout());
		control = new ControlPanel(reader);
		grid = new GridPanel(Converter.convert(reader),Converter.height, Converter.width);
		add(control, BorderLayout.WEST);
		add(grid, BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );	
		pack();
		setVisible(true);
	}
	
	public void updateTime(int step)
	{
		control.updateTime(step);
	}
	
	public int getTime()
	{
		return control.getTime();
	}

	public void updateCell(int row, int column, Cell cell) {
		grid.updateCell(row, column, cell);	
	}

	public void updateScore(int points, String color) {
		control.updateScore(points, color);		
	}

	public void transferPoints(int points, String sender, String receiver) {
		control.transferPoints(points, sender, receiver);		
	}

}
