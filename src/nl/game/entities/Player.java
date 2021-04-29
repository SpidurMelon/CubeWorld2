package nl.game.entities;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import nl.game.audio.AudioMaster;
import nl.game.effects.ParticleMaster;
import nl.game.effects.ParticleUtils;
import nl.game.map.BlockData;
import nl.game.map.Chunk;
import nl.game.map.ChunkData;
import nl.game.objects.Camera;
import nl.game.objects.HitBox;
import nl.game.objects.Line;
import nl.game.rendering.shaders.BlockShader;
import nl.game.rendering.shaders.Shader;
import nl.game.setup.MainCubeWorld;
import nl.game.util.ColorUtils;
import nl.game.util.RenderUtils;
import nl.game.weapons.Sword;
import nl.game.weapons.Weapon;
import nl.game.world.World;

public class Player extends MoveableEntity {
	private static final float walkSpeed = 0.7f, startJumpSpeed = 1.5f;
	private static final float width = 4, height = 6, depth = 4;
	
	public Camera camera = new Camera();
	public static final int FOV = 90;
	
	private Vector3i breakPos = new Vector3i();
	private Vector3i placePos = new Vector3i();
	
	private boolean leftMouseDown = false, rightMouseDown = false;
	private boolean canBreakBlock = true, canPlaceBlock = true;
	private int reach = 5;
	
	private boolean combatMode = false;
	private Sword sword;
	private World world;
	
	public Player(World world) {
		super(0, 0, 0, width, height, depth);
		this.world = world;
		glfwSetMouseButtonCallback(MainCubeWorld.window.getID(), new GLFWMouseButtonCallback() {
			public void invoke(long window, int button, int action, int mods) {
				onMouseClick(button, action, mods);
			}
		});
		glfwSetCursorPosCallback(MainCubeWorld.window.getID(), new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) {
				onMouseMove(xpos, ypos);
			}
		});
		camera.setThirdPerson(5, 5, 5);
		EntityModel model = new EntityModel();
		model.putPlyData("res/models/torch.ply");
		sword = new Sword(this, model);
	}
	
	private HitBox blockBox = new HitBox();
	private static int placeSound = AudioMaster.loadSound("res/sounds/Steps/step1.wav");
	private static int breakSound = AudioMaster.loadSound("res/sounds/Steps/step2.wav");
	public void tick(double delta) {
		checkInput(delta);
		if (!World.NOCLIP && inAir) {
			velocity.y -= World.gravityStrength*delta;
		} else if (!inAir) {
			velocity.y = 0;
		}
		
		world.applyMovement(this, delta);
		
		position.mul(-1, camera.worldTranslation);
		
		if (combatMode) {
			combatTick(delta);
		} else {
			buildTick(delta);
		}
	}
	public void combatTick(double delta) {
		sword.tick(delta);
		if (leftMouseDown) {
			sword.onClick();
		}
	}
	public void buildTick(double delta) {
		if (leftMouseDown && canBreakBlock && BlockData.isDestructible(ChunkData.getBlock(breakPos)) && breakPos.y != -1) {
			canBreakBlock = false;
			Vector4f blockColor = BlockData.getColor(ChunkData.getBlock(breakPos));
			ChunkData.setBlock(breakPos, 0, true, true);
			ParticleUtils.particleSPLOSION(breakPos.x*BlockData.blockSize, breakPos.y*BlockData.blockSize, breakPos.z*BlockData.blockSize, 
					blockColor, 
					100, 1.2f);
			AudioMaster.playSound(breakSound, 1, 0.75f+(float)(Math.random()/4.0));
		}
		
		if (rightMouseDown && canPlaceBlock && (ChunkData.getBlock(placePos) == null || ChunkData.getBlock(placePos) == 0) && placePos.y != -1) {
			canPlaceBlock = false;
			ChunkData.setBlock(placePos, 1, true, true);
			AudioMaster.playSound(placeSound, 1, 0.75f+(float)(Math.random()/4.0));
		}
		
		Line cameraRay = camera.getRay(reach);
		Vector3i playerFeet = getFeetCoords();
		float blockDistance = 999999;
		for (int z = playerFeet.z-reach; z <= playerFeet.z+reach; z++) {
			for (int y = playerFeet.y-reach; y <= playerFeet.y+reach; y++) {
				for (int x = playerFeet.x-reach; x <= playerFeet.x+reach; x++) {
					if (BlockData.isSolid(ChunkData.getBlock(x, y, z))) {
						ChunkData.getBox(x, y, z, blockBox);
						if (cameraRay.intersects(blockBox) && cameraRay.point1.distance(blockBox.getCenter()) < blockDistance) {
							breakPos.set(x, y, z);
							blockDistance = cameraRay.point1.distance(blockBox.getCenter());
						}
					}
				}
			}
		}
		
		if (blockDistance == 999999) {
			breakPos.set(-1, -1, -1);
			placePos.set(-1, -1, -1);
		} else {
			placePos = ChunkData.getBlockCoord(camera.getRay(reach).intersection(ChunkData.getBox(breakPos), -0.001f));
		}
	}
	private FloatBuffer vertices = BufferUtils.createFloatBuffer(16*16*20*6*6*7);
	public void secondTick() {
		vertices.clear();
		int totalVertices = 0;
		for (int z = World.statueCorner.z; z < World.statueCorner.z+World.statueDepth; z++) {
			for (int y = World.statueCorner.y; y < World.statueCorner.y+World.statueHeight; y++) {
				for (int x = World.statueCorner.x; x < World.statueCorner.x+World.statueWidth; x++) {
					//ChunkData.getBox(x, y, z).draw();
					Integer currentBlock = ChunkData.getBlock(x, y, z);
					if (BlockData.isSolid(currentBlock)) {
						boolean[] faces = ChunkData.allFaces(x, y, z);
						for (int i = 0; i < faces.length; i++) {
							if (faces[i]) {
								for (int j = 0; j < 6; j++) {
									totalVertices+=1;
									vertices.put(BlockData.basicVertices[BlockData.basicIndices[i*6+j]].x+(x-World.statueCorner.x)*BlockData.blockSize-(World.statueWidth*BlockData.blockSize)/2);
									vertices.put(BlockData.basicVertices[BlockData.basicIndices[i*6+j]].y+(y-World.statueCorner.y)*BlockData.blockSize);
									vertices.put(BlockData.basicVertices[BlockData.basicIndices[i*6+j]].z+(z-World.statueCorner.z)*BlockData.blockSize-(World.statueDepth*BlockData.blockSize)/2);
									
									vertices.put(BlockData.getColor(currentBlock).x*BlockData.getFaceBrightness(i));
									vertices.put(BlockData.getColor(currentBlock).y*BlockData.getFaceBrightness(i));
									vertices.put(BlockData.getColor(currentBlock).z*BlockData.getFaceBrightness(i));
									vertices.put(1);
								}
							}
						}
					}
				}
			}
			
		}
		vertices.flip();
		model.putRawData(vertices, null, totalVertices);
	}
	
	public void draw(BlockShader shader) {
		ChunkData.getBox(breakPos).draw();
		if (World.DEBUG) {
			hitbox.draw();
			
			Vector3f origin = new Vector3f();
			
			RenderUtils.setColor(ColorUtils.RED);
			RenderUtils.renderLine(origin, new Vector3f(3, 0, 0));
			
			RenderUtils.setColor(ColorUtils.GREEN);
			RenderUtils.renderLine(origin, new Vector3f(0, 3, 0));
			
			RenderUtils.setColor(ColorUtils.BLUE);
			RenderUtils.renderLine(origin, new Vector3f(0, 0, 3));
			
			RenderUtils.setColor(ColorUtils.RED);
			RenderUtils.renderPoint(position, 10);
		}
		Matrix4f worldTransform = camera.getWorldTransform();
		
		worldTransform.translate(hitbox.getCenterX(), hitbox.y, hitbox.getCenterZ());
		worldTransform.rotate((float)Math.toRadians(-camera.rotation.y+180), 0, 1, 0);
		
		worldTransform.scale(width/(World.statueWidth*BlockData.blockSize), height/(World.statueHeight*BlockData.blockSize), depth/(World.statueDepth*BlockData.blockSize));
		worldTransform.translate(BlockData.blockSize/2, BlockData.blockSize/2, BlockData.blockSize/2);
		
		shader.setWorldTransformation(worldTransform);
		super.draw();
		sword.draw(shader, 0.2f);
	}
	
	public void onMouseClick(int button, int action, int mods) {
		if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
			leftMouseDown = true;
		}
		if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_RELEASE) {
			leftMouseDown = false;
			canBreakBlock = true;
		}
		if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
			rightMouseDown = true;
		}
		if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_RELEASE) {
			rightMouseDown = false;
			canPlaceBlock = true;
		}
	}
	private static boolean switchedMode = false;
	public void checkInput(double delta) {
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_Q) == GLFW_PRESS) {
			if (!switchedMode) {
				if (!combatMode) {
					combatMode = true;
					camera.setThirdPerson(0, 5, 5);
					placePos.set(-1, -1, -1);
					breakPos.set(-1, -1, -1);
				} else {
					combatMode = false;
					camera.setThirdPerson(5, 5, 5);
					sword.setPosition(0f, 0f, 0f);
				}
				switchedMode = true;
			} 
		}
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_Q) == GLFW_RELEASE) {
			switchedMode = false;
		}
		checkMovement(delta);
	}
	public void checkMovement(double delta) {
		float toFront = 0, toRight = 0, toUp = 0;
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_D) == GLFW_PRESS) {
			toRight += walkSpeed;
		}
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_A) == GLFW_PRESS) {
			toRight -= walkSpeed;
		}
		if (!World.NOCLIP) {
			if (!inAir && glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_SPACE) == GLFW_PRESS) {
				velocity.y = startJumpSpeed;
				inAir = true;
			} 
		} else {
			if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_SPACE) == GLFW_PRESS) {
				toUp += walkSpeed;
			}
			if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
				toUp -= walkSpeed;
			}
		}
		
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_W) == GLFW_PRESS) {
			toFront += walkSpeed;
		}
		if (glfwGetKey(MainCubeWorld.window.getID(), GLFW_KEY_S) == GLFW_PRESS) {
			toFront -= walkSpeed;
		}
		
		if (World.DEBUG) {
			toFront*=3;
			toUp*=3;
			toRight*=3;
		}
		
		if (toFront != 0 && toRight != 0) {
			toFront *= Math.cos(Math.toRadians(45));
			toRight *= Math.cos(Math.toRadians(45));
		}
		
		setVelocity((float)(toRight*Math.cos(Math.toRadians(camera.rotation.y))+toFront*Math.sin(Math.toRadians(camera.rotation.y))),
					null,
					-(float)(toFront*Math.cos(Math.toRadians(camera.rotation.y))-toRight*Math.sin(Math.toRadians(camera.rotation.y))));
		
		if (World.NOCLIP) {
			setVelocity(null, toUp, null);
		}
	}
	private float mouseSensitivity = 0.1f;
	private Point restPosition = new Point(MainCubeWorld.window.getWidth()/2, MainCubeWorld.window.getHeight()/2);
	protected void onMouseMove(double xpos, double ypos) {
		rotation += -(restPosition.x-xpos)*mouseSensitivity;
		camera.rotation.x += -(restPosition.y-ypos)*mouseSensitivity;
		camera.rotation.y = rotation;
		
		if (camera.rotation.x < -90) {
			camera.rotation.x = -90;
		} else if (camera.rotation.x > 90) {
			camera.rotation.x = 90;
		}
		
		glfwSetCursorPos(MainCubeWorld.window.getID(), restPosition.x, restPosition.y);
	}
}
