package com.boxhead.builder;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.game_objects.Harvestable;
import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BuilderGame extends Game {

    private SpriteBatch batch;
    private static GameScreen gameScreen;
    private static LoadingScreen loadingScreen;
    private static MenuScreen menuScreen;

    private static Screen currentScreen;

    private static File saveDirectory;
    private static final ExecutorService loadingExecutor = Executors.newSingleThreadExecutor();
    private static Future<Exception> loadingException;
    private static long lastSaveTime = 0;

    private static BuilderGame instance;

    private BuilderGame() {}

    @Override
    public void create() {
        batch = new SpriteBatch();
        gameScreen = new GameScreen(batch);
        loadingScreen = new LoadingScreen(batch);
        menuScreen = new MenuScreen(batch);

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) saveDirectory = new File(System.getenv("APPDATA") + "/Box Head/saves/");
        else if (os.contains("mac"))
            saveDirectory = new File(System.getProperty("user.home") + "/Library/Application Support/Box Head/saves/");
        else if (os.contains("nix") || os.contains("nux") || os.indexOf("aix") > 0)
            saveDirectory = new File(System.getProperty("user.home") + "/Home/.local/share/Box Head/saves/");
        else throw new RuntimeException("Unsupported OS");

        if (!saveDirectory.exists()) saveDirectory.mkdirs();

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
        loadingScreen.dispose();
        menuScreen.dispose();
        loadingExecutor.shutdownNow();
    }

    @Override
    public void resize(int width, int height) {
        currentScreen.resize(width, height);
    }

    @Override
    public void resume() {
        super.resume();
        setScreen(currentScreen);
    }

    @Override
    public void pause() {
        super.pause();
    }

    public static void generateNewWorld() {
        toLoadingScreen(menuScreen);
        loadingException = loadingExecutor.submit(() -> {
            LoadingScreen.setMessage("Loading...");
            World.generate((int) (Math.random() * 1000), new Vector2i(2001, 2001));
            return null;
        });
    }

    public static void saveToFile(File file) {
        toLoadingScreen(gameScreen);
        loadingException = loadingExecutor.submit(() -> {
            LoadingScreen.setMessage("Saving...");
            try {
                file.createNewFile();

                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));

                World.saveWorld(out);

                saveCollection(Logistics.supplyRequests, out);
                saveCollection(Logistics.outputRequests, out);
                saveCollection(Logistics.readyOrders, out);
                saveCollection(Logistics.storages, out);
                saveCollection(Logistics.transportOffices, out);
                saveCollection(Harvestable.timeTriggers, out);
                saveMap(Logistics.orderRequests, out);
                saveMap(Logistics.deliveriesInProgress, out);

                out.close();

                lastSaveTime = System.currentTimeMillis();

            } catch (IOException e) {
                e.printStackTrace();
                return e;
            }
            return null;
        });
    }

    public static void saveToFile(String fileName) {
        saveToFile(new File(saveDirectory, fileName));
    }

    public static void loadFromFile(File file) {
        toLoadingScreen(menuScreen);
        loadingException = loadingExecutor.submit(() -> {
            LoadingScreen.setMessage("Loading...");
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {

                World.loadWorld(in);

                LoadingScreen.setMessage("Loading Logistics...");
                loadCollection(Logistics.supplyRequests, in);
                loadCollection(Logistics.outputRequests, in);
                loadCollection(Logistics.readyOrders, in);
                loadCollection(Logistics.storages, in);
                loadCollection(Logistics.transportOffices, in);
                loadCollection(Harvestable.timeTriggers, in);
                loadMap(Logistics.orderRequests, in);
                loadMap(Logistics.deliveriesInProgress, in);

                lastSaveTime = System.currentTimeMillis();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return e;
            }
            return null;
        });
    }

    public static <T extends Serializable> void saveCollection(Collection<T> collection, ObjectOutputStream out) throws IOException {
        out.writeInt(collection.size());
        for (T t : collection) {
            out.writeObject(t);
        }
    }

    public static <K extends Serializable, V extends Serializable> void saveMap(Map<K, V> map, ObjectOutputStream out) throws IOException {
        out.writeInt(map.size());
        for (Map.Entry<K, V> e : map.entrySet()) {
            out.writeObject(Pair.of(e.getKey(), e.getValue()));
        }
    }

    public static <T extends Serializable> void loadCollection(Collection<T> collection, ObjectInputStream in) throws IOException, ClassNotFoundException {
        collection.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            collection.add((T) in.readObject());
        }
    }

    public static <K extends Serializable, V extends Serializable> void loadMap(Map<K, V> map, ObjectInputStream in) throws IOException, ClassNotFoundException {
        map.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            Pair<K, V> pair = (Pair<K, V>) in.readObject();
            map.put(pair.first, pair.second);
        }
    }

    public static File getSaveFile(String fileName) {
        return new File(saveDirectory + File.separator + fileName);
    }

    public static SortedSet<File> getSortedSaveFiles() {
        SortedSet<File> saves = new TreeSet<>((s1, s2) -> Long.compare(s2.lastModified(), s1.lastModified()));
        File[] arr = saveDirectory.listFiles();
        if (arr != null) {
            saves.addAll(Arrays.asList(arr));
            saves.removeIf(s -> !(s.isFile() && s.getName().endsWith(".save")));
        }
        return saves;
    }

    public static BuilderGame getInstance() {
        if (instance == null) instance = new BuilderGame();
        return instance;
    }

    public static Future<Exception> getLoadingException() {
        return loadingException;
    }

    public static GameScreen getGameScreen() {
        return gameScreen;
    }

    public static LoadingScreen getLoadingScreen() {
        return loadingScreen;
    }

    public static MenuScreen getMenuScreen() {
        return menuScreen;
    }

    public static long timeSinceLastSave() {
        return System.currentTimeMillis() - lastSaveTime;
    }

    public static void toLoadingScreen(Screen nextScreen) {
        BuilderGame.getInstance().setScreen(loadingScreen);
        LoadingScreen.setNextScreen(nextScreen);
    }

    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
        currentScreen = screen;
    }
}
