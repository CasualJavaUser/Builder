package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class UI {
    public static final Color DEFAULT_COLOR = new Color(1, 1, 1, 1);
    public static final Color SEMI_TRANSPARENT = new Color(1, 1, 1, .5f);
    public static final Color PRESSED_COLOR = new Color(.8f, .8f, .8f, 1);

    private static Button buildingButton, npcButton, homeButton, workplaceButton, serviceButton;
    private static ButtonGroup buildingMenu, mainMenu;

    private static UIElement[][] layers;

    public static void initUI() {
        buildingButton = new Button(Textures.getIcon("house"), new Vector2i(10, 10)) {
            @Override
            void onClick() {
                buildingMenu.setVisible(!buildingMenu.isVisible());
            }
        };
        npcButton = new Button(Textures.getIcon("npc"), new Vector2i(84, 10)) {
            @Override
            void onClick() {
                World.spawnNPC(new NPC(new Texture("funguy.png"), new Vector2i(World.getGridWidth()/2, World.getGridHeight()/2)));
            }
        };
        homeButton = new Button(Textures.getIcon("home"), new Vector2i(10, 84)) {
            @Override
            void onClick() {
                BuilderGame.getGameScreen().build(Buildings.Types.DEFAULT_RESIDENTIAL_BUILDING);
                buildingMenu.setVisible(false);
            }
        };
        workplaceButton = new Button(Textures.getIcon("workplace"), new Vector2i(84, 84)) {
            @Override
            void onClick() {
                BuilderGame.getGameScreen().build(Buildings.Types.DEFAULT_PRODUCTION_BUILDING);
                buildingMenu.setVisible(false);
            }
        };
        serviceButton = new Button(Textures.getIcon("service"), new Vector2i(158, 84)) {
            @Override
            void onClick() {
                BuilderGame.getGameScreen().build(Buildings.Types.DEFAULT_SERVICE_BUILDING);
                buildingMenu.setVisible(false);
            }
        };

        mainMenu = new ButtonGroup(null, new Vector2i(), buildingButton, npcButton);
        buildingMenu = new ButtonGroup(null, new Vector2i(), homeButton, workplaceButton, serviceButton);

        mainMenu.setVisible(true);

        layers = new UIElement[][] {{mainMenu, buildingMenu}};
    }

    public static void drawUI(SpriteBatch batch) {
        for (UIElement[] layer : layers) {
            for (UIElement element : layer) {
                if(element.isVisible()) element.draw(batch);
            }
        }
    }

    public static void checkUI() {
        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            for (UIElement[] layer : layers) {
                for (UIElement element : layer) {
                    if(element.isVisible()) {
                        if (element instanceof Button && ((Button) element).isClicked()) {
                            ((Button) element).onClick();
                            return;
                        }
                        else if (element instanceof ButtonGroup && ((ButtonGroup) element).clicked() != null) {
                            ((ButtonGroup) element).clicked().onClick();
                            return;
                        }
                    }
                }
            }
        }
    }
}
