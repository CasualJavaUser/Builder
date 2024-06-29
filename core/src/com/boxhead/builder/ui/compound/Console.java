package com.boxhead.builder.ui.compound;

import com.boxhead.builder.Debug;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.*;

public class Console extends Window {
    private final TextArea consoleTextArea;

    public Console() {
        super(Window.Style.THIN, new BoxPane(true, 0));

        final int consoleWidth = 600;
        consoleTextArea = new TextArea("", consoleWidth, TextArea.Align.LEFT);
        ScrollPane scrollPane = new ScrollPane(consoleWidth, 300);
        scrollPane.addUIComponent(consoleTextArea);

        TextField textField = new TextField("Command", Textures.Ui.WIDE_TEXT_FIELD);
        Button acceptButton = new Button(Textures.Ui.GO_BUTTON);
        Pane bottomPane = new BoxPane(false);
        bottomPane.addUIComponents(textField, acceptButton);

        Pane mainPane = tabs[0];
        mainPane.addUIComponents(scrollPane, bottomPane);

        acceptButton.setOnUp(() -> {
            Debug.handleCommand(textField.text);
            textField.text = "";
            scrollPane.scrollToBottom();
        });

        setTintCascading(UI.WHITE);
        setOnOpen(() -> {
            textField.text = "";
            scrollPane.scrollToBottom();
            UI.setActiveTextField(textField);
            UI.setActiveScrollPane(scrollPane);
            UI.setActiveButton(acceptButton);
        });
        setVisible(false);
    }

    public void setText(String text) {
        consoleTextArea.setText(text);
    }
}
