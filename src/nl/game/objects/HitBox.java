package nl.game.objects;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import nl.game.map.BlockData;
import nl.game.map.Chunk;
import nl.game.util.ColorUtils;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.*;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class HitBox {
	private int vbo;
	public float x, y, z, width, height, depth;
	private Vector3f center = new Vector3f();
	public HitBox() {}
	public HitBox(float x, float y, float z, float width, float height, float depth) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	public void setLocation(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		center.set(getCenterX(), getCenterY(), getCenterZ());
	}
	public void setSize(float width, float height, float depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		center.set(getCenterX(), getCenterY(), getCenterZ());
	}
	public void setBounds(float x, float y, float z, float width, float height, float depth) {
		setLocation(x, y, z);
		setSize(width, height, depth);
	}
	
	public Vector3f getLeastResistantVector(boolean[] allowedDirections, HitBox staticBox) {
		Vector3f[] collisionVectors = getCollisionVectors(staticBox);
		if (collisionVectors == null) {
			return null;
		}
		for (int i = 0; i < 6; i++) {
			if (collisionVectors[i].x > 0 && allowedDirections[BlockData.RIGHT]) {
				return collisionVectors[i];
			}
			if (collisionVectors[i].x < 0 && allowedDirections[BlockData.LEFT]) {
				return collisionVectors[i];
			}
			if (collisionVectors[i].y > 0 && allowedDirections[BlockData.TOP]) {
				return collisionVectors[i];
			}
			if (collisionVectors[i].y < 0 && allowedDirections[BlockData.BOTTOM]) {
				return collisionVectors[i];
			}
			if (collisionVectors[i].z > 0 && allowedDirections[BlockData.FRONT]) {
				return collisionVectors[i];
			}
			if (collisionVectors[i].z < 0 && allowedDirections[BlockData.BACK]) {
				return collisionVectors[i];
			}
		}
		return null;
	}
	
	public Vector3f[] getCollisionVectors(HitBox staticBox) {
		if (this.intersects(staticBox)) {
			float maxAllowedDX = width/2+staticBox.width/2;
			float currentDX = Math.abs(getCenterX()-staticBox.getCenterX());
			float separationDX;
			if (currentDX < maxAllowedDX) {
				separationDX = maxAllowedDX-currentDX;
			} else {
				separationDX = 0;
			}
			
			float maxAllowedDY = height/2+staticBox.height/2;
			float currentDY = Math.abs(getCenterY()-staticBox.getCenterY());
			float separationDY;
			if (currentDY < maxAllowedDY) {
				separationDY = maxAllowedDY-currentDY;
			} else {
				separationDY = 0;
			}
			
			float maxAllowedDZ = depth/2+staticBox.depth/2;
			float currentDZ = Math.abs(getCenterZ()-staticBox.getCenterZ());
			float separationDZ;
			if (currentDZ < maxAllowedDZ) {
				separationDZ = maxAllowedDZ-currentDZ;
			} else {
				separationDZ = 0;
			}
			
			Vector3f[] result = new Vector3f[6];
			float xMultiplier = 1, yMultiplier= 1, zMultiplier = 1;
			for (int i = 0; i < 6; i++) {
				if (separationDX <= separationDY && separationDX <= separationDZ) {
					if (getCenterX() > staticBox.getCenterX()) {
						result[i] = new Vector3f(separationDX*xMultiplier, 0, 0);
					} else {
						result[i] = new Vector3f(-separationDX*xMultiplier, 0, 0);
					}
					if (separationDX <= maxAllowedDX) {
						separationDX = (maxAllowedDX*2)-separationDX;
					} else {
						separationDX = Float.POSITIVE_INFINITY;
					}
					xMultiplier*=-1;
				} 
				else if (separationDY <= separationDX && separationDY <= separationDZ) {
					if (getCenterY() > staticBox.getCenterY()) {
						result[i] = new Vector3f(0, separationDY*yMultiplier, 0);
					} else {
						result[i] = new Vector3f(0, -separationDY*yMultiplier, 0);
					}
					if (separationDY <= maxAllowedDY) {
						separationDY = (maxAllowedDY*2)-separationDY;
					} else {
						separationDY = Float.POSITIVE_INFINITY;
					}
					yMultiplier*=-1;
				}
				else if (separationDZ <= separationDX && separationDZ <= separationDY) {
					if (getCenterZ() > staticBox.getCenterZ()) {
						result[i] = new Vector3f(0, 0, separationDZ*zMultiplier);
					} else {
						result[i] = new Vector3f(0, 0, -separationDZ*zMultiplier);
					}
					if (separationDZ <= maxAllowedDZ) {
						separationDZ = (maxAllowedDZ*2)-separationDZ;
					} else {
						separationDZ = Float.POSITIVE_INFINITY;
					}
					zMultiplier*=-1;
				}
			}
			return result;
		} 
		return null;
	}
	
	public boolean intersects(HitBox other) {
		return 	Math.abs(getCenterX()-other.getCenterX()) < (width/2+other.width/2) && 
				Math.abs(getCenterY()-other.getCenterY()) < (height/2+other.height/2) && 
				Math.abs(getCenterZ()-other.getCenterZ()) < (depth/2+other.depth/2);
	}
	
	public void draw() {
		if (vbo == 0) {
			vbo = glGenBuffers();
		}
		FloatBuffer buffer = BufferUtils.createFloatBuffer(2*4*3*7);
		putHorizontals(buffer);
		putVerticals(buffer);
		putDeptals(buffer);
		buffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 7*4, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 7*4, 3*4);
		
		glLineWidth(3);
		glDrawArrays(GL_LINES, 0, 2*4*3);
		glLineWidth(1);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public float getMaxX() {
		return x+width;
	}
	
	public float getMaxY() {
		return y+height;
	}
	
	public float getMaxZ() {
		return z+depth;
	}
	
	public float getCenterX() {
		return x+width/2;
	}
	
	public float getCenterY() {
		return y+height/2;
	}
	
	public float getCenterZ() {
		return z+depth/2;
	}
	
	public Vector3f getCenter() {
		return center;
	}
	
	public float distance(HitBox other) {
		return Math.abs(this.getCenterX()-other.getCenterX())+Math.abs(this.getCenterY()-other.getCenterY())+Math.abs(this.getCenterZ()-other.getCenterZ());
	}
	
	private void putHorizontals(FloatBuffer buffer) {
		buffer.put(x);
		buffer.put(y);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x);
		buffer.put(y+height);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y+height);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x);
		buffer.put(y);
		buffer.put(z+depth);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y);
		buffer.put(z+depth);
		putBasics(buffer);
		
		buffer.put(x);
		buffer.put(y+height);
		buffer.put(z+depth);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y+height);
		buffer.put(z+depth);
		putBasics(buffer);
	}
	
	private void putVerticals(FloatBuffer buffer) {
		buffer.put(x);
		buffer.put(y);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x);
		buffer.put(y+height);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y+height);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x);
		buffer.put(y);
		buffer.put(z+depth);
		putBasics(buffer);
		
		buffer.put(x);
		buffer.put(y+height);
		buffer.put(z+depth);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y);
		buffer.put(z+depth);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y+height);
		buffer.put(z+depth);
		putBasics(buffer);
	}
	
	private void putDeptals(FloatBuffer buffer) {
		buffer.put(x);
		buffer.put(y);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x);
		buffer.put(y);
		buffer.put(z+depth);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y);
		buffer.put(z+depth);
		putBasics(buffer);
		
		buffer.put(x);
		buffer.put(y+height);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x);
		buffer.put(y+height);
		buffer.put(z+depth);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y+height);
		buffer.put(z);
		putBasics(buffer);
		
		buffer.put(x+width);
		buffer.put(y+height);
		buffer.put(z+depth);
		putBasics(buffer);
	}
	
	private void putBasics(FloatBuffer buffer) {
		buffer.put(ColorUtils.BLUE.x);
		buffer.put(ColorUtils.BLUE.y);
		buffer.put(ColorUtils.BLUE.z);
		buffer.put(ColorUtils.BLUE.w);
	}
}
