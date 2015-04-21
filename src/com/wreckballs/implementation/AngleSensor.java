package com.wreckballs.implementation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.wreckballs.framework.AngleSensorInterface;

public class AngleSensor implements AngleSensorInterface, SensorEventListener{

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private float mXAngleOffset = 0, mYAngleOffset = 0, mZAngleOffset = 0;
	private float mXAngle = 0, mYAngle = 0, mZAngle = 0;
	
	public AngleSensor(Context context){
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	/**
	 * Function that starts getting angles
	 */
	public void start() {
        if(!mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME)){
        	Log.d("adrian","registration not found");
        }
    }

    /**
     * Function that stops getting angles
     */
    public void stop() {
        mSensorManager.unregisterListener(this);
    }

	@Override
	public void setZeroAngleX(float x) {
		mXAngleOffset = x;
	}

	@Override
	public void setZeroAngleY(float y) {
		mYAngleOffset = y;
	}

	@Override
	public void setZeroAngleZ(float z) {
		mZAngleOffset = z;		
	}

	@Override
	public float getAngleX() {
		return mXAngle;
	}

	@Override
	public float getAngleY() {
		return mYAngle;
	}

	@Override
	public float getAngleZ() {
		return mZAngle;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float x =event.values[0];
		float y =event.values[1];
		float z =event.values[2];
		float convertion = (float)(180.0/Math.PI);
		mXAngle = (float)Math.atan2(x, Math.hypot(y, z)) * convertion - mXAngleOffset;
		mYAngle = (float)Math.atan2(y, Math.hypot(x, z)) * convertion - mYAngleOffset;
		mZAngle = (float)Math.atan2(z, Math.hypot(x, y)) * convertion - mZAngleOffset;
	}

}
