package com.boxhead.builder;

import com.badlogic.gdx.utils.Timer;

public class Logic {

    private static Timer.Task task = new Timer.Task() {
        @Override
        public void run() {
            World.addTime(1);
        }
    };

    public static Timer.Task getTask() {
        return task;
    }
}
