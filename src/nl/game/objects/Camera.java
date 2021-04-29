package nl.game.objects;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {
	public Vector3f worldTranslation = new Vector3f();
	private Vector3f thirdPerson = new Vector3f();
	public Vector2f rotation = new Vector2f();
	private Matrix4f worldTransform = new Matrix4f();
	public static final Matrix4f nullTransform = new Matrix4f();
	public Matrix4f getWorldTransform() {
		return worldTransform
				.rotationXYZ((float)Math.toRadians(rotation.x), (float)Math.toRadians(rotation.y), 0)
				.translate(worldTranslation.add(-(float)(thirdPerson.x*Math.cos(Math.toRadians(rotation.y))-thirdPerson.z*Math.sin(Math.toRadians(rotation.y))), -thirdPerson.y, -(float)(thirdPerson.x*Math.sin(Math.toRadians(rotation.y))+thirdPerson.z*Math.cos(Math.toRadians(rotation.y))), new Vector3f()))
		;
	}
	
	public void setThirdPerson(float x, float y, float z) {
		thirdPerson.set(x, y, z);
	}
	
	private Vector3f position = new Vector3f();
	public Vector3f getPosition() {
		worldTranslation.add(-(float)(thirdPerson.x*Math.cos(Math.toRadians(rotation.y))-thirdPerson.z*Math.sin(Math.toRadians(rotation.y))), -thirdPerson.y, -(float)(thirdPerson.x*Math.sin(Math.toRadians(rotation.y))+thirdPerson.z*Math.cos(Math.toRadians(rotation.y))), position);
		position.mul(-1);
		return position;
	}
	
	private Vector3f rayPoint = new Vector3f();
	public Vector3f getRayPoint(float size) {
		Vector3f position = getPosition();
		position.add(	(float)(size*Math.sin(Math.toRadians(rotation.y))*Math.cos(Math.toRadians(rotation.x))),
						(float)(-size*Math.sin(Math.toRadians(rotation.x))), 
						(float)(-size*Math.cos(Math.toRadians(rotation.y))*Math.cos(Math.toRadians(rotation.x))),
						rayPoint
					);
		return rayPoint;
	}
	
	private Line ray = new Line();
	public Line getRay(float size) {
		ray.point1.set(getPosition());
		ray.point2.set(getRayPoint(size));
		return ray;
	}
	
}
