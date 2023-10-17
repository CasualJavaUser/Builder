package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Animals;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.game_objects.Harvestables;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.game_objects.buildings.*;
import com.boxhead.builder.ui.popup.Popups;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

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
    private static TextField activeTextField = null;
    private static Button activeButton = null;
    private static ScrollPane activeScrollPane = null;
    private static Clickable clickedElement = null;
    private static final Set<UIElement> saveWindowElements = new HashSet<>();

    @AddToUI private static TextArea tipArea;
    @AddToUI private static Button buildMenuButton, npcButton, workButton, restButton, demolishButton, tilingButton, shiftMenuButton, statisticsMenuButton, pauseGameButton, bridgeButton;

    @AddToUI private static UIElement timeElementGroup;
    @AddToUI private static Clock clock;
    @AddToUI private static Button pauseButton, playButton, x2Button, x3Button;

    @AddToUI private static ResourceList resourceList;

    @AddToUI private static NPCStatWindow npcStatWindow;
    @AddToUI private static BuildingStatWindow buildingStatWindow;

    @AddToUI private static ShiftMenu shiftMenu;
    @AddToUI private static StatisticsMenu statisticsMenu;
    @AddToUI private static BuildingMenu buildingMenu;
    @AddToUI private static FarmResourceMenu farmResourceMenu;
    @AddToUI private static PauseMenu pauseMenu;

    @AddToUI private static Window saveWindow;
    @AddToUI private static TextArea saveText;
    @AddToUI private static ScrollPane scrollPane;
    @AddToUI private static Button saveWindowBackButton;

    @AddToUI private static Window settingsWindow;
    @AddToUI private static TextArea settingsText;
    @AddToUI private static Button settingsWindowBackButton;

    public static final int PADDING = 10;
    private static boolean isPaused = false;

    public enum Anchor {
        CENTER,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_CENTER;

        private final UIElement element;

        Anchor() {
            this.element = new UIElement();
        }

        public UIElement getElement() {
            return element;
        }
    }

    public enum Layer {
        WORLD(true),
        IN_GAME(true),
        PAUSE_MENU(false),
        SAVE_MENU(false),
        SETTINGS_MENU(false),
        POPUP(false),
        CONSOLE(false);

        private final List<UIElement> elements;
        private boolean isVisible;

        Layer(boolean isVisible) {
            elements = new ArrayList<>();
            this.isVisible = isVisible;
        }

        public void setVisible(boolean visible) {
            isVisible = visible;
        }

        public boolean isVisible() {
            return isVisible;
        }

        public List<UIElement> getElements() {
            return elements;
        }

        public void addElement(UIElement element) {
            elements.add(element);
        }

        public void removeElement(UIElement element) {
            elements.remove(element);
        }
    }

    public static void init() {
        timeElementGroup = new UIElement(Anchor.TOP_RIGHT.getElement(), Layer.IN_GAME, new Vector2i(), true);
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Pixel.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 30;
        parameter.spaceY = -6;
        FONT = generator.generateFont(parameter);
        generator.dispose();

        //region saveWindow
        saveWindow = new Window(Textures.get(Textures.Ui.MENU_WINDOW), Anchor.CENTER.getElement(), Layer.SAVE_MENU, new Vector2i());
        saveWindow.setContentWidth(500);
        saveWindow.setContentHeight(500);
        saveWindow.setLocalPosition(-saveWindow.getWindowWidth() / 2, -saveWindow.getWindowHeight() / 2);

        saveText = new TextArea(
                "Save",
                saveWindow,
                Layer.SAVE_MENU,
                new Vector2i(0, saveWindow.getWindowHeight() - saveWindow.getEdgeWidth() - PADDING),
                saveWindow.getWindowWidth(),
                TextArea.Align.CENTER
        );

        scrollPane = new ScrollPane(
                saveWindow,
                Layer.SAVE_MENU,
                0,
                saveWindow.getEdgeWidth() + PADDING *2 + 32,
                saveWindow.getWindowWidth(),
                saveText.getLocalPosition().y - 25);

        saveWindowBackButton = new Button(
                Textures.get(Textures.Ui.SMALL_BUTTON),
                saveWindow,
                Layer.SAVE_MENU,
                new Vector2i(saveWindow.getWindowWidth()/2 - 40, saveWindow.getEdgeWidth() + PADDING),
                "Back");
        saveWindowBackButton.setOnUp(() -> Layer.SAVE_MENU.setVisible(false));

        saveWindow.setTint(WHITE);
        saveText.setTint(WHITE);
        saveWindowBackButton.setTint(WHITE);
        //endregion

        //region settingsWindow
        {
            settingsWindow = new Window(Textures.get(Textures.Ui.MENU_WINDOW), Anchor.CENTER.getElement(), Layer.SETTINGS_MENU, new Vector2i());
            settingsWindow.setContentWidth(500);
            settingsWindow.setContentHeight(500);
            settingsWindow.setLocalPosition(-settingsWindow.getWindowWidth() / 2, -settingsWindow.getWindowHeight() / 2);

            settingsText = new TextArea(
                    "Settings",
                    settingsWindow,
                    Layer.SETTINGS_MENU,
                    new Vector2i(0, settingsWindow.getWindowHeight() - 25),
                    settingsWindow.getWindowWidth(),
                    TextArea.Align.CENTER
            );

            settingsWindowBackButton = new Button(
                    Textures.get(Textures.Ui.SMALL_BUTTON),
                    settingsWindow,
                    Layer.SETTINGS_MENU,
                    new Vector2i(settingsWindow.getWindowWidth()/2 - 40, settingsWindow.getEdgeWidth() + PADDING),
                    "Back");
            settingsWindowBackButton.setOnUp(() -> Layer.SETTINGS_MENU.setVisible(false));

            settingsWindow.setTint(WHITE);
            settingsText.setTint(WHITE);
            settingsWindowBackButton.setTint(WHITE);
        }
        //endregion

        //region mainButtonGroup
        {
            int x = PADDING;
            buildMenuButton =      new Button(Textures.get(Textures.Ui.HAMMER), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x, PADDING));
            npcButton =            new Button(Textures.get(Textures.Ui.NPC), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            workButton =           new Button(Textures.get(Textures.Ui.WORK), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            restButton =           new Button(Textures.get(Textures.Ui.REST), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            demolishButton =       new Button(Textures.get(Textures.Ui.DEMOLISH), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            tilingButton =         new Button(Textures.get(Textures.Ui.PATH), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            shiftMenuButton =      new Button(Textures.get(Textures.Ui.SHIFTS), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            statisticsMenuButton = new Button(Textures.get(Textures.Ui.GRAPH_BUTTON), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            bridgeButton =         new Button(Textures.get(Textures.Ui.PATH), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));

            pauseGameButton = new Button(Textures.get(Textures.Ui.PAUSE_GAME), Anchor.BOTTOM_RIGHT.getElement(), Layer.IN_GAME, new Vector2i(-48 - PADDING, PADDING));

            buildMenuButton.setOnUp(() -> buildingMenu.setVisible(!buildingMenu.isVisible()));

            npcButton.setOnUp(() -> {
                Vector2i position = new Vector2i(World.getGridWidth() / 2, World.getGridHeight() / 2);
                World.spawnVillager(new Villager(position));
            });

            workButton.setOnUp(() -> {
                World.advanceTime(28770);
            });
            restButton.setOnUp(() -> {
                World.advanceTime(57570);
            });
            demolishButton.setOnUp(() -> {
                if (Buildings.isInDemolishingMode()) Buildings.turnOffDemolishingMode();
                else Buildings.toDemolishingMode();
            });
            tilingButton.setOnUp(() -> {
                if (!Tiles.isInPathMode())
                    Tiles.toPathMode(Tile.PATH);
                else
                    Tiles.turnOffPathMode();
            });
            bridgeButton.setOnUp(() -> {
                if (!Tiles.isInBridgeMode())
                    Tiles.toBridgeMode();
                else
                    Tiles.turnOffBridgeMode();
            });
            shiftMenuButton.setOnUp(() -> shiftMenu.setVisible(!shiftMenu.isVisible()));
            statisticsMenuButton.setOnUp(() -> statisticsMenu.setVisible(!statisticsMenu.isVisible()));
            pauseGameButton.setOnUp(() -> showPauseMenu(true));
        }
        //endregion

        //region timeElementGroup
        TextureRegion clockTexture = Textures.get(Textures.Ui.CLOCK_FACE);
        clock = new Clock(timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() - PADDING, -clockTexture.getRegionHeight() - PADDING));

        pauseButton = new Button(
                Textures.get(Textures.Ui.PAUSE),
                timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() - 9, -clockTexture.getRegionHeight() - 36));

        playButton = new Button(
                Textures.get(Textures.Ui.PLAY),
                timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() + 23, -clockTexture.getRegionHeight() - 36));

        x2Button = new Button(
                Textures.get(Textures.Ui.X2SPEED),
                timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() + 55, -clockTexture.getRegionHeight() - 36));

        x3Button = new Button(
                Textures.get(Textures.Ui.X3SPEED),
                timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() + 87, -clockTexture.getRegionHeight() - 36));

        pauseButton.setOnClick(() -> Logic.setTickSpeed(0));
        playButton.setOnClick(() -> Logic.setTickSpeed(Logic.NORMAL_SPEED));
        x2Button.setOnClick(() -> Logic.setTickSpeed(Logic.SPEED_X2));
        x3Button.setOnClick(() -> Logic.setTickSpeed(Logic.SPEED_X3));
        //endregion

        buildingMenu = new BuildingMenu();
        shiftMenu = new ShiftMenu();
        statisticsMenu = new StatisticsMenu();
        farmResourceMenu = new FarmResourceMenu();
        pauseMenu = new PauseMenu();
        resourceList = new ResourceList();

        npcStatWindow = new NPCStatWindow(Layer.IN_GAME);
        buildingStatWindow = new BuildingStatWindow(Layer.IN_GAME);

        tipArea = new TextArea(
                "",
                Anchor.BOTTOM_CENTER.getElement(),
                Layer.IN_GAME,
                new Vector2i(0, (int)(FONT.getCapHeight() + PADDING)),
                0,
                TextArea.Align.CENTER
        );

        addUIElements();
    }

    private static void addUIElements() {
        try {
            Field[] fields = UI.class.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(AddToUI.class)) {
                    ((UIElement) field.get(UI.class)).addToUI();
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void drawUI(SpriteBatch batch, OrthographicCamera camera) {
        updateProjectionMatrix(camera);
        batch.setProjectionMatrix(UI_PROJECTION);
        for (Layer layer : Layer.values()) {
            for (UIElement element : layer.getElements()) {
                if (element.isVisible()) {
                    element.enableScissors(batch);
                    element.draw(batch);
                    element.disableScissors(batch);
                }
            }
        }
        batch.setProjectionMatrix(camera.combined);
    }

    public static void drawMenu(SpriteBatch batch, OrthographicCamera camera) {
        updateProjectionMatrix(camera);
        batch.setProjectionMatrix(UI_PROJECTION);
        for (int i = Layer.PAUSE_MENU.ordinal(); i < Layer.values().length; i++) {
            for (UIElement element : Layer.values()[i].getElements()) {
                if (element.isVisible()) {
                    element.enableScissors(batch);
                    element.draw(batch);
                    element.disableScissors(batch);
                }
            }
        }
        batch.setProjectionMatrix(camera.combined);
    }

    public static void drawPopups(SpriteBatch batch, OrthographicCamera camera) {
        updateProjectionMatrix(camera);
        batch.setProjectionMatrix(UI_PROJECTION);
        if (Popups.getActivePopup() != null) Popups.getActivePopup().draw(batch);
        batch.setProjectionMatrix(camera.combined);
    }

    private static void updateProjectionMatrix(OrthographicCamera camera) {
        UI_PROJECTION.setToScaling(camera.combined.getScaleX() * camera.zoom, camera.combined.getScaleY() * camera.zoom, 0);
        UI_PROJECTION.setTranslation(-1, -1, 0);
    }

    public static boolean handleUiInteraction() {
        boolean interacted = false;

        if(activeTextField != null) activeTextField.write();
        if(activeScrollPane != null && activeScrollPane.isVisible() && activeScrollPane.isMouseOver())
            activeScrollPane.scroll();

        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) {
            Layer layer = getTopVisibleLayer();
            if (layer != null) interacted = onClick(getTopVisibleLayer());
        }
        else if (clickedElement != null) {
            if (clickedElement instanceof Button button) activeButton = button;
            if (InputManager.isButtonDown(InputManager.LEFT_MOUSE)) interacted = onHold();
            if (InputManager.isButtonUp(InputManager.LEFT_MOUSE)) {
                interacted = onUp();
                clickedElement = null;
            }
        }
        else if (activeButton != null && activeButton.getLayer().equals(getTopVisibleLayer())) {
            if (InputManager.isKeyPressed(Input.Keys.ENTER)) activeButton.onClick();
            if (InputManager.isKeyDown(Input.Keys.ENTER)) activeButton.onHold();
            if (InputManager.isKeyUp(Input.Keys.ENTER)) activeButton.onUp();
        }

        return interacted;
    }

    /**
     * Checks if any element in the given layer has been clicked.
     * @param layer the layer in which the clicked element is searched for
     * @return true if an element has been clicked
     */
    private static boolean onClick(Layer layer) {
        for (int i = layer.getElements().size()-1; i >= 0; i--) {
            UIElement element = layer.getElements().get(i);
            if (element.isVisible() && element instanceof Clickable clickable && clickable.isMouseOver()) {
                clickable.onClick();
                clickedElement = clickable;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any element in the given layers has been clicked.
     * @param layers the layers in which the clicked element is searched for
     * @return true if an element has been clicked
     */
    private static boolean onClick(Layer... layers) {
        for (Layer layer : layers) {
            for (int i = layer.getElements().size()-1; i >= 0; i--) {
                UIElement element = layer.getElements().get(i);
                if (element.isVisible() && element instanceof Clickable && ((Clickable) element).isMouseOver()) {
                    ((Clickable) element).onClick();
                    clickedElement = ((Clickable) element);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean onHold() {
        if (clickedElement.isMouseOver() && ((UIElement) clickedElement).isVisible()) {
            clickedElement.onHold();
            return true;
        }
        return false;
    }

    private static boolean onUp() {
        if (clickedElement.isMouseOver() && ((UIElement) clickedElement).isVisible()) {
            clickedElement.onUp();
            return true;
        }
        return false;
    }

    public static void pause(boolean pause) {
        showPauseMenu(pause);
    }

    public static boolean isPaused() {
        return isPaused;
    }

    public static void resizeUI() {
        Anchor.CENTER.getElement().setGlobalPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        Anchor.TOP_LEFT.getElement().setGlobalPosition(0, Gdx.graphics.getHeight());
        Anchor.TOP_RIGHT.getElement().setGlobalPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Anchor.BOTTOM_LEFT.getElement().setGlobalPosition(0, 0);
        Anchor.BOTTOM_RIGHT.getElement().setGlobalPosition(Gdx.graphics.getWidth(), 0);
        Anchor.BOTTOM_CENTER.getElement().setGlobalPosition(Gdx.graphics.getWidth()/2, 0);
        scrollPane.updateScissors();
    }

    public static void showNPCStatWindow(Villager villager) {
        npcStatWindow.pin(villager);
        npcStatWindow.setVisible(true);
    }

    public static void showBuildingStatWindow(Building building) {
        buildingStatWindow.pin(building);
        buildingStatWindow.setVisible(true);
    }

    public static void onEscape() {
        if (buildingMenu.isVisible() || shiftMenu.isVisible() || statisticsMenu.isVisible() || farmResourceMenu.isVisible()) {
            closeInGameMenus();
        } else if (!isPaused) {
            if (Buildings.isInBuildingMode() || Buildings.isInDemolishingMode() || Tiles.isInPathMode() || Tiles.isInBridgeMode()) {
                Buildings.turnOffBuildingMode();
                Buildings.turnOffDemolishingMode();
                Tiles.turnOffPathMode();
                Tiles.turnOffBridgeMode();
            } else if (buildingStatWindow.isVisible() || npcStatWindow.isVisible()) {
                buildingStatWindow.setVisible(false);
                npcStatWindow.setVisible(false);
            } else {
                showPauseMenu(true);
            }
        } else {
            if(getTopVisibleLayer() == Layer.PAUSE_MENU) showPauseMenu(false);
            else getTopVisibleLayer().setVisible(false);
        }
    }

    private static Layer getTopVisibleLayer() {
        for (int i = Layer.values().length-1; i >= 0; i--) {
            if(Layer.values()[i].isVisible()) return Layer.values()[i];
        }
        return null;
    }

    public static void showPauseMenu(boolean open) {
        Logic.pause(open);
        isPaused = open;
        DEFAULT_COLOR.set(open ? DARK : World.getSkyColor(World.getTime()));
        DEFAULT_UI_COLOR.set(open ? DARK : WHITE);
        Layer.PAUSE_MENU.setVisible(open);
    }

    private static void showLoadMenu() {
        Layer.SAVE_MENU.getElements().removeAll(saveWindowElements);
        scrollPane.clear();
        activeScrollPane = scrollPane;

        Layer.SAVE_MENU.setVisible(true);
        saveText.setText("Load");

        SortedSet<File> saves = BuilderGame.getSortedSaveFiles();

        if (saves.size() == 0) {
            TextArea textArea = new TextArea(Textures.get(Textures.Ui.WIDE_AREA), "No saves", scrollPane, Layer.SAVE_MENU, new Vector2i(), TextArea.Align.CENTER);
            textArea.setTint(WHITE);
            textArea.addToUI();
            scrollPane.addElement(textArea);
        }
        else {
            for (File save : saves) {
                createSaveField(save, false);
            }
        }
    }

    public static void showSaveMenu() {
        Layer.SAVE_MENU.getElements().removeAll(saveWindowElements);
        scrollPane.clear();
        activeScrollPane = scrollPane;

        Layer.SAVE_MENU.setVisible(true);
        saveText.setText("Save");
        TextureRegion texture = Textures.get(Textures.Ui.WIDE_AREA);

        SortedSet<File> saves = BuilderGame.getSortedSaveFiles();

        Button newSaveButton = new Button(texture, scrollPane, Layer.SAVE_MENU, new Vector2i(), "New save");

        newSaveButton.setOnUp(() -> {
            Popups.showPopup("New save", "New save...", s -> {
                if (BuilderGame.getSaveFile(s + ".save").exists()) {
                    Popups.showPopup("Override save?", () -> {
                        BuilderGame.saveToFile(s + ".save");
                        Layer.SAVE_MENU.setVisible(false);
                    });
                }
                else {
                    BuilderGame.saveToFile(s + ".save");
                    Layer.SAVE_MENU.setVisible(false);
                }
            });
        });

        saveWindowElements.add(newSaveButton);
        newSaveButton.setTint(WHITE);
        newSaveButton.addToUI();
        scrollPane.addElement(newSaveButton);

        for (File save : saves) {
            createSaveField(save, true);
        }
    }

    public static void setActiveTextField(TextField activeTextField) {
        UI.activeTextField = activeTextField;
    }

    public static void setActiveButton(Button activeButton) {
        UI.activeButton = activeButton;
    }

    public static void setActiveScrollPane(ScrollPane activeScrollPane) {
        UI.activeScrollPane = activeScrollPane;
    }

    public static void setTip(String tip) {
        tipArea.setText(tip);
    }

    private static void createSaveField(File saveFile, boolean isSaving) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();

        UIElement area = new UIElement(Textures.get(Textures.Ui.WIDE_AREA), scrollPane, Layer.SAVE_MENU, new Vector2i());

        TextArea textArea = new TextArea(
                saveFile.getName().substring(0, saveFile.getName().lastIndexOf(".")) + "\n" +
                    dateFormat.format(saveFile.lastModified()),
                area,
                Layer.SAVE_MENU,
                new Vector2i(158, area.getHeight() - 15),
                area.getWidth() - 148,
                TextArea.Align.CENTER
        );

        Button saveButton = new Button(Textures.get(isSaving ? Textures.Ui.SAVE : Textures.Ui.LOAD), area, Layer.SAVE_MENU, new Vector2i(PADDING, PADDING));

        saveButton.setOnUp(() -> {
            if (isSaving) {
                Popups.showPopup("Override save?", () -> {
                    BuilderGame.saveToFile(saveFile);
                    Layer.SAVE_MENU.setVisible(false);
                });
            } else {
                if (BuilderGame.timeSinceLastSave()  > 60_000) {
                    Popups.showPopup("Load save?", () -> {
                        BuilderGame.loadFromFile(saveFile);
                        Layer.SAVE_MENU.setVisible(false);
                    });
                }
                else {
                    BuilderGame.loadFromFile(saveFile);
                    Layer.SAVE_MENU.setVisible(false);
                }
            }
        });

        Button deleteButton = new Button(Textures.get(Textures.Ui.DELETE), area, Layer.SAVE_MENU, new Vector2i(PADDING * 2 + 64, PADDING));

        deleteButton.setOnUp(() -> {
            Popups.showPopup("Delete file?", () -> {
                saveFile.delete();
                if (isSaving) showSaveMenu();
                else showLoadMenu();
            });
        });

        saveWindowElements.add(area);
        saveWindowElements.add(textArea);
        saveWindowElements.add(saveButton);
        saveWindowElements.add(deleteButton);

        area.setTint(WHITE);
        textArea.setTint(WHITE);
        saveButton.setTint(WHITE);
        deleteButton.setTint(WHITE);

        area.addToUI();
        textArea.addToUI();
        saveButton.addToUI();
        deleteButton.addToUI();

        scrollPane.addElement(area);

        textArea.setScissors(scrollPane.getScissors());
        saveButton.setScissors(scrollPane.getScissors());
        deleteButton.setScissors(scrollPane.getScissors());
    }

    public static ResourceList getResourceList() {
        return resourceList;
    }

    public static NPCStatWindow getNpcStatWindow() {
        return npcStatWindow;
    }

    public static BuildingStatWindow getBuildingStatWindow() {
        return buildingStatWindow;
    }

    /**
     * Changes pause menu buttons for in-game.
     */
    public static void setInGame(boolean inGame) {
        pauseMenu.setInGame(inGame);
    }

    public static  void closeInGameMenus() {
        buildingMenu.setVisible(false);
        shiftMenu.setVisible(false);
        statisticsMenu.setVisible(false);
        farmResourceMenu.setVisible(false);
    }

    public static void drawGraph(ShapeRenderer renderer) {
        if (statisticsMenu.isVisible()) {
            statisticsMenu.drawGraph(renderer);
        }
    }

    public static void showFarmResourceMenu(FarmBuilding<?> farmBuilding) {
        farmResourceMenu.show(farmBuilding);
    }

    private static class PauseMenu extends Window {
        private static Button newGameButton, resumeButton, loadButton, saveButton, settingsButton, quitToMenuButton, quitButton;

        public PauseMenu() {
            super(Textures.get(Textures.Ui.MENU_WINDOW), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i());
            setContentWidth(200);
            setContentHeight(380);
            setLocalPosition(-getWindowWidth() / 2, -getWindowHeight() / 2);
            setTint(WHITE);

            int x = -Textures.get(Textures.Ui.BIG_BUTTON).getRegionWidth() / 2;
            newGameButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, 116), "New game");
            resumeButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, 116), "Resume");
            loadButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, 42), "Load");
            saveButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, -32), "Save");
            settingsButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, -106), "Settings");
            quitToMenuButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, -180), "Quit to menu");
            quitButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, -180), "Quit");

            newGameButton.setOnUp(BuilderGame::generateNewWorld);
            resumeButton.setOnUp(() -> showPauseMenu(false));
            loadButton.setOnUp(UI::showLoadMenu);
            saveButton.setOnUp(UI::showSaveMenu);
            settingsButton.setOnUp(() -> Layer.SETTINGS_MENU.setVisible(true));
            quitToMenuButton.setOnUp(() -> BuilderGame.getInstance().setScreen(BuilderGame.getMenuScreen()));
            quitButton.setOnUp(() -> Gdx.app.exit());

            newGameButton.setTint(WHITE);
            resumeButton.setTint(WHITE);
            loadButton.setTint(WHITE);
            saveButton.setTint(WHITE);
            settingsButton.setTint(WHITE);
            quitToMenuButton.setTint(WHITE);
            quitButton.setTint(WHITE);
        }

        public void setInGame(boolean inGame) {
            resumeButton.setVisible(inGame);
            newGameButton.setVisible(!inGame);
            quitToMenuButton.setVisible(inGame);
            quitButton.setVisible(!inGame);
        }

        @Override
        public void addToUI() {
            super.addToUI();
            newGameButton.addToUI();
            resumeButton.addToUI();
            loadButton.addToUI();
            saveButton.addToUI();
            settingsButton.addToUI();
            quitToMenuButton.addToUI();
            quitButton.addToUI();
        }
    }

    private static class BuildingMenu extends Window {
        private final TextArea divider;
        private final UIElement buildingImage;
        private final Button buildButton;
        private final TextArea descriptionArea;

        private final Button infrastructureTabButton;
        private final Button housingTabButton;
        private final Button resourcesTabButton;
        private final Button servicesTabButton;

        private final Tab[] tabs;

        BuildingMenu() {
            super(Textures.get(Textures.Ui.WINDOW), Anchor.CENTER.getElement(), Layer.IN_GAME, new Vector2i(), false);
            setContentWidth(454);
            setContentHeight(400);
            setLocalPosition(-getWindowWidth() / 2, -getWindowHeight() / 2);

            divider = new TextArea(Textures.get(Textures.Ui.DIVIDER), "", this, Layer.IN_GAME, new Vector2i(), TextArea.Align.CENTER);
            divider.setLocalPosition(
                    texture.getRegionWidth() + PADDING,
                    texture.getRegionHeight() + 128 + PADDING * 2
            );

            buildingImage = new UIElement(null, this, Layer.IN_GAME, new Vector2i(getEdgeWidth() + PADDING, getEdgeWidth() + PADDING));

            descriptionArea = new TextArea("", this, Layer.IN_GAME, new Vector2i(), 200, TextArea.Align.LEFT);

            buildButton = new Button(
                    Textures.get(Textures.Ui.BUILD),
                    this,
                    Layer.IN_GAME,
                    new Vector2i(getWindowWidth() - getEdgeWidth() - 64 - PADDING, getEdgeWidth() + PADDING));
            buildButton.setVisible(false);

            tabs = new Tab[4];

            //infrastructure
            tabs[0] = new Tab(this, true,
                    Pair.of(Building.Type.STORAGE_BARN, Textures.Ui.BARN),
                    Pair.of(ProductionBuilding.Type.BUILDERS_HUT, Textures.Ui.BIG_HAMMER),
                    Pair.of(ProductionBuilding.Type.TRANSPORT_OFFICE, Textures.Ui.CARRIAGE)
            );

            //housing
            tabs[1] = new Tab(this, false,
                    Pair.of(ResidentialBuilding.Type.LOG_CABIN, Textures.Ui.HOUSE)
            );

            //resources
            tabs[2] = new Tab(this, false,
                    Pair.of(ProductionBuilding.Type.LUMBERJACKS_HUT, Textures.Ui.AXE),
                    Pair.of(ProductionBuilding.Type.MINE, Textures.Ui.PICKAXE),
                    Pair.of(ProductionBuilding.Type.STONE_GATHERERS, Textures.Ui.PICKAXE_WITH_STONE),
                    Pair.of(PlantationBuilding.Type.PLANTATION, Textures.Ui.HOE),
                    Pair.of(RanchBuilding.Type.RANCH, Textures.Ui.COW),
                    Pair.of(WaterBuilding.Type.FISHING_HUT, Textures.Ui.FISHING_ROD),
                    Pair.of(WaterBuilding.Type.WATERMILL, Textures.Ui.WHEEL)
            );

            //services
            tabs[3] = new Tab(this, false,
                    Pair.of(ServiceBuilding.Type.HOSPITAL, Textures.Ui.CROSS),
                    Pair.of(ServiceBuilding.Type.PUB, Textures.Ui.MUG),
                    Pair.of(SchoolBuilding.Type.SCHOOL, Textures.Ui.BOOK)
            );

            int x = PADDING;
            infrastructureTabButton = new Button(Textures.get(Textures.Ui.INFRASTRUCTURE_TAB), this, Layer.IN_GAME, new Vector2i(x, getWindowHeight()-5));
            housingTabButton = new Button(Textures.get(Textures.Ui.HOUSING_TAB), this, Layer.IN_GAME, new Vector2i(x += 32 + PADDING, getWindowHeight()-5));
            resourcesTabButton = new Button(Textures.get(Textures.Ui.RESOURCES_TAB), this, Layer.IN_GAME, new Vector2i(x += 32 + PADDING, getWindowHeight()-5));
            servicesTabButton = new Button(Textures.get(Textures.Ui.SERVICES_TAB), this, Layer.IN_GAME, new Vector2i(x += 32 + PADDING, getWindowHeight()-5));

            infrastructureTabButton.setOnUp(() -> showTab(0));
            housingTabButton.setOnUp(() -> showTab(1));
            resourcesTabButton.setOnUp(() -> showTab(2));
            servicesTabButton.setOnUp(() -> showTab(3));
        }

        private void showTab(int tabIndex) {
            for (int i = 0; i < tabs.length; i++) {
                tabs[i].setVisible(i == tabIndex);
            }
        }

        private void showBuildingStats(Building.Type type) {
            float scale = 128f / type.getTexture().getRegionHeight();
            if (type.getTexture().getRegionWidth() * scale > 192) scale = 192f / type.getTexture().getRegionWidth();
            buildingImage.setScale(scale);
            buildingImage.setTexture(type.getTexture());
            divider.setText(type.name);

            String description = "";
            description += "build cost:\n" + type.buildCost;
            if (type instanceof ProductionBuilding.Type productionType && productionType.range != 0)
                description += "\n\nrange: " + productionType.range;

            descriptionArea.setText(description);
            descriptionArea.setLocalPosition(
                    buildingImage.getLocalPosition().x + (int)(buildingImage.getWidth() * scale) + PADDING,
                    buildingImage.getLocalPosition().y + (int)(buildingImage.getHeight() * scale)
            );

            buildButton.setOnUp(() -> {
                Buildings.toBuildingMode(type);
                buildingMenu.setVisible(false);
            });

            buildButton.setVisible(true);
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible)
                closeInGameMenus();
            super.setVisible(visible);
        }

        @Override
        public void addToUI() {
            super.addToUI();
            divider.addToUI();
            buildingImage.addToUI();
            buildButton.addToUI();
            descriptionArea.addToUI();
            infrastructureTabButton.addToUI();
            housingTabButton.addToUI();
            resourcesTabButton.addToUI();
            servicesTabButton.addToUI();
            for (Tab tab : tabs) {
                tab.addToUI();
            }
        }

        private class Tab extends UIElement {
            private static final int ROW_LENGTH = 6;

            private final Button[] buttons;

            @SafeVarargs
            public Tab(Window parent, boolean visible, Pair<Building.Type, Textures.Ui>... buttons) {
                super(
                        parent,
                        Layer.IN_GAME,
                        new Vector2i(parent.getEdgeWidth() + PADDING, parent.getWindowHeight() - parent.getEdgeWidth() - PADDING),
                        visible
                );

                this.buttons = new Button[buttons.length];

                int x = 0, y = PADDING;
                for (int i = 0; i < buttons.length; i++) {
                    Pair<Building.Type, Textures.Ui> pair = buttons[i];
                    if (i%ROW_LENGTH == 0) {
                        y -= PADDING + 64;
                        x = 0;
                    }
                    Button button = new Button(Textures.get(pair.second), this, Layer.IN_GAME, new Vector2i(x, y));
                    button.setOnUp(() -> showBuildingStats(pair.first));
                    x += 74;
                    this.buttons[i] = button;
                }
            }

            @Override
            public void addToUI() {
                super.addToUI();
                for (Button button : buttons) {
                    button.addToUI();
                }
            }
        }
    }

    private static class ShiftMenu extends UIElement {
        private static final int COLUMN_WIDTH = 48;
        private static final int NAME_WIDTH = 210;

        final Window window;
        final TextArea[] textAreas;
        final TextArea[] timeLabels;
        final CheckBox[] checkBoxes;
        final UIElement timelineTop;
        final UIElement[] timelineSegments;
        final ProductionBuilding.Type[] types;

        ShiftMenu() {
            super(Anchor.TOP_LEFT.getElement(), Layer.IN_GAME, Vector2i.zero(), false);
            window = new Window(Textures.get(Textures.Ui.WINDOW), this, layer, Vector2i.zero(), true);
            window.setContentWidth(PADDING * 3 + NAME_WIDTH + COLUMN_WIDTH * 3);

            types = Stream.of(ProductionBuilding.Type.values(), ServiceBuilding.Type.values(), PlantationBuilding.Type.values(), RanchBuilding.Type.values(), SchoolBuilding.Type.values())
                    .flatMap(Arrays::stream)
                    .sorted(Comparator.comparing(type -> type.name))
                    .toArray(ProductionBuilding.Type[]::new);

            textAreas = new TextArea[types.length];
            timeLabels = new TextArea[6];
            checkBoxes = new CheckBox[types.length * 3];
            timelineSegments = new UIElement[types.length];

            int y = -window.getEdgeWidth() - PADDING;
            int height = PADDING;
            int columnOffset = window.getEdgeWidth() + PADDING + NAME_WIDTH + (COLUMN_WIDTH - 32) / 2;
            int shiftOffset = 18;

            timeLabels[0] = new TextArea("3:00",   this, layer, new Vector2i(window.getEdgeWidth() + PADDING + NAME_WIDTH + shiftOffset, y), COLUMN_WIDTH, TextArea.Align.CENTER);
            timeLabels[1] = new TextArea("11:00",  this, layer, new Vector2i(window.getEdgeWidth() + PADDING + NAME_WIDTH + COLUMN_WIDTH + shiftOffset, y), COLUMN_WIDTH, TextArea.Align.CENTER);
            timeLabels[2] = new TextArea("19:00",  this, layer, new Vector2i(window.getEdgeWidth() + PADDING + NAME_WIDTH + COLUMN_WIDTH * 2 + shiftOffset, y), COLUMN_WIDTH, TextArea.Align.CENTER);

            y -= (int)FONT.getLineHeight() + 5;
            height += FONT.getLineHeight() + 5;

            timeLabels[3] = new TextArea("0:00",  this, layer, new Vector2i(window.getEdgeWidth() + PADDING + NAME_WIDTH, y), COLUMN_WIDTH, TextArea.Align.CENTER);
            timeLabels[4] = new TextArea("8:00",  this, layer, new Vector2i(window.getEdgeWidth() + PADDING + NAME_WIDTH + COLUMN_WIDTH, y), COLUMN_WIDTH, TextArea.Align.CENTER);
            timeLabels[5] = new TextArea("16:00", this, layer, new Vector2i(window.getEdgeWidth() + PADDING + NAME_WIDTH + COLUMN_WIDTH * 2, y), COLUMN_WIDTH, TextArea.Align.CENTER);

            y -= (int)FONT.getLineHeight() + PADDING * 3;
            height += FONT.getLineHeight() + PADDING * 3;

            timelineTop = new UIElement(
                    Textures.get(Textures.Ui.TIMELINE_TOP),
                    this,
                    layer,
                    new Vector2i(columnOffset, y - (int)FONT.getLineHeight() + 32)
            );

            for (int i = 0; i < types.length; i++) {
                ProductionBuilding.Type type = types[i];

                textAreas[i] = new TextArea(
                        type.name,
                        this,
                        layer,
                        new Vector2i(window.getEdgeWidth() + PADDING, y),
                        NAME_WIDTH,
                        TextArea.Align.RIGHT
                );

                timelineSegments[i] = new UIElement(
                        Textures.get(Textures.Ui.TIMELINE_SEGMENT),
                        this,
                        layer,
                        new Vector2i(columnOffset, y - (int)FONT.getLineHeight())
                );

                for (int j = 0; j < ProductionBuilding.SHIFTS_PER_JOB; j++) {
                    int x = columnOffset + COLUMN_WIDTH * j;
                    if (type instanceof ServiceBuilding.Type)
                        x += shiftOffset;

                    checkBoxes[i * 3 + j] = new CheckBox(
                            this,
                            layer,
                            new Vector2i(x, y - (int)FONT.getLineHeight()),
                            type.getShiftActivity(j)
                    );

                    int shiftIndex = j;
                    checkBoxes[i * 3 + j].setOnUp((active) -> type.setShiftActivity(shiftIndex, active));
                }
                y -= 35;
                height += FONT.getLineHeight() + 15;
            }

            height -= 15;
            height += PADDING;
            window.setContentHeight(height);
            window.setLocalPosition(0, -window.getWindowHeight());
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible)
                closeInGameMenus();
            super.setVisible(visible);
        }

        @Override
        public void addToUI() {
            super.addToUI();
            window.addToUI();
            timelineTop.addToUI();
            for (UIElement segment : timelineSegments) {
                segment.addToUI();
            }
            for (TextArea timeLabel : timeLabels) {
                timeLabel.addToUI();
            }
            for (TextArea textArea : textAreas) {
                textArea.addToUI();
            }
            for (CheckBox checkBox : checkBoxes) {
                checkBox.addToUI();
            }
        }
    }

    private static class StatisticsMenu extends UIElement {
        final int COLUMN_WIDTH = 256;
        final int GRAPH_SIZE = 307;

        final Window window;
        final Button[] buttons;
        final UIElement graph;
        final Color graphColor;

        private Statistics.Type currentStat;
        private float upperLimit = 100;

        public StatisticsMenu() {
            super(Anchor.TOP_LEFT.getElement(), Layer.IN_GAME, Vector2i.zero(), false);
            window = new Window(Textures.get(Textures.Ui.WINDOW), this, layer, Vector2i.zero(), true);
            window.setContentWidth(PADDING * 2 + COLUMN_WIDTH + PADDING + GRAPH_SIZE);
            graph = new UIElement(
                    Textures.get(Textures.Ui.GRAPH),
                    this,
                    layer,
                    new Vector2i(window.getEdgeWidth() + PADDING * 2 + COLUMN_WIDTH, -window.getEdgeWidth() - PADDING - GRAPH_SIZE),
                    true
            );
            graphColor = new Color(0.098f, 0.082f, 0.063f, 1f);

            buttons = new Button[Statistics.Type.values().length];
            int y = -window.getEdgeWidth();
            for (int i = 0; i < buttons.length; i++) {
                y -= 32 + PADDING;
                buttons[i] = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), this, layer, new Vector2i(window.getEdgeWidth() + PADDING, y), "", TextArea.Align.RIGHT);
                int statIndex = i;
                buttons[i].setOnUp(() -> {
                    currentStat = Statistics.Type.values()[statIndex];
                    updateUpperLimit();
                });
            }

            window.setWindowHeight(Math.max(-y, GRAPH_SIZE + PADDING + window.getEdgeWidth()) + PADDING + window.getEdgeWidth());
            window.setLocalPosition(0, -window.getWindowHeight());

            currentStat = Statistics.Type.values()[0];
        }

        private void updateValues() {
            for (int i = 0; i < buttons.length; i++) {
                Statistics.Type statistic = Statistics.Type.values()[i];
                String spacing = "      ";
                spacing = spacing.substring(String.valueOf(statistic.getValue()).length());
                buttons[i].setText(statistic.name().toLowerCase() + spacing + statistic.getValue());
            }
            updateUpperLimit();
        }

        protected void drawGraph(ShapeRenderer renderer) {
            Vector2i origin = graph.getGlobalPosition().plus(11, 7);
            float[] values = Statistics.getValues().get(currentStat);
            Vector2i prevPoint = new Vector2i(290, (int)(values[0] / (upperLimit /300)));
            Vector2i point = new Vector2i();

            for (int i = 1; i < Statistics.VALUES_PER_STAT; i++) {
                point.set(10 * (Statistics.VALUES_PER_STAT - 1 - i), (int)(values[i] / (upperLimit /300)));
                renderer.rectLine(prevPoint.x + origin.x, prevPoint.y + origin.y, point.x + origin.x, point.y + origin.y, 5, graphColor, graphColor);
                prevPoint.set(point);
            }
        }

        private void updateUpperLimit() {
            float max = 0;
            for (float value : Statistics.getValues().get(currentStat)) {
                if (value > max) max = value;
            }
            upperLimit = 10;
            while (upperLimit < max)
                upperLimit *= 10;
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible) {
                updateValues();
                closeInGameMenus();
            }
            super.setVisible(visible);
        }

        @Override
        public void addToUI() {
            super.addToUI();
            window.addToUI();
            graph.addToUI();
            for (Button button : buttons) {
                button.addToUI();
            }
        }
    }

    public static class ResourceList extends UIElement {
        Label[] labels = new Label[Resource.values().length];

        public ResourceList() {
            super(null, Anchor.TOP_LEFT.getElement(), Layer.IN_GAME, new Vector2i(20, -20), true);
            for (int i = 0; i < labels.length; i++) {
                TextureRegion texture = getResourcesTexture(Resource.values()[i]);
                labels[i] = new Label(texture, this, layer, Vector2i.zero());
                labels[i].setVisible(false);
                labels[i].setTexture(getResourcesTexture(Resource.values()[i]));
            }
        }

        public void initData() {
            int y = 0;
            for (int i = 0; i < Resource.values().length; i++) {
                Resource resource = Resource.values()[i];
                if (resource == Resource.NOTHING) continue;
                if (Resource.getStored(resource) > 0) {
                    labels[i].setVisible(true);
                    labels[i].setText(Resource.getStored(resource) + "");
                    labels[i].setLocalPosition(0, y);
                    y -= 25;
                }

            }
        }

        public void updateData(Resource resource) {
            Label label = labels[resource.ordinal()];
            int amount = Resource.getStored(resource);

            label.setText(amount + "");

            label.setVisible(amount > 0);
            organiseLabels();
        }

        private void organiseLabels() {
            int y = 0;
            for (Label label : labels) {
                if (label.isVisible()) {
                    label.setLocalPosition(0, y);
                    y -= 25;
                }
            }
        }

        private TextureRegion getResourcesTexture(Resource resource) {
            String resourceName = resource.toString().toUpperCase();
            return Textures.get(Textures.Resource.valueOf(resourceName));
        }

        @Override
        public void addToUI() {
            super.addToUI();
            for (Label label : labels) {
                label.addToUI();
            }
        }
    }

    private static class FarmResourceMenu extends UIElement {
        private final int DATA_WIDTH = 200;

        private final Window window;
        private final Button[] cropButtons;
        private final Button[] animalButtons;
        private final Button acceptButton;
        private final TextArea descriptionArea;
        private Harvestables.Type currentCrop = null;
        private Animals.Type currentAnimal = null;
        private FarmBuilding<?> building;

        public FarmResourceMenu() {
            super(Anchor.TOP_LEFT.getElement(), Layer.IN_GAME, Vector2i.zero(), false);
            window = new Window(Textures.get(Textures.Ui.WINDOW), this, layer, Vector2i.zero(), true);
            window.setContentWidth(PADDING * 3 + Textures.get(Textures.Ui.SMALL_BUTTON).getRegionWidth() + DATA_WIDTH);

            Harvestables.Type[] cropTypes = Arrays.stream(Harvestables.Type.values())
                    .filter(type -> type.characteristic.equals(Harvestable.Characteristic.FIELD_CROP))
                    .toArray(Harvestables.Type[]::new);

            Animals.Type[] animalTypes = Arrays.stream(Animals.Type.values())
                    .filter(type -> type.growthTime > 0)
                    .toArray(Animals.Type[]::new);

            cropButtons = new Button[cropTypes.length];
            animalButtons = new Button[animalTypes.length];
            acceptButton = new Button(
                    Textures.get(Textures.Ui.SMALL_BUTTON),
                    window,
                    layer,
                    new Vector2i(window.getEdgeWidth() + PADDING * 2 + Textures.get(Textures.Ui.SMALL_BUTTON).getRegionWidth(), window.getEdgeWidth() + PADDING),
                    "accept"
            );
            descriptionArea = new TextArea(
                    "",
                    this,
                    layer,
                    new Vector2i(window.getEdgeWidth() + PADDING * 2 + Textures.get(Textures.Ui.SMALL_BUTTON).getRegionWidth(),-window.getEdgeWidth() - PADDING),
                    DATA_WIDTH, TextArea.Align.LEFT
            );

            for (int i = 0; i < cropTypes.length; i++) {
                cropButtons[i] = new Button(
                        Textures.get(Textures.Ui.SMALL_BUTTON),
                        this,
                        layer,
                        new Vector2i(window.getEdgeWidth() + PADDING, -window.getEdgeWidth() - (PADDING + 32) * (i+1)),
                        cropTypes[i].name().toLowerCase()
                );
                int cropIndex = i;
                cropButtons[i].setOnUp(() -> {
                    currentCrop = cropTypes[cropIndex];
                    setDescription(currentCrop);
                });
            }

            for (int i = 0; i < animalTypes.length; i++) {
                animalButtons[i] = new Button(
                        Textures.get(Textures.Ui.SMALL_BUTTON),
                        this,
                        layer,
                        new Vector2i(window.getEdgeWidth() + PADDING, -window.getEdgeWidth() - (PADDING + 32) * (i+1)),
                        animalTypes[i].name().toLowerCase()
                );
                int animalIndex = i;
                animalButtons[i].setOnUp(() -> {
                    currentAnimal = animalTypes[animalIndex];
                    setDescription(currentAnimal);
                });
            }

            acceptButton.setOnUp(() -> {
                if (building instanceof RanchBuilding)
                    ((RanchBuilding) building).setAnimal(currentAnimal);
                else
                    ((PlantationBuilding) building).setCrop(currentCrop);
                setVisible(false);
            });
        }

        public void show(FarmBuilding<?> farmBuilding) {
            boolean isRanch = farmBuilding instanceof RanchBuilding;

            int height = 32 + PADDING;
            if (isRanch) {
                currentAnimal = ((RanchBuilding) farmBuilding).getAnimal();
                setDescription(currentAnimal);
                height *= animalButtons.length;
            } else {
                currentCrop = ((PlantationBuilding) farmBuilding).getCrop();
                setDescription(currentCrop);
                height *= cropButtons.length;
            }
            window.setContentHeight(Math.max(height + PADDING, 150));
            window.setLocalPosition(0, -window.getWindowHeight());

            for (Button button : cropButtons) {
                button.setVisible(!isRanch);
            }
            for (Button button : animalButtons) {
                button.setVisible(isRanch);
            }
            setVisible(true);

            building = farmBuilding;
        }

        private void setDescription(Harvestables.Type type) {
            descriptionArea.setText(currentCrop.name().toLowerCase() +
                    "\nresource: " + currentCrop.characteristic.resource.name().toLowerCase() +
                    "\nyield: " + currentCrop.yield +
                    "\ngrowth time: " + (currentCrop.growthTime / (float)World.FULL_DAY) + " days");
        }

        private void setDescription(Animals.Type type) {
            descriptionArea.setText(currentAnimal.name().toLowerCase() +
                "\nresource: " + currentAnimal.resource.name().toLowerCase() +
                "\nyield: " + currentAnimal.yield +
                "\ngrowth time: " + (currentAnimal.growthTime / (float)World.FULL_DAY) + " days");
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible)
                closeInGameMenus();
            super.setVisible(visible);
        }

        @Override
        public void addToUI() {
            super.addToUI();
            window.addToUI();
            for (Button button : cropButtons) {
                button.addToUI();
            }
            for (Button button : animalButtons) {
                button.addToUI();
            }
            descriptionArea.addToUI();
            acceptButton.addToUI();
        }
    }

    public static void loadShiftMenuValues() {
        for (int i = 0; i < shiftMenu.types.length; i++) {
            for (int j = 0; j < ProductionBuilding.SHIFTS_PER_JOB; j++) {
                shiftMenu.checkBoxes[j + i * ProductionBuilding.SHIFTS_PER_JOB].setValue(shiftMenu.types[i].getShiftActivity(j));
            }
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface AddToUI {}
}
