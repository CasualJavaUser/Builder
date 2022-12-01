package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.InputManager;
import com.boxhead.builder.Logic;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.Building;
import com.boxhead.builder.game_objects.Buildings;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.utils.Vector2i;

import java.io.File;
import java.util.*;

public class UI {
    public static final Color DEFAULT_COLOR = new Color(1, 1, 1, 1);
    public static final Color SEMI_TRANSPARENT = new Color(1, 1, 1, .5f);
    public static final Color SEMI_TRANSPARENT_RED = new Color(.86f, .25f, .25f, .4f);
    public static final Color SEMI_TRANSPARENT_GREEN = new Color(.25f, .86f, .25f, .4f);
    public static final Color PRESSED_COLOR = new Color(.8f, .8f, .8f, 1);
    public static final Color VERY_TRANSPARENT = new Color(1, 1, 1, .2f);
    public static final Color WHITE = new Color(1, 1, 1, 1);
    public static final Color DARK = new Color(.5f, .5f, .5f, 1);

    public static final BitmapFont FONT = new BitmapFont();
    public static final int FONT_SIZE = 15;

    private static final List<Set<UIElement>> layers = new ArrayList<>();

    private static Button buildingButton, npcButton, workButton, restButton, demolishButton, homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton,
            transportOfficeButton, pauseButton, playButton, x2Button, x3Button, resumeButton, loadButton, saveButton, quitButton;

    private static UIElement buildingButtonGroup, mainButtonGroup, timeElementGroup, pauseMenu;

    private static Window pauseMenuWindow, saveWindow;

    private static NPCStatWindow npcStatWindow;
    private static BuildingStatWindow buildingStatWindow;

    private static ResourceList resourceList;
    private static Clock clock;

    private static Clickable clickedElement = null;

    private static final int PAUSE_MENU_LAYERS = 3, SAVE_MENU_LAYER = 1;

    public static void init() {
        pauseMenu = new UIElement(null, new Vector2i(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2), false);
        mainButtonGroup = new UIElement(null, new Vector2i(0, 10), true);
        buildingButtonGroup = new UIElement(null, new Vector2i(0, 84), false);
        timeElementGroup = new UIElement(null, new Vector2i(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), true);

        //region pauseMenu
        int menuWidth = 120, menuHeight = 163;
        pauseMenuWindow = new Window(Textures.get(Textures.Ui.WINDOW), pauseMenu, new Vector2i());
        pauseMenuWindow.setWidth(menuWidth);
        pauseMenuWindow.setHeight(menuHeight);
        pauseMenuWindow.setLocalPosition(-pauseMenuWindow.getWindowWidth() / 2, -pauseMenuWindow.getWindowHeight() / 2);
        pauseMenuWindow.setTint(WHITE);

        resumeButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, new Vector2i(-40, 40), "Resume", () -> showPauseMenu(false));
        loadButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, new Vector2i(-40, 3), "Load", UI::showLoadMenu);
        saveButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, new Vector2i(-40, -34), "Save", UI::showSaveMenu);
        quitButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, new Vector2i(-40, -71), "Quit", () -> Gdx.app.exit());

        resumeButton.setTint(WHITE);
        loadButton.setTint(WHITE);
        saveButton.setTint(WHITE);
        quitButton.setTint(WHITE);
        //endregion

        //region saveWindow
        saveWindow = new Window(Textures.get(Textures.Ui.WINDOW), pauseMenu, new Vector2i(), false);
        saveWindow.setWindowWidth(280);
        saveWindow.setHeight(300);
        saveWindow.setLocalPosition(-saveWindow.getWindowWidth() / 2, -saveWindow.getWindowHeight() / 2);
        saveWindow.setTint(WHITE);
        //endregion

        //region mainButtonGroup
        buildingButton = new Button(Textures.get(Textures.Ui.HOUSE), mainButtonGroup, new Vector2i(10, 0),
                () -> buildingButtonGroup.setVisible(!buildingButtonGroup.isVisible()));
        npcButton = new Button(Textures.get(Textures.Ui.NPC), mainButtonGroup, new Vector2i(84, 0),
                () -> {
                    Vector2i position = new Vector2i(World.getGridWidth() / 2, World.getGridHeight() / 2);
                    World.spawnNPC(new NPC(Textures.get(Textures.Npc.FUNGUY), position));
                });

        workButton = new Button(Textures.get(Textures.Ui.WORK), mainButtonGroup, new Vector2i(158, 0),
                () -> World.setTime(25170));
        restButton = new Button(Textures.get(Textures.Ui.REST), mainButtonGroup, new Vector2i(232, 0),
                () -> World.setTime(57570));
        demolishButton = new Button(Textures.get(Textures.Ui.DEMOLISH), mainButtonGroup, new Vector2i(306, 0),
                Buildings::switchDemolishingMode);
        //endregion

        //region buildingButtonGroup
        homeButton = new Button(Textures.get(Textures.Ui.HOME), buildingButtonGroup, new Vector2i(10, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_RESIDENTIAL_BUILDING);
                    buildingButtonGroup.setVisible(false);
                }, false);
        workplaceButton = new Button(Textures.get(Textures.Ui.WORKPLACE), buildingButtonGroup, new Vector2i(84, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_PRODUCTION_BUILDING);
                    buildingButtonGroup.setVisible(false);
                }, false);
        serviceButton = new Button(Textures.get(Textures.Ui.SERVICE), buildingButtonGroup, new Vector2i(158, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_SERVICE_BUILDING);
                    buildingButtonGroup.setVisible(false);
                }, false);
        storageButton = new Button(Textures.get(Textures.Ui.STORAGE), buildingButtonGroup, new Vector2i(232, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_STORAGE_BUILDING);
                    buildingButtonGroup.setVisible(false);
                }, false);
        constructionOfficeButton = new Button(Textures.get(Textures.Ui.CONSTRUCTION_OFFICE), buildingButtonGroup, new Vector2i(306, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.CONSTRUCTION_OFFICE);
                    buildingButtonGroup.setVisible(false);
                }, false);
        transportOfficeButton = new Button(Textures.get(Textures.Ui.CONSTRUCTION_OFFICE), buildingButtonGroup, new Vector2i(380, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.TRANSPORT_OFFICE);
                    buildingButtonGroup.setVisible(false);
                }, false);
        //endregion

        //region timeElementGroup
        TextureRegion clockTexture = Textures.get(Textures.Ui.CLOCK_FACE);
        clock = new Clock(timeElementGroup,
                new Vector2i(-clockTexture.getRegionWidth() - 10, -clockTexture.getRegionHeight() - 10));

        pauseButton = new Button(
                Textures.get(Textures.Ui.PAUSE),
                timeElementGroup,
                new Vector2i(-clockTexture.getRegionWidth() - 9, -clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(0), true);

        playButton = new Button(
                Textures.get(Textures.Ui.PLAY),
                timeElementGroup,
                new Vector2i(-clockTexture.getRegionWidth() + 23, -clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(Logic.NORMAL_SPEED), true);

        x2Button = new Button(
                Textures.get(Textures.Ui.X2SPEED),
                timeElementGroup,
                new Vector2i(-clockTexture.getRegionWidth() + 55, -clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(Logic.SPEED_X2), true);

        x3Button = new Button(
                Textures.get(Textures.Ui.X3SPEED),
                timeElementGroup,
                new Vector2i(-clockTexture.getRegionWidth() + 87, -clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(Logic.SPEED_X3), true);
        //endregion

        npcStatWindow = new NPCStatWindow();
        buildingStatWindow = new BuildingStatWindow();

        resourceList = new ResourceList();

        layers.add(new HashSet<>());
        layers.add(new HashSet<>(Arrays.asList(saveWindow)));
        layers.add(new HashSet<>(Arrays.asList(resumeButton, loadButton, saveButton, quitButton)));
        layers.add(new HashSet<>(Arrays.asList(pauseMenuWindow)));
        layers.add(new HashSet<>(Arrays.asList(npcStatWindow, buildingStatWindow)));
        layers.add(new HashSet<>(Arrays.asList(buildingButton, npcButton, workButton, restButton, demolishButton,
                homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton, transportOfficeButton,
                clock, pauseButton, playButton, x2Button, x3Button, resourceList)));
    }

    public static void drawUI(SpriteBatch batch) {
        for (int i = layers.size() - 1; i >= 0; i--) {
            for (UIElement element : layers.get(i)) {
                if (element.isVisible()) {
                    element.draw(batch);
                }
            }
        }
    }

    public static boolean handleMouseInput() {
        int topLayers = UI.layers.size();
        if (Logic.isPaused()) topLayers = PAUSE_MENU_LAYERS;
        boolean interacted = false;

        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) interacted = UI.onClick(topLayers);
        else if (clickedElement != null) {
            if (InputManager.isButtonDown(InputManager.LEFT_MOUSE)) interacted = UI.onHold();
            if (InputManager.isButtonUp(InputManager.LEFT_MOUSE)) {
                interacted = UI.onUp();
                clickedElement = null;
            }
        }

        return interacted;
    }

    /**
     * Checks if any element in the first layers has been clicked. If so, the onClick method is invoked on the clicked element.
     * @param topLayers the number of top layers in which the clicked element is searched for
     * @return true if an element has been clicked
     */
    private static boolean onClick(int topLayers) {
        if (topLayers > layers.size()) topLayers = layers.size();
        for (int i = 0; i < topLayers; i++) {
            for (UIElement element : layers.get(i)) {
                if (element.isVisible() && element instanceof Clickable && ((Clickable) element).isMouseOver()) {
                    ((Clickable) element).onClick();
                    clickedElement = (Clickable) element;
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

    public static void resizeUI() {
        timeElementGroup.setGlobalPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        resourceList.setGlobalPosition(20, Gdx.graphics.getHeight() - 20);
        pauseMenu.setGlobalPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    }

    public static void showNPCStatWindow(NPC npc) {
        npcStatWindow.show(npc);
    }

    public static void showBuildingStatWindow(Building building) {
        buildingStatWindow.show(building);
    }

    public static void onEscape() {
        if (!Logic.isPaused()) {
            if (Buildings.isInBuildingMode()) {
                Buildings.turnOffBuildingMode();
            } else if (buildingStatWindow.isVisible() || npcStatWindow.isVisible()) {
                buildingStatWindow.setVisible(false);
                npcStatWindow.setVisible(false);
            } else if (buildingButtonGroup.isVisible()) {
                buildingButtonGroup.setVisible(false);
            } else {
                showPauseMenu(true);
            }
        } else {
            for (UIElement element : layers.get(SAVE_MENU_LAYER)) {
                if (element.isVisible()) {
                    element.setVisible(false);
                    return;
                }
            }
            showPauseMenu(false);
        }
    }

    private static void showPauseMenu(boolean open) {
        layers.get(SAVE_MENU_LAYER - 1).clear();

        Logic.pause(open);
        DEFAULT_COLOR.set(open ? DARK : WHITE);
        pauseMenu.setVisible(open);
    }

    private static void showLoadMenu() {
        saveWindow.setVisible(true);
        TextureRegion texture = Textures.get(Textures.Ui.WIDE_AREA);
        Vector2i buttonPos = new Vector2i(20, saveWindow.getWindowHeight() - 84);

        File file = new File(System.getenv("APPDATA") + "/../LocalLow/Box Head/");
        File[] saves = file.listFiles();

        if (saves == null) {
            Button button = new Button(texture, saveWindow, buttonPos, "No saves", () -> {});
            button.setTint(WHITE);
            layers.get(0).add(button);
        }
        else {
            for (File save : saves) {
                if(save.isFile() && save.getName().substring(save.getName().lastIndexOf(".")).equals(".save")) {
                    Button button = new Button(texture, saveWindow, buttonPos.clone(), save.getName().substring(0, save.getName().lastIndexOf(".")),
                            () -> {});  //TODO load from file
                    button.setTint(WHITE);
                    layers.get(0).add(button);
                    buttonPos.set(buttonPos.x, buttonPos.y - texture.getRegionHeight() - 10);
                }
            }
        }
    }

    public static void showSaveMenu() {
        layers.get(SAVE_MENU_LAYER - 1).clear();

        saveWindow.setVisible(true);
        TextureRegion texture = Textures.get(Textures.Ui.WIDE_AREA);
        Vector2i buttonPos = new Vector2i(20, saveWindow.getWindowHeight() - 84);

        File file = new File(System.getenv("APPDATA") + "/../LocalLow/Box Head/");
        File[] saves = file.listFiles();
        if (saves == null) saves = new File[0];

        Button firstButton = new Button(texture, saveWindow, buttonPos.clone(), "New save",
                () -> {});
        buttonPos.set(buttonPos.x, buttonPos.y - texture.getRegionHeight() - 10);
        firstButton.setTint(WHITE);
        layers.get(0).add(firstButton);

        for (File save : saves) {
            if (save.isFile() && save.getName().substring(save.getName().lastIndexOf(".")).equals(".save")) {
                Button button = new Button(texture, saveWindow, buttonPos.clone(), save.getName().substring(0, save.getName().lastIndexOf(".")),
                        () -> {
                        });  //TODO save to file
                button.setTint(WHITE);
                layers.get(0).add(button);
                buttonPos.set(buttonPos.x, buttonPos.y - texture.getRegionHeight() - 10);
            }
        }
    }
}
