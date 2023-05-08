package com.boxhead.builder.ui.popup;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Action;

import java.util.function.Consumer;

public abstract class Popups {
    private static InputPopup inputPopup;
    private static QuestionPopup questionPopup;
    private static WarningPopup warningPopup;
    private static InfoPopup infoPopup;
    private static Popup activePopup = null;

    public static void showPopup(String text, String prompt, Consumer<String> onAccept) {
        hidePopup();

        inputPopup = InputPopup.getInstance();
        inputPopup.text = text;
        inputPopup.prompt = prompt;
        inputPopup.onAccept = onAccept;

        activePopup = inputPopup;
        UI.setActiveTextField(inputPopup.getTextField());
        UI.setActiveButton(inputPopup.getOkButton());
        activePopup.setVisible(true);
        UI.Layer.POPUP.setVisible(true);
    }

    public static void showPopup(String text, Action onAccept) {
        hidePopup();

        questionPopup = QuestionPopup.getInstance();
        questionPopup.text = text;
        questionPopup.onAccept = onAccept;

        activePopup = questionPopup;
        questionPopup.setVisible(true);
        UI.setActiveButton(questionPopup.getOkButton());
        UI.Layer.POPUP.setVisible(true);
    }

    public static void showPopup(String text) {
        hidePopup();

        warningPopup = WarningPopup.getInstance();
        warningPopup.text = text;

        activePopup = warningPopup;
        activePopup.setVisible(true);
        UI.setActiveButton(warningPopup.getOkButton());
        UI.Layer.POPUP.setVisible(true);
    }

    public static void showInfoPopup(String text) {
        hidePopup();

        infoPopup = InfoPopup.getInstance();
        infoPopup.text = text;

        activePopup = infoPopup;
        activePopup.setVisible(true);
        UI.Layer.POPUP.setVisible(true);
    }

    public static boolean isActive() {
        return activePopup != null && activePopup.isVisible();
    }

    public static void hidePopup() {
        UI.Layer.POPUP.setVisible(false);
        if(activePopup != null) activePopup.setVisible(false);
    }

    public static Popup getActivePopup() {
        return activePopup;
    }

    public static void setText(String text) {
        if (activePopup != null) activePopup.setText(text);
    }
}
