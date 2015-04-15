package org.whitesoft.games.outrun;

public class GameStats 
{
	int lapNumber = 0;
	float endgameTimer = 0;

	float lapTime;
	float fastestLapTime = -1;
	float lastLapTime = -1;
	private float warningThreshold = 10;
	int maxSpeed = 240;
	int currentSpeed;

	boolean initialized = false;
	private float percentFinished = 0;

	public void checkpoint(float increaseTime)
	{
		checkpoint(increaseTime, false);
	}
	
	public void checkpoint(float increaseTime, boolean autoDecrease)
	{
		endgameTimer += increaseTime - (autoDecrease ? 5*lapNumber : 0);
		if (lapNumber > 0)
		{
			lastLapTime = lapTime;
			if ( (fastestLapTime < 0) ||  (lastLapTime < fastestLapTime) )
				fastestLapTime = lastLapTime;
		}
		lapNumber++;
		lapTime = 0;
		initialized = true;
	}

	private String floatToString(float time)
	{
		if ( time >= 0)
		{
			int millies = (int) ((time - (int) time) * 1000);
			int seconds = ( (int) time ) % 60;
			int minutes = ( (int) time ) / 60;

			String s = String.format("%02d:%02d:%03d", minutes, seconds, millies);

			return s;
		}
		return "--:--:---";
	}

	public String getLapTime()
	{
		return floatToString(lapTime);
	}

	public String getFastestLapTime()
	{
		return floatToString(fastestLapTime);
	}

	public void update(float delta, float curSpeed, float percent)
	{
		endgameTimer -= delta;
		lapTime += delta;
		currentSpeed = (int) (curSpeed * maxSpeed);
		percentFinished = percent;
	}

	public boolean isTimeUp()
	{
		if (initialized)
			return endgameTimer <= 0;
		return false;
	}

	public boolean countdownWarning() 
	{
		if (lapNumber > 0)
		{
			if (endgameTimer < warningThreshold )
				return true;
		}
		return false;
	}

	public String getPercentFinished() 
	{
		return String.format("%03d%%", (int) (percentFinished * 100));
	}
}
