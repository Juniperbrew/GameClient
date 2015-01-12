package com.juniper.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.juniper.game.GameClient;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		//.width = 1600;
		//config.height = 900;
		config.width = 800;
		config.height = 450;

		new LwjglApplication(new GameClient(), config);
	}
}
