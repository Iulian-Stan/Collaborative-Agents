package mas.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import mas.core.Cell;

public class IconCreator 
{
	private static final int SIZE = 64; //size of cell
	private static final Font FONT_DRAW = new Font("Book Antiqua", Font.BOLD, 32);
	private static final Font FONT_WRITE = new Font("Book Antiqua", Font.PLAIN, 12);
	private BufferedImage AGENT_OFF; //agent not carrying
	private BufferedImage AGENT_ON; //agent carrying
	private BufferedImage OBSTACLE;
	private BufferedImage HOLE;  //actual hole
	private BufferedImage HOLED; //filled hole
	private BufferedImage TILE;

	private Graphics2D[][] images; //all images are stored as corresponding graphics

	/*
	 * Initialize the images
	 */
	public IconCreator()
	{
		try 
		{
			AGENT_OFF = ImageIO.read(new File("resources/AgentOff.jpg"));
			AGENT_ON = ImageIO.read(new File("resources/AgentOn.jpg"));
			OBSTACLE = ImageIO.read(new File("resources/Obstacle.jpg"));
			HOLE = ImageIO.read(new File("resources/Hole.jpg"));
			HOLED = ImageIO.read(new File("resources/Holed.jpg"));
			TILE = ImageIO.read(new File("resources/Coin.jpg"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	/*
	 * Add graphics to labels
	 */
	public void init(JLabel[][] tiles, Cell[][] cells, int height, int width)
	{
		BufferedImage buffer;
		images = new Graphics2D[height][width];
		for (int h = 0; h < height; ++h)
			for(int w = 0; w < width; ++w)
			{
				buffer = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
				tiles[h][w].setIcon(new ImageIcon(buffer));
				images[h][w] = buffer.createGraphics();
				images[h][w].setColor(Color.WHITE);
				images[h][w].setFont(FONT_DRAW);
				images[h][w].fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
				if (cells[h][w].isHole())
					drawHole(h, w, cells[h][w].getDepth(), cells[h][w].getColor());
				else if (cells[h][w].isObstacle())
					drawObstacle(h, w);
				else
				{
					for (String agent : cells[h][w].getAgents())
						drawAgent(h, w, agent, cells[h][w].getAgentTile(agent));
					int nr = cells[h][w].getTilesNr(), index = 0;
					for (String tile : cells[h][w].getTiles())
						drawTile(h, w, tile, cells[h][w].getTilesNr(tile), index++, nr);
				}
			}	
	}

	/*
	 * Draw agent depending on it's color and the color of the carrying tile
	 */
	public void drawAgent(int h, int w, String agent, String coin)
	{
		BufferedImage original, img;
		if (coin == "")
		{
			original = AGENT_OFF;
		}
		else 
			original = AGENT_ON;
		try {
			img = changeColor(original, Colors.AGENT, agent, true);
			if (coin != "")
				img = changeColor(img, Colors.TILE, coin, false);
			images[h][w].drawImage(img, 0, 0, null);
		} catch (Exception e){ //IllegalArgumentException | IllegalAccessException
				//| NoSuchFieldException | SecurityException e) {
			images[h][w].drawImage(original, 0, 0, null);
			e.printStackTrace();
		}
	}

	/*
	 * Draw obstacle
	 */
	public void drawObstacle(int h, int w)
	{			
		images[h][w].drawImage(OBSTACLE, 0, 0, null);
	}

	/*
	 * Draw tiles and it's number
	 */
	public void drawHole(int h, int w, int depth, String color)
	{
		images[h][w].drawImage(HOLE, 0, 0, null);
		try {
			images[h][w].setColor((Color) Color.class.getField(color).get(null));
		} catch (Exception e) {//IllegalArgumentException | IllegalAccessException
				//| NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		finally
		{
			images[h][w].drawString(String.valueOf(depth), SIZE/2 - FONT_DRAW.getSize()/4, SIZE/2 + FONT_DRAW.getSize()/4);
		}
	}
	
	public void drawHoled(int h, int w)
	{			
		images[h][w].drawImage(HOLED, 0, 0, null);
	}

	public void drawTile(int h, int w, String color, int number, int index, int nr)
	{
		try {
			BufferedImage img = changeColor(TILE, Colors.TILE, color, true);
			images[h][w].drawImage(img, SIZE/2, SIZE / nr * index, null);
		} catch (Exception e) {//IllegalArgumentException | IllegalAccessException
				//| NoSuchFieldException | SecurityException e) {
			images[h][w].drawImage(TILE, SIZE/2, 0, null);
			e.printStackTrace();
		}
		finally
		{
			images[h][w].setColor(Color.black);
			images[h][w].setFont(FONT_WRITE);
			images[h][w].drawString(String.valueOf(number), SIZE/2, SIZE / nr * index + FONT_WRITE.getSize());
		}
	}

	private BufferedImage changeColor(BufferedImage original, Colors target, String desired, boolean createNew) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException
	{
		int height = original.getHeight();
		int widht = original.getWidth();
		int oPixel, oRed, oGreen, oBlue;
		Color desiredColor = (Color) Color.class.getField(desired).get(null);
		float[] HSB;

		float targetHue = target.getHue(),
				desiredHue = Color.RGBtoHSB(desiredColor.getRed(), desiredColor.getGreen(), desiredColor.getBlue(), null)[0];

		BufferedImage buffer;
		if (createNew) 
			buffer = new BufferedImage(widht, height, BufferedImage.TYPE_INT_RGB);
		else 
			buffer = original;
		WritableRaster raster = buffer.getRaster();
		for (int h = 0; h < height; ++h)
		{
			for (int w = 0; w < widht; ++w)
			{
				oPixel = original.getRGB(w,h);
				oRed   = (int)((oPixel&0x00FF0000)>>>16); 
				oGreen = (int)((oPixel&0x0000FF00)>>>8);  
				oBlue  = (int) (oPixel&0x000000FF);
				HSB = Color.RGBtoHSB(oRed,oGreen,oBlue,null);
				if (Math.abs(HSB[0]-targetHue) < 0.05)
				{
					HSB[0] = (1-Math.abs(HSB[0]-targetHue))*desiredHue;
					oPixel = Color.HSBtoRGB(HSB[0], HSB[1], HSB[2]);
					oRed   = (int)((oPixel&0x00FF0000)>>>16); 
					oGreen = (int)((oPixel&0x0000FF00)>>>8);  
					oBlue  = (int) (oPixel&0x000000FF);
				}
				raster.setPixel(w,h,new int[] {oRed,oGreen,oBlue});
			}
		}
		return buffer;
	}
	
	public void updateCell(int row, int column, Cell cell)
	{
		images[row][column].setColor(Color.WHITE);
		images[row][column].fillRect(0, 0, SIZE, SIZE);
		if (cell.isHole())
			drawHole(row, column, cell.getDepth(), cell.getColor());
		else if (cell.isObstacle())
			drawObstacle(row, column);
		else
		{
			if (cell.getDepth() == 0)
				drawHoled(row, column);
			for (String agent : cell.getAgents())
				drawAgent(row, column, agent, cell.getAgentTile(agent));
			int nr = cell.getTilesNr(), index = 0;
			for (String tile : cell.getTiles())
				drawTile(row, column, tile, cell.getTilesNr(tile), index++, nr);
		}
	}
}
