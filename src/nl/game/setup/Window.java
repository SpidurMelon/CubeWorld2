package nl.game.setup;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.Toolkit;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

public class Window {
	private long window;
	private int width, height;
	public Window() {
		this(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height, true);
	}
	
	public Window(int width, int height, boolean fullScreen) {
		this.width = width;
		this.height = height;
		
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		if (fullScreen) {
			window = glfwCreateWindow(width, height, "Game_1", glfwGetPrimaryMonitor(), 0);
		} else {
			window = glfwCreateWindow(width, height, "Game_1", 0, 0);
		}
		GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (vidMode.width()-width)/2, (vidMode.height()-height)/2);
		glfwShowWindow(window);
		glfwMakeContextCurrent(window);
		
		GL.createCapabilities();
	}
	
	public void setVSync(boolean vsync) {
		if (vsync) {
			glfwSwapInterval(1);
		} else {
			glfwSwapInterval(0);
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public long getID() {
		return window;
	}
}
