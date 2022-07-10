package com.boxhead.builder;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;

public class BuilderGame extends Game {

    GameScreen gameScreen;
    InputMultiplexer inputMultiplexer;

    @Override
    public void create() {
        inputMultiplexer = new InputMultiplexer();
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

    @Override
    public void resume() {
        super.resume();
        setScreen(gameScreen);
    }

    @Override
    public void pause() {
        super.pause();
    }
}
