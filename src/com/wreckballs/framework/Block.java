package com.wreckballs.framework;

/**Map is builded of blocks
 * @author lagoru
 *
 */
public class Block{
	/**
	 * Those are not real values - they need to be proportional changed
	 */
	public float x,y,height,width;
	
	public Block(){}
	
	@Override
	public String toString(){
		String out = String.valueOf(x) + ":" + String.valueOf(y) + ":" 
				+ String.valueOf(height) + ":" + String.valueOf(width);
		return out;
	}
	
	public void fromString(String string){
		String[] splited = string.split(":");
		x = Float.parseFloat(splited[0]);
		y = Float.parseFloat(splited[1]);
		height = Float.parseFloat(splited[2]);
		width = Float.parseFloat(splited[3]);
	}
}
