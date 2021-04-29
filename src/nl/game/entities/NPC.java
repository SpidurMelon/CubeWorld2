package nl.game.entities;

import nl.game.world.World;

public class NPC extends MoveableEntity {
	public Player faceToPlayer;
	private World world;
	public NPC(float x, float y, float z, float width, float height, float depth, Player player, World world) {
		super(x, y, z, width, height, depth);
		this.world = world;
		faceToPlayer = player;
		model.putPlyData("res/models/player.ply");
	}
	public void tick(double delta) {
		if (inAir) {
			velocity.y -= World.gravityStrength*delta;
		} else {
			velocity.y = 0;
		}
		world.applyMovement(this, delta);
		if (faceToPlayer.position.z > position.z) {
			rotation = (float)Math.atan((faceToPlayer.position.x-position.x)/(faceToPlayer.position.z-position.z));
		} else {
			rotation = (float)(Math.atan((faceToPlayer.position.x-position.x)/(faceToPlayer.position.z-position.z))+Math.PI);
		}
	}
}
