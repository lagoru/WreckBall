package com.wreckballs.game;

import com.wreckballs.framework.Screen;
import com.wreckballs.implementation.BluetoothService;
import com.wreckballs.implementation.Game;

public class MainGameActivity extends Game{

	@Override
	public Screen getInitScreen() {
		return new PreLoadingScreen(this);
	}
}
