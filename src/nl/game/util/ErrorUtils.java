package nl.game.util;

import static org.lwjgl.opengl.GL46.*;

public class ErrorUtils {
	public static void printGLErrors(boolean printNeutral) {
		int errorCode = glGetError();
		if (errorCode == GL_INVALID_OPERATION) {
			System.err.println("GL_INVALID_OPERATION: " + errorCode);
		} else if (errorCode == GL_INVALID_VALUE) {
			System.err.println("GL_INVALID_VALUE: " + errorCode);
		} else if (errorCode == GL_INVALID_ENUM) {
			System.err.println("GL_INVALID_ENUM: " + errorCode);
		} else if (errorCode == GL_STACK_OVERFLOW) {
			System.err.println("GL_STACK_OVERFLOW: " + errorCode);
		} else if (errorCode == GL_STACK_UNDERFLOW) {
			System.err.println("GL_STACK_UNDERFLOW: " + errorCode);
		} else if (errorCode == GL_OUT_OF_MEMORY) {
			System.err.println("GL_OUT_OF_MEMORY: " + errorCode);
		} else if (errorCode == GL_INVALID_FRAMEBUFFER_OPERATION) {
			System.err.println("GL_INVALID_FRAMEBUFFER_OPERATION: " + errorCode);
		} else if (errorCode == GL_CONTEXT_LOST) {
			System.err.println("GL_CONTEXT_LOST: " + errorCode);
		} else if (errorCode != 0) {
			System.err.println("Undefined error with code " + errorCode);
		} else if (printNeutral) {
			System.out.println("No errors found");
		}
	}
}
