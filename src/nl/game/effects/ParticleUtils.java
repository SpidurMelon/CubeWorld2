package nl.game.effects;

import org.joml.Vector4f;

public class ParticleUtils {
	public static void particleSPLOSION(float x, float y, float z, Vector4f color, int amountOfParticles, float splosionPower) {
		for (int i = 0; i < amountOfParticles; i++) {
			ParticleMaster.showParticle(x, y, z, 
					(float)(Math.random()-0.5)*splosionPower, (float)(Math.random()-0.5)*splosionPower, (float)(Math.random()-0.5)*splosionPower,
					true, 
					color);
		}
	}
	public static void magicParticleSPLOSION(float x, float y, float z, Vector4f color, int amountOfParticles, float splosionPower) {
		for (int i = 0; i < amountOfParticles; i++) {
			ParticleMaster.showParticle(x, y, z, 
					(float)(Math.random()-0.5)*splosionPower, (float)(Math.random()-0.5)*splosionPower, (float)(Math.random()-0.5)*splosionPower,
					false, 
					color);
		}
	}
}
