package com.wreckballs.game;

import com.wreckballs.framework.GameInterface;
import com.wreckballs.framework.GraphicsInterface;
import com.wreckballs.framework.Screen;
import com.wreckballs.framework.GraphicsInterface.ImageFormat;

public class LoadingScreen extends Screen{

	public LoadingScreen(GameInterface game) {
		super(game);
	}

	@Override
	public void update(float deltaTime) {
		GraphicsInterface g = game.getGraphics();
		Assets.sMenu = g.newImage("main_menu.jpg", ImageFormat.RGB565);
		Assets.sButton_one_player = g.newImage("button_single.jpg", ImageFormat.RGB565);
		Assets.sButton_two_players = g.newImage("button_multi.jpg", ImageFormat.RGB565);
        Assets.sBall1= g.newImage("ball_1.jpg", ImageFormat.RGB565);
        Assets.sGameFrameTexture = g.newImage("wood.jpg", ImageFormat.RGB565);
        Assets.sButtonPickServer = g.newImage("pick_server_button.jpg", ImageFormat.RGB565);
        Assets.sWaitForServerString = g.newImage("wait_for_server.jpg", ImageFormat.RGB565);
        Assets.sPressToStartButton = g.newImage("press_to_start_button.jpg", ImageFormat.RGB565);
        //Assets.sBall2= g.newImage("ball_2.jpg", ImageFormat.RGB565);
        //Assets.sMenu= g.newImage("menu.jpg", ImageFormat.RGB565);
        //Assets.sBall1= g.newImage("ball_1.jpg", ImageFormat.RGB565);
       
        game.setScreen(new MainMenuScreen(game));
		
	}

	@Override
	public void paint(float deltaTime) {
		GraphicsInterface g = game.getGraphics();
        g.drawScaledImage(Assets.sLoadingScreen, 0, 0, g.getWidth(), g.getHeight(),
        		0, 0, Assets.sLoadingScreen.getWidth(), Assets.sLoadingScreen.getHeight());
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
