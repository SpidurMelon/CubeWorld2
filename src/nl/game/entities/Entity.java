package nl.game.entities;

import nl.game.objects.HitBox;

public abstract class Entity {
	protected HitBox hitbox;
	protected EntityModel model = new EntityModel();
	public Entity(float x, float y, float z, float width, float height, float depth) {
		this.hitbox = new HitBox(x-width/2, y-height/2, z-depth/2, width, height, depth);
	}
	public EntityModel getModel() {
		return model;
	}
	public HitBox getHitBox() {
		return hitbox;
	}
	public void draw() {
		model.draw();
	}
	public void destroy() {
		model.destroy();
	}
}
