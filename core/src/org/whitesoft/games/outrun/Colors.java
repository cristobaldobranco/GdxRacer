package org.whitesoft.games.outrun;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;


public class Colors {
	static boolean initialized = false;
	static HashMap<String, Color> colors = new HashMap<String, Color>();

	public static Color getColor(String color)
	{
		if (!initialized)
		{
			generateColorMap();
			initialized = true;
		}
		return colors.get(color);
	}
	
	private static void generateColorMap()
	{
		colors.clear();
		colors.put("SKY", new Color(0x72D7EEFF));
		colors.put("TREE", new Color(0x005108FF));
		colors.put("FOG", new Color(0x005108FF));
		colors.put("LIGHTROAD", new Color(0x6B6B6BFF));
		colors.put("DARKROAD", new Color(0x696969FF));
		colors.put("LIGHTGRASS", new Color(0x10AA10FF));
		colors.put("DARKGRASS", new Color(0x009A00FF));
		colors.put("LIGHTRUMBLE", new Color(0x555555FF));
		colors.put("DARKRUMBLE", new Color(0xBBBBBBFF));
		colors.put("LIGHTLANE", new Color(0xCCCCCCFF));
		
	}
	

}
