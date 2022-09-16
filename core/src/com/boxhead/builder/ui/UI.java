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
    public static final Color PRESSED_COLOR = new Color(.8f, .8f, .8f, 1);
    public static final BitmapFont FONT = new BitmapFont();
    public static final int FONT_SIZE = 15;

    private static final List<Set<UIElement>> layers = new ArrayList<>();

    private static Button buildingButton, npcButton, homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton, workButton, restButton;
    private static ButtonGroup buildingMenu, mainMenu;

    private static NPCStatWindow NPCStatWindow;
    private static BuildingStatWindow buildingStatWindow;

    private static ResourceList resourceList;
    private static Clock clock;

    public static void init() {
        buildingButton = new Button(Textures.get(Textures.Ui.HOUSE), new Vector2i(10, 10),
                () -> buildingMenu.setVisible(!buildingMenu.isVisible()));
        npcButton = new Button(Textures.get(Textures.Ui.NPC), new Vector2i(84, 10),
                () -> {
                    Vector2i position = new Vector2i(World.getGridWidth() / 2, World.getGridHeight() / 2);
                    World.spawnNPC(new NPC(Textures.get(Textures.Npc.FUNGUY), position));
                });

        workButton = new Button(Textures.get(Textures.Ui.WORK), new Vector2i(158, 10),
                () -> World.setTime(25170));
        restButton = new Button(Textures.get(Textures.Ui.REST), new Vector2i(232, 10),
                () -> World.setTime(57570));

        homeButton = new Button(Textures.get(Textures.Ui.HOME), new Vector2i(10, 84),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_RESIDENTIAL_BUILDING);
                    buildingMenu.setVisible(false);
                });
        workplaceButton = new Button(Textures.get(Textures.Ui.WORKPLACE), new Vector2i(84, 84),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_PRODUCTION_BUILDING);
                    buildingMenu.setVisible(false);
                });
        serviceButton = new Button(Textures.get(Textures.Ui.SERVICE), new Vector2i(158, 84),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_SERVICE_BUILDING);
                    buildingMenu.setVisible(false);
                });
        storageButton = new Button(Textures.get(Textures.Ui.STORAGE), new Vector2i(232, 84),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_STORAGE_BUILDING);
                    buildingMenu.setVisible(false);
                });
        constructionOfficeButton = new Button(Textures.get(Textures.Ui.CONSTRUCTION_OFFICE), new Vector2i(306, 84),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.CONSTRUCTION_OFFICE);
                    buildingMenu.setVisible(false);
                });

        mainMenu = new ButtonGroup(null, new Vector2i(), buildingButton, npcButton, workButton, restButton);
        buildingMenu = new ButtonGroup(null, new Vector2i(), homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton);

        NPCStatWindow = new NPCStatWindow();
        buildingStatWindow = new BuildingStatWindow();

        resourceList = new ResourceList();

        TextureRegion clockTexture = Textures.get(Textures.Ui.CLOCK_FACE);
        clock = new Clock(new Vector2i(Gdx.graphics.getWidth() - clockTexture.getRegionWidth() - 10,
                Gdx.graphics.getHeight() - clockTexture.getRegionHeight() - 10));

        mainMenu.setVisible(true);

        layers.add(new HashSet<>(Arrays.asList(mainMenu, buildingMenu, clock, resourceList)));
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

    public static boolean isAnyClickableElementClickedOrHeld() {
        for (Set<UIElement> layer : layers) {
            for (UIElement element : layer) {
                if (element.isVisible() && element instanceof Clickable) {
                    Clickable clickableElement = (Clickable) element;
                    if (clickableElement.isClicked() || clickableElement.isHeld()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void handleClickableElementsOnClickAndOnHold() {
        for (Set<UIElement> layer : layers) {
            for (UIElement element : layer) {
                if (element.isVisible() && element instanceof Clickable) {
                    Clickable clickableElement = (Clickable) element;
                    if (clickableElement.isClicked())
                        clickableElement.onClick();
                    if (clickableElement.isHeld())
                        clickableElement.onHold();
                }
            }
        }
    }

    public static void resizeUI() {
        clock.setPosition(Gdx.graphics.getWidth() - clock.texture.getRegionWidth() - 10,
                Gdx.graphics.getHeight() - clock.texture.getRegionHeight() - 10);
        resourceList.setPosition(20, Gdx.graphics.getHeight() - 20);
    }

    public static void showNPCStatWindow(NPC npc) {
        NPCStatWindow.show(npc);
    }

    public static void showBuildingStatWindow(Building building) {
        buildingStatWindow.show(building);
    }
}
