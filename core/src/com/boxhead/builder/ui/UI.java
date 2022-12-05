package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Building;
import com.boxhead.builder.game_objects.Buildings;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.utils.Vector2i;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

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

    private static TextField activeTextField = null;

    private static Button buildingButton, npcButton, workButton, restButton, demolishButton, homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton,
            transportOfficeButton, pauseButton, playButton, x2Button, x3Button, resumeButton, loadButton, saveButton, quitButton;

    private static UIElement buildingButtonGroup, mainButtonGroup, timeElementGroup, pauseMenu;

    private static Window pauseWindow, saveWindow;
    private static InputPopup savePopup;
    private static TextArea saveText;

    private static NPCStatWindow npcStatWindow;
    private static BuildingStatWindow buildingStatWindow;

    private static ResourceList resourceList;
    private static Clock clock;

    private static Clickable clickedElement = null;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private enum Layer {
        STAT_WINDOWS        (new HashSet<>(Arrays.asList(npcStatWindow, buildingStatWindow))),
        MAIN_BUTTONS        (new HashSet<>(Arrays.asList(buildingButton, npcButton, workButton, restButton, demolishButton,
                                                        homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton,
                                                        transportOfficeButton, clock, pauseButton, playButton, x2Button, x3Button, resourceList))),
        PAUSE_WINDOW        (new HashSet<>(Arrays.asList(pauseWindow))),
        PAUSE_MENU_BUTTONS  (new HashSet<>(Arrays.asList(resumeButton, loadButton, saveButton, quitButton))),
        SAVE_WINDOW         (new HashSet<>(Arrays.asList(saveWindow))),
        SAVE_MENU_BUTTONS   (new HashSet<>()),
        SAVE_TEXT           (new HashSet<>(Arrays.asList(saveText))),
        SAVE_POPUP          (new HashSet<>(Arrays.asList(savePopup)));

        Set<UIElement> elements;

        Layer(Set<UIElement> elements) {
            this.elements = elements;
        }

        public Set<UIElement> getElements() {
            return elements;
        }
    }

    public static void init() {
        pauseMenu = new UIElement(null, new Vector2i(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2), false);
        mainButtonGroup = new UIElement(null, new Vector2i(0, 10), true);
        buildingButtonGroup = new UIElement(null, new Vector2i(0, 84), false);
        timeElementGroup = new UIElement(null, new Vector2i(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), true);

        //region pauseMenu
        int menuWidth = 120, menuHeight = 163;
        pauseWindow = new Window(Textures.get(Textures.Ui.WINDOW), pauseMenu, new Vector2i());
        pauseWindow.setWidth(menuWidth);
        pauseWindow.setHeight(menuHeight);
        pauseWindow.setLocalPosition(-pauseWindow.getWindowWidth() / 2, -pauseWindow.getWindowHeight() / 2);
        pauseWindow.setTint(WHITE);

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

        saveText = new TextArea("Save", saveWindow, new Vector2i(0, saveWindow.getWindowHeight() - 25), saveWindow.getWindowWidth(), true);
        saveText.setTint(WHITE);
        //endregion

        //region savePopup
        savePopup = new InputPopup(Textures.get(Textures.Ui.WINDOW), pauseMenu, new Vector2i(), "New Save",
                s -> {
                    BuilderGame.saveToFile(s + ".save");
                    saveWindow.setVisible(false);
                });
        savePopup.setLocalPosition(-savePopup.getWindowWidth() / 2, -savePopup.getWindowHeight() / 2);
        savePopup.setTint(WHITE);
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
                () -> Buildings.setDemolishingMode(!Buildings.isDemolishing()));
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
    }

    public static void drawUI(SpriteBatch batch) {
        for (Layer layer : Layer.values()) {
            for (UIElement element : layer.getElements()) {
                if (element.isVisible()) {
                    element.draw(batch);
                }
            }
        }
    }

    public static boolean handleUiInteraction() {
        boolean interacted = false;
        Layer layer;

        if(activeTextField != null) activeTextField.write();

        if(!Logic.isPaused()) {
            layer = Layer.MAIN_BUTTONS;
        }
        else layer = Layer.PAUSE_MENU_BUTTONS;
        if(saveWindow.isVisible()) layer = Layer.SAVE_MENU_BUTTONS;
        if(savePopup.isVisible()) layer = Layer.SAVE_POPUP;

        if (InputManager.isButtonPressed(InputManager.LEFT_MOUSE)) {
            interacted = onClick(layer);
            if(!Logic.isPaused() && !interacted) interacted = onClick(Layer.STAT_WINDOWS);  //todo clean up
        }
        else if (clickedElement != null) {
            if (InputManager.isButtonDown(InputManager.LEFT_MOUSE)) interacted = onHold();
            if (InputManager.isButtonUp(InputManager.LEFT_MOUSE)) {
                interacted = onUp();
                clickedElement = null;
            }
        }

        return interacted;
    }

    /**
     * Checks if any element in the given layer has been clicked.
     * @param layer the number of the layer in which the clicked element is searched for
     * @return true if an element has been clicked
     */
    private static boolean onClick(Layer layer) {
        for (UIElement element : layer.getElements()) {
            if (element.isVisible() && element instanceof Clickable && ((Clickable) element).isMouseOver()) {
                clickedElement = ((Clickable) element).onClick();
                return true;
            }
        }
        return false;
    }

    private static boolean onClick() {
        for (Layer layer : Layer.values()) {
            for (UIElement element : layer.getElements()) {
                if (element.isVisible() && element instanceof Clickable && ((Clickable) element).isMouseOver()) {
                    clickedElement = ((Clickable) element).onClick();
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
            if (Buildings.isInBuildingMode() || Buildings.isDemolishing()) {
                Buildings.turnOffBuildingMode();
                Buildings.setDemolishingMode(false);
            } else if (buildingStatWindow.isVisible() || npcStatWindow.isVisible()) {
                buildingStatWindow.setVisible(false);
                npcStatWindow.setVisible(false);
            } else if (buildingButtonGroup.isVisible()) {
                buildingButtonGroup.setVisible(false);
            } else {
                showPauseMenu(true);
            }
        } else {
            if(savePopup.isVisible()) savePopup.setVisible(false);
            else if(saveWindow.isVisible()) saveWindow.setVisible(false);
            else showPauseMenu(false);
        }
    }

    private static void showPauseMenu(boolean open) {
        Logic.pause(open);
        DEFAULT_COLOR.set(open ? DARK : WHITE);
        pauseMenu.setVisible(open);
    }

    private static void showLoadMenu() {
        Set<UIElement> layer = Layer.SAVE_MENU_BUTTONS.getElements();
        layer.clear();

        saveWindow.setVisible(true);
        saveText.setText("Load");
        TextureRegion texture = Textures.get(Textures.Ui.WIDE_AREA);
        Vector2i buttonPos = new Vector2i(20, saveWindow.getWindowHeight() - 114);

        SortedSet<File> saves = BuilderGame.getSortedSaveFiles();

        if (saves.size() == 0) {
            Button button = new Button(texture, saveWindow, buttonPos, "No saves", () -> {});
            button.setTint(WHITE);
            layer.add(button);
        }
        else {
            for (File save : saves) {
                SimpleDateFormat dateFormat = new SimpleDateFormat();
                Button button = new Button(
                        texture,
                        saveWindow,
                        buttonPos.clone(),
                        save.getName().substring(0, save.getName().lastIndexOf(".")) + "\n" + dateFormat.format(save.lastModified()),
                        () -> {BuilderGame.loadFromFile(save); saveWindow.setVisible(false);});
                button.setTint(WHITE);
                layer.add(button);
                buttonPos.set(buttonPos.x, buttonPos.y - texture.getRegionHeight() - 10);
            }
        }
    }

    public static void showSaveMenu() {
        Set<UIElement> layer = Layer.SAVE_MENU_BUTTONS.getElements();
        layer.clear();

        saveWindow.setVisible(true);
        saveText.setText("Save");
        TextureRegion texture = Textures.get(Textures.Ui.WIDE_AREA);
        Vector2i buttonPos = new Vector2i(20, saveWindow.getWindowHeight() - 114);

        SortedSet<File> saves = BuilderGame.getSortedSaveFiles();

        Button newSaveButton = new Button(texture, saveWindow, buttonPos.clone(), "New save",
                () -> savePopup.setVisible(true));
        buttonPos.set(buttonPos.x, buttonPos.y - texture.getRegionHeight() - 10);
        newSaveButton.setTint(WHITE);
        layer.add(newSaveButton);

        for (File save : saves) {
            SimpleDateFormat dateFormat = new SimpleDateFormat();
            Button button = new Button(
                    texture,
                    saveWindow,
                    buttonPos.clone(),
                    save.getName().substring(0, save.getName().lastIndexOf(".")) + "\n" + dateFormat.format(save.lastModified()),
                    () -> {BuilderGame.saveToFile(save); saveWindow.setVisible(false);});
            button.setTint(WHITE);
            layer.add(button);
            buttonPos.set(buttonPos.x, buttonPos.y - texture.getRegionHeight() - 10);
        }
    }

    public static void setActiveTextField(TextField activeTextField) {
        UI.activeTextField = activeTextField;
    }
}
