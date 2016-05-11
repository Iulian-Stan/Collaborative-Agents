package mas.gui;

public enum Colors {
	AGENT(.06f), //Agent image hue color
	TILE(.14f);  //Tile image hue color
	private final float _hue; 

	private Colors (float hue)
	{
		_hue = hue;
	}

	public float getHue()
	{
		return _hue;
	}
}
