package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boxhead.builder.game_objects.buildings.Buildings;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Range;
import com.boxhead.builder.utils.Vector2i;

public class GameScreen implements Screen {

    public static final OrthographicCamera camera = new OrthographicCamera();
    public static final Viewport viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
    private final SpriteBatch batch;
    private final ShapeRenderer renderer;
    private static final Vector2 mouseWorldPos = new Vector2(0, 0);

    private final Range<Float> ZOOM_RANGE = Range.between(0.1f, 1f);
    private static final float NORMAL_SPEED = 350, FAST_SPEED = 650, SCROLL_SPEED = 30;

    GameScreen(SpriteBatch batch) {
        this.batch = batch;
        renderer = new ShapeRenderer();
        renderer.setAutoShapeType(true);

        Tile.init();
        World.generate(60, new Vector2i(101, 101));
        World.temp();
        UI.init();
        Logic.init();
        Debug.init();
        Statistics.init();

        centerCamera();
    }

    @Override
    public void render(float deltaTime) {
        updateMouseWorldPosition();

        ScreenUtils.clear(Color.BLACK);
        if (!UI.isPaused() && !UI.isConsoleOpen()) {
            scroll();
            moveCamera(deltaTime);
        }

        batch.begin();
        UI.DEFAULT_COLOR.set(World.getAmbientColor(World.getTime()));
        if (UI.isPaused()) {
            UI.DEFAULT_COLOR.set(new Color(UI.DEFAULT_COLOR.r * UI.DARK.r, UI.DEFAULT_COLOR.g * UI.DARK.g, UI.DEFAULT_COLOR.b * UI.DARK.b, UI.DARK.a));
        }
        batch.setColor(UI.DEFAULT_COLOR);
        World.drawMap(batch);
        World.drawObjects(batch);
        //World.showBuildableTiles(batch);
        //World.showNavigableTiles(batch);
        //World.pathfindingTest(batch);

        if (!UI.handleUiInteractions()) {
            if (Buildings.isInBuildingMode()) Buildings.handleBuildingMode(batch);
            else if (Buildings.isInDemolishingMode()) Buildings.handleDemolishingMode(batch);
            else if (Tiles.getCurrentMode() != null) Tiles.handleCurrentMode(batch);
            else {
                World.handleObjectInteractions();
            }
        }
        /*if (!UI.handleUiInteraction() && !UI.isPaused()) {
            if (Buildings.isInBuildingMode()) Buildings.handleBuildingMode(batch);
            else if (Buildings.isInDemolishingMode()) Buildings.handleDemolishingMode();
            else if (Tiles.getCurrentMode() == Tiles.Mode.PATH) Tiles.handlePathMode(batch);
            else if (Tiles.getCurrentMode() == Tiles.Mode.BRIDGE) Tiles.handleBridgeMode(batch);
            else if (Tiles.getCurrentMode() == Tiles.Mode.REMOVE_PATH) Tiles.handleRemovingMode(batch);
            else {
                World.handleObjectInteractions();
            }
        }*/

        handleInput();

        if (InputManager.isListeningForKey()) {
            InputManager.listenForKey();
        }

        //UI.drawUI(batch, camera);
        UI.drawUI(batch, camera);

        batch.end();

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setProjectionMatrix(UI.UI_PROJECTION);
        UI.drawGraph(renderer);
        renderer.end();

        if (InputManager.isKeyPressed(Input.Keys.GRAVE)) {
            if (!UI.isConsoleOpen())
                UI.openConsole();
            else
                UI.closeConsole();
        }

        InputManager.resetKeysAndScroll();
    }

    private static void handleInput() {
        if (InputManager.isKeyPressed(Input.Keys.ESCAPE)) {
            UI.onEscape();
        }
        else if (!UI.isPaused() && !UI.isConsoleOpen()) {
            if (InputManager.isKeyPressed(InputManager.KeyBinding.PAUSE) && !UI.isConsoleOpen())
                Logic.pause(!Logic.isPaused());
            else if (InputManager.isKeyPressed(InputManager.KeyBinding.TICK_SPEED_1))
                Logic.setTickSpeed(Logic.NORMAL_SPEED);
            else if (InputManager.isKeyPressed(InputManager.KeyBinding.TICK_SPEED_2))
                Logic.setTickSpeed(Logic.SPEED_X2);
            else if (InputManager.isKeyPressed(InputManager.KeyBinding.TICK_SPEED_3))
                Logic.setTickSpeed(Logic.SPEED_X3);
            else if (InputManager.isKeyPressed(InputManager.KeyBinding.OPEN_BUILD_MENU))
                UI.openCloseBuildMenu();
        }
    }

    public static Vector2 getMouseWorldPosition() {
        return new Vector2(mouseWorldPos);
    }

    public static Vector2i getMouseGridPosition() {
        int gridX = ((int) mouseWorldPos.x) / World.TILE_SIZE;  //explicit cast and division instead of Vector2i functions
        int gridY = ((int) mouseWorldPos.y) / World.TILE_SIZE;  //to make sure JVM performs a shift-right division
        return new Vector2i(gridX, gridY);
    }

    private static void updateMouseWorldPosition() {
        mouseWorldPos.set(viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY())));
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
        UI.resizeUI(width, height);
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
        UI.showUI(UI.Screen.IN_GAME);
    }

    @Override
    public void dispose() {
    }

    private void moveCamera(float deltaTime) {
        final float deltaPosition = InputManager.isKeyDown(InputManager.KeyBinding.SHIFT)
                ? FAST_SPEED * deltaTime * camera.zoom
                : NORMAL_SPEED * deltaTime * camera.zoom;

        if (InputManager.isKeyDown(InputManager.KeyBinding.MOVE_RIGHT))
            camera.position.x += deltaPosition;
        if (InputManager.isKeyDown(InputManager.KeyBinding.MOVE_LEFT))
            camera.position.x -= deltaPosition;
        if (InputManager.isKeyDown(InputManager.KeyBinding.MOVE_UP))
            camera.position.y += deltaPosition;
        if (InputManager.isKeyDown(InputManager.KeyBinding.MOVE_DOWN))
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

    public static void centerCamera() {
        camera.position.set((float) World.getWidth() / 2, (float) World.getHeight() / 2, camera.position.z);
        camera.update();
    }
}
