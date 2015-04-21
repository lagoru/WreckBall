package com.wreckballs.framework;

/**
 * Interface that allows to carry additional data for bodies in world physics
 * @author lagoru
 *
 */
public class BodyUserData {
	public enum TypeOfBody{
		MAIN_BALL,
		ENEMY_BALL,
		TERRAIN,
		END_POINT
	}
	public TypeOfBody bodyType;
	public boolean isScaled;
	public float x,y,width,height;
	public ImageInterface image;
}
