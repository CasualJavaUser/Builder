package com.boxhead.builder;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BuilderGame extends Game {

    private GameScreen gameScreen;
    private InputMultiplexer inputMultiplexer;
    public SpriteBatch batch;

    @Override
    public void create() {
        inputMultiplexer = new InputMultiplexer();
        batch = new SpriteBatch();
        gameScreen = new GameScreen(this);

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
        batch.dispose();
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
