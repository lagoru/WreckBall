package com.wreckballs.framework;

import com.wreckballs.implementation.BluetoothService;

import android.content.Context;

public interface GameInterface {

    public AudioInterface getAudio();

    public InputInterface getInput();

    public FileIOInterface getFileIO();

    public GraphicsInterface getGraphics();

    public void setScreen(Screen screen);

    public Screen getCurrentScreen();

    public Screen getInitScreen();
    
    public Context getContext();
    
    public boolean isMultiplayer();
    
    public void setIsMultiplayer(boolean isMultiplayer);
    
    public BluetoothService getBluetoothService();
}
