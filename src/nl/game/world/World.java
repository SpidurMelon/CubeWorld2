package nl.game.world;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.Timer;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;

import nl.game.effects.Particle;
import nl.game.effects.ParticleMaster;
import nl.game.entities.Entity;
import nl.game.entities.MoveableEntity;
import nl.game.entities.NPC;
import nl.game.entities.Player;
import nl.game.map.BlockData;
import nl.game.map.Chunk;
import nl.game.map.ChunkData;
import nl.game.map.Structure;
import nl.game.objects.Camera;
import nl.game.objects.HitBox;
import nl.game.objects.Line;
import nl.game.rendering.shaders.BlockShader;
import nl.game.rendering.shaders.ParticleShader;
import nl.game.rendering.shaders.Shader;
import nl.game.setup.MainCubeWorld;
import nl.game.util.ColorUtils;
import nl.game.util.MathUtils;
import nl.game.util.RenderUtils;
import nl.game.util.TransformUtils;

public class World {
	public static final float gravityStrength = 0.05f;
	
	private long secondTimer = System.currentTimeMillis();
	private int framesThisSecond, ticksThisSecond;
	private int totalDrawTime, totalTickTime;
	
	public static boolean DEBUG = false, NOCLIP = false, COUNTING = false;
	private static final int collisionAccuracy = 3;
	
	private BlockShader blockShader = new BlockShader();
	
	public static Vector3i statueCorner = new Vector3i();
	public static HitBox statueBox;
	public static int statueWidth = 16, statueHeight = 24, statueDepth = 16;
	
	public static Player player;
	private Vector3f crossHair = new Vector3f(0, 0, -1);
	
	private ArrayList<MoveableEntity> entities = new ArrayList<>();
	
	public static final int renderDistance = 20;
	private Vector2i chunkOffset = new Vector2i(0, 0);
	private Chunk[][] chunks = new Chunk[renderDistance][renderDistance];
	public ArrayList<Chunk> loadingChunks = new ArrayList<Chunk>();
	private int chunkLoadingSpeed = 1000;
	private Chunk currentPlayerChunk;
	
	public static final int collisionRadius = 2;
	
	public World() {
		blockShader.bind();
		blockShader.setPerspectiveTransformation(TransformUtils.getPerspectiveTransform(Player.FOV, MainCubeWorld.window.getWidth()/(float)MainCubeWorld.window.getHeight(), 0.1f, 100000));
		blockShader.setFogStart(renderDistance*ChunkData.chunkSize*BlockData.blockSize*0.5f*0.7f);
		blockShader.setFogEnd(renderDistance*ChunkData.chunkSize*BlockData.blockSize*0.5f*0.8f);
		blockShader.setFogColor(MainCubeWorld.BGColor);
			
		chunks[0][0] = createChunk(0, 0);
		
		player = new Player(this);
		player.setPosition(0f, 60f*BlockData.blockSize, 0f);
		
		entities.add(player);
		entities.add(new NPC(0, 20*5, 0, 5, 5, 5, player, this));
		
		ParticleMaster.init();
	}

	public void tick(double delta) {
		long startTickTime = 0;
		if (COUNTING) {
			startTickTime = System.currentTimeMillis();
		}
		
		for (int z = 0; z < chunks.length; z++) {
			for (int x = 0; x < chunks[z].length; x++) {
				if (chunks[z][x] != null && chunks[z][x].contains(player.getPosition())) {
					currentPlayerChunk = chunks[z][x];
					chunkOffset.set(-currentPlayerChunk.chunkPos.x+renderDistance/2, -currentPlayerChunk.chunkPos.y+renderDistance/2);
				}
			}
		}
		
		for (int z = 0; z < chunks.length; z++) {
			for (int x = 0; x < chunks[z].length; x++) {
				chunks[z][x] = createChunk(x-chunkOffset.x, z-chunkOffset.y);
			}
		}
		
		for (MoveableEntity e:entities) {
			e.tick(delta);
		}
		
		for (Particle p:ParticleMaster.particles) {
			p.tick(delta);
		}
		
		checkInput(delta);
		
		int remainingBlocksToLoad = chunkLoadingSpeed;
		for (int i = 0; i < loadingChunks.size(); i++) {
			Chunk currentLoadingChunk = loadingChunks.get(i);
			remainingBlocksToLoad = currentLoadingChunk.load(remainingBlocksToLoad);
			if (remainingBlocksToLoad == 0) {
				break;
			}
		}
		if (COUNTING) {
			totalTickTime += System.currentTimeMillis()-startTickTime;
			ticksThisSecond++;
		}
		if (System.currentTimeMillis() - secondTimer > 1000) {
			secondTick();
			secondTimer += 1000;
		} 
	}
	
	public void secondTick() {
		player.secondTick();
		if (COUNTING) {
			System.out.println("Free memory: " + Runtime.getRuntime().freeMemory()/(1024*1024));
			System.out.println("FPS: " + framesThisSecond + ", TPS:" + ticksThisSecond);
			System.out.println("DrawTime: " + totalDrawTime + ", TickTime:" + totalTickTime);
			
			totalDrawTime = 0;
			totalTickTime = 0;
			framesThisSecond = 0;
			ticksThisSecond = 0;
		}
	}
	
	public void draw() {
		long startDrawTime = 0;
		if (COUNTING) {
			startDrawTime = System.currentTimeMillis();
		}
		blockShader.bind();
		blockShader.setWorldTransformation(Camera.nullTransform);
		RenderUtils.renderPoint(crossHair, 5);
		blockShader.setWorldTransformation(player.camera.getWorldTransform());
		
		for (int z = 0; z < chunks.length; z++) {
			for (int x = 0; x < chunks[z].length; x++) {
				if (chunks[z][x] != null) {
					chunks[z][x].drawChunk(DEBUG);
				}
			}
		}
		
		statueBox.draw();
		
		for (MoveableEntity e:entities) {
			e.draw(blockShader);
		}
		
		ParticleMaster.drawParticles(player.camera);
		
		if (COUNTING) {
			totalDrawTime += System.currentTimeMillis()-startDrawTime;
			framesThisSecond++;
		}
	}
	
	private TreeMap<Vector3f, Vector3i> collideableBlocks = new TreeMap<Vector3f, Vector3i>(new Comparator<Vector3f>() {
		public int compare(Vector3f v1, Vector3f v2) {
			double v1ToPlayer = v1.distance(player.getHitBox().getCenter());
			double v2ToPlayer = v2.distance(player.getHitBox().getCenter());
			return (int)Math.signum(v1ToPlayer-v2ToPlayer);
		}
	});
	
	private HitBox blockBox = new HitBox();
	public boolean applyCollision(MoveableEntity entity) {
		if (NOCLIP && entity instanceof Player) {
			return false;
		}
		collideableBlocks.clear();
		
		Vector3i playerFeet = entity.getFeetCoords();
		
		for (int z = playerFeet.z-collisionRadius; z <= playerFeet.z+collisionRadius; z++) {
			for (int y = playerFeet.y-collisionRadius; y <= playerFeet.y+collisionRadius; y++) {
				for (int x = playerFeet.x-collisionRadius; x <= playerFeet.x+collisionRadius; x++) {
					if (BlockData.isSolid(ChunkData.getBlock(x, y, z)) && ChunkData.hasFace(x, y, z)) {
						Vector3f absoluteBlockPosition = ChunkData.getAbsoluteBlockPositionCenter(x, y, z);
						collideableBlocks.put(absoluteBlockPosition, new Vector3i(x, y, z));
					}
				}
			}
		}
		
		boolean onGround = false;
		for (Entry<Vector3f, Vector3i> block:collideableBlocks.entrySet()) {
			blockBox.setBounds(block.getKey().x-0.5f*BlockData.blockSize, block.getKey().y-0.5f*BlockData.blockSize, block.getKey().z-0.5f*BlockData.blockSize, BlockData.blockSize, BlockData.blockSize, BlockData.blockSize);
			
			if (entity.getHitBox().intersects(blockBox)) {
				boolean[] directionsAllowed = ChunkData.allFaces(block.getValue().x, block.getValue().y, block.getValue().z);
				
				if (directionsAllowed[BlockData.TOP] && !directionsAllowed[BlockData.BOTTOM]) {
					boolean shouldPushUp = true;
					for (int i = 1; i < (entity.getHitBox().height/BlockData.blockSize)+1; i++) {
						if (BlockData.isSolid(ChunkData.getBlock(block.getValue().x, block.getValue().y+i, block.getValue().z))) {
							shouldPushUp = false;
							break;
						}
					}
					
					if (blockBox.getMaxY() > entity.getHitBox().getMaxY() || 
						BlockData.isSolid(ChunkData.getBlock(ChunkData.getBlockCoord(entity.getHitBox().getCenterX(), entity.getHitBox().getMaxY()+BlockData.blockSize, entity.getHitBox().getCenterZ())))) {
						shouldPushUp = false;
					}
					
					if (shouldPushUp) {
						directionsAllowed[BlockData.FRONT] = false;
						directionsAllowed[BlockData.BACK] = false;
						directionsAllowed[BlockData.LEFT] = false;
						directionsAllowed[BlockData.RIGHT] = false;
					}
				} 
				
				Vector3f collisionVector = entity.getHitBox().getLeastResistantVector(directionsAllowed, blockBox);
				
				if (collisionVector != null) {
					entity.addPosition(collisionVector);
					if (collisionVector.y > 0 && entity.getVelocity().y < 0) {
						onGround = true;
					} else if (collisionVector.y < 0 && entity.getVelocity().y > 0) {
						entity.setVelocity(null, 0f, null);
					}
					if (entity instanceof Player) {
						Player player = (Player)entity;
						player.getPosition().mul(-1, player.camera.worldTranslation);
					}
				}
				
			}
		}
		return onGround;
	}

	public void applyMovement(MoveableEntity entity, double delta) {
		boolean entityOnGround = false;
		for (int i = 0; i < collisionAccuracy; i++) {
			entity.applyVelocity((float)(delta/collisionAccuracy));
			if (applyCollision(entity)) {
				entityOnGround = true;
			}
		}
		if (entityOnGround) {
			entity.inAir = false;
			entity.setVelocity(null, 0f, null);
		} else {
			entity.inAir = true;
		}
	}
	
	private HashMap<Point, Chunk> allChunks = new HashMap<Point, Chunk>();
	public Chunk getChunk(int ix, int iz) {
		return allChunks.get(new Point(ix, iz));
	}
	public Chunk createChunk(int ix, int iz) {
		Chunk testChunk = getChunk(ix, iz);
		if (testChunk != null) {
			return testChunk;
		}
		
		Chunk newChunk = new Chunk(ix, iz, this);
		allChunks.put(new Point(ix, iz), newChunk);
		ChunkData.chunks.put(new Point(ix, iz), newChunk);
		newChunk.update(false);
		return newChunk;
	}
	
	private boolean switchedDebug = false, switchedNoclip = false, switchedCounters = false, switchedVsync = false;
	private boolean VSync = false;
	public void checkInput(double delta) {
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
			glfwSetWindowShouldClose(MainCubeWorld.window.getID(), true);
		}
		
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_KP_SUBTRACT) == GLFW_PRESS) {
			if (!switchedDebug) {
				DEBUG = !DEBUG;
				switchedDebug = true;
			}
		} else {
			switchedDebug = false;
		}
		
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_KP_ADD) == GLFW_PRESS) {
			if (!switchedNoclip) {
				NOCLIP = !NOCLIP;
				switchedNoclip = true;
			}
		} else {
			switchedNoclip = false;
		}
		
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_KP_MULTIPLY) == GLFW_PRESS) {
			if (!switchedCounters) {
				COUNTING = !COUNTING;
				secondTimer = System.currentTimeMillis();
				switchedCounters = true;
			}
		} else {
			switchedCounters = false;
		}
		
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_KP_DIVIDE) == GLFW_PRESS) {
			if (!switchedVsync) {
				VSync = !VSync;
				MainCubeWorld.window.setVSync(VSync);
				System.out.println("VSync = " + VSync);
				switchedVsync = true;
			}
		} else {
			switchedVsync = false;
		}
		
	}
	
	public void destroy() {
		Integer[][][] statue = new Integer[World.statueDepth][World.statueHeight][World.statueWidth];
		for (int z = 0; z < World.statueDepth; z++) {
			for (int y = 0; y < World.statueHeight; y++) {
				for (int x = 0; x < World.statueWidth; x++) {
					statue[z][y][x] = ChunkData.getBlock(x+statueCorner.x, y+statueCorner.y, z+statueCorner.z);
				}
			}
		}
		Structure.writeStructure(statue, "Statue");
		for (Chunk c:allChunks.values()) {
			c.destroy();
		}
		for (Entity e:entities) {
			e.destroy();
		}
		for (Entity e:ParticleMaster.particles) {
			e.destroy();
		}
	}
}
