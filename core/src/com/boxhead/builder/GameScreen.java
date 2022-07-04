package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends InputAdapter implements Screen {

    private OrthographicCamera camera;
    private Viewport viewport;
    private InputManager input;

    private SpriteBatch batch;
    private SpriteBatch transparentBatch;
    private World world;

    private float moveSpeed;
    private boolean isBuilding = false;

    private float timeSinceStart = 0;
    private float previousTime = 0;

    private final float MAX_ZOOM = 1f, MIN_ZOOM = 0.1f,
                        NORMAL_SPEED = 250, FAST_SPEED = 450,
                        SCROLL_SPEED = 100;

    GameScreen() {
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(screenWidth, screenHeight, camera);
        input = InputManager.getInstance();

        Textures.initTextures();

        world = new World(new Vector2i(100, 130));

        //world.generateMap();
        world.debug();

        batch = new SpriteBatch();
        transparentBatch = new SpriteBatch();
        transparentBatch.setColor(1,1,1,.5f);

        moveSpeed = NORMAL_SPEED;
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);
        batch.begin();
        transparentBatch.begin();

        moveCamera(deltaTime);
        world.drawMap(batch);

        if(Gdx.input.isKeyJustPressed(Input.Keys.Q)) isBuilding = !isBuilding;

        for (Building building : world.getBuildings()) {
            batch.draw(building.getTexture(), building.getX(), building.getY());
        }

        if(isBuilding) {
            build(Buildings.get(Buildings.Types.DEFAULT_FUNCTIONAL_BUILDING));
        }

        batch.end();
        transparentBatch.end();
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
        transparentBatch.dispose();
    }

    public void moveCamera(float deltaTime) {
        if(Gdx.input.isKeyPressed(InputManager.FAST)) moveSpeed = FAST_SPEED;
        else moveSpeed = NORMAL_SPEED;
        if(Gdx.input.isKeyPressed(InputManager.RIGHT)) camera.position.x += moveSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.LEFT)) camera.position.x -= moveSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.UP)) camera.position.y += moveSpeed * deltaTime;
        if(Gdx.input.isKeyPressed(InputManager.DOWN)) camera.position.y -= moveSpeed * deltaTime;

        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            camera.position.x -= Gdx.input.getDeltaX() * camera.zoom;
            camera.position.y += Gdx.input.getDeltaY() * camera.zoom;
        }

        //default camera position is (worldWidth / 2, worldHeight / 2)
        if(camera.position.x < (float)viewport.getScreenWidth() / 2 * camera.zoom) camera.position.x = (float)viewport.getScreenWidth()/2 * camera.zoom;
        if(camera.position.x > world.getWidth() - (float)viewport.getScreenWidth() / 2 * camera.zoom) camera.position.x = world.getWidth() - (float)viewport.getScreenWidth() / 2 * camera.zoom;
        if(camera.position.y < (float)viewport.getScreenHeight() / 2 * camera.zoom) camera.position.y = (float)viewport.getScreenHeight() / 2 * camera.zoom;
        if(camera.position.y > world.getHeight() - (float)viewport.getScreenHeight() / 2 * camera.zoom) camera.position.y = world.getHeight() - (float)viewport.getScreenHeight() / 2 * camera.zoom;

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

    private void build(Building building) {
        TextureRegion texture = building.getTexture();

        Vector3 mousePos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        int mouseX = (int)mousePos.x + World.TILE_SIZE /(texture.getRegionWidth()/ World.TILE_SIZE)%2,
                mouseY = (int)mousePos.y + World.TILE_SIZE /(texture.getRegionHeight()/ World.TILE_SIZE)%2;
        int posX = mouseX - (mouseX % World.TILE_SIZE) - texture.getRegionWidth()/(texture.getRegionWidth()/ World.TILE_SIZE),
                posY = mouseY - (mouseY % World.TILE_SIZE) - texture.getRegionHeight()/(texture.getRegionHeight()/ World.TILE_SIZE);

        transparentBatch.draw(texture, posX, posY);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            building.setPosition(posX, posY);
            world.addBuilding(building);
            isBuilding = false;
        }
    }
}
