package com.boxhead.builder.ui.compound;

import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.Logic;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.BoxPane;
import com.boxhead.builder.ui.Button;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.Window;

public class PauseWindow extends Window {
    public PauseWindow(LoadWindow loadWindow, SaveWindow saveWindow, SettingsWindow settingsWindow) {
        super(Window.Style.THICK, new BoxPane());
        setOnOpen(() -> {
            UI.DEFAULT_UI_COLOR.set(UI.DARK);
            Logic.pause(true);
            UI.disableHUD();
        });
        setOnClose(() -> {
            UI.DEFAULT_UI_COLOR.set(UI.WHITE);
            Logic.pause(false);
            UI.enableHUD();
        });

        Button resumeButton = new Button(Textures.Ui.BIG_BUTTON, "Resume");
        Button loadButton = new Button(Textures.Ui.BIG_BUTTON, "Load");
        Button saveButton = new Button(Textures.Ui.BIG_BUTTON, "Save");
        Button settingsButton = new Button(Textures.Ui.BIG_BUTTON, "Settings");
        Button quitToMenuButton = new Button(Textures.Ui.BIG_BUTTON, "Quit to menu");

        resumeButton.setOnUp(this::close);
        loadButton.setOnUp(loadWindow::open);
        saveButton.setOnUp(saveWindow::open);
        settingsButton.setOnUp(settingsWindow::open);
        quitToMenuButton.setOnUp(() -> BuilderGame.getInstance().setScreen(BuilderGame.getMenuScreen()));

        addUIComponents(resumeButton, loadButton, saveButton, settingsButton, quitToMenuButton);
        setTintCascading(UI.WHITE);
        setVisible(false);
    }
}
