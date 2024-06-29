package com.boxhead.builder.ui.compound;

import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.SortedSet;

public class LoadWindow extends Window {
    public LoadWindow() {
        super(Window.Style.THICK, new BoxPane());

        TextArea title = new TextArea("Load");
        Button backButton = new Button(Textures.Ui.SMALL_BUTTON, "Back");
        backButton.setOnUp(this::close);

        ScrollPane scrollPane = new ScrollPane(500, 400);

        addUIComponents(title, scrollPane, backButton);
        setTintCascading(UI.WHITE);
        setVisible(false);
        setOnOpen(() -> {
            scrollPane.clear();

            SortedSet<File> saves = BuilderGame.getSortedSaveFiles();
            SimpleDateFormat dateFormat = new SimpleDateFormat();
            for (File save : saves) {
                Pane pane = new ImagePane(Textures.Ui.WIDE_AREA, false);
                TextArea textArea = new TextArea(
                        save.getName().substring(0, save.getName().lastIndexOf("."))
                                + "\n" + dateFormat.format(save.lastModified()),
                        300,
                        TextArea.Align.CENTER
                );

                pane.addUIComponent(textArea);
                pane.addUIComponent(getLoadButton(save));
                pane.addUIComponent(getDeleteButton(save));
                scrollPane.addUIComponent(pane);
            }

            scrollPane.setTintCascading(UI.WHITE);
            scrollPane.pack();
            UI.setActiveScrollPane(scrollPane);
        });
    }

    private Button getLoadButton(File save) {
        Button loadButton = new Button(Textures.Ui.LOAD);
        loadButton.setOnUp(() -> {
            if (BuilderGame.timeSinceLastSave()  > 60_000) {
                Popups.confirm("Load save?", () -> {
                    BuilderGame.loadFromFile(save);
                    close();
                });
            }
            else {
                BuilderGame.loadFromFile(save);
                close();
            }
        });
        return loadButton;
    }

    private Button getDeleteButton(File save) {
        Button deleteButton = new Button(Textures.Ui.DELETE);
        deleteButton.setOnUp(() ->
                Popups.confirm("Delete file?", () -> {
                            save.delete();
                            open();
                        }
                ));
        return deleteButton;
    }
}
