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
	}
	
	public void radioEnable(boolean on)
	{
		if (on)
		{			
			if (!radioFiles.isEmpty())
			{
				music = Gdx.audio.newMusic(Gdx.files.internal("music/" + radioFiles.get(radioIndex) ));
				music.setVolume(0.4f);
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
	}
	
	public void enginePitch(float pct)
	{
		engineSound.setPitch(engineSoundPlayerId, 1 + pct*4);
	}
}
