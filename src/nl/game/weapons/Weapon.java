package nl.game.weapons;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import nl.game.entities.EntityModel;
import nl.game.entities.MoveableEntity;
import nl.game.rendering.shaders.BlockShader;
import nl.game.world.World;

public abstract class Weapon extends MoveableEntity {
	protected MoveableEntity holder;
	protected Vector3f offset = new Vector3f();
	protected Vector3f weaponRotation = new Vector3f();
	protected float damage = 1;
	public Weapon(MoveableEntity holder) {
		super(holder.getPosition().x+5, holder.getPosition().y, holder.getPosition().z, 1, 1, 1);
		this.holder = holder;
	}
	public Weapon(MoveableEntity holder, EntityModel model) {
		super(holder.getHitBox().getCenterX(), holder.getHitBox().getCenterY(), holder.getHitBox().getCenterZ(), 1, 1, 1);
		this.holder = holder;
		this.model = model;
		offset.set(holder.getHitBox().width/2, 0, 0);
	}
	public void tick(double delta) {
		rotation = holder.getRotation();
		setPosition(holder.getHitBox().getCenter());
	}
	public void draw(BlockShader shader, float scale) {
		Matrix4f worldTransform = World.player.camera.getWorldTransform();
		worldTransform.translate(hitbox.getCenterX(), hitbox.y, hitbox.getCenterZ());
		worldTransform.rotate((float)Math.toRadians(-rotation), 0, 1, 0);
		worldTransform.translate(offset);
		worldTransform.rotate((float)Math.toRadians(-90), 1, 0, 0);
		worldTransform.rotate((float)Math.toRadians(weaponRotation.x), 0, 1, 0);
		worldTransform.rotate((float)Math.toRadians(weaponRotation.y), 1, 0, 0);
		worldTransform.rotate((float)Math.toRadians(weaponRotation.z), 0, 0, 1);
		worldTransform.scale(scale);
		shader.setWorldTransformation(worldTransform);
		
		super.draw();
	}
	public abstract void onClick();
	public void setHolder(MoveableEntity holder) {
		this.holder = holder;
	}
	public Vector3f getWeaponRotation() {
		return weaponRotation;
	}
}
