package org.whitesoft.games.outrun;

import com.badlogic.gdx.Game;

public class GdxOutrunGame extends Game {

    MainGameScreen mainGameScreen;

	@Override
    public void create() {
		mainGameScreen = new MainGameScreen(this);
        setScreen(mainGameScreen);              
    }	
}

