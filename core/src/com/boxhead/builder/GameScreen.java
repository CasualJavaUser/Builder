package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boxhead.builder.game_objects.Buildings;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Vector2i;
import org.apache.commons.lang3.Range;

public class GameScreen extends InputAdapter implements Screen {

    public static final OrthographicCamera camera = new OrthographicCamera();
    private final SpriteBatch batch;
    private final Viewport viewport;
    private final Matrix4 uiProjection;

    private final Range<Float> ZOOM_RANGE = Range.between(0.1f, 1f);
    private final float NORMAL_SPEED = 250, FAST_SPEED = 450, SCROLL_SPEED = 50;

    GameScreen(SpriteBatch batch) {
        this.batch = batch;
        this.viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        this.uiProjection = new Matrix4();

        Textures.init();
        World.init(new Vector2i(101, 101));
        UI.init();
        Logic.init();

        camera.position.set((float) World.getWidth() / 2, (float) World.getHeight() / 2, camera.position.z);
        camera.update();
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(Color.BLACK);
        if(!Logic.isPaused()) moveCamera(deltaTime);

        batch.begin();
        World.drawMap(batch);
        World.drawObjects(batch);
        //World.showBuildableTiles(batch);

        if (!UI.handleClickableElementsInteractions()) {
            if (Buildings.isInBuildingMode()) {
                Buildings.handleBuildingMode(batch);
            } else if (Buildings.isDemolishing()) {
                Buildings.demolish();
            } else {
                World.handleNpcsAndBuildingsOnClick();
            }
        }

        if (InputManager.isKeyPressed(Input.Keys.ESCAPE)) UI.onEscape();

        drawUI();

        batch.end();
    }

    public static Vector3 getMouseWorldPosition() {
        return camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    }

    @Override
    public void resize(int width, int height) {
        viewport.setWorldSize(width, height);
        viewport.update(width, height, false);
        batch.setProjectionMatrix(camera.combined);
        UI.resizeUI();
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if(Logic.isPaused()) return false;
        Vector3 mousePositionBefore = getMouseWorldPosition();
        camera.zoom = ZOOM_RANGE.fit(camera.zoom + amountY / SCROLL_SPEED);
        camera.update();

        Vector3 mousePositionAfter = getMouseWorldPosition();
        final float cameraX = camera.position.x + (mousePositionBefore.x - mousePositionAfter.x);
        final float cameraY = camera.position.y + (mousePositionBefore.y - mousePositionAfter.y);
        camera.position.set(cameraX, cameraY, 0);
        //zoom to cursor

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        return true;
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

    private void moveCamera(float deltaTime) {
        final float deltaPosition = InputManager.isKeyDown(InputManager.FAST)
                ? FAST_SPEED * deltaTime
                : NORMAL_SPEED * deltaTime;

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
        UI.drawUI(batch);
        batch.setProjectionMatrix(camera.combined);
    }

    public Viewport getViewport() {
        return viewport;
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
}
