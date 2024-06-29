package com.boxhead.builder.ui.compound;

import com.boxhead.builder.Textures;
import com.boxhead.builder.game_objects.buildings.*;
import com.boxhead.builder.ui.*;
import com.boxhead.builder.utils.Pair;

public class BuildWindow extends Window {
    private DrawableComponent buildingImage;
    private Button buildButton;
    private TextArea descriptionArea;

    public BuildWindow() {
        super(Window.Style.THIN,
                Pair.of(new BoxPane(), Textures.Ui.INFRASTRUCTURE_TAB),
                Pair.of(new BoxPane(), Textures.Ui.HOUSING_TAB),
                Pair.of(new BoxPane(), Textures.Ui.RESOURCES_TAB),
                Pair.of(new BoxPane(), Textures.Ui.SERVICES_TAB)
        );

        Pane infrastructureTab = tabs[0];
        Pane housingTab = tabs[1];
        Pane resourcesTab = tabs[2];
        Pane servicesTab = tabs[3];

        Building.Type type = ResidentialBuilding.Type.LOG_CABIN;
        Pane infoPane = new BoxPane(false);

        buildingImage = new DrawableComponent(type.getTexture());
        buildingImage.setScale(128f / type.getTexture().getRegionWidth());

        descriptionArea = new TextArea(type.name, 200, TextArea.Align.LEFT);

        buildButton = new Button(Textures.Ui.BUILD);
        buildButton.setOnUp(() -> {
            Buildings.toBuildingMode(type);
            close();
        });

        infoPane.addUIComponent(buildingImage);
        infoPane.addUIComponent(descriptionArea);
        infoPane.addUIComponent(buildButton);
        infoPane.pack();

        infrastructureTab.addUIComponents(
                getBuildMenuTab(
                        Pair.of(Building.Type.STORAGE_BARN, Textures.Ui.BARN),
                        Pair.of(ProductionBuilding.Type.BUILDERS_HUT, Textures.Ui.BIG_HAMMER),
                        Pair.of(ProductionBuilding.Type.TRANSPORT_OFFICE, Textures.Ui.CARRIAGE)
                ),
                new DrawableComponent(Textures.Ui.DIVIDER),
                infoPane
        );

        housingTab.addUIComponents(
                getBuildMenuTab(
                        Pair.of(ResidentialBuilding.Type.LOG_CABIN, Textures.Ui.HOUSE)
                ),
                new DrawableComponent(Textures.Ui.DIVIDER),
                infoPane
        );

        resourcesTab.addUIComponents(
                getBuildMenuTab(
                        Pair.of(ProductionBuilding.Type.LUMBERJACKS_HUT, Textures.Ui.AXE),
                        Pair.of(ProductionBuilding.Type.MINE, Textures.Ui.PICKAXE),
                        Pair.of(ProductionBuilding.Type.STONE_GATHERERS, Textures.Ui.PICKAXE_WITH_STONE),
                        Pair.of(PlantationBuilding.Type.PLANTATION, Textures.Ui.HOE),
                        Pair.of(RanchBuilding.Type.RANCH, Textures.Ui.COW),
                        Pair.of(WaterBuilding.Type.FISHING_HUT, Textures.Ui.FISHING_ROD),
                        Pair.of(WaterBuilding.Type.WATERMILL, Textures.Ui.WHEEL)
                ),
                new DrawableComponent(Textures.Ui.DIVIDER),
                infoPane
        );

        servicesTab.addUIComponents(
                getBuildMenuTab(
                        Pair.of(ServiceBuilding.Type.HOSPITAL, Textures.Ui.CROSS),
                        Pair.of(ServiceBuilding.Type.PUB, Textures.Ui.MUG),
                        Pair.of(SchoolBuilding.Type.SCHOOL, Textures.Ui.BOOK)
                ),
                new DrawableComponent(Textures.Ui.DIVIDER),
                infoPane
        );

        setVisible(false);
    }

    @SafeVarargs
    private Pane getBuildMenuTab(Pair<Building.Type, Textures.Ui>... buttons) {
        final int COLUMNS = 6, ROWS = 3;

        Pane pane = new GridPane(COLUMNS, ROWS, 64, 64);
        for (Pair<Building.Type, Textures.Ui> pair : buttons) {
            Button button = new Button(pair.second);
            button.setOnUp(() -> {
                buildingImage.texture = pair.first.getTexture();
                buildingImage.setScale(128f / pair.first.getTexture().getRegionWidth());
                descriptionArea.text = pair.first.name;
                buildButton.setOnUp(() -> {
                    Buildings.toBuildingMode(pair.first);
                    close();
                });
            });
            pane.addUIComponent(button);
        }
        pane.pack();
        return pane;
    }
}
