package com.boxhead.builder.ui.compound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.InputManager;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.*;

public class SettingsWindow extends Window {
    public SettingsWindow() {
        super(Window.Style.THICK, new BoxPane());
        setOnClose(BuilderGame::saveSettings);
        addUIComponent(new TextArea("Settings"));

        final int scrollPaneWidth = 350;
        final int optLabelWidth = 150;
        ScrollPane scrollPane = new ScrollPane(scrollPaneWidth, 330);

        scrollPane.addUIComponent(new TextArea("General", scrollPaneWidth, TextArea.Align.CENTER));
        CheckBox fsCheckBox = new CheckBox(Gdx.graphics.isFullscreen(), fullscreen -> {
            if (fullscreen)
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            else {
                Gdx.graphics.setWindowedMode(960, 640);
            }
        });
        BoxPane fsOptionPane = new BoxPane(false);
        fsOptionPane.addUIComponents(
                new TextArea("Fullscreen", optLabelWidth, TextArea.Align.LEFT),
                fsCheckBox
        );

        scrollPane.addUIComponent(fsOptionPane);

        scrollPane.addUIComponent(new TextArea("Keybindings", scrollPaneWidth, TextArea.Align.CENTER));
        for (InputManager.KeyBinding binding : InputManager.KeyBinding.values()) {
            BoxPane rowPane = new BoxPane(false);
            Button firstButton = new Button(Textures.Ui.SMALL_BUTTON, Input.Keys.toString(binding.keys.first));
            firstButton.setOnUp(() -> {
                InputManager.stopListening();
                firstButton.setText("...");
                InputManager.startListeningForKey(binding.keys, firstButton, true);
            });
            Button secondButton = new Button(Textures.Ui.SMALL_BUTTON, Input.Keys.toString(binding.keys.second));
            secondButton.setOnUp(() -> {
                InputManager.stopListening();
                secondButton.setText("...");
                InputManager.startListeningForKey(binding.keys, secondButton, false);
            });
            rowPane.addUIComponent(new TextArea(binding.name().toLowerCase(), optLabelWidth, TextArea.Align.LEFT));
            rowPane.addUIComponent(firstButton);
            rowPane.addUIComponent(secondButton);
            scrollPane.addUIComponent(rowPane);
        }

        addUIComponent(scrollPane);
        Button backButton = new Button(Textures.Ui.SMALL_BUTTON, "back");
        backButton.setOnUp(() -> {
            close();
            InputManager.stopListening();
        });

        addUIComponent(backButton);
        setTintCascading(UI.WHITE);
        setVisible(false);
        setOnOpen(() -> UI.setActiveScrollPane(scrollPane));
    }
}
