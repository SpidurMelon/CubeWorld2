package nl.game.map;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;

import org.j3d.PerlinNoiseGenerator;
import org.joml.SimplexNoise;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import nl.game.objects.HitBox;
import nl.game.util.MathUtils;
import nl.game.world.World;

public class ChunkData {

	public static final int chunkSize = 16, chunkHeight = 128;
	public static HashMap<Point, Chunk> chunks = new HashMap<Point, Chunk>();
	private static HashMap<Point, Integer[][][]> chunkData = new HashMap<Point, Integer[][][]>();
	
	public static Integer[][][] getChunkData(int chunkIX, int chunkIZ) {
		return getChunkData(new Point(chunkIX, chunkIZ));
	}
	public static Integer[][][] getChunkData(Point chunkPos) {
		if (chunkData.containsKey(chunkPos)) {
			return chunkData.get(chunkPos);
		} 
		return null;
	}
	public static Integer[][][] createChunkData(int chunkIX, int chunkIZ) {
		return createChunkData(new Point(chunkIX, chunkIZ));
	}
	public static Integer[][][] createChunkData(Point chunkPos) {
		if (!chunkData.containsKey(chunkPos)) {
			chunkData.put(chunkPos, new Integer[chunkSize][chunkHeight][chunkSize]);
			generate(chunkPos);
		} 
		return chunkData.get(chunkPos);
	}
	
	public static Integer getBlock(Vector3i position) {
		return getBlock(position.x, position.y, position.z, 0, 0);
	}
	public static Integer getBlock(int x, int y, int z) {
		return getBlock(x, y, z, 0, 0);
	}
	public static Integer getBlock(Vector3i position, Point chunkPos) {
		return getBlock(position.x, position.y, position.z, chunkPos.x, chunkPos.y);
	}
	public static Integer getBlock(int x, int y, int z, Point chunkPos) {
		return getBlock(x, y, z, chunkPos.x, chunkPos.y);
	}
	public static Integer getBlock(int x, int y, int z, int chunkX, int chunkZ) {
		if (y < 0 || y >= chunkHeight) {
			return 0;
		}
		
		while (x < 0) {
			x += chunkSize;
			chunkX--;
		}
		while (x >= chunkSize) {
			x -= chunkSize;
			chunkX++;
		}
		while (z < 0) {
			z += chunkSize;
			chunkZ--;
		}
		while (z >= chunkSize) {
			z -= chunkSize;
			chunkZ++;
		}
		
		Integer[][][] chunkData = createChunkData(chunkX, chunkZ);
		if (chunkData == null) {
			return 1;
		}
		return chunkData[z][y][x];
	}
	
	public static Vector3i getBlockCoord(Vector3f position) {
		return getBlockCoord(position.x, position.y, position.z);
	}
	public static Vector3i getBlockCoord(float px, float py, float pz) {
		return new Vector3i((int)Math.floor((px+0.5*BlockData.blockSize)/BlockData.blockSize), 
							(int)Math.floor((py+0.5*BlockData.blockSize)/BlockData.blockSize), 
							(int)Math.floor((pz+0.5*BlockData.blockSize)/BlockData.blockSize));
	}
	
	public static void setBlock(Vector3i position, Point chunkPos, Integer value, boolean priority, boolean updateSurroundings) {
		setBlock(position.x, position.y, position.z, chunkPos.x, chunkPos.y, value, priority, updateSurroundings);
	}
	public static void setBlock(Vector3i position, Integer value, boolean priority, boolean updateSurroundings) {
		setBlock(position.x, position.y, position.z, 0, 0, value, priority, updateSurroundings);
	}
	public static void setBlock(int x, int y, int z, Point chunkPos, Integer value, boolean priority, boolean updateSurroundings) {
		setBlock(x, y, z, chunkPos.x, chunkPos.y, value, priority, updateSurroundings);
	}
	public static void setBlock(int x, int y, int z, int chunkX, int chunkZ, Integer value, boolean priority, boolean updateSurroundings) {
		if (y < 0 || y >= chunkHeight) {
			return;
		}
		
		while (x < 0) {
			x += chunkSize;
			chunkX--;
		}
		while (x >= chunkSize) {
			x -= chunkSize;
			chunkX++;
		}
		while (z < 0) {
			z += chunkSize;
			chunkZ--;
		}
		while (z >= chunkSize) {
			z -= chunkSize;
			chunkZ++;
		}
		
		if (value != null && Math.abs(value) == 1337) {
			World.statueCorner.set(x+chunkX*chunkSize, y, z+chunkZ*chunkSize);
			World.statueBox = getBox(x, y, z, World.statueWidth, World.statueHeight, World.statueDepth, chunkX, chunkZ);
		}
		
		createChunkData(chunkX, chunkZ)[z][y][x] = value;
		Chunk chunkToUpdate = chunks.get(new Point(chunkX, chunkZ));
		if (chunkToUpdate != null) {
			chunkToUpdate.update(priority);
		}
		
		if (updateSurroundings) {
			if (x == 0) {
				Chunk chunk2ToUpdate = chunks.get(new Point(chunkX-1, chunkZ));
				if (chunk2ToUpdate != null) {
					chunk2ToUpdate.update(priority);
				}
			}
			if (x == chunkSize-1) {
				Chunk chunk2ToUpdate = chunks.get(new Point(chunkX+1, chunkZ));
				if (chunk2ToUpdate != null) {
					chunk2ToUpdate.update(priority);
				}
			}
			if (z == 0) {
				Chunk chunk2ToUpdate = chunks.get(new Point(chunkX, chunkZ-1));
				if (chunk2ToUpdate != null) {
					chunk2ToUpdate.update(priority);
				}
			}
			if (z == chunkSize-1) {
				Chunk chunk2ToUpdate = chunks.get(new Point(chunkX, chunkZ+1));
				if (chunk2ToUpdate != null) {
					chunk2ToUpdate.update(priority);
				}
			}
		}
		
	}
	
	public static Vector3f getAbsoluteBlockPositionCenter(int x, int y, int z) {
		return getAbsoluteBlockPositionCenter(x, y, z, 0, 0);
	}
	public static Vector3f getAbsoluteBlockPositionCenter(int x, int y, int z, Point chunkPos) {
		return getAbsoluteBlockPositionCenter(x, y, z, chunkPos.x, chunkPos.y);
	}
	public static Vector3f getAbsoluteBlockPositionCenter(int x, int y, int z, int chunkX, int chunkZ) {
		return new Vector3f(x*BlockData.blockSize+chunkX*ChunkData.chunkSize*BlockData.blockSize, y*BlockData.blockSize, z*BlockData.blockSize+chunkZ*ChunkData.chunkSize*BlockData.blockSize);
	}
	
	public static HitBox getBox(Vector3i position) {
		return getBox(position.x, position.y, position.z, 0, 0);
	}
	public static HitBox getBox(Vector3i position, Point chunkPos) {
		return getBox(position.x, position.y, position.z, chunkPos.x, chunkPos.y);
	}
	public static HitBox getBox(int x, int y, int z) {
		return getBox(x, y, z, 0, 0);
	}
	public static HitBox getBox(int x, int y, int z, Point chunkPos) {
		return getBox(x, y, z, chunkPos.x, chunkPos.y);
	}
	public static HitBox getBox(int x, int y, int z, int chunkX, int chunkZ) {
		return getBox(x, y, z, 1, 1, 1, chunkX, chunkZ);
	}
	public static HitBox getBox(int x, int y, int z, int width, int height, int depth, int chunkX, int chunkZ) {
		return new HitBox(	x*BlockData.blockSize+chunkX*ChunkData.chunkSize*BlockData.blockSize-0.5f*BlockData.blockSize, y*BlockData.blockSize-0.5f*BlockData.blockSize, z*BlockData.blockSize+chunkZ*ChunkData.chunkSize*BlockData.blockSize-0.5f*BlockData.blockSize,
							BlockData.blockSize*width, BlockData.blockSize*height, BlockData.blockSize*depth);
	}
	
	public static void getBox(int x, int y, int z, HitBox target) {
		getBox(x, y, z, 0, 0, target);
	}
	public static void getBox(int x, int y, int z, Point chunkPos, HitBox target) {
		getBox(x, y, z, chunkPos.x, chunkPos.y, target);
	}
	public static void getBox(int x, int y, int z, int chunkX, int chunkZ, HitBox target) {
		target.setBounds(	x*BlockData.blockSize+chunkX*ChunkData.chunkSize*BlockData.blockSize-0.5f*BlockData.blockSize, y*BlockData.blockSize-0.5f*BlockData.blockSize, z*BlockData.blockSize+chunkZ*ChunkData.chunkSize*BlockData.blockSize-0.5f*BlockData.blockSize,
							BlockData.blockSize, BlockData.blockSize, BlockData.blockSize);
	}
	
	public static boolean shouldDrawFace(int face, int x, int y, int z) {
		return shouldDrawFace(face, x, y, z, 0, 0);
	}
	public static boolean shouldDrawFace(int face, Vector3i position, Point chunkPos) {
		return shouldDrawFace(face, position.x, position.y, position.z, chunkPos);
	}
	public static boolean shouldDrawFace(int face, int x, int y, int z, Point chunkPos) {
		return shouldDrawFace(face, x, y, z, chunkPos.x, chunkPos.y);
	}
	public static boolean shouldDrawFace(int face, int x, int y, int z, int chunkX, int chunkZ) {
		if (!BlockData.isSolid(getBlock(x, y, z, chunkX, chunkZ))) {
			return false;
		}
		if (face == BlockData.FRONT && !BlockData.isSolid(getBlock(x, y, z+1, chunkX, chunkZ))) {
			return true;
		}
		else if (face == BlockData.BACK && !BlockData.isSolid(getBlock(x, y, z-1, chunkX, chunkZ))) {
			return true;
		}
		else if (face == BlockData.LEFT && !BlockData.isSolid(getBlock(x-1, y, z, chunkX, chunkZ))) {
			return true;
		}
		else if (face == BlockData.RIGHT && !BlockData.isSolid(getBlock(x+1, y, z, chunkX, chunkZ))) {
			return true;
		}
		else if (face == BlockData.TOP && !BlockData.isSolid(getBlock(x, y+1, z, chunkX, chunkZ))) {
			return true;
		}
		else if (face == BlockData.BOTTOM && !BlockData.isSolid(getBlock(x, y-1, z, chunkX, chunkZ))) {
			return true;
		}
		return false;
	}
	
	public static boolean[] allFaces(int x, int y, int z) {
		return allFaces(x, y, z, 0, 0);
	}
	public static boolean[] allFaces(int x, int y, int z, Point chunkPos) {
		return allFaces(x, y, z, chunkPos.x, chunkPos.y);
	}
	public static boolean[] allFaces(int x, int y, int z, int chunkX, int chunkZ) {
		boolean[] result = new boolean[] {false, false, false, false, false, false};
		if (!BlockData.isSolid(getBlock(x, y, z, chunkX, chunkZ))) {
			return result;
		}
		if (!BlockData.isSolid(getBlock(x, y, z+1, chunkX, chunkZ))) {
			result[BlockData.FRONT] = true;
		}
		if (!BlockData.isSolid(getBlock(x, y, z-1, chunkX, chunkZ))) {
			result[BlockData.BACK] = true;
		}
		if (!BlockData.isSolid(getBlock(x-1, y, z, chunkX, chunkZ))) {
			result[BlockData.LEFT] = true;
		}
		if (!BlockData.isSolid(getBlock(x+1, y, z, chunkX, chunkZ))) {
			result[BlockData.RIGHT] = true;
		}
		if (!BlockData.isSolid(getBlock(x, y+1, z, chunkX, chunkZ))) {
			result[BlockData.TOP] = true;
		}
		if (!BlockData.isSolid(getBlock(x, y-1, z, chunkX, chunkZ))) {
			result[BlockData.BOTTOM] = true;
		}
		return result;
	}
	
	public static boolean hasFace(int x, int y, int z, Point chunkPos) {
		return hasFace(x, y, z, chunkPos.x, chunkPos.y);
	}
	public static boolean hasFace(int x, int y, int z) {
		return hasFace(x, y, z, 0, 0);
	}
	public static boolean hasFace(int x, int y, int z, int chunkX, int chunkZ) {
		boolean[] faces = allFaces(x, y, z, chunkX, chunkZ);
		for (boolean face:faces) {
			if (face) {
				return true;
			}
		}
		return false;
	}
	
	private static int seed = 8;
	private static PerlinNoiseGenerator png = new PerlinNoiseGenerator(seed);
	private static final float smoothNessX = 60f, smoothNessZ = 60f;
	private static final float caveSeparationX = 30f, caveSeparationY = 30f, caveSeparationZ = 30f;
	private static final float caveWidth = 0.1f;
	private static final int maxAmplitude = 20;
	private static final int seaLevel = (int)(maxAmplitude*0.6);
	
	private static PerlinNoiseGenerator cng = new PerlinNoiseGenerator(seed+1);
	private static final int cloudLevel = 60;
	
	private static boolean generateStatue = true;
	private static Structure pillar = new Structure("res/structures/Pillar.str");
	private static Structure statue = new Structure("res/structures/Statue.str");
	
	private static Integer[][][] generate(Point chunkPos) {
		Integer[][][] result = new Integer[chunkSize][chunkHeight][chunkSize];
		for (int bz = 0; bz < result.length; bz++) {
			for (int bx = 0; bx < result[0][0].length; bx++) {
				int height = (int)Math.min(chunkHeight-1, Math.max(2, png.noise2((bx+chunkPos.x*chunkSize)/smoothNessX, (bz+chunkPos.y*chunkSize)/smoothNessZ)*maxAmplitude+seaLevel));
				for (int by = 0; by <= height; by++) {
					if (png.noise3((bx+chunkPos.x*chunkSize)/caveSeparationX, by/caveSeparationY, (bz+chunkPos.y*chunkSize)/caveSeparationZ) < caveWidth) {
						setBlock(bx, by, bz, chunkPos, 2, false, false);
					}
				}
				if (BlockData.isSolid(getBlock(bx, height, bz, chunkPos)) && MathUtils.randInt(0, 300) == 0) {
					plantTree(bx, height+1, bz, chunkPos);
				}
			}
		}
		
		//Clouds
		for (int bz = 0; bz < result.length; bz++) {
			for (int bx = 0; bx < result[0][0].length; bx++) {
				for (int by = 0; by <= 40; by++) {
					if (cng.noise3((bx+chunkPos.x*chunkSize)/50f, by/20f, (bz+chunkPos.y*chunkSize)/50f) < -0.2f) {
						setBlock(bx, by+cloudLevel, bz, chunkPos, 5, false, false);
					}
				}
			}
		}
		
		if (generateStatue) {
			generateStatue = false;
			int pillarHeight = 30;
			generateStructure(0, pillarHeight, 0, chunkPos, pillar, true);
			generateStructure(0, pillarHeight+4, 0, chunkPos, statue, false);
		}
		
		return result;
	}
	private static int generateStructure(int x, int z, Point chunkCoords, Structure structure, boolean indestructible) {
		for (int y = 0; y < chunkHeight; y++) {
			if (getBlock(x, y, z, chunkCoords) == null || getBlock(x, y, z, chunkCoords) == 0) {
				if (y != 0) {
					generateStructure(x, y, z, chunkCoords, structure, indestructible);
					return y;
				}
				return -1;
			}
		}
		return -1;
	}
	private static void generateStructure(int x, int y, int z, Point chunkCoords, Structure structure, boolean indestructible) {
		for (int bz = 0; bz < structure.depth; bz++) {
			for (int by = 0; by < structure.height; by++) {
				for (int bx = 0; bx < structure.width; bx++) {
					int blockToPlace = structure.structureData[bz][by][bx];
					if (indestructible && blockToPlace > 0) {
						blockToPlace *= -1;
					}
					setBlock(x+bx-structure.width/2, y+by, z+bz-structure.depth/2, chunkCoords, blockToPlace, false, false);
				}
			}
		}
	}
	private static void plantTree(int x, int z, Point chunkCoords) {
		for (int y = 0; y < chunkHeight; y++) {
			if (getBlock(x, y, z, chunkCoords) == null || getBlock(x, y, z, chunkCoords) == 0) {
				if (y != 0) {
					plantTree(x, y, z, chunkCoords);
				}
				return;
			}
		}
	}
	private static final int treeHeight = 7;
	private static void plantTree(int x, int y, int z, Point chunkCoords) {
		setBlock(x, y, z, chunkCoords, 3, false, false);
		setBlock(x, y+1, z, chunkCoords, 3, false, false);
		setBlock(x, y+2, z, chunkCoords, 3, false, false);
		for (int i = 3; i < treeHeight; i++) {
			setBlock(x, y+i, z, chunkCoords, 3, false, false);
			setBlock(x+1, y+i, z, chunkCoords, 2, false, false);
			setBlock(x-1, y+i, z, chunkCoords, 2, false, false);
			setBlock(x, y+i, z+1, chunkCoords, 2, false, false);
			setBlock(x, y+i, z-1, chunkCoords, 2, false, false);
		}
		setBlock(x, y+treeHeight, z, chunkCoords, 2, false, false);
	}
}
