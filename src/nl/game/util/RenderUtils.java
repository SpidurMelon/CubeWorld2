package nl.game.util;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import nl.game.objects.Line;

public class RenderUtils {
	private static final int pointBuffer = glGenBuffers();
	private static final int lineBuffer = glGenBuffers();
	private static Vector4f color = ColorUtils.RED;
	
	public static void setColor(Vector4f color) {
		RenderUtils.color = color;
	}
	
	public static void renderPoint(Vector3f position, int pointSize) {
		FloatBuffer prePointBuffer = BufferUtils.createFloatBuffer(1*8);
		prePointBuffer.put(position.x);
		prePointBuffer.put(position.y);
		prePointBuffer.put(position.z);
		prePointBuffer.put(color.x);
		prePointBuffer.put(color.y);
		prePointBuffer.put(color.z);
		prePointBuffer.put(color.w);
		prePointBuffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, pointBuffer);
		glBufferData(GL_ARRAY_BUFFER, prePointBuffer, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 7*4, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 7*4, 3*4);
		
		glPointSize(pointSize);
		glDrawArrays(GL_POINTS, 0, 1);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public static void renderLine(Line line) {
		renderLine(line.point1, line.point2);
	}
	
	public static void renderLine(Vector3f point1, Vector3f point2) {
		FloatBuffer preLineBuffer = BufferUtils.createFloatBuffer(4*7);
		preLineBuffer.put(point1.x);
		preLineBuffer.put(point1.y);
		preLineBuffer.put(point1.z);
		preLineBuffer.put(color.x);
		preLineBuffer.put(color.y);
		preLineBuffer.put(color.z);
		preLineBuffer.put(color.w);
		
		preLineBuffer.put(point2.x);
		preLineBuffer.put(point2.y);
		preLineBuffer.put(point2.z);
		preLineBuffer.put(color.x);
		preLineBuffer.put(color.y);
		preLineBuffer.put(color.z);
		preLineBuffer.put(color.w);
		preLineBuffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, lineBuffer);
		glBufferData(GL_ARRAY_BUFFER, preLineBuffer, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 7*4, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 7*4, 3*4);
		
		glDrawArrays(GL_LINES, 0, 2);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
}
