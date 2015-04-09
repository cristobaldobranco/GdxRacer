package org.whitesoft.games.outrun;

import java.util.Random;

public class Util {
	
	public static Random rnd = new Random();

	public static float accelerate(float speed, float accel, float dt) {
		return speed + (accel * dt);  
	}

	public static float limit(float value, float min, float max) {
		return Math.max(min, Math.min(value, max)); 
	}

	public static float exponentialFog(float distance, float density) {
		return (float) (1 / (Math.pow(Math.E, (distance * distance * density))));	}

	public static void project(RoadSegmentPoint p, float cameraX, float cameraY, float cameraZ, 
			float cameraDepth, float width, float height, float roadWidth) {
		p.camera.x     = p.world.x - cameraX;
		p.camera.y     = p.world.y - cameraY;
		p.camera.z     = p.world.z - cameraZ;
		p.screenScale = cameraDepth/p.camera.z;
		p.screen.x     = Math.round((width/2)  + (p.screenScale * p.camera.x  * width/2));
		p.screen.y     = Math.round((height/2) - (p.screenScale * p.camera.y  * height/2));
		p.screen.z     = Math.round(             (p.screenScale * roadWidth   * width/2));  // was screen.w
	}
/*
	project: function(p, cameraX, cameraY, cameraZ, cameraDepth, width, height, roadWidth) {
		p.camera.x     = (p.world.x || 0) - cameraX;
		p.camera.y     = (p.world.y || 0) - cameraY;
		p.camera.z     = (p.world.z || 0) - cameraZ;
		p.screen.scale = cameraDepth/p.camera.z;
		p.screen.x     = Math.round((width/2)  + (p.screen.scale * p.camera.x  * width/2));
		p.screen.y     = Math.round((height/2) - (p.screen.scale * p.camera.y  * height/2));
		p.screen.w     = Math.round(             (p.screen.scale * roadWidth   * width/2));
	},
*/	
	public static boolean overlap(float x1, float w1, float x2, float w2) {
		return overlap(x1, w1, x2, w2, 1);
	}

	private final static int [] posNegChoice = {-1, 1 };

	public static int randomSign()
	{
		return randomChoice(posNegChoice);
	}
	
	public static boolean overlap(float x1, float w1, float x2, float w2, float percent) {
		float half = percent/2;
		float min1 = x1 - (w1*half);
		float max1 = x1 + (w1*half);
		float min2 = x2 - (w2*half);
		float max2 = x2 + (w2*half);
		return ! ((max1 < min2) || (min1 > max2));
	}

	public static float increase(float start, float increment, float max) {
		float result = start + increment;
		while (result >= max)
			result -= max;
		while (result < 0)
			result += max;
		return result;
	}

	
	public static int randomInt(int min, int max)
	{
		return Math.round(interpolate(min, max, rnd.nextFloat())); 
	}
	
	public static float interpolate(float a, float b, float percent) {
		return a + (b-a)*percent;
	}

	public static int randomChoice(int [] choices)
	{
		return choices[randomInt(0, choices.length - 1)];
	}
	
	public static float percentRemaining(float n, float total)
	{
		return (n%total) / total;
	}
	
	public static float easeIn(float a, float b, float percent)
	{
		return (float) (a + (b-a)*Math.pow(percent,2));
	}
	public static float easeOut(float a, float b, float percent)
	{
		return (float) (a + (b-a)*(1-Math.pow(1-percent,2)));
	}
	public static float easeInOut(float a, float b, float percent)
	{
		return (float) (a + (b-a)*((-Math.cos(percent*Math.PI)/2) + 0.5));
	}
}


/*
var Util = {

		timestamp:        function()                  { return new Date().getTime();                                    },
		toInt:            function(obj, def)          { if (obj !== null) { var x = parseInt(obj, 10); if (!isNaN(x)) return x; } return Util.toInt(def, 0); },
		toFloat:          function(obj, def)          { if (obj !== null) { var x = parseFloat(obj);   if (!isNaN(x)) return x; } return Util.toFloat(def, 0.0); },

}

*/