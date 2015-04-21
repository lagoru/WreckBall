package com.wreckballs.game;

import java.util.ArrayList;
import java.util.List;

import com.wreckballs.framework.Block;
import com.wreckballs.framework.BodyUserData.TypeOfBody;
import com.wreckballs.framework.GameInterface;
import com.wreckballs.framework.GraphicsInterface;
import com.wreckballs.framework.BodyUserData;
import com.wreckballs.framework.InputInterface.TouchEvent;
import com.wreckballs.framework.Screen;
import com.wreckballs.implementation.AngleSensor;
import com.wreckballs.implementation.BluetoothService;
import com.wreckballs.implementation.BluetoothService.TypeOfConnection;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

public class GameScreen extends Screen {
	enum GameState {
		CreateMap,Ready,Running, GameOver
	}

	private boolean mPlayerWon = false;
	private boolean mSecondPlayerRunning = false;
	private GameState state = GameState.CreateMap;

	private Paint paint;
	private AngleSensor mSensor;
	private WorldPhysics mPhysics;
	private BluetoothService mBluetoothService;

	//Strings used in communication
	//during sending map
	private final String MAP_SEND = "MAP_SEND";
	private final String END_POINT_SEND = "END_POINT_SEND";
	private final String OK = "OK";
	//during play
	private final String RUNNING = "RUNNING";
	private final String READY = "READY";
	private final String WON = "WON";

	//Data for client of game
	ArrayList<Block> mBlocks;
	Point mEndPoint;

	private int sWidithScale = 900, sHeigthScale = 800; 
	int mOffsetX;

	public GameScreen(GameInterface game) {
		super(game);

		// Defining a paint object
		paint = new Paint();
		paint.setTextSize(30);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);

		mSensor = new AngleSensor(game.getContext());
		mBluetoothService = game.getBluetoothService();
		
	}

	/**Generates map and end point
	 * 
	 */
	private void generateMap(){
		MazeGenerator mGenerator = new MazeGenerator(10,10);
		mBlocks = mGenerator.getBlocks();
		mEndPoint = mGenerator.getEndPoint();
	}

	/**Creates drawing map
	 * 
	 */
	private void createMap(){
		// Initialize game objects here
		//I suppose 800x900 should be enough
		//map 10x10
		int meters_on_map = 100;
		float scale = meters_on_map/10;
		mPhysics = new WorldPhysics(meters_on_map, sWidithScale , sHeigthScale);

		mOffsetX = (game.getGraphics().getWidth() - game.getGraphics().getHeight())/2;

		mPhysics.addMainBall(0.5f*scale ,0.5f*scale, 0.2f*scale, Assets.sBall1);

		//point maze at the middle of the screen

		for(Block block : mBlocks){
			mPhysics.addRectangle(block.x*scale , block.y*scale, block.width*scale, 
					block.height*scale, false, false, Assets.sGameFrameTexture, TypeOfBody.TERRAIN);
		}
		mPhysics.addRectangle((mEndPoint.x + (float)0.5)*scale, (mEndPoint.y+ (float)0.5)*scale,
				0.6f*scale, 0.6f*scale, false, false, Assets.sGameFrameTexture, TypeOfBody.END_POINT);
	}

	/**Sends data of map to client
	 * 
	 */
	private void sendDataToClient(){
		
		mBluetoothService.write(MAP_SEND);
		
		if(!OK.equals(mBluetoothService.read())){
			//bad connection - return to main menu
			game.setScreen(new MainMenuScreen(game));
			return;
		}
		
		for(int i = 0; i < mBlocks.size(); i++){
			mBluetoothService.write(mBlocks.get(i).toString());
			Log.d("adrian", "block sended2");
			if(!OK.equals(mBluetoothService.read())){
				//bad connection - return to main menu
				game.setScreen(new MainMenuScreen(game));
				return;
			}
		}
		mBluetoothService.write(END_POINT_SEND);
		if(!OK.equals(mBluetoothService.read())){
			//bad connection - return to main menu
			game.setScreen(new MainMenuScreen(game));
			return;
		}
		String endPointString = String.valueOf(mEndPoint.x) +":"+String.valueOf(mEndPoint.y);
		mBluetoothService.write(endPointString);
		mBluetoothService.write(END_POINT_SEND);
		if(!OK.equals(mBluetoothService.read())){
			//bad connection - return to main menu
			game.setScreen(new MainMenuScreen(game));
			return;
		}
	}

	/**Function that receives data from server and builds map
	 * 
	 */
	private void receiveDataFromServer(){
		
		if(!MAP_SEND.equals(mBluetoothService.read())){
			//bad connection - return to main menu
			game.setScreen(new MainMenuScreen(game));
			return;
		}
		mBluetoothService.write(OK);
		
		String readMessage = String.valueOf(mBluetoothService.read());
		while(!END_POINT_SEND.equals(readMessage)){
			if(readMessage == null){
				//return to menu if problem
				game.setScreen(new MainMenuScreen(game));
				return;
			}
			Block block = new Block();
			block.fromString(readMessage);
			mBlocks.add(block);
			mBluetoothService.write(OK);
			readMessage = String.valueOf(mBluetoothService.read());
		}
		mBluetoothService.write(OK);
		readMessage = String.valueOf(mBluetoothService.read());
		if(readMessage == null){
			//return to menu if problem
			game.setScreen(new MainMenuScreen(game));
			return;
		}
		String[] splited = readMessage.split(":");
		mEndPoint = new Point(Integer.valueOf(splited[0]),Integer.valueOf(splited[1]));
		mBluetoothService.write(OK);
	}

	@Override
	public void update(float deltaTime) {
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		// We have four separate update methods in this example.
		// Depending on the state of the game, we call different update methods.
		// Refer to Unit 3's code. We did a similar thing without separating the
		// update methods.
		//game.getBluetoothService().write("Handshake");
		//game.getBluetoothService().read();
		if(state == GameState.CreateMap){
			if(game.isMultiplayer()){
				if(mBluetoothService.getTypeOfConnection() == TypeOfConnection.SERVER){
					generateMap();
					createMap();
					sendDataToClient();
				}else{
					receiveDataFromServer();
					createMap();
				}
			}else{
				generateMap();
				createMap();
			}
			state = GameState.Ready;
		}
		if (state == GameState.Ready){
			updateReady(touchEvents);
		}
		if (state == GameState.Running){
			updateRunning(touchEvents, deltaTime);
		}
		if (state == GameState.GameOver){
			updateGameOver(touchEvents);
		}
	}

	private void updateReady(List<TouchEvent> touchEvents) {
		GraphicsInterface g = game.getGraphics();
		// This example starts with a "Ready" screen.
		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (inBounds(event, 5, 
					g.getHeight()/2 - Assets.sPressToStartButton.getHeight()/2, 
					Assets.sPressToStartButton.getWidth(), Assets.sPressToStartButton.getHeight())){
				state = GameState.Running;
			}
		}

		if(RUNNING.equals(mBluetoothService.read())){
			mSecondPlayerRunning = true;
		}
	}

	private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {
		if(!game.isMultiplayer()){
			mPhysics.setAngle(mSensor.getAngleX(), mSensor.getAngleY());
			mPhysics.step(deltaTime);

			if(mPhysics.isGameFinished()){
				state = GameState.GameOver;
				mPlayerWon = true;
			}
		}else{
			if(mSecondPlayerRunning){
				mPhysics.setAngle(mSensor.getAngleX(), mSensor.getAngleY());
				mPhysics.step(deltaTime);

				String read = String.valueOf(mBluetoothService.read());
				String[] splited = read.split(":");
				if(WON.equals(splited[0])){
					state = GameState.GameOver;
					mPlayerWon = false;
					return;
				}

				if(RUNNING.equals(splited[0])){
					mPhysics.setPositionOfEnemyBall(Float.valueOf(splited[1]),
							Float.valueOf(splited[2]),Float.valueOf(splited[3]),Float.valueOf(splited[4]));
				}else{
					//blad 
					game.setScreen(new MainMenuScreen(game));
				}

				if(mPhysics.isGameFinished()){
					state = GameState.GameOver;
					mPlayerWon = true;
				}
			}
		}
	}

	private void updateGameOver(List<TouchEvent> touchEvents) {

		if(game.isMultiplayer()){
			state = GameState.Ready;
		}else{
			generateMap();
			state = GameState.CreateMap;
		}
	}

	@Override
	public void paint(float deltaTime) {
		GraphicsInterface g = game.getGraphics();
		g.clearScreen(Color.WHITE);

		//if (state == GameState.CreateMap)
		//drawPausedUI();
		if (state == GameState.Ready)
			drawReadyUI();
		if (state == GameState.Running)
			drawRunningUI();
		if (state == GameState.GameOver)
			drawGameOverUI();
	}

	public void paintBodies(GraphicsInterface g){
		List<BodyUserData> list = mPhysics.getPaintDataObjects();
		for(BodyUserData paintData : list){
			int x = (int)(paintData.x - paintData.width/2);
			int y = (int)(paintData.y - paintData.height/2);

			if(paintData.isScaled){
				//Log.d("adrian data", String.valueOf(x) + " " + String.valueOf(y) +
				//		" " + String.valueOf(paintData.width)+ " " + String.valueOf(paintData.height));
				g.drawScaledImage(paintData.image, x + mOffsetX, y, 
						(int)(paintData.width), (int)(paintData.height)
						, 0, 0, paintData.image.getWidth(), paintData.image.getHeight());
			}else{
				g.drawImage(paintData.image, x+ mOffsetX, y, 0, 0, 
						(int)(paintData.width), (int)(paintData.height));
			}
		}
	}

	private void nullify() {

		// Set all variables to null. You will be recreating them in the
		// constructor.
		paint = null;

		// Call garbage collector to clean up memory.
		System.gc();
	}

	private void drawReadyUI() {
		GraphicsInterface g = game.getGraphics();
		drawRunningUI();
		g.drawImage(Assets.sPressToStartButton, 5
				, g.getHeight()/2 - Assets.sPressToStartButton.getHeight()/2 );
	}

	private void drawRunningUI() {
		GraphicsInterface g = game.getGraphics();

		g.drawString(String.valueOf(mSensor.getAngleX()), 100, 80, paint);
		g.drawString(String.valueOf(mSensor.getAngleY()), 100, 120, paint);
		paintBodies(g);
	}

	private void drawPausedUI() {
		GraphicsInterface g = game.getGraphics();
		// Darken the entire screen so you can display the Paused screen.
		g.drawARGB(155, 0, 0, 0);

	}

	private void drawGameOverUI() {
		GraphicsInterface g = game.getGraphics();
		g.drawRect(0, 0, 1281, 801, Color.BLACK);
		g.drawString("GAME OVER.", 640, 300, paint);

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
	public void pause() {
		mSensor.stop();
	}

	@Override
	public void resume() {
		mSensor.start();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void backButton() {
		game.setScreen(new MainMenuScreen(game));
	}
}
