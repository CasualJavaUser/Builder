package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends InputAdapter implements Screen {

    private OrthographicCamera camera;
    private Viewport viewport;
    private InputManager input;

    private SpriteBatch batch;
    private Texture map;

    private float moveSpeed;

    private final float MAX_ZOOM = 1f, MIN_ZOOM = 0.1f,
                        NORMAL_SPEED = 250, FAST_SPEED = 450,
                        SCROLL_SPEED = 100;

    private final int CELL_SIZE = 16;

    private final List<Building> buildings = new ArrayList<>();

    private boolean isBuilding = false;

    GameScreen() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(screenWidth, screenHeight, camera);
        input = InputManager.getInstance();

        map = new Texture("grid.png");

        batch = new SpriteBatch();

        moveSpeed = NORMAL_SPEED;
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);
        batch.begin();

        moveCamera(deltaTime);
        draw(map, 0, 0);

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)) isBuilding = !isBuilding;

        for (Building building: buildings) {
            batch.draw(building.getTexture(), building.getX(), building.getY());
        }

        if(isBuilding) {
            build(Buildings.DEFAULT_BUILDING);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        viewport.setWorldSize(width, height);
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

    public void moveCamera(float deltaTime) {
        if(Gdx.input.isKeyPressed(InputManager.FAST)) moveSpeed = FAST_SPEED;
        else moveSpeed = NORMAL_SPEED;
        if(Gdx.input.isKeyPressed(InputManager.RIGHT)) camera.position.x += moveSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.LEFT)) camera.position.x -= moveSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.UP)) camera.position.y += moveSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.DOWN)) camera.position.y -= moveSpeed * deltaTime;

        //default camera position is (worldWidth / 2, worldHeight / 2)
        if(camera.position.x < (float)viewport.getScreenWidth() / 2 * camera.zoom) camera.position.x = (float)viewport.getScreenWidth()/2 * camera.zoom;
        if(camera.position.x > map.getWidth() - (float)viewport.getScreenWidth() / 2 * camera.zoom) camera.position.x = map.getWidth() - (float)viewport.getScreenWidth() / 2 * camera.zoom;
        if(camera.position.y < (float)viewport.getScreenHeight() / 2 * camera.zoom) camera.position.y = (float)viewport.getScreenHeight() / 2 * camera.zoom;
        if(camera.position.y > map.getHeight() - (float)viewport.getScreenHeight() / 2 * camera.zoom) camera.position.y = map.getHeight() - (float)viewport.getScreenHeight() / 2 * camera.zoom;

        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.zoom += amountY/ SCROLL_SPEED;
        if(camera.zoom > MAX_ZOOM) camera.zoom = MAX_ZOOM;
        else if(camera.zoom < MIN_ZOOM) camera.zoom = MIN_ZOOM;
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        return false;
    }

    private void draw(Texture texture, float x, float y) {
        batch.draw(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    private void build(Building building) {
        int mouseX, mouseY;
        mouseX = (int) ((Gdx.input.getX() - viewport.getScreenWidth()/2) * camera.zoom + camera.position.x);
        mouseY = (int) (((viewport.getScreenHeight() / 2) - Gdx.input.getY()) * camera.zoom + camera.position.y);

        draw(building.getTexture(), mouseX - (mouseX % CELL_SIZE), mouseY - (mouseY % CELL_SIZE));

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            building.setPosition(mouseX - (mouseX % CELL_SIZE), mouseY - (mouseY % CELL_SIZE));
            buildings.add(building);
            isBuilding = false;
        }
    }

    private Vector2 screenToWorld(float x, float y) {
        return new Vector2(0,0);
    }
}
