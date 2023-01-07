package com.boxhead.builder;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.ui.popup.Popups;

import java.io.*;
import java.util.*;

public class BuilderGame extends Game {

    private SpriteBatch batch;
    private static GameScreen gameScreen;
    private static File saveDirectory;

    @Override
    public void create() {
        batch = new SpriteBatch();
        gameScreen = new GameScreen(batch);

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) saveDirectory = new File(System.getProperty("user.home") + "/Appdata/LocalLow/Box Head/saves/");
        else if (os.contains("mac")) saveDirectory = new File(System.getProperty("user.home") + "/Library/Application Support/Box Head/saves/");
        else if (os.contains("nix") || os.contains("nux") || os.indexOf("aix") > 0) saveDirectory = new File(System.getProperty("user.home") + "/Home/.local/share/Box Head/saves/");
        else throw new RuntimeException("Unsupported OS");

        if(!saveDirectory.exists()) saveDirectory.mkdirs();

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(InputManager.getInstance());
        Gdx.input.setInputProcessor(inputMultiplexer);

        setScreen(gameScreen);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        NPC.executor.shutdown();
        batch.dispose();
        gameScreen.dispose();
    }

    @Override
    public void resize(int width, int height) {
        gameScreen.resize(width, height);
    }

    @Override
    public void resume() {
        super.resume();
        setScreen(gameScreen);
    }

    @Override
    public void pause() {
        super.pause();
    }

    public static boolean saveToFile(File file) {
        try {
            file.createNewFile();

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));

            out.writeInt(World.getSEED());
            out.writeInt(World.getTime());
            saveCollection(World.getGameObjects(), out);
            saveCollection(World.getBuildings(), out);
            saveCollection(World.getFieldWorks(), out);
            saveCollection(World.getNpcs(), out);
            saveCollection(World.getNavigableTiles(), out);

            out.close();

        } catch (IOException e) {
            Popups.showPopup(e.getClass().getName());
            return false;
        }
        return true;
    }

    public static boolean saveToFile(String fileName) {
        return saveToFile(new File(saveDirectory, fileName));
    }

    public static boolean loadFromFile(File file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            World.setSEED(in.readInt());
            World.setTime(in.readInt());
            loadCollection(World.getGameObjects(), in);
            loadCollection(World.getBuildings(), in);
            loadCollection(World.getFieldWorks(), in);
            loadCollection(World.getNpcs(), in);
            loadCollection(World.getNavigableTiles(), in);
        } catch (IOException e) {
            Popups.showPopup(e.getClass().getName());
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static<T extends Serializable> void saveCollection(Collection<T> collection, ObjectOutputStream out) throws IOException {
        out.writeInt(collection.size());
        for (T t : collection) {
            out.writeObject(t);
        }
    }

    private static<T extends Serializable> void loadCollection(Collection<T> collection,  ObjectInputStream in) throws IOException, ClassNotFoundException {
        collection.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            collection.add((T)in.readObject());
        }
    }

    /*public static File[] getSaveFiles() {
        File file = new File(saveDirectory);
        File[] saves = file.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".save"));
        if (saves == null) saves = new File[0];
        return saves;
    }*/

    public static File getSaveFile(String fileName) {
        return new File(saveDirectory + File.separator + fileName);
    }

    public static SortedSet<File> getSortedSaveFiles() {
        SortedSet<File> saves = new TreeSet<>((s1, s2) -> Long.compare(s2.lastModified(), s1.lastModified()));
        File[] arr = saveDirectory.listFiles();
        if(arr != null) {
            saves.addAll(Arrays.asList(arr));
            saves.removeIf(s -> !(s.isFile() && s.getName().endsWith(".save")));
        }
        return saves;
    }

    public static GameScreen getGameScreen() {
        return gameScreen;
    }
}
