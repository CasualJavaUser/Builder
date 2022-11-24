package com.boxhead.builder;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.game_objects.NPC;

public class BuilderGame extends Game {

    private SpriteBatch batch;
    private static GameScreen gameScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        gameScreen = new GameScreen(batch);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(InputManager.getInstance());
        Gdx.input.setInputProcessor(inputMultiplexer);

        setScreen(gameScreen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        NPC.executor.shutdown();
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

    public static GameScreen getGameScreen() {
        return gameScreen;
    }
}
