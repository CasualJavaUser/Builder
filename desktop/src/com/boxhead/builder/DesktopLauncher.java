package com.boxhead.builder;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setWindowedMode(960, 640);
		config.setWindowSizeLimits(960, 640, 9999, 9999);
		config.setTitle("Builder");
		new Lwjgl3Application(BuilderGame.getInstance(), config);
	}
}
