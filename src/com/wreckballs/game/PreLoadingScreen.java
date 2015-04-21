package com.wreckballs.game;

import com.wreckballs.framework.GameInterface;
import com.wreckballs.framework.GraphicsInterface;
import com.wreckballs.framework.GraphicsInterface.ImageFormat;
import com.wreckballs.framework.Screen;

//no painting here
public class PreLoadingScreen extends Screen{

	public PreLoadingScreen(GameInterface game) {
		super(game);
	}

	@Override
	public void update(float deltaTime) {
		GraphicsInterface g = game.getGraphics();
        Assets.sLoadingScreen= g.newImage("loading_screen.jpg", ImageFormat.RGB565);

       
        game.setScreen(new LoadingScreen(game));
		
	}

	@Override
	public void paint(float deltaTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void backButton() {
		// TODO Auto-generated method stub
		
	}

}
