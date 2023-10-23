package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.boxhead.builder.ui.Button;
import com.boxhead.builder.utils.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import static com.badlogic.gdx.Input.Keys.*;

public class InputManager extends InputAdapter {
    public enum KeyBinding {
        MOVE_LEFT(LEFT, A),
        MOVE_RIGHT(RIGHT, D),
        MOVE_UP(UP, W),
        MOVE_DOWN(DOWN, S),
        SHIFT(SHIFT_LEFT, SHIFT_RIGHT),
        PLACE_MULTIPLE(CONTROL_LEFT, CONTROL_RIGHT),
        PAUSE(SPACE),
        TICK_SPEED_1(NUM_1),
        TICK_SPEED_2(NUM_2),
        TICK_SPEED_3(NUM_3);

        public final Pair<Integer, Integer> keys;

        KeyBinding(Integer a, Integer b) {
            keys = Pair.of(a, b);
        }

        KeyBinding(Integer a) {
            keys = Pair.of(a, UNKNOWN);
        }
    }

    public static final int LEFT_MOUSE = Input.Buttons.LEFT;
    public static final int RIGHT_MOUSE = Input.Buttons.RIGHT;

    private static float scrollDelta = 0;

    private static final boolean[] buttonsUp = new boolean[5];
    private static final boolean[] keysUp = new boolean[Input.Keys.MAX_KEYCODE];

    private static InputManager instance = new InputManager();

    private static char keyTyped = Input.Keys.UNKNOWN;
    private static int keyDown = Input.Keys.UNKNOWN;

    private static boolean listeningForKey = false;
    private static Pair<Integer, Integer> bindingToUpdate = null;
    private static Button buttonToUpdate = null;
    private static boolean updateFirst = true;

    static {
        Arrays.fill(buttonsUp, true);
        Arrays.fill(keysUp, true);
    }

    private InputManager() {}

    public static boolean isKeyPressed(KeyBinding keyBinding) {
        return (keyBinding.keys.first != UNKNOWN && Gdx.input.isKeyJustPressed(keyBinding.keys.first)) ||
                (keyBinding.keys.second != UNKNOWN && Gdx.input.isKeyJustPressed(keyBinding.keys.second));
    }

    public static boolean isKeyDown(KeyBinding keyBinding) {
        return (keyBinding.keys.first != UNKNOWN && Gdx.input.isKeyPressed(keyBinding.keys.first)) ||
                (keyBinding.keys.second != UNKNOWN && Gdx.input.isKeyPressed(keyBinding.keys.second));
    }

    public static boolean isKeyPressed(int key) {
        return Gdx.input.isKeyJustPressed(key);
    }

    public static boolean isKeyDown(int key) {
        return Gdx.input.isKeyPressed(key);
    }

    public static boolean isKeyUp(int key) {
        boolean isKeyUp = !isKeyDown(key) && !keysUp[key];
        keysUp[key] = !isKeyDown(key);
        return isKeyUp;
    }

    public static boolean isButtonPressed(int button) {
        return Gdx.input.isButtonJustPressed(button);
    }

    public static boolean isButtonDown(int button) {
        return Gdx.input.isButtonPressed(button);
    }

    public static boolean isButtonUp(int button) {
        boolean isButtonUp = !isButtonDown(button) && !buttonsUp[button];
        buttonsUp[button] = !isButtonDown(button);
        return isButtonUp;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        scrollDelta = amountY;
        return amountY != 0 || amountX != 0;
    }

    @Override
    public boolean keyDown(int keycode) {
        keyDown = keycode;
        return super.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        keyTyped = character;
        return super.keyTyped(character);
    }

    public static float getScroll() {
        return scrollDelta;
    }

    public static boolean isScrolled() {
        return scrollDelta != 0;
    }

    public static void resetKeysAndScroll() {
        keyTyped = Input.Keys.UNKNOWN;
        scrollDelta = 0;
    }

    public static InputManager getInstance() {
        if(instance == null) instance = new InputManager();
        return instance;
    }

    public static char getKeyTyped() {
        char kt = Input.Keys.UNKNOWN;
        if(Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) kt = keyTyped;
        return kt;
    }

    public static int getKeyDown() {
        int kd = Input.Keys.UNKNOWN;
        if(Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) kd = keyDown;
        return kd;
    }

    public static String getKeyName(KeyBinding keyBinding) {
        return Input.Keys.toString(keyBinding.keys.first);
    }

    public static void startListeningForKey(Pair<Integer, Integer> binding, Button button, boolean first) {
        bindingToUpdate = binding;
        buttonToUpdate = button;
        listeningForKey = true;
        updateFirst = first;
    }

    public static void listenForKey() {
        int keyDown = getKeyDown();
        if (keyDown != Input.Keys.UNKNOWN) {
            buttonToUpdate.setText(Input.Keys.toString(keyDown));
            if (updateFirst)
                bindingToUpdate.first = keyDown;
            else
                bindingToUpdate.second = keyDown;
            listeningForKey = false;
        }
    }

    public static void stopListening() {
        if (buttonToUpdate != null) {
            Integer keycode = updateFirst ? bindingToUpdate.first : bindingToUpdate.second;
            String keyName = "";
            if (keycode != UNKNOWN) keyName = Input.Keys.toString(keycode);
            buttonToUpdate.setText(keyName);
        }
        listeningForKey = false;
    }

    public static boolean isListeningForKey() {
        return listeningForKey;
    }

    public static void saveSettings(ObjectOutputStream oos) throws IOException {
        for (KeyBinding binding : KeyBinding.values()) {
            oos.write(0);
            oos.writeUTF(binding.name());
            oos.writeInt(binding.keys.first);
            oos.writeInt(binding.keys.second);
        }
    }

    public static void loadSettings(ObjectInputStream ois) throws IOException {
        while (ois.read() != -1) {
            KeyBinding binding = KeyBinding.valueOf(ois.readUTF());
            binding.keys.first = ois.readInt();
            binding.keys.second = ois.readInt();
        }
    }
}
