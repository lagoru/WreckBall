package com.wreckballs.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.wreckballs.framework.Block;

import android.graphics.Point;
import android.util.Log;


public class MazeGenerator {
	private final int x;
	private final int y;
	private final int[][] maze;

	public MazeGenerator(int x, int y) {
		this.x = x;
		this.y = y;
		maze = new int[this.x][this.y];
		generateMaze(0, 0);
	}

	private void generateMaze(int cx, int cy) {
		DIR[] dirs = DIR.values();
		Collections.shuffle(Arrays.asList(dirs));
		for (DIR dir : dirs) {
			int nx = cx + dir.dx;
			int ny = cy + dir.dy;
			if (between(nx, x) && between(ny, y)
					&& (maze[nx][ny] == 0)) {
				maze[cx][cy] |= dir.bit;
				maze[nx][ny] |= dir.opposite.bit;
				generateMaze(nx, ny);
			}
		}
	}

	private static boolean between(int v, int upper) {
		return (v >= 0) && (v < upper);
	}

	private enum DIR {
		N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
		private final int bit;
		private final int dx;
		private final int dy;
		private DIR opposite;

		// use the static initializer to resolve forward references
		static {
			N.opposite = S;
			S.opposite = N;
			E.opposite = W;
			W.opposite = E;
		}

		private DIR(int bit, int dx, int dy) {
			this.bit = bit;
			this.dx = dx;
			this.dy = dy;
		}
	};
	
	/**Zwraca listę obiektow do rysowania na mapie 
	 * łączy mniejsze bloki w wysokości
	 * @return
	 */
	public ArrayList<Block> getBlocks(){
		ArrayList<Block> blockList = new ArrayList<Block>();

		float ceilingBlockWidth = 1;
		//koordynaty są podawane od POŁOWY, (x i y) to środek rysowanego wielokątu
		//ceilings
		for(int i = 0; i < x; i++){
			for(int j = 0; j < y; j++){
				if((maze[j][i] & 1) == 0){
					if(blockList.size() != 0 && 
							blockList.get(blockList.size()-1).x + blockList.get(blockList.size()-1).width 
							== j){
						blockList.get(blockList.size()-1).width++;
					}else{
						Block block = new Block();
						block.width = 1;
						block.height = 0.2f;
						block.x = j;
						block.y = i;
						blockList.add(block);
					}
				}
			}
		}
		//poprawiamy połozenie x (by było pośrodku bloku)
		for(Block block : blockList){
			block.x = block.x + block.width/2;
		}

		int startOfWall = blockList.size();
		boolean firstBlock = true;
		//walls
		for(int i = 0; i < x; i++){
			for(int j = 0; j < y; j++){
				if((maze[i][j] & 8) == 0 ){
					//if(firstBlock && blockList.get(blockList.size()-1).y + blockList.get(blockList.size()-1).height
					//		== (j+1)){
					//	blockList.get(blockList.size()-1).height++;
					//	Log.d("adrian","joined1");
					//}else{
						firstBlock=false;
						Block block = new Block();
						block.width = (float)0.2;
						block.height = 1;
						block.x = i;
						block.y = j;
						blockList.add(block);
					//}
					//Log.d("adrian", String.valueOf(blockList.get(blockList.size()-1).y +
						//	blockList.get(blockList.size()-1).height)+"  "+ String.valueOf(j));
				}
				//TODO do zobaczenia później
			}
		}
		//poprawiamy połozenie y (by było pośrodku bloku)
		for(int i = startOfWall; i < blockList.size();i++){
			blockList.get(i).y = blockList.get(i).y + blockList.get(i).height/2.0f;
		}
		//floor
		Block block = new Block();
		block.width = 0;
		block.height=(float)0.2;
		for(int i = 0; i < x; i++){
			block.width++;
		}
		block.x = block.width/2;
		block.y = block.height/2+y-(float)0.2;//zeby bylo widac troche
		blockList.add(block);

		//right wall
		Block block1= new Block();

		block1.height=0;
		block1.width= (float)0.2;
		for(int i = 0; i < y; i++){
			block1.height++;
		}
		block1.x=x+(block1.width/2);
		block1.y=block1.height/2;
		blockList.add(block1);

		return blockList;
	}

	public Point getEndPoint(){
		endPointLength = 0;
		mVisited = new boolean[x][y];
		findEndPoint(0,0,0);

		return endPoint;
	}

	private int endPointLength;
	Point endPoint = new Point();
	private boolean[][] mVisited;

	private void findEndPoint(int _x, int _y, int len){
		if(_x < 0 || _x > x || _y < 0 || _y > y || mVisited[_x][_y]){
			return;
		}

		mVisited[_x][_y] = true;
		if(endPointLength < len){
			endPointLength = len;
			endPoint.x=_x;
			endPoint.y=_y;
		}
		if((maze[_x][_y] & 1) != 0){ //up
			findEndPoint(_x,_y-1,len+1);
		}
		if(_x + 1 < x && (maze[_x+1][_y] & 8) != 0){ // right
			findEndPoint(_x+1,_y,len+1);
		}
		if(_y+1 < y && (maze[_x][_y+1] & 1) != 0){ //down
			findEndPoint(_x,_y + 1,len+1);
		}
		if((maze[_x][_y] & 8) != 0){ //left
			findEndPoint(_x-1,_y,len+1);
		}
	}

}
