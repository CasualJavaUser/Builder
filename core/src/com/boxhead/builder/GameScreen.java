package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boxhead.builder.game_objects.Building;
import com.boxhead.builder.game_objects.Buildings;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Vector2i;

public class GameScreen extends InputAdapter implements Screen {

    private final BuilderGame game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Matrix4 uiProjection;

    private final float MAX_ZOOM = 1f, MIN_ZOOM = 0.1f,
            NORMAL_SPEED = 250, FAST_SPEED = 450,
            SCROLL_SPEED = 50;


    GameScreen(BuilderGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        this.uiProjection = new Matrix4();

        Textures.initTextures();
        World.initWorld(new Vector2i(101, 101));
        UI.initUI();

        Timer.schedule(Logic.getTask(), 0, .005f);

        camera.position.set((float) World.getWidth() / 2, (float) World.getHeight() / 2, camera.position.z);
        camera.update();

        //World.generateMap();
        World.debug();
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);
        game.batch.begin();

        moveCamera(deltaTime);
        World.drawMap(game.batch);

        if (!UI.isUIClicked())
            UI.checkObjects();

        drawObjects();

        if (Buildings.isInBuildingMode())
            Buildings.handleBuildingMode(game.batch);

        UI.updateUI();
        drawUI();

        game.batch.end();
    }

    public void moveCamera(float deltaTime) {
        final float minCameraX = (float) viewport.getScreenWidth() / 2 * camera.zoom;
        final float maxCameraX = World.getWidth() - (float) viewport.getScreenWidth() / 2 * camera.zoom;

        final float minCameraY = (float) viewport.getScreenHeight() / 2 * camera.zoom;
        final float maxCameraY = World.getHeight() - (float) viewport.getScreenHeight() / 2 * camera.zoom;

        final float deltaPosition = Gdx.input.isKeyPressed(InputManager.FAST)
                ? FAST_SPEED * deltaTime
                : NORMAL_SPEED * deltaTime;

        if (Gdx.input.isKeyPressed(InputManager.RIGHT))
            camera.position.x += deltaPosition;
        if (Gdx.input.isKeyPressed(InputManager.LEFT))
            camera.position.x -= deltaPosition;
        if (Gdx.input.isKeyPressed(InputManager.UP))
            camera.position.y += deltaPosition;
        if (Gdx.input.isKeyPressed(InputManager.DOWN))
            camera.position.y -= deltaPosition;

        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            camera.position.x -= Gdx.input.getDeltaX() * camera.zoom;
            camera.position.y += Gdx.input.getDeltaY() * camera.zoom;
        }

        camera.position.x = inBoundaries(camera.position.x, minCameraX, maxCameraX);
        camera.position.y = inBoundaries(camera.position.y, minCameraY, maxCameraY);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Vector3 getMousePosition() {
        return camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    }

    @Override
    public void resize(int width, int height) {
        viewport.setWorldSize(width, height);
        viewport.update(width, height, false);
        game.batch.setProjectionMatrix(camera.combined);
        UI.resizeUI();
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        Vector3 mousePositionBefore = getMousePosition();
        camera.zoom = inBoundaries(camera.zoom + amountY / SCROLL_SPEED, MIN_ZOOM, MAX_ZOOM);
        camera.update();

        Vector3 mousePositionAfter = getMousePosition();
        final float cameraX = camera.position.x + (mousePositionBefore.x - mousePositionAfter.x);
        final float cameraY = camera.position.y + (mousePositionBefore.y - mousePositionAfter.y);
        camera.position.set(cameraX, cameraY, 0);
        //zoom to cursor

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        return false;
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

    private void drawObjects() {
        for (NPC npc : World.getNpcs()) {
            if (!npc.isInBuilding()) {
                float x = npc.getSpritePosition().x * World.TILE_SIZE;
                float y = npc.getSpritePosition().y * World.TILE_SIZE;
                game.batch.draw(npc.getTexture(), x, y);
            }
        }

        for (Building building : World.getBuildings()) {
            building.draw(game.batch);
        }
    }

    private void drawUI() {
        uiProjection.setToScaling(camera.combined.getScaleX() * camera.zoom, camera.combined.getScaleY() * camera.zoom, 0);
        uiProjection.setTranslation(-1, -1, 0);
        game.batch.setProjectionMatrix(uiProjection);
        UI.drawUI(game.batch);
        game.batch.setProjectionMatrix(camera.combined);
    }

    // todo: move to utility class
    private static float inBoundaries(float value, float min, float max) {
        float result = Math.min(value, max);
        result = Math.max(result, min);
        return result;
    }
}
