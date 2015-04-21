package com.wreckballs.game;


import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.wreckballs.framework.GameInterface;
import com.wreckballs.framework.GraphicsInterface;
import com.wreckballs.framework.Screen;
import com.wreckballs.framework.InputInterface.TouchEvent;

public class MultiplayerScreen extends Screen{
	enum MenuStatus{
		WAITING,
		SERVER_CHOOSED,
		CONNECTED
	}
	private MenuStatus mStatus = MenuStatus.WAITING;
	private Paint mPaint;

	private Pair<String,String> mServerChoosed;

	public MultiplayerScreen(GameInterface game) {
		super(game);
		game.getBluetoothService().register();
		//mRefreshBluetoothWorker.execute();
		//start discovery of other devices
		game.getBluetoothService().startDiscovery();
		//start server
		game.getBluetoothService().start();
		mPaint = new Paint();
		mPaint.setTextSize(60);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.WHITE);
		game.getBluetoothService().setIsServer(true);
	}

	@Override
	public void update(float deltaTime) {
		GraphicsInterface g = game.getGraphics();
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		if(mStatus == MenuStatus.WAITING){
			int len = touchEvents.size();
			for (int i = 0; i < len; i++) {
				TouchEvent event = touchEvents.get(i);
				if (event.type == TouchEvent.TOUCH_UP) {
					if (inBounds(event, g.getWidth()/2 - Assets.sButtonPickServer.getWidth()/2, 
							g.getHeight()/2 + Assets.sButtonPickServer.getHeight(), 
							Assets.sButtonPickServer.getWidth(), Assets.sButtonPickServer.getHeight())) {
						new AsyncTask< Void,Void,Void >(){
							@Override
							protected void onProgressUpdate(Void ...params) {
								super.onProgressUpdate(params);
								final List<Pair<String,String>> availableDevices = 
										game.getBluetoothService().getAvailableDevices();

								CharSequence[] sequence = new CharSequence[availableDevices.size()];
								for(int j = 0; j < availableDevices.size(); j++){
									sequence[j] = availableDevices.get(j).first;
								}
								AlertDialog.Builder builder = new AlertDialog.Builder(game.getContext());
								// Set the dialog title
								builder.setTitle("Pick a server").setSingleChoiceItems(sequence, -1
										, new  DialogInterface.OnClickListener(){
									@Override
									public void onClick(DialogInterface arg0, int which) {
										mServerChoosed = Pair.create(availableDevices.get(which).first
												, availableDevices.get(which).second);
									}
								})
								// Set the action buttons
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										// User clicked OK, so save the mSelectedItems results somewhere
										// or return them to the component that opened the dialog
										mStatus = MenuStatus.SERVER_CHOOSED;
									}
								})
								.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {

									}
								});
								builder.create();
								builder.show();
							}

							@Override
							protected Void doInBackground(Void... params) {
								this.publishProgress(params);
								game.getBluetoothService().startDiscovery();
								return null;
							}
						}.execute();
					}
				}
			}
		}

		if(mStatus == MenuStatus.SERVER_CHOOSED){
			game.getBluetoothService().setIsServer(false);
			game.getBluetoothService().stop(); //lets stop server before connection
			game.getBluetoothService().connect(mServerChoosed.second);
			mStatus = MenuStatus.CONNECTED;
		}

		if(game.getBluetoothService().getState() == game.getBluetoothService().STATE_CONNECTED){
			game.setScreen(new GameScreen(game));
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
		g.drawImage(Assets.sWaitForServerString, g.getWidth()/2 - Assets.sWaitForServerString.getWidth()/2
				, g.getHeight()/2 - Assets.sWaitForServerString.getHeight()/2 );
		g.drawImage(Assets.sButtonPickServer, g.getWidth()/2 - Assets.sButtonPickServer.getWidth()/2,
				g.getHeight()/2 + Assets.sButtonPickServer.getHeight());
	}

	@Override
	public void pause() {
		//game.getBluetoothService().stop();
	}

	@Override
	public void resume() {
		//game.getBluetoothService().start();
	}

	@Override
	public void dispose() {
		game.getBluetoothService().stopDiscovery();
	}

	@Override
	public void backButton() {
		game.setScreen(new MainMenuScreen(game));
	}
}
