package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.popup.Popups;

import java.util.concurrent.ExecutionException;

public class LoadingScreen implements Screen {
    private final SpriteBatch batch;
    private static final OrthographicCamera camera = new OrthographicCamera();
    private static final Viewport viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
    private static Screen nextScreen;

    public LoadingScreen(SpriteBatch batch) {
        this.batch = batch;
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        nextScreen = BuilderGame.getMenuScreen();
    }

    @Override
    public void show() {
        Popups.showPopup("");
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BROWN);
        batch.begin();
        UI.drawPopups(batch, camera);
        batch.end();
        if (BuilderGame.getLoadingException().isDone()) {
            try {
                Exception exception = BuilderGame.getLoadingException().get();
                if (exception == null) {
                    Popups.hidePopup();
                    BuilderGame.getInstance().setScreen(BuilderGame.getGameScreen());
                }
                else {
                    Popups.showPopup(exception);
                    BuilderGame.getInstance().setScreen(nextScreen);
                }
            } catch (ExecutionException | InterruptedException e) {
                Popups.showPopup(e);
                BuilderGame.getInstance().setScreen(nextScreen);
            }
        }
    }

    public static void setMessage(String message) {
        Popups.setText(message);
    }

    public static void setNextScreen(Screen nextScreen) {
        LoadingScreen.nextScreen = nextScreen;
    }

    @Override
    public void resize(int width, int height) {
        viewport.setWorldSize(width, height);
        viewport.update(width, height, false);
        batch.setProjectionMatrix(camera.combined);
        UI.resizeUI();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
