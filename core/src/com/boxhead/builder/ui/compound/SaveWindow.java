package com.boxhead.builder.ui.compound;

import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.SortedSet;

public class SaveWindow extends Window {
    public SaveWindow() {
        super(Window.Style.THICK, new BoxPane());

        TextArea title = new TextArea("Save");
        Button backButton = new Button(Textures.Ui.SMALL_BUTTON, "Back");
        backButton.setOnUp(this::close);

        ScrollPane scrollPane = new ScrollPane(500, 400);
        Button newSaveButton = getNewSaveButton();

        addUIComponents(title, scrollPane, backButton);
        setTintCascading(UI.WHITE);
        setVisible(false);
        setOnOpen(() -> {
            scrollPane.clear();
            scrollPane.addUIComponent(newSaveButton);

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

                Button saveButton = new Button(Textures.Ui.SAVE);
                saveButton.setOnUp(() ->
                        Popups.confirm("Override save?", () -> {
                                    BuilderGame.saveToFile(save);
                                    close();
                                }
                        ));

                Button deleteButton = new Button(Textures.Ui.DELETE);
                deleteButton.setOnUp(() ->
                        Popups.confirm("Delete file?", () -> {
                                    save.delete();
                                    open();
                                }
                        ));

                pane.addUIComponent(textArea);
                pane.addUIComponent(saveButton);
                pane.addUIComponent(deleteButton);
                scrollPane.addUIComponent(pane);
            }

            scrollPane.setTintCascading(UI.WHITE);
            scrollPane.pack();
            UI.setActiveScrollPane(scrollPane);
        });
    }

    private Button getNewSaveButton() {
        Button newSaveButton = new Button(Textures.Ui.WIDE_AREA, "New save");
        newSaveButton.setOnUp(() -> {
            Popups.prompt("New save", name -> {
                if (BuilderGame.getSaveFile(name + ".save").exists()) {
                    Popups.prompt("Override save?", n -> {
                        BuilderGame.saveToFile(n + ".save");
                        close();
                    });
                }
                else {
                    BuilderGame.saveToFile(name + ".save");
                    close();
                }
            });
        });
        return newSaveButton;
    }
}
