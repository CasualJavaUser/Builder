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

public class MenuScreen implements Screen {
    private final SpriteBatch batch;
    private static final OrthographicCamera camera = new OrthographicCamera();
    private static final Viewport viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);

    public MenuScreen(SpriteBatch batch) {
        this.batch = batch;
        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void show() {
        UI.setInGame(false);
        UI.showPauseMenu(true);
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BROWN);
        UI.handleUiInteraction();
        batch.begin();
        UI.drawMenu(batch, camera);
        batch.end();
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
