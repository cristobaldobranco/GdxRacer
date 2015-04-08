package org.whitesoft.games.outrun;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.whitesoft.games.outrun.RoadSegment.SpriteWrapper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class MainGameScreen implements Screen {
	/*
	static int LENGTH_NONE = 0;
	static int LENGTH_SHORT = 25;
	static int LENGTH_MEDIUM = 50;
	static int LENGTH_LONG = 100;

	static int HILL_NONE = 0;
	static int HILL_LOW  = 20;
	static int HILL_MEDIUM = 40;
	static int HILL_HIGH = 60;

	static int CURVE_NONE = 0;
	static int CURVE_EASY = 2;
	static int CURVE_MEDIUM = 4;
	static int CURVE_HARD = 6;
	 */
	enum Curve{
		NONE(0),
		EASY(2),
		MEDIUM(4),
		HARD(6);
		//...

		private float value;

		private Curve (float v)
		{
			value = v;
		}

		public float getValue()
		{
			return value;
		}

		private static final List<Curve> VALUES =
				Collections.unmodifiableList(Arrays.asList(values()));
		private static final int SIZE = VALUES.size();
		private static final Random RANDOM = new Random();

		public static Curve randomLetter()  {
			return VALUES.get(RANDOM.nextInt(SIZE));
		}

	}	

	enum Hill {
		NONE(0),
		LOW(20),
		MEDIUM(40),
		HIGH(60);
		//...

		private float value;

		private Hill (float v)
		{
			value = v;
		}

		public float getValue()
		{
			return value;
		}

		private static final List<Hill> VALUES =
				Collections.unmodifiableList(Arrays.asList(values()));
		private static final int SIZE = VALUES.size();
		private static final Random RANDOM = new Random();

		public static Hill randomLetter()  {
			return VALUES.get(RANDOM.nextInt(SIZE));
		}
	}		

	enum Length {
		NONE(0),
		SHORT(25),
		MEDIUM(50),
		LONG(100);
		//...

		private int value;

		private Length (int v)
		{
			value = v;
		}

		public int getValue()
		{
			return value;
		}

		private static final List<Length> VALUES =
				Collections.unmodifiableList(Arrays.asList(values()));
		private static final int SIZE = VALUES.size();
		private static final Random RANDOM = new Random();

		public static Length randomLetter()  {
			return VALUES.get(RANDOM.nextInt(SIZE));
		}
	}		
	enum RaceState {
		RACE_STATE_PRERACE,
		RACE_STATE_RACE,
		RACE_STATE_TIMEUP,
		RACE_STATE_GAMEOVER
	}

	Game game;
	SpriteBatch batch;
	Texture img;

	float fps           = 60;                      // how many 'update' frames per second
	float step1          = 1/fps;                   // how long is each frame (in seconds)
	int width         = Gdx.graphics.getWidth();                    // logical canvas width
	int height        = 768;                     // logical canvas height
	float resolution;
	Vector<RoadSegment> segments;                      // array of road segments

	Vector<Car> cars;

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
	float  spriteScale   = 1;

	boolean keyLeft       = false;
	boolean keyRight      = false;
	boolean keyFaster     = false;
	boolean keySlower     = false;

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
	//	float endgameTimer = 0;
	int lapNumber = 0;
	float preRaceCountdown = 3.9f;

	GameStats gameStats = new GameStats();

	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;
	//	private boolean raceStarted = false;
	private int startSegment;
	//	private boolean raceTimeUp = false;
	private boolean checkpointFired = false;
	//	private boolean gameOver = false;
	RaceState raceState = RaceState.RACE_STATE_PRERACE;

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
		Render.initialize(shapeRenderer);
		spriteScale = (float) (0.3 / Render.instance.getSpriteWidth("player_straight"));

		reset();
	}

	//=========================================================================
	// BUILD ROAD GEOMETRY
	//=========================================================================


	private float lastY() {
		return (segments.size() == 0) ? 0 : segments.get(segments.size()-1).p2.world.y;
	}

	private void addSegment(float curve, float y) {
		int n = segments.size();
		RoadSegment segment = new RoadSegment(n, segmentLength, lastY(), y);
		segment.curve = curve;
		segment.setColors(
				Math.floor(n/rumbleLength)%2 == 0 ? 
						Colors.getColor("DARKROAD") : Colors.getColor("LIGHTROAD"), 
						Math.floor(n/rumbleLength)%2 == 0 ? 
								Colors.getColor("DARKRUMBLE") : Colors.getColor("LIGHTRUMBLE"), 
								Math.floor(n/rumbleLength)%2 == 0 ? 
										Colors.getColor("DARKGRASS") : Colors.getColor("LIGHTGRASS"),
										Math.floor(n/rumbleLength)%2 == 0 ? 
												Colors.getColor("DARKLANE") : Colors.getColor("LIGHTLANE"));

		segments.add(segment);
	}

	//	private void addSprite(int n, String sprite, float offset)
	//	{
	//		segments.get(n).addSprite(sprite, offset);
	//	}

	private void addRoad(float enter, float hold, float leave, float curve, float y)
	{
		float startY   = lastY();
		float endY     = startY + ( (int) y) * segmentLength;
		float n, total = enter + hold + leave;
		for(n = 0 ; n < enter ; n++)
			addSegment(Util.easeIn(0, curve, n/enter), Util.easeInOut(startY, endY, n/total));
		for(n = 0 ; n < hold  ; n++)
			addSegment(curve, Util.easeInOut(startY, endY, (enter+n)/total));
		for(n = 0 ; n < leave ; n++)
			addSegment(Util.easeInOut(curve, 0, n/leave), Util.easeInOut(startY, endY, (enter+hold+n)/total));
	}

	private void addStraight(int num)
	{
		Gdx.app.log("TrackGen", "addStraight(" +  num + ")");
		//		num = (num > 0) ? num : Length.MEDIUM.value;
		addRoad(num, num, num, 0, 0);
	}

	private void addHill(int num, float height)
	{
		Gdx.app.log("TrackGen", "addHill(" +  num + ", " + height + ")");
		//		num = (num > 0) ? num : Length.MEDIUM.value;
		//		height = (height > 0) ? height : Hill.MEDIUM.value;
		addRoad(num, num, num, 0, height);
	}

	private void addCurve(int num, float curve, float height)
	{
		//		num = (num > 0) ? num : Length.MEDIUM.value;
		curve = (curve > 0) ? curve : Curve.MEDIUM.value;
		//		height = (height > 0) ? height : Hill.MEDIUM.value;
		Gdx.app.log("TrackGen", "addCurve(" +  num + ", " + curve + ", "+ height + ")");
		addRoad(num, num, num, curve, height);
	}

	private void addLowRollingHills(int num, float height)
	{
		Gdx.app.log("TrackGen", "addLowRollingHills(" +  num + ", " + height + ")");		
		//		num = (num > 0) ? num : Length.LENGTH_SHORT;
		//		height = (height > 0) ? height : HILL_LOW;
		addRoad(num, num, num,  0,                height/2);
		addRoad(num, num, num,  0,               -height);
		addRoad(num, num, num,  Curve.EASY.value, height);
		addRoad(num, num, num,  0,                0);
		addRoad(num, num, num, -Curve.EASY.value, height/2);
		addRoad(num, num, num,  0,                0);
	}

	private void addSCurves()
	{
		Gdx.app.log("TrackGen", "addSCurves()");		
		addRoad(Length.MEDIUM.value, Length.MEDIUM.value, Length.MEDIUM.value,  -Curve.EASY.value,    Hill.NONE.value);
		addRoad(Length.MEDIUM.value, Length.MEDIUM.value, Length.MEDIUM.value,   Curve.MEDIUM.value,  Hill.MEDIUM.value);
		addRoad(Length.MEDIUM.value, Length.MEDIUM.value, Length.MEDIUM.value,   Curve.EASY.value,   -Hill.LOW.value);
		addRoad(Length.MEDIUM.value, Length.MEDIUM.value, Length.MEDIUM.value,  -Curve.EASY.value,    Hill.MEDIUM.value);
		addRoad(Length.MEDIUM.value, Length.MEDIUM.value, Length.MEDIUM.value,  -Curve.MEDIUM.value, -Hill.MEDIUM.value);
	}

	private void addBumps() 
	{
		Gdx.app.log("TrackGen", "addBumps()");		
		addRoad(10, 10, 10, 0,  5);
		addRoad(10, 10, 10, 0, -2);
		addRoad(10, 10, 10, 0, -5);
		addRoad(10, 10, 10, 0,  8);
		addRoad(10, 10, 10, 0,  5);
		addRoad(10, 10, 10, 0, -7);
		addRoad(10, 10, 10, 0,  5);
		addRoad(10, 10, 10, 0, -2);
	}

	private void addDownhillToEnd(int num) {
		Gdx.app.log("TrackGen", "addLowRollingHills(" +  num + ")");		
		num = num > 0 ? num : 200;    	
		addRoad(num, num, num, -Curve.EASY.value, -lastY()/segmentLength);
	}

	private void generateRandomTrack(int cutOffLength)
	{
		Random rnd = new Random(10);

		addStraight(Length.randomLetter().value);

		while (segments.size() < cutOffLength)
		{
			int r = rnd.nextInt(10); //50% of elements are curves
			switch (r)
			{
			case 0: addLowRollingHills(Length.randomLetter().value, Hill.randomLetter().value);
			case 1: addSCurves(); 
			case 2: addStraight(Length.randomLetter().value);
			case 3: addBumps();
			case 4: addHill(Length.randomLetter().value, Hill.randomLetter().value);
			default: addCurve(Length.randomLetter().value, Curve.randomLetter().value, Hill.randomLetter().value);
			}
		}
		addDownhillToEnd(0);
	}

	void resetRoad() {
		segments = new Vector<RoadSegment>();
		generateRandomTrack(5500);
		/*
 		addStraight(Length.SHORT.value);
 		addLowRollingHills(0, 0);
		addSCurves();
		addCurve(Length.MEDIUM.value, Curve.MEDIUM.value, Hill.LOW.value);
		addBumps();
		addLowRollingHills(0,0);
		addCurve(Length.LONG.value*2, Curve.MEDIUM.value, Hill.MEDIUM.value);
		addStraight(0);
		addHill(Length.MEDIUM.value, Hill.HIGH.value);
		addSCurves();
		addCurve(Length.LONG.value, -Curve.MEDIUM.value, Hill.NONE.value);
		addHill(Length.LONG.value, Hill.HIGH.value);
		addCurve(Length.LONG.value, Curve.MEDIUM.value, -Hill.LOW.value);
		addBumps();
		addHill(Length.LONG.value, -Hill.MEDIUM.value);
		addStraight(0);
		addSCurves();
		addDownhillToEnd(0);
		 */
		resetSprites();
		resetCars();

		startSegment = findSegment(playerZ).index + 2;
		segments.get(findSegment(playerZ).index + 2).setUniColor(Color.WHITE);
		segments.get(findSegment(playerZ).index + 3).setUniColor(Color.WHITE);
		//		for(int n = 0 ; n < rumbleLength ; n++)
		//			segments.get(segments.size() -1-n).colorRoad = Color.BLACK;

		trackLength = segments.size() * segmentLength;
	}

	void resetCars()
	{
		cars = new Vector<Car>();

		String [] carNames = { "car01","car02", "car03", "car04", "truck", "semi"} ;		 
		RoadSegment segment;
		float offset, z, speed;
		String spriteName;
		for (int n = 0 ; n < totalCars ; n++) 
		{
			int neg = Util.randomInt(0,  1);
			offset = (float) (Math.random() * 0.8f * (neg == 1 ? 1 : -1));
			z      = (float) (Math.floor(Math.random() * segments.size()) * segmentLength);

			spriteName = carNames[Util.randomInt(0, carNames.length - 1)];

			speed  = (float) (maxSpeed/4 + Math.random() * maxSpeed/(spriteName.equals("semi") ? 4 : 2));
			Car car = new Car();
			car.offset = offset;
			car.spriteName = spriteName;
			car.speed = speed;
			car.z = z;
			car.width = Render.instance.getSpriteWidth(spriteName);
			segment = findSegment(car.z);
			segment.cars.add(car);
			cars.add(car);
		}
	}

	void resetSprites()
	{
		int [] posNegChoice = {-1, 1 };
		String [] plants = { "tree1","tree2", "dead_tree1", "dead_tree2", "palm_tree", "bush1", "bush2", "cactus", "stump", "boulder1", "boulder2", "boulder3" } ;    

		int n, i;
		segments.get( 20).addSprite("billboard07", -1);
		segments.get( 40).addSprite("billboard06", -1);
		segments.get( 60).addSprite("billboard08", -1);
		segments.get( 80).addSprite("billboard09", -1);
		segments.get(100).addSprite("billboard01", -1);
		segments.get(120).addSprite("billboard02", -1);
		segments.get(140).addSprite("billboard03", -1);
		segments.get(160).addSprite("billboard04", -1);
		segments.get(180).addSprite("billboard05", -1);
		segments.get(240).addSprite("billboard07", -1.2f);
		segments.get(240).addSprite("billboard06", 1.2f);		
		segments.get(segments.size() - 25).addSprite("billboard07", -1.2f);
		segments.get(segments.size() - 25).addSprite("billboard07", 1.2f);		

		for(n = 10 ; n < 200 ; n += 4 + Math.floor(n/100)) 
		{
			segments.get(n).addSprite("palm_tree", (float) (0.5f + Math.random()*0.5f)); 
			segments.get(n).addSprite("palm_tree", (float) (  1f + Math.random()*  2f));
		}
		for(n = 250 ; n < 1000 ; n += 5) 
		{
			segments.get(n).addSprite("column", 1.1f);
			segments.get(n + Util.randomInt(0,5)).addSprite("tree1", (float) (-1 - (Math.random() * 2)));
			segments.get(n + Util.randomInt(0,5)).addSprite("tree2", (float) (-1 - (Math.random() * 2)));
		}
		for(n = 200 ; n < segments.size() ; n += 3) {
			segments.get(n).addSprite(plants[Util.randomInt(0, plants.length - 1)], (float) (Util.randomChoice(posNegChoice) * (2 + Math.random() * 5))); 
		}
		for(n = 1000 ; n < (segments.size()-50) ; n += 100) 
		{
			int side      = Util.randomChoice(posNegChoice);
			segments.get(n + Util.randomInt(0, 50)).addSprite("billboard0" + Util.randomInt(1, 9), -side);

			for(i = 0 ; i < 20 ; i++) {
				String s = plants[Util.randomInt(0, plants.length - 1)];
				float offset = (float) (side * (1.5 + Math.random()));
				segments.get(n + Util.randomInt(0, 50)).addSprite(s, offset); 
			}

		}
	}

	RoadSegment findSegment(float z) {
		return segments.get((int) (Math.floor(z/segmentLength) % segments.size()));
	}

	void updateGameWorld(float dt) 
	{
		RoadSegment playerSegment = findSegment(position + playerZ);		
		float startPosition = position;
		float speedPercent  = speed/maxSpeed;
		float playerW       = Render.instance.getSpriteWidth("player_straight") * spriteScale;
		updateCars(dt, playerSegment, playerW);

		position = Util.increase(position, dt * speed, trackLength);

		float dx = dt * 2 * speedPercent; // at top speed, should be able to cross from left to right (-1 to 1) in 1 second

		if (keyLeft)
			playerX = playerX - dx;
		else if (keyRight)
			playerX = playerX + dx;

		playerX = playerX - (dx * speedPercent * playerSegment.curve * centrifugal);		

		if (keyFaster)
			speed = Util.accelerate(speed, accel, dt);
		else if (keySlower)
			speed = Util.accelerate(speed, breaking, dt);
		else
			speed = Util.accelerate(speed, decel, dt);

		if ((playerX < -1) || (playerX > 1)) {
			if (speed > offRoadLimit) {
				speed = Util.accelerate(speed, offRoadDecel, dt);
			}

			for(SpriteWrapper sprite : playerSegment.sprites) {
				float  spriteW = sprite.width * spriteScale;
				if (Util.overlap(playerX, playerW, sprite.offset + spriteW/2 * (sprite.offset > 0 ? 1 : -1), spriteW)) 
				{
					if  ( (raceState == RaceState.RACE_STATE_TIMEUP) || (raceState == RaceState.RACE_STATE_GAMEOVER) )
					{
						speed = 0;
					}
					else
					{
						speed = maxSpeed/5;
					}
					position = Util.increase(playerSegment.p1.world.z, -playerZ, trackLength); // stop in front of sprite (at front of segment)
					break;
				}
			}			
		}

		for(Car car : playerSegment.cars) {
			float carW = car.width * spriteScale;
			if (speed > car.speed) {
				if (Util.overlap(playerX, playerW, car.offset, carW, 0.8f)) {
					speed    = car.speed * (car.speed/speed);
					position = Util.increase(car.z, -playerZ, trackLength);
					break;
				}
			}
		}

		if (playerSegment.index == startSegment)
		{
			if (!checkpointFired )
			{
				raceState = RaceState.RACE_STATE_RACE;
				gameStats.checkpoint(5);
				checkpointFired = true;
			}
		}
		else
		{
			checkpointFired = false;
		}

		if ((raceState == RaceState.RACE_STATE_TIMEUP) && speed <= 0)
			raceState = RaceState.RACE_STATE_GAMEOVER;

		playerX = Util.limit(playerX, -2, 2);     // dont ever let player go too far out of bounds
		speed   = Util.limit(speed, 0, maxSpeed); // or exceed maxSpeed

		skyOffset  = Util.increase(skyOffset,  skySpeed  * playerSegment.curve * (position-startPosition)/segmentLength, 1);
		hillOffset = Util.increase(hillOffset, hillSpeed * playerSegment.curve * (position-startPosition)/segmentLength, 1);
		treeOffset = Util.increase(treeOffset, treeSpeed * playerSegment.curve * (position-startPosition)/segmentLength, 1);

	}

	private void updateCars(float dt, RoadSegment playerSegment, float playerW) {
		RoadSegment oldSegment, newSegment;
		for(Car car : cars) 
		{
			oldSegment  = findSegment(car.z);
			car.offset  = car.offset + updateCarOffset(car, oldSegment, playerSegment, playerW);
			car.z       = Util.increase(car.z, dt * car.speed, trackLength);
			car.percent = Util.percentRemaining(car.z, segmentLength); // useful for interpolation during rendering phase
			newSegment  = findSegment(car.z);
			if (oldSegment != newSegment) {
				int index = oldSegment.cars.indexOf(car);
				oldSegment.cars.remove(index);
				newSegment.cars.add(car);
			}
		}
	}


	private float updateCarOffset(Car car, RoadSegment carSegment, RoadSegment playerSegment, float playerW) {
		int i;
		RoadSegment segment;

		float otherCarW, dir;
		int lookahead = 50;
		float carW = car.width * spriteScale;
		// optimization, dont bother steering around other cars when 'out of sight' of the player
		if ((carSegment.index - playerSegment.index) > drawDistance)
			return 0;
		for(i = 1 ; i < lookahead ; i++) {
			segment = segments.get((carSegment.index+i)%segments.size());
			if ((segment == playerSegment) && (car.speed > speed) && (Util.overlap(playerX, playerW, car.offset, carW, 1.2f))) {
				if (playerX > 0.5)
					dir = -0.3f;
				else if (playerX < -0.5)
					dir = 0.3f;
				else
					dir = (car.offset > playerX) ? 1 : -1;
				return dir * 1/i * (car.speed-speed)/maxSpeed; // the closer the cars (smaller i) and the greated the speed ratio, the larger the offset
			}
			for(Car otherCar : segment.cars) {
				otherCarW = otherCar.width * spriteScale;
				if ((car.speed > otherCar.speed) && Util.overlap(car.offset, carW, otherCar.offset, otherCarW, 1.2f)) {
					if (otherCar.offset > 0.5)
						dir = -0.3f;
					else if (otherCar.offset < -0.5)
						dir = 0.3f;
					else
						dir = (car.offset > otherCar.offset) ? 1 : -1;

					float ret = dir/i * (car.speed-otherCar.speed)/maxSpeed; 
					System.out.println(ret);
					return ret; 
				}
			}
		}
		// if no cars ahead, but I have somehow ended up off road, then steer back on
		if (car.offset < -0.9)
			return 0.1f;
		else if (car.offset > 0.9)
			return -0.1f;
		else
			return 0;
	}	

	void draw(float dt)
	{
		int n;
		RoadSegment segment;

		RoadSegment baseSegment = findSegment(position);
		RoadSegment playerSegment = findSegment(position+playerZ);
		float maxy        = height;
		float basePercent   = Util.percentRemaining(position, segmentLength);
		float playerPercent = Util.percentRemaining(position+playerZ, segmentLength);
		float playerY       = Util.interpolate(playerSegment.p1.world.y, playerSegment.p2.world.y, playerPercent);
		float x = 0;
		float dx = - (baseSegment.curve * basePercent);

		Gdx.gl.glClearColor(114f / 255, 215 / 255f, 238/ 255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Render.instance.startRenderSequence();

		Render.instance.background(width, height, 0, skyOffset,  resolution * skySpeed  * playerY);//BACKGROUND.SKY);
		Render.instance.background(width, height, 1, hillOffset, resolution * hillSpeed * playerY);
		Render.instance.background(width, height, 2, treeOffset, resolution * treeSpeed * playerY);

		for(n = 0 ; n < drawDistance; n++) {

			segment        = segments.get((baseSegment.index + n) % segments.size());
			segment.looped = segment.index < baseSegment.index;
			segment.fog    = Util.exponentialFog(n/drawDistance, fogDensity);
			segment.clip   = maxy;

			Util.project(segment.p1, (playerX * roadWidth) - x,      playerY + cameraHeight, position - (segment.looped ? trackLength : 0), cameraDepth, width, height, roadWidth);
			Util.project(segment.p2, (playerX * roadWidth) - x - dx, playerY + cameraHeight, position - (segment.looped ? trackLength : 0), cameraDepth, width, height, roadWidth);

			x = x + dx;
			dx = dx + segment.curve;

			if ((segment.p1.camera.z <= cameraDepth)         || // behind us
					(segment.p2.screen.y >= segment.p1.screen.y) || // back face cull
					(segment.p2.screen.y >= maxy))                  // clip by (already rendered) hill
				continue;

			Render.instance.segment(width, lanes, segment);

			maxy = segment.p1.screen.y;
		}

		for(n = (int) (drawDistance-1) ; n > 0 ; n--) {
			segment = segments.get((baseSegment.index + n) % segments.size());


			for(Car car : segment.cars) 
			{
				String spriteName      = car.spriteName;
				float spriteScale = Util.interpolate(segment.p1.screenScale, segment.p2.screenScale, car.percent);
				float spriteX     = Util.interpolate(segment.p1.screen.x,     segment.p2.screen.x,     car.percent) + (spriteScale * car.offset * roadWidth * width/2);
				float spriteY     = Util.interpolate(segment.p1.screen.y,     segment.p2.screen.y,     car.percent);
				Render.instance.sprite(width, height, resolution, roadWidth, spriteName, spriteScale, spriteX, spriteY, -0.5f, -1, segment.clip);
			}	        

			for(SpriteWrapper sprite : segment.sprites) {

				float spriteScale = segment.p1.screenScale;
				float spriteX     = segment.p1.screen.x + (spriteScale * sprite.offset * roadWidth * width/2);
				float spriteY     = segment.p1.screen.y;
				Render.instance.sprite(width, height, resolution, roadWidth , sprite.sprite, spriteScale, spriteX, spriteY, (sprite.offset < 0 ? -1 : 0), -1, segment.clip);
			}

			if (segment == playerSegment) 
			{
				Render.instance.player(width, height, resolution, roadWidth, speed/maxSpeed,
						cameraDepth/playerZ,
						width/2,
						(height/2) - (cameraDepth/playerZ * Util.interpolate(playerSegment.p1.camera.y, playerSegment.p2.camera.y, playerPercent) * height/2),
						speed * (keyLeft ? -1 : keyRight ? 1 : 0),
						playerSegment.p2.world.y - playerSegment.p1.world.y);
			}

		}
		Render.instance.finishRenderSequence();

		switch (raceState)
		{
		case RACE_STATE_PRERACE:
			Render.instance.text(Integer.toString((int) preRaceCountdown), 0);
			preRaceCountdown -= dt;
			if (preRaceCountdown <= 0)
			{
				Render.instance.text("GO!", 2);
				raceState = RaceState.RACE_STATE_RACE;
			}
			break;
		case RACE_STATE_RACE:
			break;
		case RACE_STATE_GAMEOVER:
			Render.instance.text("GAME OVER!", 0);
			break;
		}

		Render.instance.ui(gameStats);

	}

	@Override
	public void render(float delta) {
		getInput();
		updateGameStats(delta);
		updateGameWorld(delta);
		draw(delta);
	}

	private void updateGameStats(float delta) 
	{
		if (raceState == RaceState.RACE_STATE_RACE)
		{
			gameStats.update(delta);
			if (gameStats.isTimeUp())
			{
				raceState = RaceState.RACE_STATE_TIMEUP;
			}
		}
	}

	private void getInput() 
	{
		if ( (raceState != RaceState.RACE_STATE_GAMEOVER) && (raceState != RaceState.RACE_STATE_PRERACE))
		{
			if (raceState != RaceState.RACE_STATE_TIMEUP)
			{
				keyFaster = Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP) ;
			}
			else
			{
				keyFaster = false;
			}
			keySlower = Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN) ;
			keyLeft   = Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT) ;
			keyRight  = Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT) ;
		}

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
		cameraDepth            = (float) (1 / Math.tan((fieldOfView/2) * Math.PI/180));
		playerZ                = (cameraHeight * cameraDepth);
		resolution             = height/480;

		if (segments == null || segments.size() ==0)
			resetRoad(); // only rebuild road when necessary
	}
}
