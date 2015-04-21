package com.wreckballs.game;

import java.util.List;

import com.wreckballs.framework.GameInterface;
import com.wreckballs.framework.GraphicsInterface;
import com.wreckballs.framework.InputInterface.TouchEvent;
import com.wreckballs.framework.Screen;



public class MainMenuScreen extends Screen {
	public MainMenuScreen(GameInterface game) {
		super(game);
	}


	@Override
	public void update(float deltaTime) {
		GraphicsInterface g = game.getGraphics();
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();


		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_UP) {
				if (inBounds(event, g.getWidth()/2 - Assets.sButton_one_player.getWidth()/2, 
						g.getHeight()/2 - Assets.sButton_one_player.getHeight()/2, 
						Assets.sButton_one_player.getWidth(), Assets.sButton_one_player.getHeight())) {
					//START GAME
					game.setIsMultiplayer(false);
					game.setScreen(new GameScreen(game));              
				}
				
				if (inBounds(event, g.getWidth()/2 - Assets.sButton_two_players.getWidth()/2, 
						g.getHeight()/2 + Assets.sButton_two_players.getHeight(), 
						Assets.sButton_two_players.getWidth(), Assets.sButton_two_players.getHeight())) {
					//START GAME
					game.setIsMultiplayer(true);
					game.setScreen(new MultiplayerScreen(game));              
				}
			}
		}
	}

	private boolean inBounds(TouchEvent event, int x, int y, int width,
			int height) {
		if (event.x > x && event.x < x + width - 1 && event.y > y
				&& event.y < y + height - 1)
			return true;
		else
			return false;
	}

	@Override
	public void paint(float deltaTime) {
		GraphicsInterface g = game.getGraphics();
		g.clearScreen(0);
		g.drawScaledImage(Assets.sMenu, 0, 0, 
				g.getWidth(), g.getHeight()
				, 0, 0, Assets.sMenu.getWidth(), Assets.sMenu.getHeight());
		
		//przyciski w połowie
		g.drawImage(Assets.sButton_one_player, g.getWidth()/2 - Assets.sButton_one_player.getWidth()/2
				, g.getHeight()/2 - Assets.sButton_one_player.getHeight()/2 );
		
		g.drawImage(Assets.sButton_two_players, g.getWidth()/2 - Assets.sButton_two_players.getWidth()/2
				, g.getHeight()/2 + Assets.sButton_two_players.getHeight() );
	}


	@Override
	public void pause() {
	}


	@Override
	public void resume() {


	}


	@Override
	public void dispose() {


	}


	@Override
	public void backButton() {
		//Display "Exit Game?" Box


	}
}
