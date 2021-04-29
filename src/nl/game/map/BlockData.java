package nl.game.map;

import org.joml.Vector3f;
import org.joml.Vector4f;

import nl.game.objects.HitBox;
import nl.game.util.ColorUtils;

public class BlockData {
	public static final int blockSize = 5;
	
	public static final int FRONT = 0, BACK = 1, LEFT = 2, RIGHT = 3, BOTTOM = 4, TOP = 5;
	public static final int[] faces = new int[] {FRONT, BACK, LEFT, RIGHT, BOTTOM, TOP};
	
	public static final Vector3f[] basicVertices = new Vector3f[] {
			new Vector3f(-(BlockData.blockSize/2f), -(BlockData.blockSize/2f), (BlockData.blockSize/2f)),
			new Vector3f((BlockData.blockSize/2f), -(BlockData.blockSize/2f), (BlockData.blockSize/2f)),
			new Vector3f((BlockData.blockSize/2f), (BlockData.blockSize/2f), (BlockData.blockSize/2f)),
			new Vector3f(-(BlockData.blockSize/2f), (BlockData.blockSize/2f), (BlockData.blockSize/2f)),
			new Vector3f(-(BlockData.blockSize/2f), -(BlockData.blockSize/2f), -(BlockData.blockSize/2f)),
			new Vector3f((BlockData.blockSize/2f), -(BlockData.blockSize/2f), -(BlockData.blockSize/2f)),
			new Vector3f((BlockData.blockSize/2f), (BlockData.blockSize/2f), -(BlockData.blockSize/2f)),
			new Vector3f(-(BlockData.blockSize/2f), (BlockData.blockSize/2f), -(BlockData.blockSize/2f)),
	};
	
	public static final int[] basicIndices = new int[] {
			//Front
			0,1,2,
			2,3,0,
			//Back
			5,4,7,
			7,6,5,
			//LeftSide
			4,0,3,
			3,7,4,
			//RightSide
			1,5,6,
			6,2,1,
			//Bottom
			4,5,1,
			1,0,4,
			//Top
			3,2,6,
			6,7,3
	};
	
	public static boolean isSolid(Integer blockID) {
		if (blockID == null) {
			return false;
		}
		blockID = Math.abs(blockID);
		if (blockID == 1337) {
			return false;
		}
		return blockID != 0;
	}
	
	public static boolean isDestructible(Integer blockID) {
		return blockID > 0;
	}
	
	public static Vector4f getColor(int blockID) {
		blockID = Math.abs(blockID);
		if (blockID == 1) {
			return ColorUtils.RED;
		} else if (blockID == 2) {
			return ColorUtils.GREEN;
		} else if (blockID == 3) {
			return ColorUtils.BROWN;
		} else if (blockID == 4) {
			return ColorUtils.GRAY;
		} else if (blockID == 5) {
			return ColorUtils.WHITE;
		} else {
			return ColorUtils.WHITE;
		}
	}
	
	public static float getFaceBrightness(int face) {
		if (face == FRONT) {
			return 0.65f;
		} else if (face == RIGHT) {
			return 0.8f;
		} else if (face == TOP) {
			return 1;
		} else if (face == BOTTOM) {
			return 0.3f;
		} else if (face == LEFT) {
			return 0.5f;
		} else if (face == BACK) {
			return 0.4f;
		} else {
			return 1;
		}
	}
}
