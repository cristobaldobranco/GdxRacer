package org.whitesoft.games.outrun;

import com.badlogic.gdx.graphics.Color;

public class RoadSegment {

	public boolean looped = false;
	public float fog = 0;
	public int index = -1;
	public RoadSegmentPoint p1; 
	public RoadSegmentPoint p2; 
	Color colorRoad;
	Color colorRumble;
	Color colorGrass;
	Color colorLane;
	public float curve;

	public RoadSegment(int n, float segmentLength, float p1y, float p2y) {
		index = n;
		p1 = new RoadSegmentPoint(n,     segmentLength, p1y);
		p2 = new RoadSegmentPoint(n + 1, segmentLength, p2y);
	}
	
	public void setColors(Color r, Color ru, Color g, Color l)
	{
		colorRoad = r;
		colorRumble = ru;
		colorGrass = g;
		colorLane = l;
	}
}


