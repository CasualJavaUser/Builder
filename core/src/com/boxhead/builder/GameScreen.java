package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends InputAdapter implements Screen {

    private OrthographicCamera camera;
    private Viewport viewport;
    private InputManager input;

    private SpriteBatch batch;
    private Texture map;

    private float mapX, mapY;
    private float scrollSpeed = 100;

    GameScreen() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(screenWidth, screenHeight, camera);
        input = InputManager.getInstance();

        map = new Texture("map.png");
        mapX = (float)(screenWidth/2 - map.getWidth() / 2);
        mapY = (float)(screenHeight/2 - map.getHeight() / 2);

        batch = new SpriteBatch();
    }

    @Override
    public void render(float deltaTime) {
        batch.begin();

        drawMap(deltaTime);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);
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
    public void show() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        map.dispose();
    }

    private void drawMap(float deltaTime) {
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) mapX = mapX - scrollSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) mapX = mapX + scrollSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(Input.Keys.UP)) mapY = mapY - scrollSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) mapY = mapY + scrollSpeed * deltaTime;

        batch.draw(map, mapX, mapY, map.getWidth(), map.getHeight());
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.zoom += amountY/scrollSpeed;
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        System.out.println("camera zoom " + camera.zoom);
        return false;
    }
}
