package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
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

    private float scrollSpeed;

    private final float MAX_ZOOM = 1f, MIN_ZOOM = 0.1f, TEXTURE_SIZE = 3f;
    private final float NORMAL_SCROLL = 250, FAST_SCROLL = 450;

    GameScreen() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(screenWidth, screenHeight, camera);
        input = InputManager.getInstance();

        map = new Texture("map.png");

        batch = new SpriteBatch();

        scrollSpeed = NORMAL_SCROLL;
    }

    @Override
    public void render(float deltaTime) {
        batch.begin();

        moveCamera(deltaTime);
        draw(map, 0, 0);

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

    /*private void moveMap(float deltaTime) {
        if(Gdx.input.isKeyPressed(InputManager.FAST)) scrollSpeed = FAST_SCROLL;
        else scrollSpeed = NORMAL_SCROLL;
        if(Gdx.input.isKeyPressed(InputManager.RIGHT)) mapX = mapX - scrollSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.LEFT)) mapX = mapX + scrollSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.UP)) mapY = mapY - scrollSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.DOWN)) mapY = mapY + scrollSpeed * deltaTime;

        System.out.println(mapX);
        if(mapX > 0) mapX = 0;
        else if (mapX < -viewport.getWorldWidth()) mapX = -viewport.getWorldWidth();
    }*/

    public void moveCamera(float deltaTime) {
        if(Gdx.input.isKeyPressed(InputManager.FAST)) scrollSpeed = FAST_SCROLL;
        else scrollSpeed = NORMAL_SCROLL;
        if(Gdx.input.isKeyPressed(InputManager.RIGHT)) camera.position.x += scrollSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.LEFT)) camera.position.x -= scrollSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.UP)) camera.position.y += scrollSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.DOWN)) camera.position.y -= scrollSpeed * deltaTime;

        //default camera position is (worldWidth / 2, worldHeight / 2)
        if(camera.position.x < viewport.getWorldWidth() / 2 * camera.zoom) camera.position.x = viewport.getWorldWidth()/2 * camera.zoom;
        if(camera.position.x > map.getWidth()*TEXTURE_SIZE - viewport.getWorldWidth() / 2 * camera.zoom) camera.position.x = map.getWidth()*TEXTURE_SIZE - viewport.getWorldWidth() / 2 * camera.zoom;
        if(camera.position.y < viewport.getWorldHeight() / 2 * camera.zoom) camera.position.y = viewport.getWorldHeight() / 2 * camera.zoom;
        if(camera.position.y > map.getHeight()*TEXTURE_SIZE - viewport.getWorldHeight() / 2 * camera.zoom) camera.position.y = map.getHeight()*TEXTURE_SIZE - viewport.getWorldHeight() / 2 * camera.zoom;

        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.zoom += amountY/scrollSpeed;
        if(camera.zoom > MAX_ZOOM) camera.zoom = MAX_ZOOM;
        else if(camera.zoom < MIN_ZOOM) camera.zoom = MIN_ZOOM;
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        return false;
    }

    private void draw(Texture texture, float x, float y) {
        batch.draw(texture, x, y, texture.getWidth() * TEXTURE_SIZE, texture.getHeight() * TEXTURE_SIZE);
    }
}
