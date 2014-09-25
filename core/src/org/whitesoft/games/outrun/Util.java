package org.whitesoft.games.outrun;

public class Util {

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

}


/*
var Util = {

		timestamp:        function()                  { return new Date().getTime();                                    },
		toInt:            function(obj, def)          { if (obj !== null) { var x = parseInt(obj, 10); if (!isNaN(x)) return x; } return Util.toInt(def, 0); },
		toFloat:          function(obj, def)          { if (obj !== null) { var x = parseFloat(obj);   if (!isNaN(x)) return x; } return Util.toFloat(def, 0.0); },
		limit:            function(value, min, max)   { return Math.max(min, Math.min(value, max));                     },
		randomInt:        function(min, max)          { return Math.round(Util.interpolate(min, max, Math.random()));   },
		randomChoice:     function(options)           { return options[Util.randomInt(0, options.length-1)];            },
		percentRemaining: function(n, total)          { return (n%total)/total;                                         },
		accelerate:       function(v, accel, dt)      { return v + (accel * dt);                                        },
		interpolate:      function(a,b,percent)       { return a + (b-a)*percent                                        },
		easeIn:           function(a,b,percent)       { return a + (b-a)*Math.pow(percent,2);                           },
		easeOut:          function(a,b,percent)       { return a + (b-a)*(1-Math.pow(1-percent,2));                     },
		easeInOut:        function(a,b,percent)       { return a + (b-a)*((-Math.cos(percent*Math.PI)/2) + 0.5);        },
		exponentialFog:   function(distance, density) { return 1 / (Math.pow(Math.E, (distance * distance * density))); },

		increase:  function(start, increment, max) { // with looping
			var result = start + increment;
			while (result >= max)
				result -= max;
			while (result < 0)
				result += max;
			return result;
		},

		project: function(p, cameraX, cameraY, cameraZ, cameraDepth, width, height, roadWidth) {
			p.camera.x     = (p.world.x || 0) - cameraX;
			p.camera.y     = (p.world.y || 0) - cameraY;
			p.camera.z     = (p.world.z || 0) - cameraZ;
			p.screen.scale = cameraDepth/p.camera.z;
			p.screen.x     = Math.round((width/2)  + (p.screen.scale * p.camera.x  * width/2));
			p.screen.y     = Math.round((height/2) - (p.screen.scale * p.camera.y  * height/2));
			p.screen.w     = Math.round(             (p.screen.scale * roadWidth   * width/2));
		},

		overlap: function(x1, w1, x2, w2, percent) {
			var half = (percent || 1)/2;
			var min1 = x1 - (w1*half);
			var max1 = x1 + (w1*half);
			var min2 = x2 - (w2*half);
			var max2 = x2 + (w2*half);
			return ! ((max1 < min2) || (min1 > max2));
		}

}

*/