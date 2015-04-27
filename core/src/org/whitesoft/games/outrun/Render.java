package org.whitesoft.games.outrun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class Render {

	public static Render instance = null;

	public static void initialize(ShapeRenderer sr)
	{
		instance = new Render(sr);
	}

	ShapeRenderer renderer;

	PolygonSprite poly;
	PolygonSpriteBatch polyBatch = new PolygonSpriteBatch(); // To assign at the beginning
	Texture textureSolid;
	short [] quadIndices = new short[] { 
			0, 1, 2,         // Two triangles using vertex indices.
			0, 2, 3          // Take care of the counter-clockwise direction. 
	};

	TextureAtlas atlas;
	TextureRegion textureRegion;
	float spriteScale; 

	Sprite playerSprite;
	Sprite [] backgroundSprites;

	private TextureRegion treesRegion;
	private TextureRegion skyRegion;
	private TextureRegion mountainsRegion;
	private Texture backgrounds;	
	BitmapFont font;

	Stage stage = new Stage();
	Skin skin;
	Table table;
	Label timeRemainingLabel;
	Label lapTimeLabel;
	Label fastestLapTimeLabel;
	Label speedLabel;
	Label percentFinishedLabel;
	Label messageLabel;
	Label text1, text2, text3;
	// table.align(Align.right | Align.bottom);

	Color normalTextColor = new Color(1,1,1,1);
	Color warningTextColor = new Color(1,0,0,1);

	String messageText;
	long messageTimestamp;
	int messageTimeOut;

	private boolean messageFlash;

	private long nextMessageFlashToogleTime;

	private Render(ShapeRenderer shapeRenderer) 
	{
		renderer = shapeRenderer;
		polyBatch.setProjectionMatrix(renderer.getProjectionMatrix());
		polyBatch.enableBlending();
		atlas = new TextureAtlas(Gdx.files.internal("sprites.atlas"));
		spriteScale = (float) 0.3 * (1f / atlas.findRegion("player_straight").getRegionWidth());
		createBackgrounds();

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("clacon.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 24;
		parameter.shadowOffsetX = 1;
		parameter.shadowOffsetY = 1;
		parameter.shadowColor = new Color(0,0,0,1);
		BitmapFont font24 = generator.generateFont(parameter); // font size 12 pixels

		parameter.size = 64;
		parameter.shadowOffsetX = 3;
		parameter.shadowOffsetY = 3;
		BitmapFont font48 = generator.generateFont(parameter); // font size 12 pixels

		parameter.size = 128;
		BitmapFont hugeFont = generator.generateFont(parameter); // font size 12 pixels

		generator.dispose(); // don't forget to dispose to avoid memory leaks!

		skin = new Skin(Gdx.files.internal("uiskin.json"));		
		font = new BitmapFont(Gdx.files.internal("font.fnt"),Gdx.files.internal("font.png"),false);

		table = new Table();
		stage.addActor(table);
//		table.setFillParent(true);
		table.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

//		table.debug();

		LabelStyle style24 = new LabelStyle(font24, null);
		LabelStyle style48 = new LabelStyle(font48, null);
		LabelStyle styleHuge = new LabelStyle(hugeFont, new Color(1, 0.9f, 0, 1));

		timeRemainingLabel = new Label("", style48);
		lapTimeLabel = new Label("", style24);
		fastestLapTimeLabel = new Label("", style24);
		percentFinishedLabel = new Label("", style24);
		messageLabel = new Label("", styleHuge);
		messageLabel.setAlignment(Align.center);
		speedLabel = new Label("0km/h", style24);
		speedLabel.setAlignment(Align.right);
		percentFinishedLabel.setAlignment(Align.left);

		text1 = new Label("CURRENT LAP", style24);
		text3 = new Label("FASTEST LAP", style24);
		text2 = new Label("", style24);

		table.columnDefaults(0).width(80).padLeft(50);
		table.columnDefaults(1).expandX();
		table.columnDefaults(2).padRight(50).width(80);

		table.top();
		table.add(text1);
		table.add(text2);
		table.add(text3);
		table.row();

		table.add(lapTimeLabel);
		table.add(timeRemainingLabel);
		table.add(fastestLapTimeLabel);
		table.row();

		table.center();
		table.add(messageLabel).colspan(3).padRight(50).expandY();
		table.row();
		table.bottom();
		table.add(percentFinishedLabel);
		table.add();
		table.add(speedLabel);
		

	}

	private void createBackgrounds() 
	{

		backgrounds = new Texture(Gdx.files.internal("background/background.png"));

		mountainsRegion 	= new TextureRegion(backgrounds, 5, 5, 1280, 480);
		skyRegion 			= new TextureRegion(backgrounds, 5, 495, 1280, 480);
		treesRegion 		= new TextureRegion(backgrounds, 5, 985, 1280, 480);

		backgroundSprites = new Sprite[3];
		backgroundSprites[0]  = new Sprite(skyRegion);
		backgroundSprites[1]  = new Sprite(mountainsRegion);
		backgroundSprites[2]  = new Sprite(treesRegion);

		for (Sprite s : backgroundSprites)
		{
			s.flip(false, true);
		}
	}	



	public void startRenderSequence() {
		polyBatch.begin();
	}

	public void finishRenderSequence() {
		polyBatch.end();
	}
	private void drawQuadPoly(float [] vertices, TextureRegion region)
	{
		PolygonRegion polyReg = new PolygonRegion(region, vertices, quadIndices);
		poly = new PolygonSprite(polyReg);
		poly.draw(polyBatch);
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

		drawQuadPoly(new float [] {0, y2, width, y2, width, y1, 0, y1}, segment.textureGrass);
		drawQuadPoly(new float [] {x1-w1-r1, y1, x1-w1, y1, x2-w2, y2, x2-w2-r2, y2}, segment.textureRumble);
		drawQuadPoly(new float [] {x1+w1+r1, y1, x1+w1, y1, x2+w2, y2, x2+w2+r2, y2}, segment.textureRumble);		
		drawQuadPoly(new float [] { x1-w1,    y1, x1+w1, y1, x2+w2, y2, x2-w2,    y2}, segment.textureRoad);


		lanew1 = w1*2/lanes;
		lanew2 = w2*2/lanes;
		lanex1 = x1 - w1 + lanew1;
		lanex2 = x2 - w2 + lanew2;
		for(lane = 1 ; lane < lanes ; lanex1 += lanew1, lanex2 += lanew2, lane++)
			drawQuadPoly(new float [] {lanex1 - l1/2, y1, lanex1 + l1/2, y1, lanex2 + l2/2, y2, lanex2 - l2/2, y2}, segment.textureLane);

		//		Render.fog(ctx, 0, y1, width, y2-y1, fog);

	}

	private float laneMarkerWidth(float w1, float lanes) {
		return w1/Math.max(32, 8*lanes);
	}

	private float rumbleWidth(float w1, float lanes) {
		return w1/Math.max(6,  2*lanes);
	}

	public void player(float width, float height, float resolution, float roadWidth, float speedPercent, float scale, float destX, float destY, float steer, float updown, boolean offroad) 
	{
		float bounce = (float) (1.5 * Math.random() * speedPercent * resolution) * Util.randomSign();
		if (offroad)
			bounce *= 5;

		String spriteName;
		if (steer < 0)
			spriteName = (updown > 0) ? "player_uphill_left" : "player_left";
		else if (steer > 0)
			spriteName = (updown > 0) ? "player_uphill_right" : "player_right";
		else
			spriteName = (updown > 0) ? "player_uphill_straight" : "player_straight";

		sprite(width, height, resolution, roadWidth, spriteName, scale, destX, destY + bounce, -0.5f, -1, 0);
	}	

	public void sprite(float width, float height, float resolution, float roadWidth, String spriteName, float scale, float destX, float destY, float offsetX, float offsetY, float clipY) 
	{
		AtlasRegion reg = atlas.findRegion(spriteName);
		if (reg == null)
		{
			System.out.println(spriteName);
			return;
		}
		TextureRegion region = new TextureRegion(reg);
//		reg.flip(false, true);
//		Sprite sprite = atlas.createSprite(spriteName);
//		sprite.flip(false, true);
		//  scale for projection AND relative to roadWidth (for tweakUI)
		float destW  = (reg.getRegionWidth()  * scale * width/2) * (spriteScale * roadWidth);
		float destH  = (reg.getRegionHeight() * scale * width/2) * (spriteScale * roadWidth);

		destX = destX + (destW * (offsetX));
		destY = destY + (destH * (offsetY));
		

		float clipH = clipY != 0 ? Math.max(0, destY+destH-clipY) : 0;
		region.setRegion(reg.getRegionX(), reg.getRegionY(), reg.getRegionWidth(), (int) (reg.getRegionHeight() - (reg.getRegionHeight()*clipH/destH)));
		if (clipH < destH)
		{
//			System.out.println(destX + ", "+  destY + ", " + destW + ", " + ( destH	-clipH));
//			sprite.setBounds(destX, destY, destW, destH);
//			sprite.draw(polyBatch);
			region.flip(false, true);
			polyBatch.draw(region, destX, destY, destW, destH-clipH);
		}
	}	
	public void background(int width, int height, int layer, float rotation, float offset) 
	{
		float sourceX = (float) Math.floor(backgroundSprites[layer].getWidth() * rotation);

		backgroundSprites[layer].setPosition(-sourceX, offset); 
		backgroundSprites[layer].draw(polyBatch);
		backgroundSprites[layer].setPosition(backgroundSprites[layer].getWidth()-sourceX, offset);
		backgroundSprites[layer].draw(polyBatch);
	}

	public float getSpriteWidth(String spriteName)
	{
		Sprite sprite = atlas.createSprite(spriteName);
		if (sprite != null)
			return sprite.getWidth();
		return 0;
	}

	public void text(String txt, int timeout)
	{
		text(txt, timeout, false);
	}
	
	
	public void text(String txt, int timeout, boolean flash)
	{
		this.messageText = txt;
		this.messageTimeOut = timeout;
		this.messageTimestamp = System.currentTimeMillis();
		this.messageFlash = flash;
		this.nextMessageFlashToogleTime = System.currentTimeMillis() + 500;
		//		messageLabel.setText(txt);
		//		stage.draw();

		//		font.draw(polyBatch, txt, x, y);
	}

	public void ui(GameStats gameStats) {
		lapTimeLabel.setText(gameStats.getLapTime());
		if (gameStats.countdownWarning())
		{
			timeRemainingLabel.getStyle().fontColor = warningTextColor;
		}
		else
			timeRemainingLabel.getStyle().fontColor = normalTextColor;

		timeRemainingLabel.setText(Integer.toString(gameStats.endgameTimer < 0 ? 0 : (int) gameStats.endgameTimer ));
		fastestLapTimeLabel.setText(gameStats.getFastestLapTime());
		speedLabel.setText(gameStats.currentSpeed + " km/h");
		percentFinishedLabel.setText(gameStats.getPercentFinished());
		messageLabel.setText(messageText);
		if (messageFlash)
		{
			if (nextMessageFlashToogleTime < System.currentTimeMillis())
			{
				if (messageLabel.getStyle().fontColor.a > 0.5f)
					messageLabel.getStyle().fontColor.a = 0;
				else
					messageLabel.getStyle().fontColor.a = 1;
				nextMessageFlashToogleTime = System.currentTimeMillis() + 500;
			}
		}
		if (messageTimeOut != 0)
		{
			if (messageTimestamp + messageTimeOut * 1000 < System.currentTimeMillis())
			{
				messageLabel.setText("");
				messageText = "";
				messageTimeOut = 0;
				messageFlash = false;
			}
		}
		stage.draw();

	}
}

