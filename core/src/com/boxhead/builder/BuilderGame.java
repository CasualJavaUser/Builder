package com.boxhead.builder;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;

public class BuilderGame extends Game {

    GameScreen gameScreen;
    InputMultiplexer inputMultiplexer = new InputMultiplexer();

    @Override
    public void create() {
        gameScreen = new GameScreen();
        inputMultiplexer.addProcessor(gameScreen);
        Gdx.input.setInputProcessor(inputMultiplexer);
        setScreen(gameScreen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        gameScreen.dispose();
    }

    @Override
    public void resize(int width, int height) {
        gameScreen.resize(width, height);
    }


}
