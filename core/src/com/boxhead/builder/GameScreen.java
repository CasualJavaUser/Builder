package com.boxhead.builder;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends InputAdapter implements Screen {

    private BuilderGame game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private float moveSpeed;
    private boolean isBuilding = false;


    private final float MAX_ZOOM = 1f, MIN_ZOOM = 0.1f,
            NORMAL_SPEED = 250, FAST_SPEED = 450,
            SCROLL_SPEED = 100;

    GameScreen(BuilderGame game) {
        this.game = game;
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(screenWidth, screenHeight, camera);

        Textures.initTextures();

        //World = new World(new Vector2i(100, 130));
        World.initWorld(new Vector2i(101, 101));
        Timer.schedule(Logic.getTask(), 0, 0.1f);

        camera.position.set((float)World.getWidth()/2, (float)World.getHeight()/2, camera.position.z);
        camera.update();

        //World.generateMap();
        World.debug();

        moveSpeed = NORMAL_SPEED;
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);
        game.batch.begin();

        moveCamera(deltaTime);
        World.drawMap(game.batch);

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) isBuilding = !isBuilding;

        for (NPC npc : World.getNpcs()) {
            if (!npc.isInBuilding()) {
                game.batch.draw(npc.getTexture(), npc.getPosition().x * World.TILE_SIZE, npc.getPosition().y * World.TILE_SIZE);
            }
        }

        for (Building building : World.getBuildings()) {
            game.batch.draw(building.getTexture(), building.getGridX() * World.TILE_SIZE, building.getGridY() * World.TILE_SIZE);
        }

        if (isBuilding) {
            build(Buildings.Types.DEFAULT_PRODUCTION_BUILDING);
        }

        drawUI();

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        viewport.setWorldSize(width, height);
        game.batch.setProjectionMatrix(camera.combined);
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

    }

    public void moveCamera(float deltaTime) {
        if (Gdx.input.isKeyPressed(InputManager.FAST)) moveSpeed = FAST_SPEED;
        else moveSpeed = NORMAL_SPEED;
        if (Gdx.input.isKeyPressed(InputManager.RIGHT)) camera.position.x += moveSpeed * deltaTime;
        if (Gdx.input.isKeyPressed(InputManager.LEFT)) camera.position.x -= moveSpeed * deltaTime;
        if (Gdx.input.isKeyPressed(InputManager.UP)) camera.position.y += moveSpeed * deltaTime;
        if (Gdx.input.isKeyPressed(InputManager.DOWN)) camera.position.y -= moveSpeed * deltaTime;

        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            camera.position.x -= Gdx.input.getDeltaX() * camera.zoom;
            camera.position.y += Gdx.input.getDeltaY() * camera.zoom;
        }

        if (camera.position.x < (float) viewport.getScreenWidth() / 2 * camera.zoom)
            camera.position.x = (float) viewport.getScreenWidth() / 2 * camera.zoom;
        if (camera.position.x > World.getWidth() - (float) viewport.getScreenWidth() / 2 * camera.zoom)
            camera.position.x = World.getWidth() - (float) viewport.getScreenWidth() / 2 * camera.zoom;
        if (camera.position.y < (float) viewport.getScreenHeight() / 2 * camera.zoom)
            camera.position.y = (float) viewport.getScreenHeight() / 2 * camera.zoom;
        if (camera.position.y > World.getHeight() - (float) viewport.getScreenHeight() / 2 * camera.zoom)
            camera.position.y = World.getHeight() - (float) viewport.getScreenHeight() / 2 * camera.zoom;

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.zoom += amountY / SCROLL_SPEED;
        if (camera.zoom > MAX_ZOOM) camera.zoom = MAX_ZOOM;
        else if (camera.zoom < MIN_ZOOM) camera.zoom = MIN_ZOOM;
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        return false;
    }

    private void build(Buildings.Types type) {
        TextureRegion texture = type.getTexture();
        Vector3 mousePos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        int mouseX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2,
                mouseY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        int posX = mouseX - (mouseX % World.TILE_SIZE),
                posY = mouseY - (mouseY % World.TILE_SIZE);

        game.batch.setColor(1, 1, 1, .5f);
        game.batch.draw(texture, posX, posY);
        game.batch.setColor(1, 1, 1, 1);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            isBuilding = !World.placeBuilding(type, new Vector2i(posX / World.TILE_SIZE, posY / World.TILE_SIZE));
        }
    }

    private void drawUI() {
        Vector3 vector = camera.unproject(new Vector3(10, viewport.getScreenHeight()-10, 0));
        Texture texture = new Texture("house_icon.png");
        game.batch.draw(texture, vector.x, vector.y, texture.getWidth()*camera.zoom, texture.getHeight()*camera.zoom);
    }
}
