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

	public RoadSegment(int n, float segmentLength) {
		index = n;
		p1 = new RoadSegmentPoint(n,     segmentLength);
		p2 = new RoadSegmentPoint(n + 1, segmentLength);
	}
	
	public void setColors(Color r, Color ru, Color g, Color l)
	{
		colorRoad = r;
		colorRumble = ru;
		colorGrass = g;
		colorLane = l;
	}
}


