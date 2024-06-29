package com.boxhead.builder.ui.compound;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.boxhead.builder.Statistics;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.*;
import com.boxhead.builder.utils.Vector2i;

public class StatisticsWindow extends Window {
    private static final Color GRAPH_COLOR = new Color(0.098f, 0.082f, 0.063f, 1f);
    private Statistics.Type displayedStat = Statistics.Type.POPULATION;
    private final DrawableComponent graph = new DrawableComponent(Textures.Ui.GRAPH);
    private static int upperLimit = 100;

    public StatisticsWindow() {
        super(Window.Style.THIN, new BoxPane(false));

        Pane buttonPane = new BoxPane();
        Button[] buttons = new Button[Statistics.Type.values().length];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Button(Textures.Ui.WIDE_BUTTON, Statistics.Type.values()[i].name().toLowerCase(), TextArea.Align.RIGHT);

            int statIndex = i;
            buttons[i].setOnUp(() -> {
                displayedStat = Statistics.Type.values()[statIndex];
                float max = 0;
                for (float value : Statistics.getValues(displayedStat)) {
                    if (value > max) max = value;
                }
                upperLimit = 10;
                while (upperLimit < max)
                    upperLimit *= 10;
            });

            buttonPane.addUIComponent(buttons[i]);
        }

        addUIComponents(buttonPane, graph);
        setVisible(false);
        setOnOpen(() -> {
            for (int i = 0; i < buttons.length; i++) {
                Statistics.Type statistic = Statistics.Type.values()[i];
                String value = String.valueOf(statistic.getValue());
                buttons[i].setText(statistic.name().toLowerCase() + " ".repeat(4 - value.length() + 3) + value);
            }
        });
    }

    public void drawGraph(ShapeRenderer renderer) {
        if (isVisible()) {
            float[] values = Statistics.getValues(displayedStat);
            Vector2i prevPoint = new Vector2i(290, (int) (values[0] / (upperLimit / 300f)));
            Vector2i point = new Vector2i();

            for (int i = 1; i < Statistics.VALUES_PER_STAT; i++) {
                point.set(10 * (Statistics.VALUES_PER_STAT - 1 - i), (int) (values[i] / (upperLimit / 300f)));
                renderer.rectLine(
                        prevPoint.x + graph.getX() + 11,
                        prevPoint.y + graph.getY() + 7,
                        point.x + graph.getX() + 11,
                        point.y + graph.getY() + 7,
                        5, GRAPH_COLOR, GRAPH_COLOR
                );
                prevPoint.set(point);
            }
        }
    }
}
