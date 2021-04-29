package nl.game.rendering.shaders;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ParticleShader extends Shader {
	private static final String SHADERNAME = "particleshader";
	
	public ParticleShader() {
		super();
		
		setVertexShader(loadShader(SHADERNAME + ".vs"));
		setFragmentShader(loadShader(SHADERNAME + ".fs"));
		compileProgram();
		
		addUniform("perspectiveTransform");
		addUniform("worldTransform");
		
		addUniform("color");
		
		addUniform("fogStart");
		addUniform("fogEnd");
		addUniform("fogColor");
		
		addUniform("entityPosition");
	}

	public void bindAttributes() {
		bindAttribute(0, "position");
	}
	
	public void setPerspectiveTransformation(Matrix4f matrix) {
		setUniform("perspectiveTransform", matrix);
	}
	public void setWorldTransformation(Matrix4f matrix) {
		setUniform("worldTransform", matrix);
	}
	
	public void setColor(Vector4f color) {
		setUniform("color", color);
	}
	
	public void setFogStart(float fogStart) {
		setUniform("fogStart", fogStart);
	}
	public void setFogEnd(float fogEnd) {
		setUniform("fogEnd", fogEnd);
	}
	public void setFogColor(Vector4f fogColor) {
		setUniform("fogColor", fogColor);
	}
	
	public void setEntityPosition(Vector3f entityPosition) {
		setUniform("entityPosition", entityPosition);
	}
}
