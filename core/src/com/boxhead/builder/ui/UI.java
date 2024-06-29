package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.boxhead.builder.InputManager;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.game_objects.buildings.Building;
import com.boxhead.builder.ui.compound.*;
import com.boxhead.builder.utils.Action;
import com.boxhead.builder.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class UI {
    public static final Matrix4 UI_PROJECTION = new Matrix4();

    public static final Color DEFAULT_COLOR = new Color(1, 1, 1, 1);
    public static final Color DEFAULT_UI_COLOR = new Color(1, 1, 1, 1);
    public static final Color SEMI_TRANSPARENT = new Color(1, 1, 1, .5f);
    public static final Color SEMI_TRANSPARENT_RED = new Color(.86f, .25f, .25f, .4f);
    public static final Color SEMI_TRANSPARENT_GREEN = new Color(.25f, .86f, .25f, .4f);
    public static final Color SEMI_TRANSPARENT_BLUE = new Color(.25f, .25f, .86f, .4f);
    public static final Color SEMI_TRANSPARENT_YELLOW = new Color(.86f, .86f, .25f, .4f);
    public static final Color SEMI_TRANSPARENT_MAGENTA = new Color(.86f, .25f, .86f, .4f);
    public static final Color PRESSED_COLOR = new Color(.8f, .8f, .8f, 1);
    public static final Color VERY_TRANSPARENT = new Color(1, 1, 1, .2f);
    public static final Color WHITE = new Color(1, 1, 1, 1);
    public static final Color DARK = new Color(.5f, .5f, .5f, 1);

    public static BitmapFont FONT;
    public static final float FONT_WIDTH = 10.5f;
    public static final int FONT_HEIGHT = 20;
    public static final int PADDING = 10;

    private static TextField activeTextField = null;
    private static ScrollPane activeScrollPane = null;
    private static Button activeButton = null;
    private static UIComponent clickedComponent = null;
    private static Screen activeScreen = Screen.IN_GAME;

    private static MainMenuWindow mainMenuWindow;
    private static PauseWindow pauseWindow;
    private static SettingsWindow settingsWindow;
    private static SaveWindow saveWindow;
    private static LoadWindow loadWindow;
    private static BuildWindow buildWindow;
    private static ShiftWindow shiftWindow;
    private static StatisticsWindow statisticsWindow;
    private static Console console;
    private static MainButtonsPane mainButtonsPane;
    private static ClockPane clockPane;
    private static ResourceList resourceList;
    private static Pane pauseButtonPane;
    private static TextArea tipText;
    private static InfoWindow infoWindow;
    private static FarmResourceWindow farmResourceWindow;

    private static final Stack<Pair<Action, BooleanSupplier>> onEscapeStack = new Stack<>();

    private enum Anchor {
        TOP_LEFT(c -> c.setPosition(0, Gdx.graphics.getHeight() - c.getHeight())),
        TOP_RIGHT(c -> c.setPosition(Gdx.graphics.getWidth() - c.getWidth(), Gdx.graphics.getHeight() - c.getHeight())),
        BOTTOM_LEFT(c -> c.setPosition(0, 0)),
        BOTTOM_RIGHT(c -> c.setPosition(Gdx.graphics.getWidth() - c.getWidth(), 0)),
        CENTER(c -> c.setPosition(Gdx.graphics.getWidth()/2 - c.getWidth()/2, Gdx.graphics.getHeight()/2 - c.getHeight()/2)),
        BOTTOM(c -> c.setPosition(Gdx.graphics.getWidth()/2 - c.getWidth()/2, 0));

        final Consumer<UIComponent> updatePosition;

        Anchor(Consumer<UIComponent> updatePosition) {
            this.updatePosition = updatePosition;
        }
    }

    public enum Screen {
        IN_GAME,
        MAIN_MENU;

        final List<Pair<UIComponent, Anchor>> components;

        Screen() {
            components = new ArrayList<>();
        }

        void addUIComponent(UIComponent component) {
            addUIComponent(component, null);
        }

        void addUIComponent(UIComponent component, Anchor anchor) {
            components.add(Pair.of(component, anchor));
            if (anchor != null) anchor.updatePosition.accept(component);

            if (component instanceof Window window)
                window.pack();
            else if (component instanceof Pane pane)
                pane.pack();
        }
    }

    public static void init() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 30;
        parameter.spaceY = -6;
        FONT = generator.generateFont(parameter);
        generator.dispose();

        farmResourceWindow = new FarmResourceWindow();
        infoWindow = new InfoWindow(farmResourceWindow);
        settingsWindow = new SettingsWindow();
        saveWindow = new SaveWindow();
        loadWindow = new LoadWindow();
        pauseWindow = new PauseWindow(loadWindow, saveWindow, settingsWindow);
        mainMenuWindow = new MainMenuWindow(loadWindow, settingsWindow);
        buildWindow = new BuildWindow();
        shiftWindow = new ShiftWindow();
        statisticsWindow = new StatisticsWindow();
        console = new Console();
        mainButtonsPane  = new MainButtonsPane(buildWindow, shiftWindow, statisticsWindow);
        clockPane = new ClockPane();
        resourceList = new ResourceList();

        //pause button
        pauseButtonPane = new BoxPane();
        Button pauseButton = new Button(Textures.Ui.PAUSE_GAME);
        pauseButton.setOnUp(pauseWindow::open);
        pauseButtonPane.addUIComponent(pauseButton);

        //tip text area
        tipText = new TextArea("");
        BoxPane tipTextPane = new BoxPane();
        tipTextPane.addUIComponents(tipText);

        Screen.IN_GAME.addUIComponent(infoWindow);
        Screen.IN_GAME.addUIComponent(tipTextPane, Anchor.BOTTOM);
        Screen.IN_GAME.addUIComponent(clockPane, Anchor.TOP_RIGHT);
        Screen.IN_GAME.addUIComponent(resourceList, Anchor.TOP_LEFT);
        Screen.IN_GAME.addUIComponent(pauseButtonPane, Anchor.BOTTOM_RIGHT);
        Screen.IN_GAME.addUIComponent(mainButtonsPane, Anchor.BOTTOM_LEFT);
        Screen.IN_GAME.addUIComponent(farmResourceWindow, Anchor.TOP_LEFT);
        Screen.IN_GAME.addUIComponent(buildWindow, Anchor.CENTER);
        Screen.IN_GAME.addUIComponent(shiftWindow, Anchor.TOP_LEFT);
        Screen.IN_GAME.addUIComponent(statisticsWindow, Anchor.TOP_LEFT);
        Screen.IN_GAME.addUIComponent(pauseWindow, Anchor.CENTER);
        Screen.IN_GAME.addUIComponent(settingsWindow, Anchor.CENTER);
        Screen.IN_GAME.addUIComponent(saveWindow, Anchor.CENTER);
        Screen.IN_GAME.addUIComponent(loadWindow, Anchor.CENTER);
        Screen.IN_GAME.addUIComponent(console, Anchor.TOP_LEFT);
        Screen.IN_GAME.addUIComponent(Popups.popup, Anchor.CENTER);

        Screen.MAIN_MENU.addUIComponent(mainMenuWindow, Anchor.CENTER);
        Screen.MAIN_MENU.addUIComponent(settingsWindow, Anchor.CENTER);
        Screen.MAIN_MENU.addUIComponent(loadWindow, Anchor.CENTER);
        Screen.MAIN_MENU.addUIComponent(console, Anchor.TOP_LEFT);
        Screen.MAIN_MENU.addUIComponent(Popups.popup, Anchor.CENTER);
    }

    public static void drawUI(SpriteBatch batch, OrthographicCamera camera) {
        updateProjectionMatrix(camera);
        batch.setProjectionMatrix(UI_PROJECTION);
        for (Pair<UIComponent, Anchor> pair : activeScreen.components) {
            if (pair.first.isVisible()) {
                pair.first.draw(batch);
            }
        }
        batch.setProjectionMatrix(camera.combined);
    }

    public static void drawPopup(SpriteBatch batch, OrthographicCamera camera) {
        updateProjectionMatrix(camera);
        batch.setProjectionMatrix(UI_PROJECTION);
        Popups.popup.draw(batch);
        batch.setProjectionMatrix(camera.combined);
    }

    public static boolean handleUiInteractions() {
        if (activeTextField != null && activeTextField.isVisible())
            activeTextField.write();

        if (activeScrollPane != null && activeScrollPane.isVisible() &&  activeScrollPane.isMouseOver())
            activeScrollPane.scroll();

        if (activeButton != null && activeButton.isVisible()) {
            if (InputManager.isKeyPressed(Input.Keys.ENTER))
                activeButton.onClick();
            if (InputManager.isKeyDown(Input.Keys.ENTER))
                activeButton.onHold();
            if (InputManager.isKeyUp(Input.Keys.ENTER))
                activeButton.onUp();
        }


        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) {
            for (int i = activeScreen.components.size()-1; i >= 0; i--) {
                UIComponent component = activeScreen.components.get(i).first;
                if (component.isVisible() && component.isEnabled()) {
                    if (component.isMouseOver()) {
                        clickedComponent = component.onClick();
                        return true;
                    }
                }
            }
        } else if (clickedComponent != null) {
            if (InputManager.isButtonUp(InputManager.LEFT_MOUSE)) {
                if (clickedComponent.isVisible())
                    clickedComponent.onUp();
                clickedComponent = null;
            } else if (InputManager.isButtonDown(InputManager.LEFT_MOUSE)) {
                if (clickedComponent.isVisible())
                    clickedComponent.onHold();
            }
            return true;
        }
        return false;
    }

    public static void drawGraph(ShapeRenderer renderer) {
        statisticsWindow.drawGraph(renderer);
    }

    public static void onEscape() {
        while (!onEscapeStack.isEmpty()) {
            Pair<Action, BooleanSupplier> pair = onEscapeStack.pop();
            if (pair.second.getAsBoolean()) {
                pair.first.execute();
                return;
            }
        }
        pauseWindow.open();
    }

    public static void pushOnEscapeAction(Action action, BooleanSupplier isActive) {
        onEscapeStack.push(Pair.of(action, isActive));
    }

    public static void updatePosition(UIComponent component) {
        Pair<UIComponent, Anchor> pair = activeScreen.components.stream().filter(p -> p.first == component).findFirst().orElseThrow();
        pair.second.updatePosition.accept(pair.first);
    }

    public static void resizeUI(int width, int height) {
        for (Pair<UIComponent, Anchor> pair : activeScreen.components) {
            if (pair.second != null)
                pair.second.updatePosition.accept(pair.first);

            if (pair.first instanceof Window window)
                window.pack();
            else if (pair.first instanceof Pane pane)
                pane.pack();
        }
        if (width < 1800)
            tipText.move(0, mainButtonsPane.getHeight());
    }

    public static void setActiveTextField(TextField textField) {
        activeTextField = textField;
    }

    public static void setActiveScrollPane(ScrollPane activeScrollPane) {
        UI.activeScrollPane = activeScrollPane;
    }

    public static void setActiveButton(Button activeButton) {
        UI.activeButton = activeButton;
    }

    private static void updateProjectionMatrix(OrthographicCamera camera) {
        UI_PROJECTION.setToScaling(camera.combined.getScaleX() * camera.zoom, camera.combined.getScaleY() * camera.zoom, 0);
        UI_PROJECTION.setTranslation(-1, -1, 0);
    }

    public static void showInfoWindow(Building building) {
        infoWindow.pin(building);
        infoWindow.open();
    }
    public static void showInfoWindow(Villager villager) {
        infoWindow.pin(villager);
        infoWindow.open();
    }

    public static void updateShiftWindow() {
        shiftWindow.update();
    }

    public static boolean isPaused() {
        return pauseWindow.isVisible();
    }

    public static void enableHUD() {
        mainButtonsPane.enable();
        pauseButtonPane.enable();
        clockPane.enable();
    }

    public static void disableHUD() {
        mainButtonsPane.disable();
        pauseButtonPane.disable();
        clockPane.disable();
    }

    public static void openCloseBuildMenu() {
        buildWindow.openClose();
    }

    public static void setTip(String tip) {
        tipText.setText(tip);
    }

    public static void showUI(Screen screen) {
        activeScreen = screen;
    }

    public static void updateResourceList(Resource resource) {
        resourceList.updateResourceCount(resource);
    }

    public static void openConsole() {
        console.open();
    }

    public static void closeConsole() {
        console.close();
    }

    public static boolean isConsoleOpen() {
        return console.isVisible();
    }

    public static void setConsoleText(String text) {
        console.setText(text);
    }
}
