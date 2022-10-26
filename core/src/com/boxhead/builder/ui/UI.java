package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

    public static final BitmapFont FONT = new BitmapFont();
    public static final int FONT_SIZE = 15;

    private static final List<Set<UIElement>> layers = new ArrayList<>();

    private static Button buildingButton, npcButton, workButton, restButton, homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton;
    private static UIElement buildingMenu, mainMenu;

    private static NPCStatWindow NPCStatWindow;
    private static BuildingStatWindow buildingStatWindow;

    private static ResourceList resourceList;
    private static Clock clock;

    public static void init() {
        mainMenu = new UIElement(null, new Vector2i(0, 10), true);
        buildingMenu = new UIElement(null, new Vector2i(0, 84), false);

        buildingButton = new Button(Textures.get(Textures.Ui.HOUSE), mainMenu, new Vector2i(10, 0),
                () -> buildingMenu.setVisible(!buildingMenu.isVisible()), false);
        npcButton = new Button(Textures.get(Textures.Ui.NPC), mainMenu, new Vector2i(84, 0),
                () -> {
                    Vector2i position = new Vector2i(World.getGridWidth() / 2, World.getGridHeight() / 2);
                    World.spawnNPC(new NPC(Textures.get(Textures.Npc.FUNGUY), position));
                }, false);

        workButton = new Button(Textures.get(Textures.Ui.WORK), mainMenu, new Vector2i(158, 0),
                () -> World.setTime(25170), false);
        restButton = new Button(Textures.get(Textures.Ui.REST), mainMenu, new Vector2i(232, 0),
                () -> World.setTime(57570), false);

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

        NPCStatWindow = new NPCStatWindow();
        buildingStatWindow = new BuildingStatWindow();

        resourceList = new ResourceList();

        TextureRegion clockTexture = Textures.get(Textures.Ui.CLOCK_FACE);
        clock = new Clock(new Vector2i(Gdx.graphics.getWidth() - clockTexture.getRegionWidth() - 10,
                Gdx.graphics.getHeight() - clockTexture.getRegionHeight() - 10));

        layers.add(new HashSet<>(Arrays.asList(buildingButton, npcButton, workButton, restButton,
                homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton,
                clock, resourceList)));
        layers.add(new HashSet<>(Arrays.asList(NPCStatWindow, buildingStatWindow)));
    }

    public static void drawUI(SpriteBatch batch) {
        for (Set<UIElement> layer : layers) {
            for (UIElement element : layer) {
                if (element.isVisible()) {
                    element.draw(batch);
                }
            }
        }
    }

    @Deprecated
    public static boolean isAnyClickableElementInteractedWith() {
        for (Set<UIElement> layer : layers) {
            for (UIElement element : layer) {
                if (element.isVisible() && element instanceof Clickable) {
                    Clickable clickableElement = (Clickable) element;
                    if (clickableElement.isClicked() || clickableElement.isUp() || clickableElement.isHeld()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean handleClickableElementsInteractions() {
        boolean b = false;
        for (Set<UIElement> layer : layers) {
            for (UIElement element : layer) {
                if (element.isVisible() && element instanceof Clickable) {
                    Clickable clickableElement = (Clickable) element;
                    if (clickableElement.isClicked()) {
                        clickableElement.onClick();
                        b = true;
                    }
                    if (clickableElement.isUp()) {
                        clickableElement.onUp();
                        b = true;
                    }
                    if (clickableElement.isHeld()) {
                        clickableElement.onHold();
                        b = true;
                    }
                }
            }
        }
        return b;
    }

    public static void resizeUI() {
        clock.setGlobalPosition(Gdx.graphics.getWidth() - clock.texture.getRegionWidth() - 10,
                Gdx.graphics.getHeight() - clock.texture.getRegionHeight() - 10);
        resourceList.setGlobalPosition(20, Gdx.graphics.getHeight() - 20);
    }

    public static void showNPCStatWindow(NPC npc) {
        NPCStatWindow.show(npc);
    }

    public static void showBuildingStatWindow(Building building) {
        buildingStatWindow.show(building);
    }
}
