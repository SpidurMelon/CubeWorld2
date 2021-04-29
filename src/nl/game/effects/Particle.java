package nl.game.effects;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import nl.game.entities.MoveableEntity;
import nl.game.map.BlockData;
import nl.game.map.ChunkData;
import nl.game.objects.Camera;
import nl.game.rendering.shaders.BlockShader;
import nl.game.rendering.shaders.ParticleShader;
import nl.game.rendering.shaders.Shader;
import nl.game.util.ColorUtils;
import nl.game.world.World;

public class Particle extends MoveableEntity {
	public Vector4f color = ColorUtils.RED;
	public boolean gravity = false;
	public boolean visible = false;
	public Particle() {
		super(0, 100, 0, 1, 1, 1);
		initParticle();
	}
	private void initParticle() {
		FloatBuffer particleBuffer = BufferUtils.createFloatBuffer(6*6*3);
		for (int face = 0; face < 6; face++) {
			for (int vertex = 0; vertex < 6; vertex++) {
				Vector3f vertexPos = BlockData.basicVertices[BlockData.basicIndices[face*6+vertex]];
				particleBuffer.put(vertexPos.x/(BlockData.blockSize*2));
				particleBuffer.put(vertexPos.y/(BlockData.blockSize*2));
				particleBuffer.put(vertexPos.z/(BlockData.blockSize*2));
			}
		}
		particleBuffer.flip();
		model.putRawData(particleBuffer, null, BlockData.basicIndices.length);
	}
	public void start(float x, float y, float z, float vx, float vy, float vz, boolean gravity, Vector4f color) {
		setPosition(x, y, z);
		setVelocity(vx, vy, vz);
		this.gravity = gravity;
		this.color = color;
		inAir = true;
		visible = true;
	}
	public void tick(double delta) {
		if (inAir) {
			if (gravity) {
				addVelocity(0, -(float)(World.gravityStrength*delta), 0);
			} else {
				velocity.mul(0.99f);
				if (velocity.length() < 0.01f) {
					inAir = false;
					visible = false;
				}
			}
			
			super.tick(delta);
			
			if (position.y < -100 || BlockData.isSolid(ChunkData.getBlock(ChunkData.getBlockCoord(position)))) {
				inAir = false;
				visible = false;
			}
		}
	}
	public void draw(Camera c, ParticleShader shader) {
		if (visible) {
			Matrix4f worldTransform = c.getWorldTransform();
			worldTransform.translate(position);
			//worldTransform.rotate(rotation, 0, 1, 0);
			shader.setWorldTransformation(worldTransform);
			shader.setColor(color);
			
			glBindBuffer(GL_ARRAY_BUFFER, model.vbo);
			glEnableVertexAttribArray(0);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 3*4, 0);
			
			glDrawArrays(GL_TRIANGLES, 0, BlockData.basicIndices.length);
			
			glDisableVertexAttribArray(0);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
		}
	}
}
