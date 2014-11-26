package org.whitesoft.games.outrun;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class MainGameScreen implements Screen {
	
	Game game;
	SpriteBatch batch;
	Texture img;

	float fps           = 60;                      // how many 'update' frames per second
	float step1          = 1/fps;                   // how long is each frame (in seconds)
	int width         = Gdx.graphics.getWidth();                    // logical canvas width
	int height        = 768;                     // logical canvas height
	float resolution;
	RoadSegment [] segments;                      // array of road segments

	//var background    = null;                    // our background image (loaded below)
	//  var sprites       = null;                    // our spritesheet (loaded below)
	//  var resolution    = null;                    // scaling factor to provide resolution independence (computed)
	float roadWidth     = 2000;                    // actually half the roads width, easier math if the road spans from -roadWidth to +roadWidth
	float segmentLength = 200;                     // length of a single segment
	int  rumbleLength  = 3;                       // number of segments per red/white rumble strip
	float  trackLength   = 0;                    // z length of entire track (computed)
	float  lanes         = 3;                       // number of lanes
	float  fieldOfView   = 100;                     // angle (degrees) for field of view
	float  cameraHeight  = 1000;                    // z height of camera
	float  cameraDepth   = 0;                    // z distance camera is from screen (computed)
	float  drawDistance  = 300;                     // number of segments to draw
	float  playerX       = 0;                       // player x offset from center of road (-1 to 1 to stay independent of roadWidth)
	float  playerZ       = 0;                    // player relative z distance from camera (computed)
	float  fogDensity    = 5;                       // exponential fog density
	float  position      = 0;                       // current camera Z position (add playerZ to get player's absolute Z position)
	float  speed         = 0;                       // current speed
	float  maxSpeed      = segmentLength/step1;      // top speed (ensure we can't move more than 1 segment in a single frame to make collision detection easier)
	float  accel         =  maxSpeed/5;             // acceleration rate - tuned until it 'felt' right
	float  breaking      = -maxSpeed;               // deceleration rate when braking
	float  decel         = -maxSpeed/5;             // 'natural' deceleration rate when neither accelerating, nor braking
	float  offRoadDecel  = -maxSpeed/2;             // off road deceleration is somewhere in between
	float  offRoadLimit  =  maxSpeed/4;             // limit when off road deceleration no longer applies (e.g. you can always go at least this speed even when off road)

	boolean keyLeft       = false;
	boolean keyRight      = false;
	boolean keyFaster     = false;
	boolean keySlower     = false;

	
/*
    var offRoadDecel   = 0.99;                    // speed multiplier when off road (e.g. you lose 2% speed each update frame)
    var segments       = [];                      // array of road segments
    var cars           = [];                      // array of cars on the road
    */	
	
    float centrifugal    = 0.3f;                     // centrifugal force multiplier when going around curves
    float skySpeed       = 0.001f;                   // background sky layer scroll speed when going around curve (or up hill)
    float hillSpeed      = 0.002f;                   // background hill layer scroll speed when going around curve (or up hill)
    float treeSpeed      = 0.003f;                   // background tree layer scroll speed when going around curve (or up hill)
    float skyOffset      = 0;                       // current sky scroll offset
    float hillOffset     = 0;                       // current hill scroll offset
    float treeOffset     = 0;                       // current tree scroll offset
    int totalCars      = 200;                     // total number of cars on the road
    float currentLapTime = 0;                       // current lap time
    float lastLapTime    = 0;                    // last lap time
	
	
	
	
	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;
	Render renderer;
	
	public MainGameScreen(Game game)
	{
		this.game = game;
		
		width         = Gdx.graphics.getWidth();
		height 		  = Gdx.graphics.getHeight();
		
		camera = new OrthographicCamera(width, height);
		camera.setToOrtho(true);		
		camera.update();

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);
		renderer = new Render(shapeRenderer);

		reset();
	}
	
	//=========================================================================
	// BUILD ROAD GEOMETRY
	//=========================================================================

	void resetRoad() {
		segments = new RoadSegment[500];
		for(int n = 0 ; n < 500 ; n++) {
			segments[n] = new RoadSegment(n, segmentLength);
			segments[n].setColors(
					Math.floor(n/rumbleLength)%2 == 0 ? 
							Colors.getColor("DARKROAD") : Colors.getColor("LIGHTROAD"), 
					Math.floor(n/rumbleLength)%2 == 0 ? 
							Colors.getColor("DARKRUMBLE") : Colors.getColor("LIGHTRUMBLE"), 
					Math.floor(n/rumbleLength)%2 == 0 ? 
							Colors.getColor("DARKGRASS") : Colors.getColor("LIGHTGRASS"),
					Math.floor(n/rumbleLength)%2 == 0 ? 
							Colors.getColor("DARKLANE") : Colors.getColor("LIGHTLANE"));
			
			/*      .push({
	         index: n,
	         p1: { world: { z:  n   *segmentLength }, camera: {}, screen: {} },
	         p2: { world: { z: (n+1)*segmentLength }, camera: {}, screen: {} },
	         color: Math.floor(n/rumbleLength)%2 ? COLORS.DARK : COLORS.LIGHT
	      });
			 */      
		}

		segments[findSegment(playerZ).index + 2].colorRoad = Color.WHITE;
		segments[findSegment(playerZ).index + 3].colorRoad = Color.WHITE;
		for(int n = 0 ; n < rumbleLength ; n++)
			segments[segments.length-1-n].colorRoad = Color.BLACK;

		trackLength = segments.length * segmentLength;
	}

	RoadSegment findSegment(float z) {
		return segments[(int) (Math.floor(z/segmentLength) % segments.length)];
	}

	void updateGameWorld(float dt) {

		position = Util.increase(position, dt * speed, trackLength);

		float dx = dt * 2 * (speed/maxSpeed); // at top speed, should be able to cross from left to right (-1 to 1) in 1 second

		if (keyLeft)
			playerX = playerX - dx;
		else if (keyRight)
			playerX = playerX + dx;

		if (keyFaster)
			speed = Util.accelerate(speed, accel, dt);
		else if (keySlower)
			speed = Util.accelerate(speed, breaking, dt);
		else
			speed = Util.accelerate(speed, decel, dt);

		if (((playerX < -1) || (playerX > 1)) && (speed > offRoadLimit))
			speed = Util.accelerate(speed, offRoadDecel, dt);

		playerX = Util.limit(playerX, -2, 2);     // dont ever let player go too far out of bounds
		speed   = Util.limit(speed, 0, maxSpeed); // or exceed maxSpeed

	}

	void draw(float dt)
	{
		RoadSegment baseSegment = findSegment(position);
		float maxy        = height;

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//		renderer.background(width, height, 0);//BACKGROUND.SKY);
//		renderer.background(background, width, height, BACKGROUND.HILLS);
//		renderer.background(background, width, height, BACKGROUND.TREES);

		int n;
		RoadSegment segment;

		for(n = 0 ; n < drawDistance-1 ; n++) {

			segment        = segments[(baseSegment.index + n) % segments.length];
			segment.looped = segment.index < baseSegment.index;
			segment.fog    = Util.exponentialFog(n/drawDistance, fogDensity);

			Util.project(segment.p1, (playerX * roadWidth), cameraHeight, position - (segment.looped ? trackLength : 0), cameraDepth, width, height, roadWidth);
			Util.project(segment.p2, (playerX * roadWidth), cameraHeight, position - (segment.looped ? trackLength : 0), cameraDepth, width, height, roadWidth);

			if ((segment.p1.camera.z <= cameraDepth) || // behind us
					(segment.p2.screen.y >= maxy))          // clip by (already rendered) segment
				continue;

			renderer.segment(width, lanes, segment);

			maxy = segment.p2.screen.y;
		}

/*		
		renderer.player(width, height, resolution, roadWidth, sprites, speed/maxSpeed,
				cameraDepth/playerZ,
				width/2,
				height,
				speed * (keyLeft ? -1 : keyRight ? 1 : 0),
				0);
*/				
	}
	
		@Override
	public void render(float delta) {
//		System.out.println("Render");
		getInput();
		updateGameWorld(delta);
		draw(delta);
	}

	private void getInput() {
		keyFaster = Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP) ;
		keySlower = Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN) ;
		keyLeft   = Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT) ;
		keyRight  = Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT) ;
		if (Gdx.input.isKeyPressed(Keys.ESCAPE))
			Gdx.app.exit();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
	
	void reset() {
/*
 * 		options       = options || {};
 		canvas.width  = width  = Util.toInt(options.width,          width);
		canvas.height = height = Util.toInt(options.height,         height);
		lanes                  = Util.toInt(options.lanes,          lanes);
		roadWidth              = Util.toInt(options.roadWidth,      roadWidth);
		cameraHeight           = Util.toInt(options.cameraHeight,   cameraHeight);
		drawDistance           = Util.toInt(options.drawDistance,   drawDistance);
		fogDensity             = Util.toInt(options.fogDensity,     fogDensity);
		fieldOfView            = Util.toInt(options.fieldOfView,    fieldOfView);
		segmentLength          = Util.toInt(options.segmentLength,  segmentLength);
		rumbleLength           = Util.toInt(options.rumbleLength,   rumbleLength);
*/
		cameraDepth            = (float) (1 / Math.tan((fieldOfView/2) * Math.PI/180));
		playerZ                = (cameraHeight * cameraDepth);
		resolution             = height/480;

//		if ((segments.length==0) || (options.segmentLength) || (options.rumbleLength))
			resetRoad(); // only rebuild road when necessary
	}

	

}
