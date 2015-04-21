package com.wreckballs.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Window;
import android.view.WindowManager;

import com.wreckballs.framework.AudioInterface;
import com.wreckballs.framework.FileIOInterface;
import com.wreckballs.framework.GameInterface;
import com.wreckballs.framework.InputInterface;
import com.wreckballs.framework.Screen;

public abstract class Game extends Activity implements GameInterface {
    FastRenderView renderView;
    Graphics graphics;
    AudioInterface audio;
    InputInterface input;
    FileIOInterface fileIO;
    Screen screen;
    WakeLock wakeLock;
    boolean mIsMultiplayer;
    BluetoothService mBluetoothService;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        int frameBufferWidth = isPortrait ? 800: 1280;
        int frameBufferHeight = isPortrait ? 1280: 800;
        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
                frameBufferHeight, Config.RGB_565);
       
        float scaleX = (float) frameBufferWidth
                / getWindowManager().getDefaultDisplay().getWidth();
        float scaleY = (float) frameBufferHeight
                / getWindowManager().getDefaultDisplay().getHeight();

        renderView = new FastRenderView(this, frameBuffer);
        graphics = new Graphics(getAssets(), frameBuffer);
        fileIO = new FileIO(this);
        audio = new Audio(this);
        input = new Input(this, renderView, scaleX, scaleY);
        screen = getInitScreen();
        setContentView(renderView);
       
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyGame");
        mBluetoothService = new BluetoothService(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        screen.resume();
        //mBluetoothService.register();
        renderView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        renderView.pause();
        screen.pause();
        
        if (isFinishing())
            screen.dispose();
    }

    @Override
    public InputInterface getInput() {
        return input;
    }

    @Override
    public FileIOInterface getFileIO() {
        return fileIO;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public AudioInterface getAudio() {
        return audio;
    }
    
    @Override
    public void setScreen(Screen screen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");

        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);
        this.screen = screen;
    }
   
    public Screen getCurrentScreen() {
        return screen;
    }
    
    @Override
    public Context getContext(){
    	return this;
    }
    
    @Override
	public boolean isMultiplayer() {
		return mIsMultiplayer;
	}

	@Override
	public void setIsMultiplayer(boolean isMultiplayer) {
		mIsMultiplayer = isMultiplayer;
	}

	@Override
	public BluetoothService getBluetoothService() {
		return mBluetoothService;
	}
	
	@Override 
	public void onBackPressed(){
		if(screen != null){
			screen.backButton();
		}
	}
}
