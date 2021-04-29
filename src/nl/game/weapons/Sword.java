package nl.game.weapons;

import org.joml.Vector3f;

import nl.game.audio.AudioMaster;
import nl.game.entities.EntityModel;
import nl.game.entities.MoveableEntity;

public class Sword extends Weapon {
	public boolean swinging = false;
	public float swingAngle = 0;
	private Vector3f ogOffset = new Vector3f();
	private float swingDistance = 2;
	
	public Sword(MoveableEntity holder, EntityModel model) {
		super(holder, model);
		
	}
	public void tick(double delta) {
		super.tick(delta);
		if (swinging) {
			offset.x = (float)(swingDistance*Math.cos(Math.toRadians(swingAngle)));
			offset.z = (float)(swingDistance*Math.sin(Math.toRadians(swingAngle)));
			weaponRotation.y = swingAngle;
			swingAngle-=15*delta;
			if (swingAngle < -180) {
				endSwing();
				
			}
		}
	}
	private static int swordSound = AudioMaster.loadSound("res/sounds/Sword/Sword_swing.wav");
	public void swing() {
		if (!swinging) {
			swinging = true;
			weaponRotation.x = 90;
			ogOffset.set(offset);
			AudioMaster.playSound(swordSound, 1, (float)(0.75f+Math.random()/2));
		}
	}
	public void endSwing() {
		swinging = false;
		swingAngle = 0;
		weaponRotation.x = 0;
		weaponRotation.y = 0;
		offset.set(ogOffset);
	}
	public void onClick() {
		swing();
	}
}
