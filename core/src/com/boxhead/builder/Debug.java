package com.boxhead.builder;

import com.badlogic.gdx.Input;
import com.boxhead.builder.game_objects.Animal;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.ui.*;
import com.boxhead.builder.utils.Vector2i;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Debug {
    private static Window console;
    private static TextField textField;
    private static TextArea textArea;
    private static Button accept;

    private static String log = "";

    private static Set<Method> availableMethods;
    private static List<String> lastCommands;
    private static int lastCommandIndex = -1;
    private final static int MAX_LINES = 20;

    private Debug() {};

    @Exclude
    public static void init() {
        for (int i = 0; i < MAX_LINES; i++) {
            log = log.concat("\n");
        }

        console = new Window(Textures.get(Textures.Ui.WINDOW), UI.Anchor.TOP_LEFT.getElement(), UI.Layer.CONSOLE, Vector2i.zero());
        console.setWindowWidth(600);
        console.setContentHeight((int)(UI.FONT.getLineHeight() * MAX_LINES) + 10);
        console.setLocalPosition(0, -console.getWindowHeight());

        textField = new TextField("Command", Textures.get(Textures.Ui.WIDE_TEXT_FIELD), console, UI.Layer.CONSOLE, Vector2i.zero());
        textField.setLocalPosition(0, -textField.getHeight());

        textArea = new TextArea(
                "",
                console,
                UI.Layer.CONSOLE,
                new Vector2i(console.getEdgeWidth() + UI.PADDING, console.getWindowHeight() - console.getEdgeWidth() - UI.PADDING),
                console.getWindowWidth(),
                false);

        accept = new Button(null, UI.Layer.CONSOLE, Vector2i.zero());

        availableMethods = new HashSet<>();
        for (Method method : Debug.class.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Exclude.class))
                availableMethods.add(method);
        }

        lastCommands = new ArrayList<>();

        accept.setOnClick(() -> {
            String[] input = textField.getText().split(" ");
            lastCommands.add(textField.getText());
            lastCommandIndex = lastCommands.size();
            textField.setText("");
            for (Method method : availableMethods) {
                if (method.getName().equals(input[0])) {
                    try {
                        method.invoke(Debug.class, (Object[]) Arrays.copyOfRange(input, 1, input.length));
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        log(e.getMessage());
                    }
                    catch (IllegalArgumentException e) {
                        log("Wrong number of arguments (expected: " + method.getParameters().length + ")");
                    }
                    return;
                }
            }
            log("Method \"" + input[0] + "\" not found");
        });

        console.setTint(UI.WHITE);
        textField.setTint(UI.WHITE);
        textArea.setTint(UI.WHITE);

        console.addToUI();
        textField.addToUI();
        textArea.addToUI();
    }

    @Exclude
    public static void openConsole() {
        UI.Layer.CONSOLE.setVisible(true);
        textField.setText("");
        UI.setActiveTextField(textField);
        UI.setActiveButton(accept);
    }

    @Exclude
    public static boolean isOpen() {
        return UI.Layer.CONSOLE.isVisible();
    }

    @Exclude
    public static void handleInput() {
        if (InputManager.isKeyPressed(Input.Keys.DOWN)) {
            if (lastCommandIndex < lastCommands.size()-1)
                lastCommandIndex++;

            if (lastCommandIndex < lastCommands.size() && lastCommandIndex >= 0)
                textField.setText(lastCommands.get(lastCommandIndex));
        }
        else if (InputManager.isKeyPressed(Input.Keys.UP)) {
            if (lastCommandIndex > 0)
                lastCommandIndex--;

            if (lastCommandIndex >= 0)
                textField.setText(lastCommands.get(lastCommandIndex));
        }
    }

    @Exclude
    private static Integer tryParseInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            log(e.getMessage());
        }
        return null;
    }

    public static void quit() {
        UI.Layer.CONSOLE.setVisible(false);
    }

    public static void log(String message) {
        log += message + "\n";
        String[] lines = log.split("\n");
        String text = "";
        for (int i = 0; i < MAX_LINES; i++) {
            text = lines[lines.length-1-i] + "\n" + text;
        }
        textArea.setText(text);
    }

    public static void selectNPC(String id) {
        Integer i = tryParseInt(id);
        if (i == null) return;
        for (Villager villager : World.getVillagers()) {
            if (villager.getId() == i) {
                villager.onClick();
                log("Villager selected");
                return;
            }
        }
        log("NPC with id " + id + " not found");
    }

    public static void getSelectedNPC() {
        if (UI.getNpcStatWindow().getPinnedObject() == null) {
            log("No villager selected");
            return;
        }
        log(UI.getNpcStatWindow().getPinnedObject().toString());
    }

    public static void getNPC(String id) {
        Integer i = tryParseInt(id);
        if (i == null) return;

        for (Villager villager : World.getVillagers()) {
            if (villager.getId() == i) {
                log(villager.toString());
            }
        }
        for (Animal animal : World.getAnimals()) {
            if (animal.getId() == i) {
                log(animal.toString());
            }
        }
    }

    public static void setTickSpeed(String speed) {
        Integer s = tryParseInt(speed);
        if (s == null) return;

        Logic.setTickSpeed(Logic.NORMAL_SPEED / s);
        log("Tick speed set to " + (Logic.NORMAL_SPEED / s));
    }

    public static void average(String stat) {
        try {
            Stat s = Stat.valueOf(stat.toUpperCase());
            float sum = 0;
            for (Villager villager : World.getVillagers()) {
                sum += villager.getStats()[s.ordinal()];
            }
            log(sum / World.getVillagers().size() + "");
        } catch (IllegalArgumentException e) {
            log("Stat \"" + stat.toUpperCase() + "\" not found");
        }
    }

    public static void setTime(String time) {
        int t = 0;
        if (time.equals("work")) {
            t = 28770;
        } else if (time.equals("rest")) {
            t = 57570;
        } else if (time.contains(":")) {
            try {
                Integer hours = tryParseInt(time.split(":")[0]);
                Integer minutes = tryParseInt(time.split(":")[1]);
                if (hours == null || minutes == null)
                    return;

                t = hours * 3600 + minutes * 60;
            } catch (Exception e) {
                log(e.getMessage());
            }
        } else {
            if (tryParseInt(time) != null) {
                t = tryParseInt(time);
            }
        }

        int hours = t/3600;
        int minutes = t/60 - hours * 60;
        int seconds = t - hours * 3600 - minutes * 60;
        if (t < World.getTime())
            World.setDay(World.getDay()+1);
        World.setTime(t);
        log("Time set to " + hours + ":" + minutes + ":" + seconds);
    }

    public static void getTime() {
        int t = World.getTime();
        int hours = t/3600;
        int minutes = t/60 - hours * 60;
        int seconds = t - hours * 3600 - minutes * 60;
        log(hours + ":" + minutes + ":" + seconds);
    }

    /**
     * Excludes from debug console commands
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Exclude {}
}
