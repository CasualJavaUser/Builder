package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Logic;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.Building;
import com.boxhead.builder.game_objects.Buildings;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.utils.Vector2i;

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
                          pauseButton, playButton, x2Button, x3Button, resumeButton, loadButton, saveButton, quitButton;

    private static UIElement buildingMenu, mainMenu, timeMenu, pauseMenu;

    private static Window pauseMenuWindow;

    private static NPCStatWindow npcStatWindow;
    private static BuildingStatWindow buildingStatWindow;

    private static ResourceList resourceList;
    private static Clock clock;

    public static void init() {
        pauseMenu = new UIElement(null, new Vector2i(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2), false);
        mainMenu = new UIElement(null, new Vector2i(0, 10), true);
        buildingMenu = new UIElement(null, new Vector2i(0, 84), false);
        timeMenu = new UIElement(null, new Vector2i(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), true);

        //region pauseMenu
        int menuWidth = 120, menuHeight = 163;
        pauseMenuWindow = new Window(Textures.get(Textures.Ui.WINDOW), pauseMenu, new Vector2i());
        pauseMenuWindow.setWidth(menuWidth);
        pauseMenuWindow.setHeight(menuHeight);
        pauseMenuWindow.setLocalPosition(-pauseMenuWindow.getWindowWidth()/2, -pauseMenuWindow.getWindowHeight()/2);
        pauseMenuWindow.setTint(WHITE);

        resumeButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, new Vector2i(-40, 40), "Resume",
                () -> showPauseMenu(false));
        loadButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, new Vector2i(-40, 3), "Load",
                () -> {});
        saveButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, new Vector2i(-40, -34), "Save",
                () -> {});
        quitButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, new Vector2i(-40, -71), "Quit",
                () -> Gdx.app.exit());

        resumeButton.setTint(WHITE);
        loadButton.setTint(WHITE);
        saveButton.setTint(WHITE);
        quitButton.setTint(WHITE);
        //endregion

        //region mainMenu
        buildingButton = new Button(Textures.get(Textures.Ui.HOUSE), mainMenu, new Vector2i(10, 0),
                () -> buildingMenu.setVisible(!buildingMenu.isVisible()));
        npcButton = new Button(Textures.get(Textures.Ui.NPC), mainMenu, new Vector2i(84, 0),
                () -> {
                    Vector2i position = new Vector2i(World.getGridWidth() / 2, World.getGridHeight() / 2);
                    World.spawnNPC(new NPC(Textures.get(Textures.Npc.FUNGUY), position));
                });

        workButton = new Button(Textures.get(Textures.Ui.WORK), mainMenu, new Vector2i(158, 0),
                () -> World.setTime(25170));
        restButton = new Button(Textures.get(Textures.Ui.REST), mainMenu, new Vector2i(232, 0),
                () -> World.setTime(57570));
        demolishButton = new Button(Textures.get(Textures.Ui.DEMOLISH), mainMenu, new Vector2i(306, 0),
                Buildings::switchDemolishingMode);
        //endregion

        //region buildingMenu
        homeButton = new Button(Textures.get(Textures.Ui.HOME), buildingMenu, new Vector2i(10, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_RESIDENTIAL_BUILDING);
                    buildingMenu.setVisible(false);
                }, false);
        workplaceButton = new Button(Textures.get(Textures.Ui.WORKPLACE), buildingMenu, new Vector2i(84, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_PRODUCTION_BUILDING);
                    buildingMenu.setVisible(false);
                }, false);
        serviceButton = new Button(Textures.get(Textures.Ui.SERVICE), buildingMenu, new Vector2i(158, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_SERVICE_BUILDING);
                    buildingMenu.setVisible(false);
                }, false);
        storageButton = new Button(Textures.get(Textures.Ui.STORAGE), buildingMenu, new Vector2i(232, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_STORAGE_BUILDING);
                    buildingMenu.setVisible(false);
                }, false);
        constructionOfficeButton = new Button(Textures.get(Textures.Ui.CONSTRUCTION_OFFICE), buildingMenu, new Vector2i(306, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.CONSTRUCTION_OFFICE);
                    buildingMenu.setVisible(false);
                }, false);
        //endregion

        //region timeMenu
        TextureRegion clockTexture = Textures.get(Textures.Ui.CLOCK_FACE);
        clock = new Clock(timeMenu,
                new Vector2i(- clockTexture.getRegionWidth() - 10, - clockTexture.getRegionHeight() - 10));

        pauseButton = new Button(
                Textures.get(Textures.Ui.PAUSE),
                timeMenu,
                new Vector2i(- clockTexture.getRegionWidth() - 9, - clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(0), true);

        playButton = new Button(
                Textures.get(Textures.Ui.PLAY),
                timeMenu,
                new Vector2i(- clockTexture.getRegionWidth() + 23, - clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(Logic.NORMAL_SPEED), true);

        x2Button = new Button(
                Textures.get(Textures.Ui.X2SPEED),
                timeMenu,
                new Vector2i(- clockTexture.getRegionWidth() + 55, - clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(Logic.SPEED_X2), true);

        x3Button= new Button(
                Textures.get(Textures.Ui.X3SPEED),
                timeMenu,
                new Vector2i(- clockTexture.getRegionWidth() + 87, - clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(Logic.SPEED_X3), true);
        //endregion

        npcStatWindow = new NPCStatWindow();
        buildingStatWindow = new BuildingStatWindow();

        resourceList = new ResourceList();

        layers.add(new HashSet<>(Arrays.asList(resumeButton, loadButton, saveButton, quitButton)));
        layers.add(new HashSet<>(Arrays.asList(pauseMenuWindow)));
        layers.add(new HashSet<>(Arrays.asList(npcStatWindow, buildingStatWindow)));
        layers.add(new HashSet<>(Arrays.asList(buildingButton, npcButton, workButton, restButton, demolishButton,
                homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton,
                clock, pauseButton, playButton, x2Button, x3Button, resourceList)));
    }

    public static void drawUI(SpriteBatch batch) {
        for (int i = layers.size()-1; i >= 0; i--) {
            for (UIElement element : layers.get(i)) {
                if (element.isVisible()) {
                    element.draw(batch);
                }
            }
        }
    }

    public static boolean handleClickableElementsInteractions() {
        if(!Logic.isPaused()) {
            for (Set<UIElement> layer : layers) {
                for (UIElement element : layer) {
                    if (element.isVisible() && element instanceof Clickable) {
                        Clickable clickableElement = (Clickable) element;
                        if (clickableElement.isClicked()) {
                            clickableElement.onClick();
                        }
                        if (clickableElement.isUp()) {
                            clickableElement.onUp();
                            return true;
                        }
                        if (clickableElement.isHeld()) {
                            clickableElement.onHold();
                            return true;
                        }
                    }
                }
            }
        } else {
            for (UIElement element : layers.get(0)) {
                if (element.isVisible() && element instanceof Clickable) {
                    Clickable clickableElement = (Clickable) element;
                    if (clickableElement.isClicked()) {
                        clickableElement.onClick();
                    }
                    if (clickableElement.isUp()) {
                        clickableElement.onUp();
                        return true;
                    }
                    if (clickableElement.isHeld()) {
                        clickableElement.onHold();
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static void resizeUI() {
        timeMenu.setGlobalPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        resourceList.setGlobalPosition(20, Gdx.graphics.getHeight() - 20);
        pauseMenu.setGlobalPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
    }

    public static void showNPCStatWindow(NPC npc) {
        npcStatWindow.show(npc);
    }

    public static void showBuildingStatWindow(Building building) {
        buildingStatWindow.show(building);
    }

    public static void onEscape() {
        if(!Logic.isPaused()) {
            if (Buildings.isInBuildingMode()) {
                Buildings.turnOffBuildingMode();
                return;
            }
            if (buildingStatWindow.isVisible() || npcStatWindow.isVisible()) {
                buildingStatWindow.setVisible(false);
                npcStatWindow.setVisible(false);
                return;
            }
            if(buildingMenu.isVisible()) {
                buildingMenu.setVisible(false);
                return;
            }
            showPauseMenu(true);
        }
        else showPauseMenu(false);
    }

    public static void showPauseMenu(boolean open) {
        Logic.pause(open);
        DEFAULT_COLOR.set(open ? DARK : WHITE);
        pauseMenu.setVisible(open);
    }
}
