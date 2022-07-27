package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.*;
import com.boxhead.builder.utils.Vector2i;

public class UI {
    public static final Color DEFAULT_COLOR = new Color(1, 1, 1, 1);
    public static final Color SEMI_TRANSPARENT = new Color(1, 1, 1, .5f);
    public static final Color PRESSED_COLOR = new Color(.8f, .8f, .8f, 1);

    public static final BitmapFont FONT = new BitmapFont();

    private static Button buildingButton, npcButton, homeButton, workplaceButton, serviceButton, storageButton, fungusButton, fungusButton2;
    private static ButtonGroup buildingMenu, mainMenu;
    private static UIElement clock, minuteHand, hourHand;
    private static NPCStatWindow NPCStatWindow;
    private static BuildingStatWindow buildingStatWindow;

    private static ResourceList resourceList;

    private static TextureRegion clockTexture = Textures.getUI("clock_face");
    private static Vector2i clockPos;

    private static UIElement[][] layers;

    public static void initUI() {
        buildingButton = new Button(Textures.getUI("house"), new Vector2i(10, 10)) {
            @Override
            public void onClick() {
                buildingMenu.setVisible(!buildingMenu.isVisible());
            }
        };
        npcButton = new Button(Textures.getUI("npc"), new Vector2i(84, 10)) {
            @Override
            public void onClick() {
                World.spawnNPC(new NPC(Textures.getNPC("funguy"), new Vector2i(World.getGridWidth()/2, World.getGridHeight()/2)));
            }
        };
        fungusButton = new Button(Textures.getUI("fungus"), new Vector2i(158, 10)) {
            @Override
            public void onClick() {
                World.setTime(25170);
            }
        };
        fungusButton2 = new Button(Textures.getUI("fungus"), new Vector2i(232, 10)) {
            @Override
            public void onClick() {
                World.setTime(57570);
            }
        };
        homeButton = new Button(Textures.getUI("home"), new Vector2i(10, 84)) {
            @Override
            public void onClick() {
                Buildings.build(Buildings.Types.DEFAULT_RESIDENTIAL_BUILDING);
                buildingMenu.setVisible(false);
            }
        };
        workplaceButton = new Button(Textures.getUI("workplace"), new Vector2i(84, 84)) {
            @Override
            public void onClick() {
                Buildings.build(Buildings.Types.DEFAULT_PRODUCTION_BUILDING);
                buildingMenu.setVisible(false);
            }
        };
        serviceButton = new Button(Textures.getUI("service"), new Vector2i(158, 84)) {
            @Override
            public void onClick() {
                Buildings.build(Buildings.Types.DEFAULT_SERVICE_BUILDING);
                buildingMenu.setVisible(false);
            }
        };
        storageButton = new Button(Textures.getUI("workplace"), new Vector2i(232, 84)) {
            @Override
            public void onClick() {
                Buildings.build(Buildings.Types.DEFAULT_STORAGE_BUILDING);
                buildingMenu.setVisible(false);
            }
        };

        mainMenu = new ButtonGroup(null, new Vector2i(), buildingButton, npcButton, fungusButton, fungusButton2);
        buildingMenu = new ButtonGroup(null, new Vector2i(), homeButton, workplaceButton, serviceButton, storageButton);

        NPCStatWindow = new NPCStatWindow();
        buildingStatWindow = new BuildingStatWindow();

        resourceList = new ResourceList();

        clockTexture = Textures.getUI("clock_face");
        clockPos = new Vector2i(Gdx.graphics.getWidth() - clockTexture.getRegionWidth() - 10, Gdx.graphics.getHeight() - clockTexture.getRegionHeight() - 10);
        clock = new UIElement(clockTexture, clockPos, true);
        minuteHand = new UIElement(Textures.getUI("minute_hand"), clockPos, true);
        hourHand = new UIElement(Textures.getUI("hour_hand"), clockPos, true);

        mainMenu.setVisible(true);

        layers = new UIElement[][] {{NPCStatWindow, buildingStatWindow},
                                    {mainMenu, buildingMenu, clock, minuteHand, hourHand, resourceList}};
    }

    public static void drawUI(SpriteBatch batch) {
        for (UIElement[] layer : layers) {
            for (UIElement element : layer) {
                if(element.isVisible()) element.draw(batch);
            }
        }
    }

    /**
     * Check if any "clickable" UI element was clicked.
     * @return true if any UI element was clicked
     */
    public static boolean isUIClicked() {
        for (int i = layers.length - 1; i >= 0; i--) {
            for (int j = 0; j < layers[i].length; j++) {
                if(layers[i][j].isVisible() && layers[i][j] instanceof Clickable) {
                    Clickable element = (Clickable) layers[i][j];
                    if (element.isHeld() || element.isClicked()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void updateUI() {
        resourceList.updateData();
        minuteHand.setRotation((float)World.getTime() * 0.1f);
        hourHand.setRotation((float)360/43200 * World.getTime());
        for (int i = layers.length - 1; i >= 0; i--) {
            for (int j = 0; j < layers[i].length; j++) {
                if(layers[i][j].isVisible() && layers[i][j] instanceof Clickable) {
                    Clickable element = (Clickable) layers[i][j];
                    if (element.isHeld() || element.isClicked()) {
                        if (element.isClicked()) element.onClick();
                        if (element.isHeld()) element.onHold();
                        return;
                    }
                }
            }
        }
    }

    public static void resizeUI() {
        clockPos.set(Gdx.graphics.getWidth() - clockTexture.getRegionWidth()-10,
                     Gdx.graphics.getHeight() - clockTexture.getRegionHeight()-10);
        resourceList.setPosition(20, Gdx.graphics.getHeight() - 20);
    }

    /**
     * Checks if any NPC or building was clicked.
     */
    public static void checkObjects() {
        for (NPC npc : World.getNpcs()) {
            if (npc.isClicked()) {
                npc.onClick();
                return;
            }
        }
        for (Building building : World.getBuildings()) {
            if (building.isClicked()) {
                building.onClick();
                return;
            }
        }
    }

    public static void showNPCStatWindow(NPC npc) {
        NPCStatWindow.show(npc);
    }

    public static void showBuildingStatWindow(Building building) {
        buildingStatWindow.show(building);
    }
}
