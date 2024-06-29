package com.boxhead.builder;

import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.game_objects.buildings.Building;
import com.boxhead.builder.game_objects.buildings.ProductionBuilding;
import com.boxhead.builder.ui.UI;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Debug {
    private static final StringBuilder log = new StringBuilder();

    private static Set<Method> availableMethods;

    private Debug() {}

    @Exclude
    public static void init() {
        availableMethods = new HashSet<>();
        for (Method method : Debug.class.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Exclude.class))
                availableMethods.add(method);
        }
    }

    @Exclude
    public static void handleCommand(String command) {
        String[] input = command.split(" ");
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

    @SuppressWarnings("unused")
    public static void quit() {
        UI.closeConsole();
    }

    public static void log(String message) {
        log.append(message).append("\n\n");
        UI.setConsoleText(log.toString());
    }

    @SuppressWarnings("unused")
    public static void help() {
        StringBuilder result = new StringBuilder("available methods (num of params):");
        for (Method method : availableMethods) {
            result.append("\n").append(method.getName()).append(" ").append(method.getParameters().length);
        }
        log(result.toString());
    }

    @SuppressWarnings("unused")
    public static void selectNPC(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        UI.showInfoWindow(getVillagerById(i));
        log("Villager selected");
    }

    /*@SuppressWarnings("unused")
    public static void getSelectedNPC() {
        if (UI.getNpcStatWindow().getPinnedObject() == null) {
            log("No villager selected");
            return;
        }
        log(UI.getNpcStatWindow().getPinnedObject().toString());
    }*/

    @SuppressWarnings("unused")
    public static void getNPC(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        log(getVillagerById(i).toString());
    }

    @SuppressWarnings("unused")
    public static void selectBuilding(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        UI.showInfoWindow(getBuildingById(i));
        log("Building selected");
    }

    /*@SuppressWarnings("unused")
    public static void getSelectedBuilding() {
        if (UI.getBuildingStatWindow().getPinnedObject() == null) {
            log("No building selected");
            return;
        }
        log(UI.getBuildingStatWindow().getPinnedObject().toString());
    }*/

    @SuppressWarnings("unused")
    public static void getBuilding(String id) throws NumberFormatException {
        int i = Integer.parseInt(id);
        log(getBuildingById(i).toString());
    }

    @SuppressWarnings("unused")
    public static void setTickSpeed(String speed) throws NumberFormatException {
        float s = Float.parseFloat(speed);
        Logic.setTickSpeed(Logic.NORMAL_SPEED / s);
        log("Tick speed set to " + (Logic.NORMAL_SPEED / s));
    }

    @SuppressWarnings("unused")
    public static void average(String stat) {
        try {
            Stat s = Stat.valueOf(stat.toUpperCase());
            log(World.getAverage(s) + "");
        } catch (IllegalArgumentException e) {
            log("Stat \"" + stat.toUpperCase() + "\" not found");
        }
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public static void getTime() {
        int t = World.getTime();
        int hours = t/3600;
        int minutes = t/60 - hours * 60;
        int seconds = t - hours * 3600 - minutes * 60;
        log(hours + ":" + minutes + ":" + seconds);
    }

    @SuppressWarnings("unused")
    public static void enableShift(String buildingId, String shiftIndex) {
        int bi = Integer.parseInt(buildingId);
        int si = Integer.parseInt(shiftIndex);

        ((ProductionBuilding) getBuildingById(bi)).setShiftActivity(si, true);
    }

    @SuppressWarnings("unused")
    public static void disableShift(String buildingId, String shiftIndex) {
        int bi = Integer.parseInt(buildingId);
        int si = Integer.parseInt(shiftIndex);

        ((ProductionBuilding) getBuildingById(bi)).setShiftActivity(si, false);
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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
