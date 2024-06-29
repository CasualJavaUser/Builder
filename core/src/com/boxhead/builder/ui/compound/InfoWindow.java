package com.boxhead.builder.ui.compound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.GameScreen;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.GameObject;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.game_objects.buildings.Building;
import com.boxhead.builder.game_objects.buildings.FarmBuilding;
import com.boxhead.builder.game_objects.buildings.ProductionBuilding;
import com.boxhead.builder.ui.*;
import com.boxhead.builder.utils.Range;

public class InfoWindow extends DraggableWindow {
    protected GameObject selectedObject = null;
    protected boolean pinned;
    private static final int WIDTH = 200;

    private final DrawableComponent image;
    private final TextArea nameTA, warningTA, infoTA, empCap;
    private final Button leftButton, rightButton;
    private final Button changeResourceButton;
    private final Button turnOffButton;

    public InfoWindow(FarmResourceWindow farmResourceWindow) {
        super(Style.THIN, new BoxPane(true, 2));
        image = new DrawableComponent(null, 0, 1);
        nameTA = new TextArea("", WIDTH, TextArea.Align.CENTER);
        warningTA = new TextArea("", WIDTH, TextArea.Align.LEFT);
        warningTA.setTint(Color.RED);
        infoTA = new TextArea("", WIDTH, TextArea.Align.LEFT);
        empCap = new TextArea("", 16, TextArea.Align.CENTER);

        leftButton = new Button(Textures.Ui.LEFT_ARROW);
        leftButton.setOnUp(() -> {
            final ProductionBuilding building = ((ProductionBuilding) selectedObject);
            building.setEmployeeCapacity(building.getEmployeeCapacity() - 1);
            empCap.setText(building.getEmployeeCapacity() + "");
        });

        rightButton = new Button(Textures.Ui.RIGHT_ARROW);
        rightButton.setOnUp(() -> {
            final ProductionBuilding building = ((ProductionBuilding) selectedObject);
            building.setEmployeeCapacity(building.getEmployeeCapacity() + 1);
            empCap.setText(building.getEmployeeCapacity() + "");
        });

        turnOffButton = new Button(Textures.Ui.POWER_BUTTON);
        turnOffButton.setOnUp(() -> ((ProductionBuilding) selectedObject).switchBuildingActivity());

        changeResourceButton = new Button(Textures.Ui.SMALL_BUTTON, "resource");
        changeResourceButton.setOnUp(() -> {
            farmResourceWindow.setSelectedBuilding(((FarmBuilding<?>) selectedObject));
            farmResourceWindow.open();
            UI.pushOnEscapeAction(farmResourceWindow::close, farmResourceWindow::isVisible);
        });
        setVisible(false);
    }

    public void pin(Building building) {
        if (selectedObject instanceof ProductionBuilding pb) {
            pb.showRangeVisualiser(false);
        }

        clear();
        selectedObject = building;
        pinned = true;

        image.setScale(64f / building.getTexture().getRegionWidth());
        image.setTexture(building.getTexture());
        nameTA.setText(building.getType().name);
        warningTA.setText(building.getWarning());
        infoTA.setText(building.getInfo());

        addUIComponents(image, nameTA);
        addUIComponent(warningTA);
        addUIComponent(infoTA);

        if (building instanceof ProductionBuilding prodBuilding) {
            prodBuilding.showRangeVisualiser(true);
            empCap.setText(prodBuilding.getEmployeeCapacity() + "");
            GridPane empCapPane = new GridPane(4, 1, 16, 16, 2);
            empCapPane.setColumnWidth(0, 80);
            empCapPane.addUIComponents(new TextArea("emp cap:", 80, TextArea.Align.LEFT), leftButton, empCap, rightButton);
            addUIComponent(empCapPane);
            addUIComponent(turnOffButton);

            if (building instanceof FarmBuilding<?>) {
                addUIComponent(changeResourceButton);
            }
        }

        pack();
    }

    public void pin(Villager villager) {
        clear();
        selectedObject = villager;
        pinned = true;

        image.setScale(4);
        image.setTexture(villager.getTexture());
        nameTA.setText(villager.getName() + " " + villager.getSurname());
        infoTA.setText(villager.getInfo());

        addUIComponents(image, nameTA, infoTA);
        pack();
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (pinned) {
            updatePosition();
            if (!(getPinnedObjectBoundsX().contains(getObjectScreenPosition().x) &&
                    getPinnedObjectBoundsY().contains(getObjectScreenPosition().y)))
                close();
        }
        updateInfo();
        super.draw(batch);
    }

    private void updateInfo() {
        if (selectedObject instanceof Building building) {
            infoTA.setText(building.getInfo());
            if (selectedObject instanceof ProductionBuilding pb) {
                warningTA.setText(pb.getWarning());
            }
        }
        else if (selectedObject instanceof Villager villager) {
            infoTA.setText(villager.getInfo());
        }
    }

    private void updatePosition() {
        Vector3 objectPosition = getObjectScreenPosition();

        float cameraZoom = GameScreen.camera.zoom;
        int x = (int) (objectPosition.x + selectedObject.getTexture().getRegionWidth() / cameraZoom);
        int y = (int) (objectPosition.y + (selectedObject.getTexture().getRegionHeight()) / cameraZoom);
        Range<Integer> rangeX = Range.between(0, Gdx.graphics.getWidth() - getWidth());
        Range<Integer> rangeY = Range.between(0, Gdx.graphics.getHeight() - getHeight());
        x = rangeX.fit(x);
        y = rangeY.fit(y);

        setPosition(x, y);
        pack();
    }

    protected Vector3 getObjectScreenPosition() {
        return GameScreen.camera.project(new Vector3(
                selectedObject.getGridPosition().x * World.TILE_SIZE,
                selectedObject.getGridPosition().y * World.TILE_SIZE, 0));
    }

    @Override
    public void onHold() {
        pinned = false;
        super.onHold();
    }

    private Range<Float> getPinnedObjectBoundsX() {
        return Range.between(-selectedObject.getTexture().getRegionWidth() / GameScreen.camera.zoom, (float)Gdx.graphics.getWidth());
    }

    private Range<Float> getPinnedObjectBoundsY() {
        return Range.between(-selectedObject.getTexture().getRegionHeight() / GameScreen.camera.zoom, (float)Gdx.graphics.getHeight());
    }

    public GameObject getSelectedObject() {
        return selectedObject;
    }

    @Override
    public void close() {
        super.close();
        if (selectedObject instanceof ProductionBuilding productionBuilding) {
            productionBuilding.showRangeVisualiser(false);
        }
    }
}
