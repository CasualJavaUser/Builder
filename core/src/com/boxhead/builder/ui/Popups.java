package com.boxhead.builder.ui;

import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Action;

import java.util.function.Consumer;

public class Popups {
    protected static final Window popup = new Window(Window.Style.THICK, new BoxPane());
    private static final TextArea textArea = new TextArea("", 180, TextArea.Align.CENTER);
    static {
        popup.setVisible(false);
        popup.setOnOpen(() -> popup.setTintCascading(UI.WHITE));
    }

    public static void prompt(String text, Consumer<String> onAccept) {
        popup.clear();
        TextField textField = new TextField(text, Textures.Ui.TEXT_FIELD);

        Pane buttonPane = new BoxPane(false, 2);

        Button acceptButton = new Button(Textures.Ui.SMALL_BUTTON, "accept");
        acceptButton.setOnUp(() -> {
            if (!textField.text.isEmpty()) {
                onAccept.accept(textField.text);
                popup.close();
            }
        });

        Button cancelButton = new Button(Textures.Ui.SMALL_BUTTON, "cancel");
        cancelButton.setOnUp(popup::close);

        buttonPane.addUIComponents(acceptButton, cancelButton);

        UI.setActiveTextField(textField);
        textArea.setText(text);
        popup.addUIComponents(textArea, textField, buttonPane);
        popup.open();
        UI.updatePosition(popup);
        popup.pack();
        UI.setActiveButton(acceptButton);
    }

    public static void confirm(String text, Action onAccept) {
        popup.clear();
        Pane buttonPane = new BoxPane(false, 2);

        Button acceptButton = new Button(Textures.Ui.SMALL_BUTTON, "accept");
        acceptButton.setOnUp(() -> {
            onAccept.execute();
            popup.close();
        });

        Button cancelButton = new Button(Textures.Ui.SMALL_BUTTON, "cancel");
        cancelButton.setOnUp(popup::close);

        buttonPane.addUIComponents(acceptButton, cancelButton);

        textArea.setText(text);
        popup.addUIComponents(textArea, buttonPane);
        popup.open();
        UI.updatePosition(popup);
        popup.pack();
        UI.setActiveButton(acceptButton);
    }

    public static void alert(String text) {
        popup.clear();
        Button okButton = new Button(Textures.Ui.SMALL_BUTTON, "accept");
        okButton.setOnUp(popup::close);

        textArea.setText(text);
        popup.addUIComponents(textArea, okButton);
        popup.open();
        UI.updatePosition(popup);
        popup.pack();
        UI.setActiveButton(okButton);
    }

    public static void inform(String text) {
        popup.clear();
        textArea.setText(text);
        popup.addUIComponents(textArea);
        popup.open();
        UI.updatePosition(popup);
        popup.pack();
    }

    public static void close() {
        popup.close();
    }

    public static void setText(String text) {
        textArea.setText(text);
    }
}
