package nl.game.setup;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;

import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.stb.STBImage;
import org.newdawn.slick.openal.Audio;

import nl.game.audio.AudioMaster;
import nl.game.util.ColorUtils;
import nl.game.util.ErrorUtils;
import nl.game.world.World;

public class MainCubeWorld {
	
	public static Window window;
	public static final Vector4f BGColor = ColorUtils.SKYBLUE;
	
	public static void main(String[] args) {
		System.out.println("Using LWJGL version " + Version.getVersion());
		glfwInit();
		
		window = new Window(1200, 800, false);
		//window = new Window();
		window.setVSync(false);
		
		int UPSLimit = 200;
		long UPSLimiter = System.nanoTime();
		
		int FPSGoal = 60;
		long optimalTime = 1000000000/FPSGoal;
		long lastTickTime = System.nanoTime();
		
		glClearColor(BGColor.x, BGColor.y, BGColor.z, BGColor.w);
		glEnable(GL_DEPTH_TEST);
		
		glfwSetInputMode(window.getID(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
		STBImage.stbi_set_flip_vertically_on_load(true);
		
		AudioMaster.init();
		
		World world = new World();
		
		while (!glfwWindowShouldClose(window.getID())) {
			glfwPollEvents();
			
			glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
			
			long currentTime = System.nanoTime();
			double delta = (currentTime-lastTickTime)/(double)optimalTime;
			lastTickTime = System.nanoTime();
			
			if (delta <= 2) {
				world.tick(delta);
			}
			world.draw();
			
			glfwSwapBuffers(window.getID());
			
			while (System.nanoTime()-UPSLimiter <= 1000000000/UPSLimit) {
				
			}
			UPSLimiter = System.nanoTime();
		}
		
		ErrorUtils.printGLErrors(true);
		world.destroy();
		AudioMaster.cleanUp();
		glfwTerminate();
	}
}
