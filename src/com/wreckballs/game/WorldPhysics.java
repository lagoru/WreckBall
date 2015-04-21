package com.wreckballs.game;

import java.util.ArrayList;
import java.util.List;

import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;

import android.hardware.SensorManager;
import android.util.Log;
import android.util.Pair;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.wreckballs.framework.BodyUserData;
import com.wreckballs.framework.BodyUserData.TypeOfBody;
import com.wreckballs.framework.GraphicsInterface;
import com.wreckballs.framework.ImageInterface;

/**
 * Class that contains physics for world
 * along with object, their textures are being kept
 * also drawing is done here
 * @author lagoru
 *
 */
public class WorldPhysics implements ContactListener {
	private PhysicsWorld mPhysicsWorld;
	private Body mBall1, mBall2;
	private List< Body> lBodies;
	private static float sPixelsToMeters; 
	private static float sMetersToPixels;
	private float mAngleX, mAngleY;
	private boolean mEndGame = false;
	
	/**
	 * How many meters will i physical world take map - more meters bigger velocity available
	 * @param meters
	 * @param map_width
	 * @param map_height
	 */
	public WorldPhysics(int meters, int map_width, int map_height){
		mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false,3,3); //without gravity
		mPhysicsWorld.setContactListener(this);
		lBodies = new ArrayList<Body>();
		//cała mapa to 10m wysokości
		sMetersToPixels = map_height/meters;
		sPixelsToMeters = 1.0f/sMetersToPixels;
		//Log.d("adrian width", String.valueOf(map_height));
		//Log.d("adrian scale", String.valueOf(sMetersToPixels)+" "+String.valueOf(sPixelsToMeters));
	}

	public float getPixelsToMeters(){
		return sPixelsToMeters;
	}
	public float getMetersToPixels(){
		return sMetersToPixels;
	}
	public void addMainBall(float x, float y, float radius, ImageInterface image){
		mBall1 = addCircle(x,y,radius,true,true,image, TypeOfBody.MAIN_BALL);
	}
	
	public void addEnemyBall(float x, float y, float radius, ImageInterface image){
		mBall2 = addCircle(x,y,radius,true,true,image, TypeOfBody.ENEMY_BALL);
	}
	
	public Body addCircle(float x, float y, float radius, boolean is_dynamic_body 
			, final boolean image_scaled,ImageInterface image, TypeOfBody type){
		final FixtureDef fixtureDef = PhysicsFactory.createFixtureDef(1, 0.1f, 0.5f);
			
		Body body;
		if(is_dynamic_body){
			body = PhysicsFactory.createCircleBody(mPhysicsWorld, x
					, y, radius, BodyType.DynamicBody
				, fixtureDef,1);
			body.setBullet(true);
		}else{
			body = PhysicsFactory.createCircleBody(mPhysicsWorld, x ,
					y, radius, BodyType.StaticBody
					, fixtureDef,1);
		}
		BodyUserData paint_data = new BodyUserData();
		paint_data.height = (int)(radius*2*sMetersToPixels);
		paint_data.width = (int)(radius*2*sMetersToPixels);
		paint_data.image = image;
		paint_data.isScaled = image_scaled;
		paint_data.bodyType = type;
		body.setUserData(paint_data);
		body.setBullet(true);
		
		lBodies.add( body);
		return body;
	}
	
	public Body addRectangle(float x, float y, float width, float height ,
			boolean is_dynamic_body, boolean image_scaled ,ImageInterface image, TypeOfBody type){
		
		final FixtureDef fixtureDef = PhysicsFactory.createFixtureDef(1, 0.1f, 0.5f);
		
		Body body;
		if(is_dynamic_body){
			body = PhysicsFactory.createBoxBody(mPhysicsWorld, x, 
					y, width,
					height, BodyType.DynamicBody, fixtureDef,1);
			body.setBullet(true);
		}else{
			body = PhysicsFactory.createBoxBody(mPhysicsWorld, x, 
					y, width ,
					height, BodyType.StaticBody, fixtureDef,1);
		}
		BodyUserData paint_data = new BodyUserData();
		paint_data.height = (int)(height*sMetersToPixels);
		paint_data.width = (int)(width*sMetersToPixels);
		paint_data.image = image;
		paint_data.isScaled = image_scaled;
		paint_data.bodyType = type;
		body.setUserData(paint_data);
		lBodies.add( body);
		return body;
	}
	
	public void step(float dt){
		Vector2 velocities = mBall1.getLinearVelocity();
		velocities.x += SensorManager.GRAVITY_EARTH*Math.sin(Math.toRadians(mAngleY))*dt;
		velocities.y += SensorManager.GRAVITY_EARTH*Math.sin(Math.toRadians(mAngleX))*dt;
		mBall1.setLinearVelocity(velocities);
		mPhysicsWorld.onUpdate(dt);
	}
	
	public List<BodyUserData> getPaintDataObjects(){
		List<BodyUserData> list = new ArrayList<BodyUserData>();
		
		for(Body body : lBodies){
			BodyUserData data = (BodyUserData)body.getUserData();
			data.x = (int)(body.getPosition().x*sMetersToPixels);
			data.y = (int)(body.getPosition().y*sMetersToPixels);
			list.add(data);
		}
		return list;
	}
	
	/**
	 * Function that applies angle on ball
	 * @param x
	 * @param y
	 */
	public void setAngle(float x, float y){
		mAngleX = x;
		mAngleY = y;
		//Log.d("adrina degree", String.valueOf(x));
		//Log.d("adrina comp", String.valueOf(Math.sin(Math.toRadians(x))));
		//Log.d("adrina final", String.valueOf(Math.sin(mBall1.getMass()*9.78*Math.sin(Math.toRadians(x)))));
		//mBall1.applyForce((float)(mBall1.getMass()*9.78*Math.sin(Math.toRadians(y)))
		//		, (float)(mBall1.getMass()*9.78*Math.sin(Math.toRadians(x))),
		//		mBall1.getPosition().x+1, mBall1.getPosition().y+1);
	}

	@Override
	public void beginContact(Contact contact) {
		BodyUserData data1 = (BodyUserData)contact.getFixtureA().getBody().getUserData();
		BodyUserData data2 = (BodyUserData)contact.getFixtureB().getBody().getUserData();
		
		if((data1.bodyType == TypeOfBody.END_POINT) && (data2.bodyType == TypeOfBody.MAIN_BALL)){
			mEndGame = true;
		}
		if((data2.bodyType == TypeOfBody.END_POINT) && (data1.bodyType == TypeOfBody.MAIN_BALL)){
			mEndGame = true;
		}
	}

	@Override
	public void endContact(Contact contact) {
		
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		
	}
	
	public boolean isGameFinished(){
		return mEndGame;
	}

	public void setPositionOfEnemyBall(Float x, Float y,
			Float vel_x, Float vel_y) {
		mBall2.setLinearVelocity(vel_x, vel_y);
		mBall2.setTransform(x, y, mBall2.getAngle());
	}
}
