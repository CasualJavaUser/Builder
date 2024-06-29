package com.boxhead.builder.ui.compound;

import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.Animals;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.game_objects.Harvestables;
import com.boxhead.builder.game_objects.buildings.FarmBuilding;
import com.boxhead.builder.game_objects.buildings.PlantationBuilding;
import com.boxhead.builder.game_objects.buildings.RanchBuilding;
import com.boxhead.builder.ui.BoxPane;
import com.boxhead.builder.ui.Button;
import com.boxhead.builder.ui.TextArea;
import com.boxhead.builder.ui.Window;

import java.util.Arrays;

public class FarmResourceWindow extends Window {
    private FarmBuilding<?> selectedBuilding;
    private Harvestables.Type selectedCrop;
    private Animals.Type selectedAnimal;
    TextArea descriptionArea;

    public FarmResourceWindow() {
        super(Style.THIN, new BoxPane(false));

        Harvestables.Type[] cropTypes = Arrays.stream(Harvestables.Type.values())
                .filter(type -> type.characteristic.equals(Harvestable.Characteristic.FIELD_CROP))
                .toArray(Harvestables.Type[]::new);

        Animals.Type[] animalTypes = Arrays.stream(Animals.Type.values())
                .filter(type -> type.growthTime > 0)
                .toArray(Animals.Type[]::new);

        Button[] cropButtons = new Button[cropTypes.length];
        Button[] animalButtons = new Button[animalTypes.length];

        //GridPane buttonPane = new GridPane(1, 3, 80, 32);
        BoxPane buttonPane = new BoxPane();
        BoxPane rightPane = new BoxPane();
        Button acceptButton = new Button(Textures.Ui.SMALL_BUTTON, "accept");
        descriptionArea = new TextArea("a\na\na\na", 300, TextArea.Align.LEFT);

        for (int i = 0; i < cropTypes.length; i++) {
            cropButtons[i] = new Button(Textures.Ui.SMALL_BUTTON, cropTypes[i].name().toLowerCase());
            int cropIndex = i;
            cropButtons[i].setOnUp(() -> {
                selectedCrop = cropTypes[cropIndex];
                updateDescription(selectedCrop);
            });
        }

        for (int i = 0; i < animalTypes.length; i++) {
            animalButtons[i] = new Button(Textures.Ui.SMALL_BUTTON, animalTypes[i].name().toLowerCase());
            int animalIndex = i;
            animalButtons[i].setOnUp(() -> {
                selectedAnimal = animalTypes[animalIndex];
                updateDescription(selectedAnimal);
            });
        }

        acceptButton.setOnUp(() -> {
            if (selectedBuilding instanceof RanchBuilding ranch)
                ranch.setAnimal(selectedAnimal);
            else if (selectedBuilding instanceof PlantationBuilding plantation)
                plantation.setCrop(selectedCrop);
            setVisible(true);
        });

        setOnOpen(() -> {
            buttonPane.clear();
            if (selectedBuilding instanceof RanchBuilding ranch) {
                selectedAnimal = ranch.getAnimal();
                updateDescription(selectedAnimal);
                buttonPane.addUIComponents(animalButtons);
            } else if (selectedBuilding instanceof PlantationBuilding plantation) {
                selectedCrop = plantation.getCrop();
                updateDescription(selectedCrop);
                buttonPane.addUIComponents(cropButtons);
            }
            buttonPane.pack();
            rightPane.pack();
            pack();
        });

        rightPane.addUIComponents(descriptionArea, acceptButton);
        addUIComponents(buttonPane, rightPane);
        setVisible(false);
    }

    public void setSelectedBuilding(FarmBuilding<?> selectedBuilding) {
        this.selectedBuilding = selectedBuilding;
    }

    private void updateDescription(Animals.Type type) {
        descriptionArea.setText(
                type.name().toLowerCase() +
                "\nresource: " + type.resource.name().toLowerCase() +
                "\nyield: " + type.yield +
                "\ngrowth time: " + (type.growthTime / (float) World.FULL_DAY) + " days"
        );
    }

    private void updateDescription(Harvestables.Type type) {
        descriptionArea.setText(
                type.name().toLowerCase() +
                "\nresource: " + type.characteristic.resource.name().toLowerCase() +
                "\nyield: " + type.yield +
                "\ngrowth time: " + (type.growthTime / (float) World.FULL_DAY) + " days"
        );
    }
}

/*
private static class FarmResourceMenu extends UIElement {
        private final int DATA_WIDTH = 200;

        private final Window window;
        private final Button[] cropButtons;
        private final Button[] animalButtons;
        private final Button acceptButton;
        private final TextArea descriptionArea;
        private Harvestables.Type currentCrop = null;
        private Animals.Type currentAnimal = null;
        private FarmBuilding<?> building;

        public FarmResourceMenu() {
            super(Anchor.TOP_LEFT.getElement(), Layer.IN_GAME, Vector2i.zero(), false);
            window = new Window(Textures.get(Textures.Ui.WINDOW), this, layer, Vector2i.zero(), true);
            window.setContentWidth(PADDING * 3 + Textures.get(Textures.Ui.SMALL_BUTTON).getRegionWidth() + DATA_WIDTH);

            Harvestables.Type[] cropTypes = Arrays.stream(Harvestables.Type.values())
                    .filter(type -> type.characteristic.equals(Harvestable.Characteristic.FIELD_CROP))
                    .toArray(Harvestables.Type[]::new);

            Animals.Type[] animalTypes = Arrays.stream(Animals.Type.values())
                    .filter(type -> type.growthTime > 0)
                    .toArray(Animals.Type[]::new);

            cropButtons = new Button[cropTypes.length];
            animalButtons = new Button[animalTypes.length];
            acceptButton = new Button(
                    Textures.get(Textures.Ui.SMALL_BUTTON),
                    window,
                    layer,
                    new Vector2i(window.getEdgeWidth() + PADDING * 2 + Textures.get(Textures.Ui.SMALL_BUTTON).getRegionWidth(), window.getEdgeWidth() + PADDING),
                    "accept"
            );
            descriptionArea = new TextArea(
                    "",
                    this,
                    layer,
                    new Vector2i(window.getEdgeWidth() + PADDING * 2 + Textures.get(Textures.Ui.SMALL_BUTTON).getRegionWidth(),-window.getEdgeWidth() - PADDING),
                    DATA_WIDTH, TextArea.Align.LEFT
            );

            for (int i = 0; i < cropTypes.length; i++) {
                cropButtons[i] = new Button(
                        Textures.get(Textures.Ui.SMALL_BUTTON),
                        this,
                        layer,
                        new Vector2i(window.getEdgeWidth() + PADDING, -window.getEdgeWidth() - (PADDING + 32) * (i+1)),
                        cropTypes[i].name().toLowerCase()
                );
                int cropIndex = i;
                cropButtons[i].setOnUp(() -> {
                    currentCrop = cropTypes[cropIndex];
                    setDescription(currentCrop);
                });
            }

            for (int i = 0; i < animalTypes.length; i++) {
                animalButtons[i] = new Button(
                        Textures.get(Textures.Ui.SMALL_BUTTON),
                        this,
                        layer,
                        new Vector2i(window.getEdgeWidth() + PADDING, -window.getEdgeWidth() - (PADDING + 32) * (i+1)),
                        animalTypes[i].name().toLowerCase()
                );
                int animalIndex = i;
                animalButtons[i].setOnUp(() -> {
                    currentAnimal = animalTypes[animalIndex];
                    setDescription(currentAnimal);
                });
            }

            acceptButton.setOnUp(() -> {
                if (building instanceof RanchBuilding)
                    ((RanchBuilding) building).setAnimal(currentAnimal);
                else
                    ((PlantationBuilding) building).setCrop(currentCrop);
                setVisible(false);
            });
        }

        public void show(FarmBuilding<?> farmBuilding) {
            boolean isRanch = farmBuilding instanceof RanchBuilding;

            int height = 32 + PADDING;
            if (isRanch) {
                currentAnimal = ((RanchBuilding) farmBuilding).getAnimal();
                setDescription(currentAnimal);
                height *= animalButtons.length;
            } else {
                currentCrop = ((PlantationBuilding) farmBuilding).getCrop();
                setDescription(currentCrop);
                height *= cropButtons.length;
            }
            window.setContentHeight(Math.max(height + PADDING, 150));
            window.setLocalPosition(0, -window.getWindowHeight());

            for (Button button : cropButtons) {
                button.setVisible(!isRanch);
            }
            for (Button button : animalButtons) {
                button.setVisible(isRanch);
            }
            setVisible(true);

            building = farmBuilding;
        }

        private void setDescription(Harvestables.Type type) {
            descriptionArea.setText(currentCrop.name().toLowerCase() +
                    "\nresource: " + currentCrop.characteristic.resource.name().toLowerCase() +
                    "\nyield: " + currentCrop.yield +
                    "\ngrowth time: " + (currentCrop.growthTime / (float)World.FULL_DAY) + " days");
        }

        private void setDescription(Animals.Type type) {
            descriptionArea.setText(currentAnimal.name().toLowerCase() +
                "\nresource: " + currentAnimal.resource.name().toLowerCase() +
                "\nyield: " + currentAnimal.yield +
                "\ngrowth time: " + (currentAnimal.growthTime / (float)World.FULL_DAY) + " days");
        }

        @Override
        public void addToUI() {
            super.addToUI();
            window.addToUI();
            for (Button button : cropButtons) {
                button.addToUI();
            }
            for (Button button : animalButtons) {
                button.addToUI();
            }
            descriptionArea.addToUI();
            acceptButton.addToUI();
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible)
                closeInGameMenus();
            super.setVisible(visible);
        }
    }
 */
