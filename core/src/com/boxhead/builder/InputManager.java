package com.boxhead.builder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

public class InputManager {
    public static Pair<Integer, Integer> LEFT = Pair.of(Input.Keys.LEFT, Input.Keys.A);
    public static Pair<Integer, Integer> RIGHT = Pair.of(Input.Keys.RIGHT, Input.Keys.D);
    public static Pair<Integer, Integer> UP = Pair.of(Input.Keys.UP, Input.Keys.W);
    public static Pair<Integer, Integer> DOWN = Pair.of(Input.Keys.DOWN, Input.Keys.S);
    public static Pair<Integer, Integer> FAST = Pair.of(Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT);

    public static final int LEFT_MOUSE = Input.Buttons.LEFT;
    public static final int RIGHT_MOUSE = Input.Buttons.RIGHT;

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
}
