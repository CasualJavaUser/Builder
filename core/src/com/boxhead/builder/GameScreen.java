package com.boxhead.builder;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Vector2i;

public class GameScreen extends InputAdapter implements Screen {

    private BuilderGame game;
    private static OrthographicCamera camera;
    private Viewport viewport;

    private float moveSpeed;
    private Vector3 mouseScreenPos = new Vector3();

    private boolean isBuilding = false;
    private Buildings.Types currentBuilding = null;

    private final float MAX_ZOOM = 1f, MIN_ZOOM = 0.1f,
            NORMAL_SPEED = 250, FAST_SPEED = 450,
            SCROLL_SPEED = 50;

    private final Matrix4 UIProjection;

    GameScreen(BuilderGame game) {
        this.game = game;
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(screenWidth, screenHeight, camera);

        Textures.initTextures();
        World.initWorld(new Vector2i(101, 101));
        UI.initUI();

        Timer.schedule(Logic.getTask(), 0, .005f);

        camera.position.set((float)World.getWidth()/2, (float)World.getHeight()/2, camera.position.z);
        camera.update();

        //World.generateMap();
        World.debug();

        moveSpeed = NORMAL_SPEED;
        UIProjection = new Matrix4();
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);
        mouseScreenPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.batch.begin();


        moveCamera(deltaTime);
        World.drawMap(game.batch);

        drawObjects();

        if(!UI.isUIClicked()) UI.checkObjects();
        if (isBuilding) {
            placeBuilding(currentBuilding);
            if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) isBuilding = false;
        }

        UI.updateUI();
        drawUI();

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.setWorldSize(width, height);
        viewport.update(width, height, false);
        game.batch.setProjectionMatrix(camera.combined);
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

        Vector3 pos1 = camera.unproject(new Vector3(mouseScreenPos));

        camera.zoom += amountY / SCROLL_SPEED;
        if (camera.zoom > MAX_ZOOM) camera.zoom = MAX_ZOOM;
        else if (camera.zoom < MIN_ZOOM) camera.zoom = MIN_ZOOM;

        camera.update();

        Vector3 pos2 = camera.unproject(new Vector3(mouseScreenPos));
        camera.position.set(camera.position.x + (pos1.x - pos2.x), camera.position.y + (pos1.y - pos2.y), 0);
        //zoom to cursor

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        return false;
    }

    public void build(Buildings.Types type) {
        currentBuilding = type;
        isBuilding = true;
    }

    private void placeBuilding(Buildings.Types type) {
        TextureRegion texture = type.getTexture();
        Vector3 mousePos = camera.unproject(mouseScreenPos);

        int mouseX = (int) mousePos.x - (texture.getRegionWidth() - World.TILE_SIZE) / 2,
                mouseY = (int) mousePos.y - (texture.getRegionHeight() - World.TILE_SIZE) / 2;

        int posX = mouseX - (mouseX % World.TILE_SIZE),
                posY = mouseY - (mouseY % World.TILE_SIZE);

        game.batch.setColor(UI.SEMI_TRANSPARENT);
        game.batch.draw(texture, posX, posY);
        game.batch.setColor(UI.DEFAULT_COLOR);

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            isBuilding = !World.placeBuilding(type, new Vector2i(posX / World.TILE_SIZE, posY / World.TILE_SIZE));
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) isBuilding = true;
        }
    }

    private void drawObjects() {
        for (NPC npc : World.getNpcs()) {
            if (!npc.isInBuilding()) {
                game.batch.draw(npc.getTexture(), npc.getSpritePosition().x * World.TILE_SIZE, npc.getSpritePosition().y * World.TILE_SIZE);
            }
        }

        for (Building building : World.getBuildings()) {
            building.draw(game.batch);
        }
    }

    private void drawUI() {
        UIProjection.setToScaling(camera.combined.getScaleX()*camera.zoom, camera.combined.getScaleY()*camera.zoom, 0);
        UIProjection.setTranslation(-1,-1,0);
        game.batch.setProjectionMatrix(UIProjection);
        UI.drawUI(game.batch);
        game.batch.setProjectionMatrix(camera.combined);
    }

    public static OrthographicCamera getCamera() {
        return camera;
    }
}
