package com.wreckballs.implementation;

import java.util.List;

import com.wreckballs.framework.InputInterface.TouchEvent;

import android.view.View.OnTouchListener;


public interface TouchHandler extends OnTouchListener {
    public boolean isTouchDown(int pointer);
   
    public int getTouchX(int pointer);
   
    public int getTouchY(int pointer);
   
    public List<TouchEvent> getTouchEvents();
}
