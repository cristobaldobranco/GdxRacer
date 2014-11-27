package org.whitesoft.games.outrun;

import com.badlogic.gdx.math.Vector3;

public class RoadSegmentPoint {
	
	public RoadSegmentPoint(int n, float segmentLength, float worldy) {
		world.z =  n * segmentLength;
		world.y = worldy;
	}

	Vector3 camera = new Vector3();
	Vector3 world = new Vector3();
	Vector3 screen = new Vector3();
	
	float screenScale;
}
