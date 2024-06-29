package com.boxhead.builder.ui;

import com.boxhead.builder.utils.Vector2i;

import java.util.Arrays;

public class GridPane extends Pane {
    private final int columns, rows;
    private final int[] columnWidths;
    private final int rowHeight;

    public GridPane(int columns, int rows, int columnWidth, int rowHeight) {
        this(columns, rows, columnWidth, rowHeight, UI.PADDING);
    }

    public GridPane(int columns, int rows, int columnWidth, int rowHeight, int padding) {
        this.columns = columns;
        this.rows = rows;
        this.columnWidths = new int[columns];
        this.rowHeight = rowHeight;
        this.padding = padding;
        Arrays.fill(columnWidths, columnWidth);
    }

    @Override
    public void pack() {
        Vector2i nextPos = new Vector2i(getX(), getY() + getHeight() - rowHeight);
        int i = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (i >= components.size()) return;
                components.get(i).setPosition(nextPos);
                nextPos.add(columnWidths[x] + padding, 0);
                i++;
            }
            nextPos.set(getX(), nextPos.y - padding - rowHeight);
        }
    }

    public void setColumnWidth(int column, int width) {
        columnWidths[column] = width;
    }

    @Override
    public int getWidth() {
        int columnWidth = 0;
        for (int width : columnWidths) {
            columnWidth += width;
        }
        return columnWidth + (columns-1) * padding;
    }

    @Override
    public int getHeight() {
        return rows * (rowHeight + padding) - padding;
    }
}
