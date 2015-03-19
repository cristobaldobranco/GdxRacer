package org.whitesoft.games.outrun;

import java.util.Vector;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RoadSegment {

	class SpriteWrapper
	{
		String sprite;
		float  offset;
		float width;
		
	}
	
	public boolean looped = false;
	public float fog = 0;
	public int index = -1;
	public RoadSegmentPoint p1; 
	public RoadSegmentPoint p2; 
	Color colorRoad;
	Color colorRumble;
	Color colorGrass;
	Color colorLane;
	
	TextureRegion textureRoad;
	TextureRegion textureRumble;
	TextureRegion textureGrass;
	TextureRegion textureLane;
	
	public float curve;
	public float clip;
	
	Vector<SpriteWrapper> sprites = new Vector<SpriteWrapper>();
	Vector<Car> cars = new Vector<Car>();

	public RoadSegment(int n, float segmentLength, float p1y, float p2y) {
		index = n;
		p1 = new RoadSegmentPoint(n,     segmentLength, p1y);
		p2 = new RoadSegmentPoint(n + 1, segmentLength, p2y);
	}
	
	public void setUniColor(Color c)
	{
		setColors(c, c, c, c);
	}
	
	public void setColors(Color r, Color ru, Color g, Color l)
	{
		colorRoad = r;
		colorRumble = ru;
		colorGrass = g;
		colorLane = l;
		
		Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pix.setColor(r);//.setColor(0xDEADBEFF); // DE is red, AD is green and BE is blue.
		pix.fill();
		Texture textureSolid = new Texture(pix);
		textureRoad = new TextureRegion(textureSolid);

		pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pix.setColor(ru);//.setColor(0xDEADBEFF); // DE is red, AD is green and BE is blue.
		pix.fill();
		textureSolid = new Texture(pix);
		textureRumble = new TextureRegion(textureSolid);

		pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pix.setColor(g);//.setColor(0xDEADBEFF); // DE is red, AD is green and BE is blue.
		pix.fill();
		textureSolid = new Texture(pix);
		textureGrass = new TextureRegion(textureSolid);

		pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pix.setColor(l);//.setColor(0xDEADBEFF); // DE is red, AD is green and BE is blue.
		pix.fill();
		textureSolid = new Texture(pix);
		textureLane = new TextureRegion(textureSolid);
	}

	public void addSprite(String spriteName, float offset) {
		SpriteWrapper w = new SpriteWrapper();
		w.sprite = spriteName;
		w.offset = offset;
		w.width = Render.instance.getSpriteWidth(spriteName); 
		sprites.add(w);
	}
}


