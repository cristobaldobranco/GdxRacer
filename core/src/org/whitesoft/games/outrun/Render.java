package org.whitesoft.games.outrun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Render {
	
	ShapeRenderer renderer;

	PolygonSprite poly;
	PolygonSpriteBatch polyBatch = new PolygonSpriteBatch(); // To assign at the beginning
	Texture textureSolid;
	short [] quadIndices = new short[] { 
		    0, 1, 2,         // Two triangles using vertex indices.
		    0, 2, 3          // Take care of the counter-clockwise direction. 
	};
	
	TextureAtlas atlas;

	
	public Render(ShapeRenderer shapeRenderer) {
		renderer = shapeRenderer;
		polyBatch.setProjectionMatrix(renderer.getProjectionMatrix());
		atlas = new TextureAtlas(Gdx.files.internal("packedimages/pack.atlas"));
		AtlasRegion region = atlas.findRegion("imagename");
		Sprite sprite = atlas.createSprite("otherimagename");
	}
	
    private void drawQuadPoly(float [] vertices, Color color)
    {
		// Creating the color filling (but textures would work the same way)
		Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pix.setColor(color);//.setColor(0xDEADBEFF); // DE is red, AD is green and BE is blue.
		pix.fill();
		textureSolid = new Texture(pix);
		PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid), vertices, quadIndices);
		polyBatch.begin();
		poly = new PolygonSprite(polyReg);
		poly.draw(polyBatch);
		polyBatch.end();
//		polyBatch = new PolygonSpriteBatch();
    }

	public void segment(int width, float lanes, RoadSegment segment) {
		
		float x1 = segment.p1.screen.x;
		float y1 = segment.p1.screen.y;
		float w1 = segment.p1.screen.z;
		float x2 = segment.p2.screen.x;
		float y2 = segment.p2.screen.y;
		float w2 = segment.p2.screen.z;
				
		float r1 = rumbleWidth(w1, lanes);
		float r2 = rumbleWidth(w2, lanes);
		float l1 = laneMarkerWidth(w1, lanes);
		float l2 = laneMarkerWidth(w2, lanes);
		float lanew1, lanew2, lanex1, lanex2, lane;

		drawQuadPoly(new float [] {0, y2, width, y2, width, y1, 0, y1}, segment.colorGrass);
		drawQuadPoly(new float [] {x1-w1-r1, y1, x1-w1, y1, x2-w2, y2, x2-w2-r2, y2}, segment.colorRumble);
		drawQuadPoly(new float [] {x1+w1+r1, y1, x1+w1, y1, x2+w2, y2, x2+w2+r2, y2}, segment.colorRumble);		
		drawQuadPoly(new float [] { x1-w1,    y1, x1+w1, y1, x2+w2, y2, x2-w2,    y2}, segment.colorRoad);
		 

		lanew1 = w1*2/lanes;
		lanew2 = w2*2/lanes;
		lanex1 = x1 - w1 + lanew1;
		lanex2 = x2 - w2 + lanew2;
		for(lane = 1 ; lane < lanes ; lanex1 += lanew1, lanex2 += lanew2, lane++)
			drawQuadPoly(new float [] {lanex1 - l1/2, y1, lanex1 + l1/2, y1, lanex2 + l2/2, y2, lanex2 - l2/2, y2}, segment.colorLane);

//		Render.fog(ctx, 0, y1, width, y2-y1, fog);
		
	}

	private float laneMarkerWidth(float w1, float lanes) {
		return w1/Math.max(32, 8*lanes);
	}

	private float rumbleWidth(float w1, float lanes) {
		return w1/Math.max(6,  2*lanes);
	}
	
	public void player(float width, float height, float resolution, float roadWidth, sprites, float speedPercent, float scale, float destX, float destY, float steer, float updown) 
	{

		float bounce = (1.5 * Math.random() * speedPercent * resolution) * Util.randomChoice([-1,1]);
		Sprite sprite;
		if (steer < 0)
			sprite = (updown > 0) ? SPRITES.PLAYER_UPHILL_LEFT : SPRITES.PLAYER_LEFT;
		else if (steer > 0)
			sprite = (updown > 0) ? SPRITES.PLAYER_UPHILL_RIGHT : SPRITES.PLAYER_RIGHT;
		else
			sprite = (updown > 0) ? SPRITES.PLAYER_UPHILL_STRAIGHT : SPRITES.PLAYER_STRAIGHT;

		sprite(width, height, resolution, roadWidth, sprites, sprite, scale, destX, destY + bounce, -0.5, -1);
	}	
	
	public void sprite(float width, float height, float resolution, float roadWidth, int sprites, int sprite, float scale, float destX, float destY, float offsetX, float offsetY, float clipY) {

		//  scale for projection AND relative to roadWidth (for tweakUI)
		float destW  = (sprite.w * scale * width/2) * (SPRITES.SCALE * roadWidth);
		float destH  = (sprite.h * scale * width/2) * (SPRITES.SCALE * roadWidth);

		destX = destX + (destW * (offsetX));
		destY = destY + (destH * (offsetY));

		float clipH = clipY != 0 ? Math.max(0, destY+destH-clipY) : 0;
		if (clipH < destH)
			ctx.drawImage(sprites, sprite.x, sprite.y, sprite.w, sprite.h - (sprite.h*clipH/destH), destX, destY, destW, destH - clipH);

	}	
	
/*
	
	background: function(ctx, background, width, height, layer, rotation, offset) {


	},	
	public void background(int width, int height, int layer, int rotation, int offset) {

		float imageW = layer.w/2
		float imageH = layer.h;

		var sourceX = layer.x + Math.floor(layer.w * rotation);
		var sourceY = layer.y
				var sourceW = Math.min(imageW, layer.x+layer.w-sourceX);
		var sourceH = imageH;

		float destX = 0;
		float destY = offset;
		float destW = Math.floor(width * (sourceW/imageW));
		float destH = height;

		ctx.drawImage(background, sourceX, sourceY, sourceW, sourceH, destX, destY, destW, destH);
		if (sourceW < imageW)
			ctx.drawImage(background, layer.x, sourceY, imageW-sourceW, sourceH, destW-1, destY, width-destW, destH);	
	}
*/
}


/*
var Render = {

		polygon: function(ctx, x1, y1, x2, y2, x3, y3, x4, y4, color) {
	ctx.fillStyle = color;
	ctx.beginPath();
	ctx.moveTo(x1, y1);
	ctx.lineTo(x2, y2);
	ctx.lineTo(x3, y3);
	ctx.lineTo(x4, y4);
	ctx.closePath();
	ctx.fill();
},

//---------------------------------------------------------------------------

segment: function(ctx, width, lanes, x1, y1, w1, x2, y2, w2, fog, color) {

	var r1 = Render.rumbleWidth(w1, lanes),
			r2 = Render.rumbleWidth(w2, lanes),
			l1 = Render.laneMarkerWidth(w1, lanes),
			l2 = Render.laneMarkerWidth(w2, lanes),
			lanew1, lanew2, lanex1, lanex2, lane;

	ctx.fillStyle = color.grass;
	ctx.fillRect(0, y2, width, y1 - y2);

	Render.polygon(ctx, x1-w1-r1, y1, x1-w1, y1, x2-w2, y2, x2-w2-r2, y2, color.rumble);
	Render.polygon(ctx, x1+w1+r1, y1, x1+w1, y1, x2+w2, y2, x2+w2+r2, y2, color.rumble);
	Render.polygon(ctx, x1-w1,    y1, x1+w1, y1, x2+w2, y2, x2-w2,    y2, color.road);

	if (color.lane) {
		lanew1 = w1*2/lanes;
		lanew2 = w2*2/lanes;
		lanex1 = x1 - w1 + lanew1;
		lanex2 = x2 - w2 + lanew2;
		for(lane = 1 ; lane < lanes ; lanex1 += lanew1, lanex2 += lanew2, lane++)
			Render.polygon(ctx, lanex1 - l1/2, y1, lanex1 + l1/2, y1, lanex2 + l2/2, y2, lanex2 - l2/2, y2, color.lane);
	}

	Render.fog(ctx, 0, y1, width, y2-y1, fog);
},

//---------------------------------------------------------------------------

background: function(ctx, background, width, height, layer, rotation, offset) {

	rotation = rotation || 0;
	offset   = offset   || 0;

	var imageW = layer.w/2;
	var imageH = layer.h;

	var sourceX = layer.x + Math.floor(layer.w * rotation);
	var sourceY = layer.y
			var sourceW = Math.min(imageW, layer.x+layer.w-sourceX);
	var sourceH = imageH;

	var destX = 0;
	var destY = offset;
	var destW = Math.floor(width * (sourceW/imageW));
	var destH = height;

	ctx.drawImage(background, sourceX, sourceY, sourceW, sourceH, destX, destY, destW, destH);
	if (sourceW < imageW)
		ctx.drawImage(background, layer.x, sourceY, imageW-sourceW, sourceH, destW-1, destY, width-destW, destH);
},

//---------------------------------------------------------------------------

sprite: function(ctx, width, height, resolution, roadWidth, sprites, sprite, scale, destX, destY, offsetX, offsetY, clipY) {

	//  scale for projection AND relative to roadWidth (for tweakUI)
	var destW  = (sprite.w * scale * width/2) * (SPRITES.SCALE * roadWidth);
	var destH  = (sprite.h * scale * width/2) * (SPRITES.SCALE * roadWidth);

	destX = destX + (destW * (offsetX || 0));
	destY = destY + (destH * (offsetY || 0));

	var clipH = clipY ? Math.max(0, destY+destH-clipY) : 0;
	if (clipH < destH)
		ctx.drawImage(sprites, sprite.x, sprite.y, sprite.w, sprite.h - (sprite.h*clipH/destH), destX, destY, destW, destH - clipH);

},

//---------------------------------------------------------------------------

,

//---------------------------------------------------------------------------

fog: function(ctx, x, y, width, height, fog) {
	if (fog < 1) {
		ctx.globalAlpha = (1-fog)
				ctx.fillStyle = COLORS.FOG;
		ctx.fillRect(x, y, width, height);
		ctx.globalAlpha = 1;
	}
},

rumbleWidth:     function(projectedRoadWidth, lanes) { return projectedRoadWidth/Math.max(6,  2*lanes); },
laneMarkerWidth: function(projectedRoadWidth, lanes) { return projectedRoadWidth/Math.max(32, 8*lanes); }

}
*/