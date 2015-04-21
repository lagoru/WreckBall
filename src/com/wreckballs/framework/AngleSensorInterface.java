package com.wreckballs.framework;


/**Class provides SensorInterface
 * Getters are for non-drift influenced angle values.
 * Setter are to set 0 value for angle
 * @author lagoru
 *
 */
public interface AngleSensorInterface {

	void setZeroAngleX(float x);
	void setZeroAngleY(float y);
	void setZeroAngleZ(float z);
	float getAngleX();
	float getAngleY();
	float getAngleZ();
}
