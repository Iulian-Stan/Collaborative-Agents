package mas.gui;

import mas.core.Cell;

public interface MyGui {
	public void updateTime(int step);
	public int getTime();
	public void updateScore(int points, String color);
	public void transferPoints(int points, String sender, String reseiver);
	public void updateCell(int row, int column, Cell cell);
}
