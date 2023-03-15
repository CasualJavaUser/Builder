package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Building;
import com.boxhead.builder.game_objects.Buildings;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.ui.popup.Popups;
import com.boxhead.builder.utils.Vector2i;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class UI {
    private static final Matrix4 uiProjection = new Matrix4();

    public static final Color DEFAULT_COLOR = new Color(1, 1, 1, 1);
    public static final Color SEMI_TRANSPARENT = new Color(1, 1, 1, .5f);
    public static final Color SEMI_TRANSPARENT_RED = new Color(.86f, .25f, .25f, .4f);
    public static final Color SEMI_TRANSPARENT_GREEN = new Color(.25f, .86f, .25f, .4f);
    public static final Color PRESSED_COLOR = new Color(.8f, .8f, .8f, 1);
    public static final Color VERY_TRANSPARENT = new Color(1, 1, 1, .2f);
    public static final Color WHITE = new Color(1, 1, 1, 1);
    public static final Color DARK = new Color(.5f, .5f, .5f, 1);

    public static BitmapFont FONT;
    private static TextField activeTextField = null;
    private static Button activeButton = null;
    private static ScrollPane activeScrollPane = null;
    private static Clickable clickedElement = null;
    private static final Set<UIElement> saveWindowElements = new HashSet<>();

    @AddToUI private static Button buildMenuButton, npcButton, workButton, restButton, demolishButton, tilingButton, pauseGameButton;

    @AddToUI private static UIElement timeElementGroup;
    @AddToUI private static Clock clock;
    @AddToUI private static Button pauseButton, playButton, x2Button, x3Button;

    @AddToUI private static ResourceList resourceList;

    @AddToUI private static Window buildingWindow;
    @AddToUI private static TextArea buildWindowDivider;
    @AddToUI private static UIElement buildingImage;
    @AddToUI private static Button buildButton;
    @AddToUI private static TextArea buildingDescription;

    //infrastructure tab
    @AddToUI private static Button infrastructureTabButton;
    @AddToUI private static UIElement infrastructureTab;
    @AddToUI private static Button storageButton, constructionOfficeButton, transportOfficeButton;

    //housing tab
    @AddToUI private static Button housingTabButton;
    @AddToUI private static UIElement housingTab;
    @AddToUI private static Button logCabinButton;

    //resource tab
    @AddToUI private static Button resourcesTabButton;
    @AddToUI private static UIElement resourcesTab;
    @AddToUI private static Button lumberjackButton, mineButton, stoneGathererButton, plantationButton, ranchButton;

    //services tab
    @AddToUI private static Button servicesTabButton;
    @AddToUI private static UIElement servicesTab;
    @AddToUI private static Button serviceButton, pubButton;

    @AddToUI private static Window pauseWindow;
    @AddToUI private static Button resumeButton, loadButton, saveButton, settingsButton, quitButton;

    @AddToUI private static Window saveWindow;
    @AddToUI private static TextArea saveText;
    @AddToUI private static ScrollPane scrollPane;
    @AddToUI private static Button saveWindowBackButton;

    @AddToUI private static Window settingsWindow;
    @AddToUI private static TextArea settingsText;
    @AddToUI private static Button settingsWindowBackButton;

    @AddToUI private static NPCStatWindow npcStatWindow;
    @AddToUI private static BuildingStatWindow buildingStatWindow;

    public static final int PADDING = 10;
    private static boolean isPaused = false;

    public enum Anchor {
        CENTER,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        TOP_LEFT,
        TOP_RIGHT;

        private final UIElement element;

        Anchor() {
            this.element = new UIElement();
        }

        public UIElement getElement() {
            return element;
        }
    }

    public enum Layer {
        BUILDINGS(true),
        IN_GAME(true),
        BUILDING_MENU(false),
        PAUSE_MENU(false),
        SAVE_MENU(false),
        SETTINGS_MENU(false),
        POPUP(false);

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

        //region pauseMenu
        {
            int menuWidth = 200, menuHeight = 380;
            pauseWindow = new Window(Textures.get(Textures.Ui.MENU_WINDOW), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i());
            pauseWindow.setContentWidth(menuWidth);
            pauseWindow.setContentHeight(menuHeight);
            pauseWindow.setLocalPosition(-pauseWindow.getWindowWidth() / 2, -pauseWindow.getWindowHeight() / 2);
            pauseWindow.setTint(WHITE);

            int x = -Textures.get(Textures.Ui.BIG_BUTTON).getRegionWidth() / 2;
            resumeButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, 116), "Resume");
            loadButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, 42), "Load");
            saveButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, -32), "Save");
            settingsButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, -106), "Settings");
            quitButton = new Button(Textures.get(Textures.Ui.BIG_BUTTON), Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(x, -180), "Quit");

            resumeButton.setOnUp(() -> showPauseMenu(false));
            loadButton.setOnUp(UI::showLoadMenu);
            saveButton.setOnUp(UI::showSaveMenu);
            settingsButton.setOnUp(() -> Layer.SETTINGS_MENU.setVisible(true));
            quitButton.setOnUp(() -> Gdx.app.exit());

            resumeButton.setTint(WHITE);
            loadButton.setTint(WHITE);
            saveButton.setTint(WHITE);
            settingsButton.setTint(WHITE);
            quitButton.setTint(WHITE);
        }
        //endregion

        //region saveWindow
        saveWindow = new Window(Textures.get(Textures.Ui.MENU_WINDOW), Anchor.CENTER.getElement(), Layer.SAVE_MENU, new Vector2i());
        saveWindow.setContentWidth(500);
        saveWindow.setContentHeight(500);
        saveWindow.setLocalPosition(-saveWindow.getWindowWidth() / 2, -saveWindow.getWindowHeight() / 2);

        saveText = new TextArea("Save", saveWindow, Layer.SAVE_MENU, new Vector2i(0, saveWindow.getWindowHeight() - saveWindow.getEdgeWidth() - PADDING), saveWindow.getWindowWidth(), true);

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

            settingsText = new TextArea("Settings", settingsWindow, Layer.SETTINGS_MENU, new Vector2i(0, settingsWindow.getWindowHeight() - 25), settingsWindow.getWindowWidth(), true);

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
            buildMenuButton = new Button(Textures.get(Textures.Ui.HAMMER), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x, PADDING));
            npcButton = new Button(Textures.get(Textures.Ui.NPC), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            workButton = new Button(Textures.get(Textures.Ui.WORK), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            restButton = new Button(Textures.get(Textures.Ui.REST), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            demolishButton = new Button(Textures.get(Textures.Ui.DEMOLISH), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));
            tilingButton = new Button(Textures.get(Textures.Ui.FUNGUS), Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(x += 74, PADDING));

            pauseGameButton = new Button(Textures.get(Textures.Ui.PAUSE_GAME), Anchor.BOTTOM_RIGHT.getElement(), Layer.IN_GAME, new Vector2i(-48 - PADDING, PADDING));

            buildMenuButton.setOnUp(() -> Layer.BUILDING_MENU.setVisible(!Layer.BUILDING_MENU.isVisible()));

            npcButton.setOnUp(() -> {
                Vector2i position = new Vector2i(World.getGridWidth() / 2, World.getGridHeight() / 2);
                World.spawnVillager(new Villager((int)(Math.random()*2), position));
            });

            workButton.setOnUp(() -> {
                World.setDay(World.getDay()+1);
                World.setTime(25170);
            });
            restButton.setOnUp(() -> {
                World.setDay(World.getDay()+1);
                World.setTime(57570);
            });
            demolishButton.setOnUp(() -> {
                if (Buildings.isInDemolishingMode()) Buildings.turnOffDemolishingMode();
                else Buildings.toDemolishingMode();
            });
            tilingButton.setOnUp(() -> Tiles.toTilingMode(Tiles.TilingMode.SINGLE));
            pauseGameButton.setOnUp(() -> showPauseMenu(true));
        }
        //endregion

        //region buildingWindow
        {
            buildingWindow = new Window(Textures.get(Textures.Ui.WINDOW), Anchor.CENTER.getElement(), Layer.BUILDING_MENU, new Vector2i(), true);
            buildingWindow.setContentWidth(454);
            buildingWindow.setContentHeight(400);
            buildingWindow.setLocalPosition(-buildingWindow.getWindowWidth() / 2, -buildingWindow.getWindowHeight() / 2);

            buildWindowDivider = new TextArea(Textures.get(Textures.Ui.DIVIDER), "", buildingWindow, Layer.BUILDING_MENU, new Vector2i(), true);
            buildWindowDivider.setLocalPosition(
                    buildingWindow.getTexture().getRegionWidth() + PADDING,
                    buildingWindow.getTexture().getRegionHeight() + 128 + PADDING * 2
            );

            buildingImage = new UIElement(null, buildingWindow, Layer.BUILDING_MENU, new Vector2i(buildingWindow.getEdgeWidth() + PADDING, buildingWindow.getEdgeWidth() + PADDING));

            buildingDescription = new TextArea("", buildingWindow, Layer.BUILDING_MENU, new Vector2i(), 200, false);

            buildButton = new Button(
                    Textures.get(Textures.Ui.BUILD),
                    buildingWindow, Layer.BUILDING_MENU,
                    new Vector2i(buildingWindow.getWindowWidth() - buildingWindow.getEdgeWidth() - 64 - PADDING, buildingWindow.getEdgeWidth() + PADDING));
            buildButton.setVisible(false);

            int tabX = PADDING;

            //region infrastructureTab
            {
                infrastructureTabButton = new Button(Textures.get(Textures.Ui.INFRASTRUCTURE_TAB), buildingWindow, Layer.BUILDING_MENU, new Vector2i(tabX, buildingWindow.getWindowHeight()-5));
                infrastructureTabButton.setOnUp(() -> setVisibleTab(0));

                infrastructureTab = new UIElement(
                        buildingWindow,
                        Layer.BUILDING_MENU,
                        new Vector2i(buildingWindow.getEdgeWidth() + PADDING, buildingWindow.getWindowHeight() - buildingWindow.getEdgeWidth() - PADDING),
                        true);

                int x = 0;
                int y = -64;

                storageButton = new Button(Textures.get(Textures.Ui.BARN), infrastructureTab, Layer.BUILDING_MENU, new Vector2i(x, y));
                constructionOfficeButton = new Button(Textures.get(Textures.Ui.BIG_HAMMER), infrastructureTab, Layer.BUILDING_MENU, new Vector2i(x += 74, y));
                transportOfficeButton = new Button(Textures.get(Textures.Ui.CARRIAGE), infrastructureTab, Layer.BUILDING_MENU, new Vector2i(x + 74, y));

                storageButton.setOnUp(() -> showBuildingStats(Buildings.Type.STORAGE_BARN));
                constructionOfficeButton.setOnUp(() -> showBuildingStats(Buildings.Type.BUILDERS_HUT));
                transportOfficeButton.setOnUp(() -> showBuildingStats(Buildings.Type.TRANSPORT_OFFICE));
            }
            //endregion

            //region housingTab
            {
                housingTabButton = new Button(Textures.get(Textures.Ui.HOUSING_TAB), buildingWindow, Layer.BUILDING_MENU, new Vector2i(tabX += 32 + PADDING, buildingWindow.getWindowHeight()-5));
                housingTabButton.setOnUp(() -> setVisibleTab(1));
                housingTabButton.setTint(PRESSED_COLOR);

                housingTab = new UIElement(
                        buildingWindow,
                        Layer.BUILDING_MENU,
                        new Vector2i(buildingWindow.getEdgeWidth() + PADDING, buildingWindow.getWindowHeight() - buildingWindow.getEdgeWidth() - PADDING),
                        false);

                int x = 0;
                int y = -64;

                logCabinButton = new Button(Textures.get(Textures.Ui.HOUSE), housingTab, Layer.BUILDING_MENU, new Vector2i(x, y));

                logCabinButton.setOnUp(() -> showBuildingStats(Buildings.Type.LOG_CABIN));
            }
            //endregion

            //region resourcesTab
            {
                resourcesTabButton = new Button(Textures.get(Textures.Ui.RESOURCES_TAB), buildingWindow, Layer.BUILDING_MENU, new Vector2i(tabX += 32 + PADDING, buildingWindow.getWindowHeight()-5));
                resourcesTabButton.setOnUp(() -> setVisibleTab(2));
                resourcesTabButton.setTint(PRESSED_COLOR);

                resourcesTab = new UIElement(
                        buildingWindow,
                        Layer.BUILDING_MENU,
                        new Vector2i(buildingWindow.getEdgeWidth() + PADDING, buildingWindow.getWindowHeight() - buildingWindow.getEdgeWidth() - PADDING),
                        false);

                int x = 0;
                int y = -64;

                lumberjackButton = new Button(Textures.get(Textures.Ui.AXE), resourcesTab, Layer.BUILDING_MENU, new Vector2i(x, y));
                mineButton = new Button(Textures.get(Textures.Ui.PICKAXE), resourcesTab, Layer.BUILDING_MENU, new Vector2i(x += 74, y));
                stoneGathererButton = new Button(Textures.get(Textures.Ui.PICKAXE_WITH_STONE), resourcesTab, Layer.BUILDING_MENU, new Vector2i(x += 74, y));
                plantationButton = new Button(Textures.get(Textures.Building.TOOL_SHACK), resourcesTab, Layer.BUILDING_MENU, new Vector2i(x += 74, y));
                ranchButton = new Button(Textures.get(Textures.Building.TOOL_SHACK), resourcesTab, Layer.BUILDING_MENU, new Vector2i(x += 74, y));

                lumberjackButton.setOnUp(() -> showBuildingStats(Buildings.Type.LUMBERJACKS_HUT));
                mineButton.setOnUp(() -> showBuildingStats(Buildings.Type.MINE));
                stoneGathererButton.setOnUp(() -> showBuildingStats(Buildings.Type.STONE_GATHERERS));
                plantationButton.setOnUp(() -> showBuildingStats(Buildings.Type.PLANTATION));
                ranchButton.setOnUp(() -> showBuildingStats(Buildings.Type.RANCH));
            }
            //endregion

            //region serviceTab
            {
                servicesTabButton = new Button(Textures.get(Textures.Ui.SERVICES_TAB), buildingWindow, Layer.BUILDING_MENU, new Vector2i(tabX += 32 + PADDING, buildingWindow.getWindowHeight()-5));
                servicesTabButton.setOnUp(() -> setVisibleTab(3));
                servicesTabButton.setTint(PRESSED_COLOR);

                servicesTab = new UIElement(
                        buildingWindow,
                        Layer.BUILDING_MENU,
                        new Vector2i(buildingWindow.getEdgeWidth() + PADDING, buildingWindow.getWindowHeight() - buildingWindow.getEdgeWidth() - PADDING),
                        false);

                int x = 0;
                int y = -64;

                serviceButton = new Button(Textures.get(Textures.Ui.SERVICE), servicesTab, Layer.BUILDING_MENU, new Vector2i(x, y));
                pubButton = new Button(Textures.get(Textures.Ui.SERVICE), servicesTab, Layer.BUILDING_MENU, new Vector2i(x += 74, y));

                serviceButton.setOnUp(() -> showBuildingStats(Buildings.Type.DEFAULT_SERVICE_BUILDING));
                pubButton.setOnUp(() -> showBuildingStats(Buildings.Type.PUB));
            }
            //endregion
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

        npcStatWindow = new NPCStatWindow(Layer.IN_GAME);
        buildingStatWindow = new BuildingStatWindow(Layer.IN_GAME);

        resourceList = new ResourceList(Anchor.TOP_LEFT.getElement(), Layer.IN_GAME, new Vector2i(20, -20));

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

        Layer.BUILDING_MENU.addElement(buildMenuButton);
        Layer.BUILDING_MENU.addElement(npcButton);
        Layer.BUILDING_MENU.addElement(workButton);
        Layer.BUILDING_MENU.addElement(restButton);
        Layer.BUILDING_MENU.addElement(demolishButton);
    }

    public static void drawUI(SpriteBatch batch, OrthographicCamera camera) {
        updateProjectionMatrix(camera);
        batch.setProjectionMatrix(uiProjection);
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

    public static void drawPopups(SpriteBatch batch, OrthographicCamera camera) {
        updateProjectionMatrix(camera);
        batch.setProjectionMatrix(uiProjection);
        if (Popups.getActivePopup() != null) Popups.getActivePopup().draw(batch);
        batch.setProjectionMatrix(camera.combined);
    }

    private static void updateProjectionMatrix(OrthographicCamera camera) {
        uiProjection.setToScaling(camera.combined.getScaleX() * camera.zoom, camera.combined.getScaleY() * camera.zoom, 0);
        uiProjection.setTranslation(-1, -1, 0);
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
        if (!isPaused) {
            if (Layer.BUILDING_MENU.isVisible()) {
                Layer.BUILDING_MENU.setVisible(false);
            }
            else if (Buildings.isInBuildingMode() || Buildings.isInDemolishingMode() || Tiles.isInTilingMode()) {
                Buildings.turnOffBuildingMode();
                Buildings.turnOffDemolishingMode();
                Tiles.turnOffTilingMode();
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

    private static void setVisibleTab(int index) {
        infrastructureTab.setVisible(index == 0);
        housingTab.setVisible(index == 1);
        resourcesTab.setVisible(index == 2);
        servicesTab.setVisible(index == 3);

        infrastructureTabButton.setTint(index == 0 ? DEFAULT_COLOR : PRESSED_COLOR);
        housingTabButton.setTint(index == 1 ? DEFAULT_COLOR : PRESSED_COLOR);
        resourcesTabButton.setTint(index == 2 ? DEFAULT_COLOR : PRESSED_COLOR);
        servicesTabButton.setTint(index == 3 ? DEFAULT_COLOR : PRESSED_COLOR);
    }

    private static void showPauseMenu(boolean open) {
        Logic.pause(open);
        isPaused = open;
        DEFAULT_COLOR.set(open ? DARK : WHITE);
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
            TextArea textArea = new TextArea(Textures.get(Textures.Ui.WIDE_AREA), "No saves", scrollPane, Layer.SAVE_MENU, new Vector2i(), true);
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
                true);

        Button saveButton = new Button(Textures.get(isSaving ? Textures.Ui.SAVE : Textures.Ui.LOAD), area, Layer.SAVE_MENU, new Vector2i(PADDING, PADDING));

                saveButton.setOnUp(() -> {
                    if(isSaving) {
                        Popups.showPopup("Override save?", () -> {
                            BuilderGame.saveToFile(saveFile);
                            Layer.SAVE_MENU.setVisible(false);
                        });
                    }
                    else {
                        BuilderGame.loadFromFile(saveFile);
                        Layer.SAVE_MENU.setVisible(false);
                    }
                });

        Button deleteButton = new Button(Textures.get(Textures.Ui.DELETE), area, Layer.SAVE_MENU, new Vector2i(PADDING * 2 + 64, PADDING));

                deleteButton.setOnUp(() -> {
                    Popups.showPopup("Delete file?", () -> {
                        saveFile.delete();
                        if(isSaving) showSaveMenu();
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

        textArea.setScissors(area.getScissors());
        saveButton.setScissors(area.getScissors());
        deleteButton.setScissors(area.getScissors());
    }

    private static void showBuildingStats(Buildings.Type building) {
        float scale = 128f / building.getTexture().getRegionHeight();
        if (building.getTexture().getRegionWidth() * scale > 192) scale = 192f / building.getTexture().getRegionWidth();
        buildingImage.setScale(scale);
        buildingImage.setTexture(building.getTexture());
        buildWindowDivider.setText(building.name);

        String description = "";
        description += "build cost:\n" + building.buildCost;
        if (building.job != null && building.range != 0) description += "\n\nrange: " + building.range;

        buildingDescription.setText(description);
        buildingDescription.setLocalPosition(
                buildingImage.getLocalPosition().x + (int)(buildingImage.getWidth() * scale) + PADDING,
                buildingImage.getLocalPosition().y + (int)(buildingImage.getHeight() * scale)
                );

        buildButton.setOnUp(() -> {
            Buildings.toBuildingMode(building);
            Layer.BUILDING_MENU.setVisible(false);
        });

        buildButton.setVisible(true);
    }

    public static ResourceList getResourceList() {
        return resourceList;
    }
}
