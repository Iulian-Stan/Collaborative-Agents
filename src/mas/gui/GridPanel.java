package mas.gui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mas.core.Cell;

/*
 * Main GUI
 */
@SuppressWarnings("serial")
public class GridPanel extends JPanel{
	private JLabel[][] tiles;
	private final static IconCreator iconCreater = new IconCreator(); //graphical core 
	private int _width;
	private int _height;

	public GridPanel(Cell[][] cells, int height, int width)
	{
		super();
		_height = height;
		_width = width;

		setLayout(new GridLayout(_height, _width));
		tiles = new JLabel[_height][_width];
		for (int h = 0; h < _height; ++h)
			for (int w =0; w < _width; ++w)				
			{
				JLabel label = new JLabel();
				label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				tiles[h][w] = label;
				add(label);
			}
		initGrid(cells);
	}

	/*
	 * Create an image for each cell
	 */
	public void initGrid(Cell[][] cells)
	{
		iconCreater.init(tiles, cells, _height, _width);
	}
	
	/*
	 * Update the image of a cell according to specifications
	 */
	void updateCell(int row, int column, Cell cell)
	{
		iconCreater.updateCell(row,column,cell);
		tiles[row][column].repaint();
	}
}