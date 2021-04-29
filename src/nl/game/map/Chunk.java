package nl.game.map;

import java.awt.Color;
import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.lwjgl.opengl.GL20.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import nl.game.entities.Player;
import nl.game.objects.Camera;
import nl.game.objects.HitBox;
import nl.game.objects.Line;
import nl.game.rendering.shaders.BlockShader;
import nl.game.rendering.shaders.Shader;
import nl.game.util.ColorUtils;
import nl.game.util.ErrorUtils;
import nl.game.util.MathUtils;
import nl.game.world.World;

public class Chunk {
	public Integer[][][] blocks;
	
	private Vector3f[] preVertices;
	private int totalVertices;
	private int vbo;
	public boolean doneLoading = false;
	
	public final Point chunkPos;

	private World world;
	
	public Chunk(int x, int z, World world) {
		chunkPos = new Point(x, z);
		this.world = world;
		vbo = glGenBuffers();
		
		preVertices = new Vector3f[] {
				new Vector3f(-(BlockData.blockSize/2f)+x*ChunkData.chunkSize*BlockData.blockSize, -(BlockData.blockSize/2f), (BlockData.blockSize/2f)+z*ChunkData.chunkSize*BlockData.blockSize),
				new Vector3f((BlockData.blockSize/2f)+x*ChunkData.chunkSize*BlockData.blockSize, -(BlockData.blockSize/2f), (BlockData.blockSize/2f)+z*ChunkData.chunkSize*BlockData.blockSize),
				new Vector3f((BlockData.blockSize/2f)+x*ChunkData.chunkSize*BlockData.blockSize, (BlockData.blockSize/2f), (BlockData.blockSize/2f)+z*ChunkData.chunkSize*BlockData.blockSize),
				new Vector3f(-(BlockData.blockSize/2f)+x*ChunkData.chunkSize*BlockData.blockSize, (BlockData.blockSize/2f), (BlockData.blockSize/2f)+z*ChunkData.chunkSize*BlockData.blockSize),
				new Vector3f(-(BlockData.blockSize/2f)+x*ChunkData.chunkSize*BlockData.blockSize, -(BlockData.blockSize/2f), -(BlockData.blockSize/2f)+z*ChunkData.chunkSize*BlockData.blockSize),
				new Vector3f((BlockData.blockSize/2f)+x*ChunkData.chunkSize*BlockData.blockSize, -(BlockData.blockSize/2f), -(BlockData.blockSize/2f)+z*ChunkData.chunkSize*BlockData.blockSize),
				new Vector3f((BlockData.blockSize/2f)+x*ChunkData.chunkSize*BlockData.blockSize, (BlockData.blockSize/2f), -(BlockData.blockSize/2f)+z*ChunkData.chunkSize*BlockData.blockSize),
				new Vector3f(-(BlockData.blockSize/2f)+x*ChunkData.chunkSize*BlockData.blockSize, (BlockData.blockSize/2f), -(BlockData.blockSize/2f)+z*ChunkData.chunkSize*BlockData.blockSize),
		};
		
		blocks = ChunkData.createChunkData(x, z);
	}
	
	private FloatBuffer currentVertexData;
	private int currentX = 0, currentY = 0, currentZ = 0;
	
	public void update(boolean priority) {
		if (priority && (world.loadingChunks.isEmpty() || world.loadingChunks.get(0) != this) ) {
			if (world.loadingChunks.contains(this)) {
				world.loadingChunks.remove(this);
			}
			currentX = 0; 
			currentY = 0;
			currentZ = 0;
			currentVertexData = null;
			world.loadingChunks.add(0, this);
		} else if (!world.loadingChunks.contains(this)) {
			currentX = 0; 
			currentY = 0;
			currentZ = 0;
			currentVertexData = null;
			world.loadingChunks.add(this);
		}
	}
	
	public int load() {
		return load(ChunkData.chunkSize*ChunkData.chunkSize*ChunkData.chunkHeight);
	}
	
	public int load(int blocksToLoad) {
		if (currentVertexData == null) {
			blocks = ChunkData.createChunkData(chunkPos);
			totalVertices = getTotalFaces(blocks)*6;
			currentVertexData = BufferUtils.createFloatBuffer(totalVertices*7);
		}
		int blocksRemaining = blocksToLoad;
		
		for (int z = currentZ; z < blocks.length; z++) {
			for (int y = currentY; y < blocks[z].length; y++) {
				for (int x = currentX; x < blocks[z][y].length; x++) {
					if (BlockData.isSolid(blocks[z][y][x])) {
						boolean[] faces = ChunkData.allFaces(x, y, z, chunkPos);
						for (int i = 0; i < faces.length; i++) {
							if (faces[i]) {
								for (int j = 0; j < 6; j++) {
									currentVertexData.put(preVertices[BlockData.basicIndices[i*6+j]].x+x*BlockData.blockSize);
									currentVertexData.put(preVertices[BlockData.basicIndices[i*6+j]].y+y*BlockData.blockSize);
									currentVertexData.put(preVertices[BlockData.basicIndices[i*6+j]].z+z*BlockData.blockSize);
									//Vector4f color = BlockData.getColor(blocks[z][y][x]);
									currentVertexData.put((BlockData.getColor(blocks[z][y][x]).x)*BlockData.getFaceBrightness(i));
									currentVertexData.put(BlockData.getColor(blocks[z][y][x]).y*BlockData.getFaceBrightness(i));
									currentVertexData.put((BlockData.getColor(blocks[z][y][x]).z)*BlockData.getFaceBrightness(i));
									currentVertexData.put(BlockData.getColor(blocks[z][y][x]).w);
								}
							}
						}
						
						currentX = x+1;
						blocksRemaining--;
						if (blocksRemaining == 0) {
							return 0;
						}
					}
				}
				currentX = 0;
				currentY = y+1;
			}
			currentY = 0;
			currentZ = z+1;
		}
		currentZ = 0;
		
		currentVertexData.flip();
		
		updateVBO(currentVertexData);
		currentVertexData = null;
		
		world.loadingChunks.remove(this);
		
		//Pre-calculating surrounding chunkdata
		ChunkData.createChunkData(chunkPos.x+1, chunkPos.y);
		ChunkData.createChunkData(chunkPos.x-1, chunkPos.y);
		ChunkData.createChunkData(chunkPos.x, chunkPos.y+1);
		ChunkData.createChunkData(chunkPos.x, chunkPos.y-1);
		
		doneLoading = true;
		return blocksRemaining;
	}
	
	private void updateVBO(FloatBuffer vertexData) {
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void drawChunk(boolean lines) {
		if (doneLoading) {
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 7*4, 0);
			glEnableVertexAttribArray(1);
			glVertexAttribPointer(1, 4, GL_FLOAT, false, 7*4, 3*4);
			
			if (lines) {
				glDrawArrays(GL_LINES, 0, totalVertices);
			} else {
				glDrawArrays(GL_TRIANGLES, 0, totalVertices);
			}
			
			glDisableVertexAttribArray(0);
			glDisableVertexAttribArray(1);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
		}
	}
	
	public int getTotalFaces(Integer[][][] blocks) {
		int result = 0;
		for (int z = 0; z < blocks.length; z++) {
			for (int y = 0; y < blocks[z].length; y++) {
				for (int x = 0; x < blocks[z][y].length; x++) {
					result += amountOfFaces(x, y, z);
				}
			}
		}
		return result;
	}
	
	public int amountOfFaces(int x, int y, int z) {
		int result = 0;
		boolean[] faces = ChunkData.allFaces(x, y, z, chunkPos);
		for (boolean face:faces) {
			if (face) {
				result += 2;
			}
		}
		return result;
	}
	
	public boolean contains(Vector3f position) {
		if (position.x > preVertices[4].x && position.x <= preVertices[2].x+(ChunkData.chunkSize)*BlockData.blockSize &&
			position.z > preVertices[4].z && position.z <= preVertices[2].z+(ChunkData.chunkSize-1)*BlockData.blockSize) {
			return true;
		}
		return false;
	}
	
	public void destroy() {
		glDeleteBuffers(vbo);
	}
}
