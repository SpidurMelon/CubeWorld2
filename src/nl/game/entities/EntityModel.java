package nl.game.entities;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.*;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import nl.game.util.IOUtils;

public class EntityModel {
	public int vbo, ibo;
	protected int totalVertices;
	public boolean shouldUseIndices = false;
	
	public EntityModel() {
		vbo = glGenBuffers();
		ibo = glGenBuffers();
	}
	
	public void putPlyData(String path) {
		FloatBuffer vertices = null;
		IntBuffer indices = null;
		String data = IOUtils.readFile(path);
		String[] lines = data.split("\n");
		
		for (String line:lines) {
			String[] lineData = line.split(" ");
			if (lineData[0].equals("element")) {
				if (lineData[1].equals("vertex")) {
					vertices = BufferUtils.createFloatBuffer(Integer.valueOf(lineData[2])*7);
				}
			}
			if (lineData[0].equals("element")) {
				if (lineData[1].equals("face")) {
					totalVertices = Integer.valueOf(lineData[2])*6;
					indices = BufferUtils.createIntBuffer(Integer.valueOf(lineData[2])*6);
				}
			}
			if (lineData.length == 6) {
				vertices.put(Float.valueOf(lineData[0]));
				vertices.put(Float.valueOf(lineData[1]));
				vertices.put(Float.valueOf(lineData[2]));
				vertices.put(Integer.valueOf(lineData[3])/255f);
				vertices.put(Integer.valueOf(lineData[4])/255f);
				vertices.put(Integer.valueOf(lineData[5])/255f);
				vertices.put(1);
			}
			if (lineData.length == 5 && lineData[0].equals("4")) {
				indices.put(Integer.valueOf(lineData[1]));
				indices.put(Integer.valueOf(lineData[2]));
				indices.put(Integer.valueOf(lineData[3]));
				
				indices.put(Integer.valueOf(lineData[3]));
				indices.put(Integer.valueOf(lineData[4]));
				indices.put(Integer.valueOf(lineData[1]));
			}
		}
		
		vertices.flip();
		indices.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, ibo);
		glBufferData(GL_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		shouldUseIndices = true;
	}
	
	public void putRawData(FloatBuffer vertices, IntBuffer indices, int totalVertices) {
		this.totalVertices = totalVertices;
		shouldUseIndices = false;
		if (vertices != null) {
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
		}
		if (indices != null) {
			glBindBuffer(GL_ARRAY_BUFFER, ibo);
			glBufferData(GL_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			shouldUseIndices = true;
		}
	}
	
	public void draw() {
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 7*4, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 7*4, 3*4);
		
		if (shouldUseIndices) {
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
			glDrawElements(GL_TRIANGLES, totalVertices, GL_UNSIGNED_INT, 0);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		} else {
			glDrawArrays(GL_TRIANGLES, 0, totalVertices);
		}
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void destroy() {
		glDeleteBuffers(vbo);
		glDeleteBuffers(ibo);
	}
}
