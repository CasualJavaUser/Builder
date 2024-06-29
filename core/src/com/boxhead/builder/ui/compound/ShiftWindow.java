package com.boxhead.builder.ui.compound;

import com.boxhead.builder.game_objects.buildings.*;
import com.boxhead.builder.ui.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public class ShiftWindow extends Window {
    private final CheckBox[] checkBoxes;
    private final ProductionBuilding.Type[] types;

    public ShiftWindow() {
        super(Window.Style.THIN, new BoxPane());

        final int NAME_WIDTH = 210;

        types = Stream.of(ProductionBuilding.Type.values(), ServiceBuilding.Type.values(), PlantationBuilding.Type.values(), RanchBuilding.Type.values(), SchoolBuilding.Type.values())
                .flatMap(Arrays::stream)
                .sorted(Comparator.comparing(type -> type.name))
                .toArray(ProductionBuilding.Type[]::new);

        checkBoxes = new CheckBox[types.length * 3];
        GridPane checkBoxPane = new GridPane(4, types.length + 1, 32, 32);
        checkBoxPane.setColumnWidth(0, NAME_WIDTH);
        checkBoxPane.addUIComponents(
                new TextArea("shift number:", NAME_WIDTH, TextArea.Align.RIGHT),
                new TextArea("1", 32 , TextArea.Align.CENTER),
                new TextArea("2", 32 , TextArea.Align.CENTER),
                new TextArea("3", 32 , TextArea.Align.CENTER)
        );

        for (int i = 0; i < types.length; i++) {
            ProductionBuilding.Type type = types[i];
            checkBoxes[i * 3] = new CheckBox(type.getShiftActivity(0), (active) -> type.setShiftActivity(0, active));
            checkBoxes[i * 3 + 1] = new CheckBox(type.getShiftActivity(1), (active) -> type.setShiftActivity(1, active));
            checkBoxes[i * 3 + 2] = new CheckBox(type.getShiftActivity(2), (active) -> type.setShiftActivity(2, active));

            checkBoxPane.addUIComponents(
                    new TextArea(type.name, NAME_WIDTH, TextArea.Align.RIGHT),
                    checkBoxes[i * 3],
                    checkBoxes[i * 3 + 1],
                    checkBoxes[i * 3 + 2]
            );
        }

        addUIComponent(checkBoxPane);
        setVisible(false);
    }

    public void update() {
        for (int i = 0; i < types.length; i++) {
            ProductionBuilding.Type type = types[i];
            checkBoxes[i * 3].setValue(type.getShiftActivity(0));
            checkBoxes[i * 3 + 1].setValue(type.getShiftActivity(1));
            checkBoxes[i * 3 + 2].setValue(type.getShiftActivity(2));
        }
    }
}
