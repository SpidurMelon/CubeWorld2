package nl.game.entities;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import nl.game.map.BlockData;
import nl.game.map.ChunkData;
import nl.game.objects.HitBox;
import nl.game.rendering.shaders.BlockShader;
import nl.game.world.World;

public class MoveableEntity extends Entity {
	
	protected Vector3f position = new Vector3f();
	protected Vector3f velocity = new Vector3f();
	protected float rotation = 0;
	public boolean inAir = true;
	
	public MoveableEntity(float x, float y, float z, float width, float height, float depth) {
		super(x, y, z, width, height, depth);
		position.set(x, y, z);
	}
	public void tick(double delta) {
		applyVelocity((float)delta);
	}
	public void applyVelocity(float delta) {
		addPosition(velocity.x*delta, velocity.y*delta, velocity.z*delta);
	}
	public void draw(BlockShader shader) {
		Matrix4f worldTransform = World.player.camera.getWorldTransform();
		worldTransform.translate(hitbox.getCenterX(), hitbox.y, hitbox.getCenterZ());
		worldTransform.rotate(rotation, 0, 1, 0);
		worldTransform.rotate((float)Math.toRadians(-90), 1, 0, 0);
		shader.setWorldTransformation(worldTransform);
		
		super.draw();
	}
	
	public void updateHitbox() {
		hitbox.setLocation(position.x-hitbox.width/2, position.y-hitbox.height/2, position.z-hitbox.depth/2);
	}
	
	public void addVelocity(Vector3f velocity) {
		addVelocity(velocity.x, velocity.y, velocity.z);
	}
	public void addVelocity(float x, float y, float z) {
		velocity.add(x, y, z);
	}
	public void setVelocity(Vector3f velocity) {
		setVelocity(velocity.x, velocity.y, velocity.z);
	}
	public void setVelocity(Float x, Float y, Float z) {
		float newVelocityX = velocity.x, newVelocityY = velocity.y, newVelocityZ = velocity.z;
		if (x != null) {
			newVelocityX = x;
		}
		if (y != null) {
			newVelocityY = y;
		}
		if (z != null) {
			newVelocityZ = z;
		}
		velocity.set(newVelocityX, newVelocityY, newVelocityZ);
	}
	public Vector3f getVelocity() {
		return velocity;
	}
	
	public void addPosition(float x, float y, float z) {
		position.add(x, y, z);
		updateHitbox();
	}
	public void addPosition(Vector3f newPosition) {
		position.add(newPosition);
		updateHitbox();
	}
	public void setPosition(Float x, Float y, Float z) {
		float newPositionX = position.x, newPositionY = position.y, newPositionZ = position.z;
		if (x != null) {
			newPositionX = x;
		}
		if (y != null) {
			newPositionY = y;
		}
		if (z != null) {
			newPositionZ = z;
		}
		position.set(newPositionX, newPositionY, newPositionZ);
		updateHitbox();
	}
	public void setPosition(Vector3f newPosition) {
		position.set(newPosition);
		updateHitbox();
	}
	public Vector3f getPosition() {
		return position;
	}
	
	public float getRotation() {
		return rotation;
	}

	public Vector3i getFeetCoords() {
		return ChunkData.getBlockCoord(hitbox.getCenterX(), hitbox.y, hitbox.getCenterZ());
	}
}
