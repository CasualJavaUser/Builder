package com.boxhead.builder.ui.popup;

import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Action;

import java.util.function.Consumer;

public abstract class Popups {
    private static InputPopup inputPopup;
    private static QuestionPopup questionPopup;
    private static WarningPopup warningPopup;
    private static Popup activePopup = null;

    public static void showPopup(String text, String prompt, Consumer<String> onAccept) {
        hide();

        inputPopup = InputPopup.getInstance();
        inputPopup.text = text;
        inputPopup.prompt = prompt;
        inputPopup.onAccept = onAccept;

        activePopup = inputPopup;
        activePopup.setVisible(true);
        UI.Layer.POPUP.setVisible(true);
    }

    public static void showPopup(String text, Action onAccept) {
        hide();

        questionPopup = QuestionPopup.getInstance();
        questionPopup.text = text;
        questionPopup.onAccept = onAccept;

        activePopup = questionPopup;
        questionPopup.setVisible(true);
        UI.Layer.POPUP.setVisible(true);
    }

    public static void showPopup(String text) {
        hide();

        warningPopup = WarningPopup.getInstance();
        warningPopup.text = text;

        activePopup = warningPopup;
        activePopup.setVisible(true);
        UI.Layer.POPUP.setVisible(true);
    }

    public static boolean isActive() {
        return activePopup != null && activePopup.isVisible();
    }

    public static void hide() {
        if(activePopup != null) activePopup.setVisible(false);
    }
}
