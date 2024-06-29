package com.boxhead.builder.ui.compound;

import com.boxhead.builder.Textures;
import com.boxhead.builder.Tile;
import com.boxhead.builder.Tiles;
import com.boxhead.builder.World;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.game_objects.buildings.Buildings;
import com.boxhead.builder.ui.BoxPane;
import com.boxhead.builder.ui.Button;
import com.boxhead.builder.utils.Vector2i;

public class MainButtonsPane extends BoxPane {
    public MainButtonsPane(BuildWindow buildWindow, ShiftWindow shiftWindow, StatisticsWindow statisticsWindow) {
        super(false);
        Button buildMenuButton = new Button(Textures.Ui.HAMMER);
        Button npcButton = new Button(Textures.Ui.NPC);
        Button workButton = new Button(Textures.Ui.WORK);
        Button restButton = new Button(Textures.Ui.REST);
        Button demolishButton = new Button(Textures.Ui.DEMOLISH);
        Button pathButton = new Button(Textures.Ui.PATH);
        Button pathRemovingButton = new Button(Textures.Ui.REMOVE_PATH);
        Button shiftMenuButton = new Button(Textures.Ui.SHIFTS);
        Button statisticsMenuButton = new Button(Textures.Ui.GRAPH_BUTTON);
        Button bridgeButton = new Button(Textures.Ui.BRIDGE);

        buildMenuButton.setOnUp(buildWindow::openClose);
        npcButton.setOnUp(() -> {
            Vector2i position = new Vector2i(World.getGridWidth() / 2, World.getGridHeight() / 2);
            World.spawnVillager(new Villager(position));
        });
        workButton.setOnUp(() -> {
            World.advanceTime(28770);
        });
        restButton.setOnUp(() -> {
            World.advanceTime(57570);
        });
        demolishButton.setOnUp(() -> {
            if (Buildings.isInDemolishingMode()) Buildings.turnOffDemolishingMode();
            else Buildings.toDemolishingMode();
        });
        pathButton.setOnUp(() -> {
            if (Tiles.getCurrentMode() != Tiles.Mode.PATH)
                Tiles.toPathMode(Tile.PATH);
            else
                Tiles.turnOff();
        });
        pathRemovingButton.setOnUp(() -> {
            if (Tiles.getCurrentMode() != Tiles.Mode.REMOVE_PATH)
                Tiles.toRemovingMode();
            else
                Tiles.turnOff();
        });
        bridgeButton.setOnUp(() -> {
            if (Tiles.getCurrentMode() != Tiles.Mode.BRIDGE)
                Tiles.toBridgeMode();
            else
                Tiles.turnOff();
        });
        shiftMenuButton.setOnUp(shiftWindow::openClose);
        statisticsMenuButton.setOnUp(statisticsWindow::openClose);

        addUIComponents(buildMenuButton, npcButton, workButton, restButton, demolishButton, pathButton,
                pathRemovingButton, shiftMenuButton, statisticsMenuButton, bridgeButton);
    }
}
