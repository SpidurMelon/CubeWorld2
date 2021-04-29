package nl.game.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformUtils {
	public static Matrix4f getWorldTransform(Vector3f translation, Vector3f scale, Vector3f rotationDegrees) {
		return new Matrix4f()
				.translate(translation)
				.scale(scale)
				.rotateXYZ((float)Math.toRadians(rotationDegrees.x), (float)Math.toRadians(rotationDegrees.y), (float)Math.toRadians(rotationDegrees.z));
	}
	
	public static Matrix4f getWorldTransform(Vector3f translation, float scale, Vector3f rotationDegrees) {
		return new Matrix4f()
				.translate(translation)
				.scale(scale)
				.rotateXYZ((float)Math.toRadians(rotationDegrees.x), (float)Math.toRadians(rotationDegrees.y), (float)Math.toRadians(rotationDegrees.z));
	}
	
	public static Matrix4f getPerspectiveTransform(float fov, float screenWidth, float screenHeight, float minRenderDistance, float maxRenderDistance) {
		return new Matrix4f().perspective(fov, screenWidth/screenHeight, minRenderDistance, maxRenderDistance);
	}
	
	public static Matrix4f getPerspectiveTransform(float fov, float aspectRatio, float minRenderDistance, float maxRenderDistance) {
		return new Matrix4f().perspective(fov, aspectRatio, minRenderDistance, maxRenderDistance);
	}
}
