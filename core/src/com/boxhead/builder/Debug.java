package com.boxhead.builder;

import com.badlogic.gdx.Input;
import com.boxhead.builder.game_objects.Animal;
import com.boxhead.builder.game_objects.Building;
import com.boxhead.builder.game_objects.ProductionBuilding;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.ui.*;
import com.boxhead.builder.utils.Action;
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

    private Debug() {}

    @Exclude
    public static void init() {
        for (int i = 0; i < MAX_LINES; i++) {
            log = log.concat("\n");
        }

        console = new Window(Textures.get(Textures.Ui.WINDOW), UI.Anchor.TOP_LEFT.getElement(), UI.Layer.CONSOLE, Vector2i.zero());
        console.setWindowWidth(600);
        console.setContentHeight((int)(UI.FONT.getLineHeight() * MAX_LINES) + UI.PADDING);
        console.setLocalPosition(0, -console.getWindowHeight());

        textField = new TextField("Command", Textures.get(Textures.Ui.WIDE_TEXT_FIELD), console, UI.Layer.CONSOLE, Vector2i.zero());
        textField.setLocalPosition(0, -textField.getHeight());

        textArea = new TextArea(
                "",
                console,
                UI.Layer.CONSOLE,
                new Vector2i(console.getEdgeWidth() + UI.PADDING, console.getWindowHeight() - console.getEdgeWidth() - UI.PADDING),
                console.getWindowWidth(),
                TextArea.Align.LEFT);

        accept = new Button(null, UI.Layer.CONSOLE, Vector2i.zero());

        availableMethods = new HashSet<>();
        for (Method method : Debug.class.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Exclude.class))
                availableMethods.add(method);
        }

        lastCommands = new ArrayList<>();

        accept.setOnClick(new Action() {
            @Override
            @Exclude
            public void execute() {
                String[] input = textField.getText().split(" ");
                lastCommands.add(textField.getText());
                lastCommandIndex = lastCommands.size();
                textField.setText("");
                for (Method method : availableMethods) {
                    if (method.getName().equals(input[0])) {
                        try {
                            method.invoke(Debug.class, (Object[]) Arrays.copyOfRange(input, 1, input.length));
                        }
                        catch (IllegalAccessException e) {
                            log(e.getMessage());
                        }
                        catch (InvocationTargetException e) {
                            log(e.getTargetException().getClass().getSimpleName() + ": " + e.getTargetException().getMessage());
                        }
                        catch (IllegalArgumentException e) {
                            log("Wrong number of arguments (expected: " + method.getParameters().length + ")");
                        }
                        return;
                    }
                }
                log("Method \"" + input[0] + "\" not found");
            }
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
    public static Villager getVillagerById(int id) throws IllegalStateException {
        for (Villager villager : World.getVillagers()) {
            if (villager.getId() == id)
                return villager;
        }
        throw new IllegalStateException("Villager with id " + id + " not found");
    }

    @Exclude
    public static Building getBuildingById(int id) throws IllegalStateException {
        for (Building building : World.getBuildings()) {
            if (building.getId() == id)
                return building;
        }
        throw new IllegalStateException("Building with id " + id + " not found");
    }

    public static void quit() {
        UI.Layer.CONSOLE.setVisible(false);
    }

    public static void log(String message) {
        log += message + "\n\n";
        String[] lines = log.split("\n");
        String text = "";
        for (int i = 0; i < MAX_LINES; i++) {
            text = lines[lines.length-1-i] + "\n" + text;
        }
        textArea.setText(text);
    }

    public static void help() {
        for (Method method : availableMethods) {
            log(method.getName() + " " + method.getParameters().length);
        }
    }

    public static void selectNPC(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        getVillagerById(i).onClick();
        log("Villager selected");
    }

    public static void getSelectedNPC() {
        if (UI.getNpcStatWindow().getPinnedObject() == null) {
            log("No villager selected");
            return;
        }
        log(UI.getNpcStatWindow().getPinnedObject().toString());
    }

    public static void getNPC(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        log(getVillagerById(i).toString());
    }

    public static void selectBuilding(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        getBuildingById(i).onClick();
        log("Building selected");
    }

    public static void getSelectedBuilding() {
        if (UI.getBuildingStatWindow().getPinnedObject() == null) {
            log("No building selected");
            return;
        }
        log(UI.getBuildingStatWindow().getPinnedObject().toString());
    }

    public static void getBuilding(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        log(getBuildingById(i).toString());
    }

    public static void setTickSpeed(String speed) throws NumberFormatException {
        float s = Float.parseFloat(speed);
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
                int hours = Integer.parseInt(time.split(":")[0]);
                int minutes = Integer.parseInt(time.split(":")[1]);

                t = hours * 3600 + minutes * 60;
            } catch (Exception e) {
                log(e.getMessage());
            }
        } else {
            t = Integer.parseInt(time);
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

    public static void enableShift(String buildingId, String shiftIndex) {
        int bi = Integer.parseInt(buildingId);
        int si = Integer.parseInt(shiftIndex);

        for (Building building : World.getBuildings()) {
            if (building.getId() == bi) {
                ((ProductionBuilding) building).setShiftActivity(si, true);
            }
        }
    }

    public static void disableShift(String buildingId, String shiftIndex) {
        int bi = Integer.parseInt(buildingId);
        int si = Integer.parseInt(shiftIndex);

        ((ProductionBuilding) getBuildingById(bi)).setShiftActivity(si, false);
    }

    public static void reproduce(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        Villager villager = getVillagerById(i);
        if (villager.getPartner() != null &&
                villager.getHome() != null &&
                !villager.isLivingWithParents() &&
                villager.getHome().equals(villager.getPartner().getHome())
        ) {
            villager.reproduce();
            log("successful");
        }
        else {
            log("not possible at this moment");
        }
    }

    public static void kill(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        getVillagerById(i).die();
        log("target eliminated");
    }

    /**
     * Excludes from debug console commands
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Exclude {}
}
