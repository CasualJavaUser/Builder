package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.boxhead.builder.utils.Pair;

public class InputManager extends InputAdapter {
    public static Pair<Integer, Integer> LEFT = Pair.of(Input.Keys.LEFT, Input.Keys.A);
    public static Pair<Integer, Integer> RIGHT = Pair.of(Input.Keys.RIGHT, Input.Keys.D);
    public static Pair<Integer, Integer> UP = Pair.of(Input.Keys.UP, Input.Keys.W);
    public static Pair<Integer, Integer> DOWN = Pair.of(Input.Keys.DOWN, Input.Keys.S);
    public static Pair<Integer, Integer> FAST = Pair.of(Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT);

    public static final int LEFT_MOUSE = Input.Buttons.LEFT;
    public static final int RIGHT_MOUSE = Input.Buttons.RIGHT;

    private static float scrollDelta = 0;

    private static InputManager instance = null;

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

    public static boolean isButtonPressed(int button) {
        return Gdx.input.isButtonJustPressed(button);
    }

    public static boolean isButtonDown(int button) {
        return Gdx.input.isButtonPressed(button);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        scrollDelta = amountY;
        return amountY != 0f || amountX != 0f;
    }

    public static float getScroll() {
        float temp = scrollDelta;
        scrollDelta = 0;
        return temp;
    }

    public static boolean isScrolled() {
        return scrollDelta != 0;
    }

    public static InputManager getInstance() {
        if(instance == null) instance = new InputManager();
        return instance;
    }


}
