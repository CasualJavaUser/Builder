package com.boxhead.builder.ui.compound;

import com.badlogic.gdx.Gdx;
import com.boxhead.builder.BuilderGame;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.BoxPane;
import com.boxhead.builder.ui.Button;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.Window;

public class MainMenuWindow extends Window {
    public MainMenuWindow(LoadWindow loadWindow, SettingsWindow settingsWindow) {
        super(Window.Style.THICK, new BoxPane());

        Button newGameButton = new Button(Textures.Ui.BIG_BUTTON, "New game");
        Button loadButton = new Button(Textures.Ui.BIG_BUTTON, "Load");
        Button settingsButton = new Button(Textures.Ui.BIG_BUTTON, "Settings");
        Button quitButton = new Button(Textures.Ui.BIG_BUTTON, "Quit");

        newGameButton.setOnUp(BuilderGame::generateNewWorld);
        loadButton.setOnUp(loadWindow::open);
        settingsButton.setOnUp(settingsWindow::open);
        quitButton.setOnUp(() -> Gdx.app.exit());

        addUIComponents(newGameButton, loadButton, settingsButton, quitButton);
        setTintCascading(UI.WHITE);
    }
}
