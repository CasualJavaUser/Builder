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
import com.boxhead.builder.ui.popup.InputPopup;
import com.boxhead.builder.ui.popup.Popups;
import com.boxhead.builder.utils.Vector2i;

import java.io.File;
import java.text.SimpleDateFormat;
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

    private static TextField activeTextField = null;
    private static ScrollPane activeScrollPane = null;
    private static Clickable clickedElement = null;
    private static Set<UIElement> saveWindowElements = new HashSet<>();

    private static UIElement buildingButtonGroup, mainButtonGroup, timeElementGroup, pauseMenu;

    private static Button buildingButton, npcButton, workButton, restButton, demolishButton, homeButton, workplaceButton, serviceButton, storageButton, constructionOfficeButton,
            transportOfficeButton, pauseButton, playButton, x2Button, x3Button, resumeButton, loadButton, saveButton, quitButton;

    private static NPCStatWindow npcStatWindow;
    private static BuildingStatWindow buildingStatWindow;
    private static Window pauseWindow, saveWindow;
    private static TextArea saveText;
    private static ResourceList resourceList;
    private static Clock clock;
    private static ScrollPane scrollPane;

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
        PAUSE_MENU(false),
        SAVE_MENU(false),
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
        pauseMenu = new UIElement(Anchor.CENTER.getElement(), Layer.PAUSE_MENU, new Vector2i(), true);
        mainButtonGroup = new UIElement(Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(0, 10), true);
        buildingButtonGroup = new UIElement(Anchor.BOTTOM_LEFT.getElement(), Layer.IN_GAME, new Vector2i(0, 84), false);
        timeElementGroup = new UIElement(Anchor.TOP_RIGHT.getElement(), Layer.IN_GAME, new Vector2i(), true);

        //region pauseMenu
        int menuWidth = 120, menuHeight = 163;
        pauseWindow = new Window(Textures.get(Textures.Ui.WINDOW), pauseMenu, Layer.PAUSE_MENU, new Vector2i());
        pauseWindow.setWidth(menuWidth);
        pauseWindow.setHeight(menuHeight);
        pauseWindow.setLocalPosition(-pauseWindow.getWindowWidth() / 2, -pauseWindow.getWindowHeight() / 2);
        pauseWindow.setTint(WHITE);

        resumeButton = new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, Layer.PAUSE_MENU, new Vector2i(-40,  40), "Resume", () -> showPauseMenu(false));
        loadButton =   new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, Layer.PAUSE_MENU, new Vector2i(-40,   3), "Load", UI::showLoadMenu);
        saveButton =   new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, Layer.PAUSE_MENU, new Vector2i(-40, -34), "Save", UI::showSaveMenu);
        quitButton =   new Button(Textures.get(Textures.Ui.WIDE_BUTTON), pauseMenu, Layer.PAUSE_MENU, new Vector2i(-40, -71), "Quit", () -> Gdx.app.exit());

        resumeButton.setTint(WHITE);
        loadButton.setTint(WHITE);
        saveButton.setTint(WHITE);
        quitButton.setTint(WHITE);
        //endregion

        //region saveWindow
        saveWindow = new Window(Textures.get(Textures.Ui.WINDOW), pauseMenu, Layer.SAVE_MENU, new Vector2i());
        saveWindow.setWindowWidth(280);
        saveWindow.setHeight(300);
        saveWindow.setLocalPosition(-saveWindow.getWindowWidth() / 2, -saveWindow.getWindowHeight() / 2);

        saveText = new TextArea("Save", saveWindow, Layer.SAVE_MENU, new Vector2i(0, saveWindow.getWindowHeight() - 25), saveWindow.getWindowWidth(), true);

        scrollPane = new ScrollPane(saveWindow, Layer.SAVE_MENU, new Vector2i(0, 20), saveWindow.getWindowWidth(), saveWindow.getWindowHeight() - 75);

        saveWindow.setTint(WHITE);
        saveText.setTint(WHITE);
        //endregion

        //region mainButtonGroup
        buildingButton = new Button(Textures.get(Textures.Ui.HOUSE), mainButtonGroup, Layer.IN_GAME, new Vector2i(10, 0),
                () -> buildingButtonGroup.setVisible(!buildingButtonGroup.isVisible()));
        npcButton = new Button(Textures.get(Textures.Ui.NPC), mainButtonGroup, Layer.IN_GAME, new Vector2i(84, 0),
                () -> {
                    Vector2i position = new Vector2i(World.getGridWidth() / 2, World.getGridHeight() / 2);
                    World.spawnNPC(new NPC(Textures.get(Textures.Npc.FUNGUY), position));
                });

        workButton = new Button(Textures.get(Textures.Ui.WORK), mainButtonGroup, Layer.IN_GAME, new Vector2i(158, 0),
                () -> World.setTime(25170));
        restButton = new Button(Textures.get(Textures.Ui.REST), mainButtonGroup, Layer.IN_GAME, new Vector2i(232, 0),
                () -> World.setTime(57570));
        demolishButton = new Button(Textures.get(Textures.Ui.DEMOLISH), mainButtonGroup, Layer.IN_GAME, new Vector2i(306, 0),
                () -> Buildings.setDemolishingMode(!Buildings.isDemolishing()));
        //endregion

        //region buildingButtonGroup
        homeButton = new Button(Textures.get(Textures.Ui.HOME), buildingButtonGroup, Layer.IN_GAME, new Vector2i(10, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_RESIDENTIAL_BUILDING);
                    buildingButtonGroup.setVisible(false);
                }, false);
        workplaceButton = new Button(Textures.get(Textures.Ui.WORKPLACE), buildingButtonGroup, Layer.IN_GAME, new Vector2i(84, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_PRODUCTION_BUILDING);
                    buildingButtonGroup.setVisible(false);
                }, false);
        serviceButton = new Button(Textures.get(Textures.Ui.SERVICE), buildingButtonGroup, Layer.IN_GAME, new Vector2i(158, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_SERVICE_BUILDING);
                    buildingButtonGroup.setVisible(false);
                }, false);
        storageButton = new Button(Textures.get(Textures.Ui.STORAGE), buildingButtonGroup, Layer.IN_GAME, new Vector2i(232, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.DEFAULT_STORAGE_BUILDING);
                    buildingButtonGroup.setVisible(false);
                }, false);
        constructionOfficeButton = new Button(Textures.get(Textures.Ui.CONSTRUCTION_OFFICE), buildingButtonGroup, Layer.IN_GAME, new Vector2i(306, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.CONSTRUCTION_OFFICE);
                    buildingButtonGroup.setVisible(false);
                }, false);
        transportOfficeButton = new Button(Textures.get(Textures.Ui.CONSTRUCTION_OFFICE), buildingButtonGroup, Layer.IN_GAME, new Vector2i(380, 0),
                () -> {
                    Buildings.toBuildingMode(Buildings.Type.TRANSPORT_OFFICE);
                    buildingButtonGroup.setVisible(false);
                }, false);
        //endregion

        //region timeElementGroup
        TextureRegion clockTexture = Textures.get(Textures.Ui.CLOCK_FACE);
        clock = new Clock(timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() - 10, -clockTexture.getRegionHeight() - 10));

        pauseButton = new Button(
                Textures.get(Textures.Ui.PAUSE),
                timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() - 9, -clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(0), true);

        playButton = new Button(
                Textures.get(Textures.Ui.PLAY),
                timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() + 23, -clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(Logic.NORMAL_SPEED), true);

        x2Button = new Button(
                Textures.get(Textures.Ui.X2SPEED),
                timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() + 55, -clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(Logic.SPEED_X2), true);

        x3Button = new Button(
                Textures.get(Textures.Ui.X3SPEED),
                timeElementGroup, Layer.IN_GAME,
                new Vector2i(-clockTexture.getRegionWidth() + 87, -clockTexture.getRegionHeight() - 36),
                () -> Logic.setTickSpeed(Logic.SPEED_X3), true);
        //endregion

        npcStatWindow = new NPCStatWindow(Layer.IN_GAME);
        buildingStatWindow = new BuildingStatWindow(Layer.IN_GAME);

        resourceList = new ResourceList(Anchor.TOP_LEFT.getElement(), Layer.IN_GAME, new Vector2i(20, -20));

        addUIElements();
    }

    private static void addUIElements() {
        npcStatWindow.addToUI();
        buildingStatWindow.addToUI();

        buildingButton.addToUI();
        npcButton.addToUI();
        workButton.addToUI();
        restButton.addToUI();
        demolishButton.addToUI();
        homeButton.addToUI();
        workplaceButton.addToUI();
        serviceButton.addToUI();
        storageButton.addToUI();
        constructionOfficeButton.addToUI();
        transportOfficeButton.addToUI();
        clock.addToUI();
        pauseButton.addToUI();
        playButton.addToUI();
        x2Button.addToUI();
        x3Button.addToUI();
        resourceList.addToUI();

        pauseWindow.addToUI();
            resumeButton.addToUI();
            loadButton.addToUI();
            saveButton.addToUI();
            quitButton.addToUI();

        saveWindow.addToUI();
            saveText.addToUI();
    }

    public static void drawUI(SpriteBatch batch) {
        for (Layer layer : Layer.values()) {
            for (UIElement element : layer.getElements()) {
                if (element.isVisible()) {
                    element.enableScissors(batch);
                    element.draw(batch);
                    element.disableScissors(batch);
                }
            }
        }
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
     * @param layer the layer in which the clicked element is searched for
     * @return true if an element has been clicked
     */
    private static boolean onClick(Layer layer) {
        for (int i = layer.getElements().size()-1; i >= 0; i--) {
            UIElement element = layer.getElements().get(i);
            if (element.isVisible() && element instanceof Clickable && ((Clickable) element).isMouseOver()) {
                ((Clickable) element).onClick();
                clickedElement = ((Clickable) element);
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

    public static void resizeUI() {
        Anchor.CENTER.getElement().setGlobalPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        Anchor.TOP_LEFT.getElement().setGlobalPosition(0, Gdx.graphics.getHeight());
        Anchor.TOP_RIGHT.getElement().setGlobalPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Anchor.BOTTOM_LEFT.getElement().setGlobalPosition(0, 0);
        Anchor.BOTTOM_RIGHT.getElement().setGlobalPosition(Gdx.graphics.getWidth(), 0);
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

    private static void showPauseMenu(boolean open) {
        Logic.pause(open);
        DEFAULT_COLOR.set(open ? DARK : WHITE);
        Layer.PAUSE_MENU.setVisible(open);
    }

    private static void showLoadMenu() {
        Layer.SAVE_MENU.getElements().removeAll(saveWindowElements);
        scrollPane.clear();
        activeScrollPane = scrollPane;

        Layer.SAVE_MENU.setVisible(true);
        saveText.setText("Load");
        TextureRegion texture = Textures.get(Textures.Ui.WIDE_AREA);

        SortedSet<File> saves = BuilderGame.getSortedSaveFiles();

        if (saves.size() == 0) {
            UIElement field = new UIElement(texture, scrollPane, Layer.SAVE_MENU, new Vector2i());
            TextArea textArea = new TextArea("No saves", field, Layer.SAVE_MENU, new Vector2i(0, 40), field.getHeight(), true);
            field.setTint(WHITE);
            textArea.setTint(WHITE);
            field.addToUI();
            textArea.addToUI();
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

        Button newSaveButton = new Button(texture, scrollPane, Layer.SAVE_MENU, new Vector2i(), "New save", () -> {
            Popups.showPopup("New save", "New save...", s -> {
                if (BuilderGame.getSaveFile(s + ".save").exists()) {
                    Popups.showPopup("Override save?", () -> {
                        BuilderGame.saveToFile(s + ".save");
                        Layer.SAVE_MENU.setVisible(false);
                    });
                } else {
                    BuilderGame.saveToFile(s + ".save");
                    Layer.SAVE_MENU.setVisible(false);
                }
            });
            setActiveTextField(InputPopup.getTextField());
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

    private static void createSaveField(File saveFile, boolean isSaving) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();

        UIElement area = new UIElement(Textures.get(Textures.Ui.WIDE_AREA), scrollPane, Layer.SAVE_MENU, new Vector2i());

        TextArea textArea = new TextArea(
                saveFile.getName().substring(0, saveFile.getName().lastIndexOf(".")) + "\n" +
                    dateFormat.format(saveFile.lastModified()),
                area,
                Layer.SAVE_MENU,
                new Vector2i(0, area.getHeight() - 15),
                area.getWidth(),
                true);

        Button saveButton = new Button(
                Textures.get(Textures.Ui.WIDE_BUTTON),
                area,
                Layer.SAVE_MENU,
                new Vector2i(15, 15),
                isSaving ? "save" : "load",
                () -> {
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

        Button deleteButton = new Button(
                Textures.get(Textures.Ui.WIDE_BUTTON),
                area,
                Layer.SAVE_MENU,
                new Vector2i(saveButton.getWidth() + 30, 15),
                "delete",
                () -> {
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
}
