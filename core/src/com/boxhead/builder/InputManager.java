package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.boxhead.builder.utils.Pair;

import java.util.Arrays;

public class InputManager extends InputAdapter {
    public static Pair<Integer, Integer> LEFT = Pair.of(Input.Keys.LEFT, Input.Keys.A);
    public static Pair<Integer, Integer> RIGHT = Pair.of(Input.Keys.RIGHT, Input.Keys.D);
    public static Pair<Integer, Integer> UP = Pair.of(Input.Keys.UP, Input.Keys.W);
    public static Pair<Integer, Integer> DOWN = Pair.of(Input.Keys.DOWN, Input.Keys.S);
    public static Pair<Integer, Integer> SHIFT = Pair.of(Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT);
    public static Pair<Integer, Integer> CONTROL = Pair.of(Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT);

    public static final int LEFT_MOUSE = Input.Buttons.LEFT;
    public static final int RIGHT_MOUSE = Input.Buttons.RIGHT;

    private static float scrollDelta = 0;

    private static final boolean[] buttonsUp = new boolean[5];
    private static final boolean[] keysUp = new boolean[Input.Keys.MAX_KEYCODE];

    private static InputManager instance = new InputManager();

    private static int keyPressed = -1;
    private static char keyTyped = 0;

    static {
        Arrays.fill(buttonsUp, true);
        Arrays.fill(keysUp, true);
    }

    private InputManager() {}

    public static boolean isKeyPressed(Pair<Integer, Integer> key) {
        return (key.first != null && Gdx.input.isKeyJustPressed(key.first)) || (key.second != null && Gdx.input.isKeyJustPressed(key.second));
    }

    public static boolean isKeyDown(Pair<Integer, Integer> key) {
        return (key.first != null && Gdx.input.isKeyPressed(key.first)) || (key.second != null && Gdx.input.isKeyPressed(key.second));
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
        keyPressed = keycode;
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

    public static void resetScroll() {
        scrollDelta = 0;
    }

    public static InputManager getInstance() {
        if(instance == null) instance = new InputManager();
        return instance;
    }

    public static int getPressedKey() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) return keyPressed;
        return -1;
    }

    public static char getKeyTyped() {
        char temp;
        if(Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) temp = keyTyped;
        else temp = 0;
        keyTyped = 0;
        return temp;
    }
}
