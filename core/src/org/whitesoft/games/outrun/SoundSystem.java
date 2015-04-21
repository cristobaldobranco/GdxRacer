package org.whitesoft.games.outrun;

import java.util.Vector;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public class SoundSystem {
	Music music = null;
	Vector<String> radioFiles = new Vector<String>();
	int radioIndex = 0;
	
	Sound engineSound;
	long engineSoundPlayerId; 

	Sound tireSquealSound;
	long tireSquealId;
	float tireSquealVolumeTarget  = 0;
	float tireSquealVolumeCurrent = 0;

	public SoundSystem()
	{
		FileHandle dirHandle;
		if (Gdx.app.getType() == ApplicationType.Android) {
		  dirHandle = Gdx.files.internal("music");
		} else {
		  // ApplicationType.Desktop ..
		  dirHandle = Gdx.files.internal("./bin/music");
		}
		FileHandle[] files = dirHandle.list();
		for(FileHandle file: files) 
		{
			if (file.extension().equals("mp3"))
			{
				System.out.println(file.name());
				radioFiles.add(file.name());
			}
		}
		
		engineSound = Gdx.audio.newSound(Gdx.files.internal("sound/engine.wav"));	
		tireSquealSound = Gdx.audio.newSound(Gdx.files.internal("sound/tires_squal_loop.wav"));
		tireSquealId = tireSquealSound.loop(0);
	}
	
	public void updateRadio(float dt)
	{
		if (!radioFiles.isEmpty())
		{
			if (music != null && !music.isPlaying())
			{
				radioSkip();
			}
		}
		float step = 0.03f;
		if (Float.compare(tireSquealVolumeCurrent, tireSquealVolumeTarget) != 0)
		{
			float dv = -tireSquealVolumeCurrent + tireSquealVolumeTarget;
			dv = Util.limit(dv, -step, step);
			tireSquealVolumeCurrent += dv;
		}
		tireSquealSound.setVolume(tireSquealId, Util.limit(tireSquealVolumeCurrent, 0, 1));
	}
	
	public void radioEnable(boolean on)
	{
		if (on)
		{			
			if (!radioFiles.isEmpty())
			{
				music = Gdx.audio.newMusic(Gdx.files.internal("music/" + radioFiles.get(radioIndex) ));
				music.setVolume(0.2f);
				music.play();
			}
		}
		else
		{
			if (music != null)
			{
				music.stop();
				music.dispose();
			}
		}
	}
	
	public void radioSkip()
	{
		if (!radioFiles.isEmpty())
		{
			if (music != null)
			{
				music.stop();
				music.dispose();
				radioIndex = (radioIndex + 1) % radioFiles.size();
			}
			radioEnable(true);
		}
	}

	public void dispose() {
		if (music != null)
		{
			music.stop();
			music.dispose();
		}		
	}
	
	public void engineStart()
	{
		engineSoundPlayerId = engineSound.loop();
		engineSound.setVolume(engineSoundPlayerId, 0.3f);
	}
	
	public long carSound(long soundId, float distance, float offset, float modifier)
	{
		long ret = soundId;
		if (soundId < 0)
		{
			ret = engineSound.loop(distance, distance*modifier, offset);
		}
		else
		{
			engineSound.setPan(soundId, offset, distance +0.2f);
			engineSound.setPitch(soundId, distance*modifier);
		}
		return ret;
	}
	
	public void stopCarSound(long soundId) {
		engineSound.stop(soundId);
	}

	
	//	float [] gearRatio = {25f/240f, 50f/240f, 80f/240f, 120f/240f, 170f/240f, 250f/240f};
	float [] gearRatio = {80f/240f, 170f/240f, 250f/240f};
	int gear = 1;
	
	public void enginePitch(float pct)
	{
		int i;
		
		for (i = 0; i < gearRatio.length; i++)
		{
			if (gearRatio[i] > pct)
				break;
		}
		float gearMinValue, gearMaxValue;
		if (i == 0)
		{
			gearMinValue = 0;
			gearMaxValue = gearRatio[i];
		}
		else
		{
			gearMinValue = gearRatio[i-1];
			gearMaxValue = gearRatio[i];
		}
		
		engineSound.setPitch(engineSoundPlayerId, 3*((pct - gearMinValue) / (gearMaxValue - gearMinValue))+2);
//		System.out.println(3*((pct - gearMinValue) / (gearMaxValue - gearMinValue))+1);
	}
	
	public void tireSqueal(float force)
	{
		tireSquealVolumeTarget = force;		
	}

}
