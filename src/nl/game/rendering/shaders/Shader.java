package nl.game.rendering.shaders;

import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import nl.game.util.IOUtils;

public abstract class Shader {
	private int program;
	
	private ArrayList<Integer> shaders = new ArrayList<Integer>();
	private HashMap<String, Integer> uniforms = new HashMap<String, Integer>();
	
	public Shader() {
		program = glCreateProgram();
		
		if (program == 0) {
			System.err.print("Program creation failed");
			System.exit(1);
		}
	}
	
	public abstract void bindAttributes();
	
	protected void bindAttribute(int attribute, String variablename) {
		glBindAttribLocation(program, attribute, variablename);
	}
	
	protected void addUniform(String name) {
		int uniformLocation = glGetUniformLocation(program, name);
		
		if (uniformLocation == 0xFFFFFFFF) {
			System.err.println("Error: Could not find uniform: " + name);
			System.exit(1);
		}
		
		uniforms.put(name, uniformLocation);
	}
	
	protected void setUniform(String name, float value) {
		glUniform1f(uniforms.get(name), value);
	}
	
	protected void setUniform(String name, Vector2f value) {
		glUniform2f(uniforms.get(name), value.x, value.y);
	}
	
	protected void setUniform(String name, Vector3f value) {
		glUniform3f(uniforms.get(name), value.x, value.y, value.z);
	}
	
	protected void setUniform(String name, Vector4f value) {
		glUniform4f(uniforms.get(name), value.x, value.y, value.z, value.w);
	}
	
	protected void setUniform(String name, Vector3i value) {
		glUniform3f(uniforms.get(name), value.x, value.y, value.z);
	}
	
	protected void setUniform(String name, Matrix4f value) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(4*4);
		value.get(buffer);
		
		glUniformMatrix4fv(uniforms.get(name), false, buffer);
	}
	
	public void bind() {
		glUseProgram(program);
	}
	
	public static void unbind() {
		glUseProgram(0);
	}
	
	protected void setVertexShader(String text) {
		addToProgram(text, GL_VERTEX_SHADER);
	}
	
	protected void setFragmentShader(String text) {
		addToProgram(text, GL_FRAGMENT_SHADER);
	}
	
	protected void compileProgram() {
		glLinkProgram(program);
		if (glGetProgrami(program, GL_LINK_STATUS) == 0) {
			System.err.print(glGetProgramInfoLog(program));
			System.exit(1);
		}
		
		glValidateProgram(program);
		if (glGetProgrami(program, GL_VALIDATE_STATUS) == 0) {
			System.err.print(glGetProgramInfoLog(program));
			System.exit(1);
		}
		
		bindAttributes();
	}
	
	private void addToProgram(String text, int type) {
		int shader = glCreateShader(type);
		if (shader == 0) {
			System.err.print("Shader creation failed");
			System.exit(1);
		}
		shaders.add(shader);
		glShaderSource(shader, text);
		glCompileShader(shader);
		
		if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
			System.err.print(glGetShaderInfoLog(shader));
			System.exit(1);
		}
		
		glAttachShader(program, shader);
	}
	
	protected String loadShader(String shadername) {
		return IOUtils.readFile("res/shaders/" + shadername);
	}
	
	public int getProgram() {
		return program;
	}
	
	public void destroy() {
		for (int i:shaders) {
			glDeleteShader(i);
		}
		glDeleteProgram(program);
	}
}
