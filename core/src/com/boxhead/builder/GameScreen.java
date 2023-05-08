package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boxhead.builder.game_objects.Buildings;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Vector2i;
import org.apache.commons.lang3.Range;

public class GameScreen implements Screen {

    public static final OrthographicCamera camera = new OrthographicCamera();
    public static final Viewport viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
    private final SpriteBatch batch;
    private final Matrix4 uiProjection;

    private final Range<Float> ZOOM_RANGE = Range.between(0.1f, 1f);
    private static final float NORMAL_SPEED = 350, FAST_SPEED = 650, SCROLL_SPEED = 30;

    GameScreen(SpriteBatch batch) {
        this.batch = batch;
        this.uiProjection = new Matrix4();

        Textures.init();
        World.generate(60, new Vector2i(101, 101));
        World.temp();
        UI.init();
        UI.getResourceList().initData();
        Logic.init();
        Debug.init();

        camera.position.set((float) World.getWidth() / 2, (float) World.getHeight() / 2, camera.position.z);
        camera.update();
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);
        if (!UI.isPaused() && !Debug.isOpen()) {
            scroll();
            moveCamera(deltaTime);
        }

        batch.begin();
        World.drawMap(batch);
        World.drawObjects(batch);
        //World.showBuildableTiles(batch);
        //World.pathfindingTest(batch);

        if (!UI.handleUiInteraction() && !UI.isPaused()) {
            if (Buildings.isInBuildingMode()) Buildings.handleBuildingMode(batch);
            else if (Buildings.isInDemolishingMode()) Buildings.handleDemolishingMode();
            else if (Tiles.isInTilingMode()) Tiles.handleTilingMode(batch);
            else {
                World.handleNpcsAndBuildingsOnClick();
            }
        }

        if (InputManager.isKeyPressed(Input.Keys.ESCAPE)) UI.onEscape();
        if (InputManager.isKeyPressed(Input.Keys.SPACE) && !UI.isPaused() && !Debug.isOpen()) Logic.pause(!Logic.isPaused());

        UI.drawUI(batch, camera);

        batch.end();

        if (InputManager.isKeyPressed(Input.Keys.GRAVE)) {
            if (!Debug.isOpen())
                Debug.openConsole();
            else
                Debug.quit();
        }

        if (Debug.isOpen())
            Debug.handleInput();

        InputManager.resetScroll();
    }

    public static Vector2 getMouseWorldPosition() {
        return viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
    }

    public static Vector2 worldToScreenPosition(Vector2i position) {
        return viewport.project(new Vector2(position.x, position.y));
    }

    public static Vector2 worldToScreenPosition(float x, float y) {
        return viewport.project(new Vector2(x, y));
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
    public void show() {
        UI.adjustMenuForInGameUse(true);
    }

    @Override
    public void dispose() {
    }

    private void moveCamera(float deltaTime) {
        final float deltaPosition = InputManager.isKeyDown(InputManager.SHIFT)
                ? FAST_SPEED * deltaTime * camera.zoom
                : NORMAL_SPEED * deltaTime * camera.zoom;

        if (InputManager.isKeyDown(InputManager.RIGHT))
            camera.position.x += deltaPosition;
        if (InputManager.isKeyDown(InputManager.LEFT))
            camera.position.x -= deltaPosition;
        if (InputManager.isKeyDown(InputManager.UP))
            camera.position.y += deltaPosition;
        if (InputManager.isKeyDown(InputManager.DOWN))
            camera.position.y -= deltaPosition;

        if (InputManager.isButtonDown(InputManager.RIGHT_MOUSE)) {
            camera.position.x -= Gdx.input.getDeltaX() * camera.zoom;
            camera.position.y += Gdx.input.getDeltaY() * camera.zoom;
        }

        camera.position.x = getCameraXRange().fit(camera.position.x);
        camera.position.y = getCameraYRange().fit(camera.position.y);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }

    private void drawUI() {
        uiProjection.setToScaling(camera.combined.getScaleX() * camera.zoom, camera.combined.getScaleY() * camera.zoom, 0);
        uiProjection.setTranslation(-1, -1, 0);
        batch.setProjectionMatrix(uiProjection);
        UI.drawUI(batch, camera);
        batch.setProjectionMatrix(camera.combined);
    }

    private Range<Float> getCameraYRange() {
        final float minCameraY = (float) viewport.getScreenHeight() / 2 * camera.zoom;
        final float maxCameraY = World.getHeight() - (float) viewport.getScreenHeight() / 2 * camera.zoom;
        return Range.between(minCameraY, maxCameraY);
    }

    private Range<Float> getCameraXRange() {
        final float minCameraX = (float) viewport.getScreenWidth() / 2 * camera.zoom;
        final float maxCameraX = World.getWidth() - (float) viewport.getScreenWidth() / 2 * camera.zoom;
        return Range.between(minCameraX, maxCameraX);
    }

    public void scroll() {
        if (!InputManager.isScrolled()) return;
        Vector2 mousePositionBefore = getMouseWorldPosition();
        camera.zoom = ZOOM_RANGE.fit(camera.zoom + InputManager.getScroll() / SCROLL_SPEED);
        camera.update();

        Vector2 mousePositionAfter = getMouseWorldPosition();
        final float cameraX = camera.position.x + (mousePositionBefore.x - mousePositionAfter.x);
        final float cameraY = camera.position.y + (mousePositionBefore.y - mousePositionAfter.y);
        camera.position.set(cameraX, cameraY, 0);
        //zoom to cursor

        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }
}
