package com.boxhead.builder.ui.compound;

import com.boxhead.builder.Logic;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.BoxPane;
import com.boxhead.builder.ui.Button;
import com.boxhead.builder.ui.Clock;
import com.boxhead.builder.ui.Pane;

public class ClockPane extends BoxPane {
    public ClockPane() {
        addUIComponent(new Clock());
        Pane buttonPane = new BoxPane(false, 0);
        Button pauseButton = new Button(Textures.Ui.PAUSE);
        Button playButton = new Button(Textures.Ui.PLAY);
        Button x2Button = new Button(Textures.Ui.X2SPEED);
        Button x3Button = new Button(Textures.Ui.X3SPEED);

        pauseButton.setOnClick(() -> Logic.setTickSpeed(0));
        playButton.setOnClick(() -> Logic.setTickSpeed(Logic.NORMAL_SPEED));
        x2Button.setOnClick(() -> Logic.setTickSpeed(Logic.SPEED_X2));
        x3Button.setOnClick(() -> Logic.setTickSpeed(Logic.SPEED_X3));
        buttonPane.addUIComponents(pauseButton, playButton, x2Button, x3Button);

        addUIComponent(buttonPane);
    }
}
