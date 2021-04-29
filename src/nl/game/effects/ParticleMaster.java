package nl.game.effects;

import java.util.ArrayList;

import org.joml.Vector4f;

import nl.game.entities.Player;
import nl.game.map.BlockData;
import nl.game.map.ChunkData;
import nl.game.objects.Camera;
import nl.game.rendering.shaders.ParticleShader;
import nl.game.setup.MainCubeWorld;
import nl.game.util.TransformUtils;
import nl.game.world.World;

public class ParticleMaster {
	public static ParticleShader shader = new ParticleShader();
	
	public static void init() {
		shader.bind();
		shader.setPerspectiveTransformation(TransformUtils.getPerspectiveTransform(Player.FOV, MainCubeWorld.window.getWidth()/(float)MainCubeWorld.window.getHeight(), 0.1f, 100000));
		shader.setFogStart(World.renderDistance*ChunkData.chunkSize*BlockData.blockSize*0.5f*0.7f);
		shader.setFogEnd(World.renderDistance*ChunkData.chunkSize*BlockData.blockSize*0.5f*0.8f);
		shader.setFogColor(MainCubeWorld.BGColor);
	}
	
	public static void showParticle(float x, float y, float z, float vx, float vy, float vz, boolean gravity, Vector4f color) {
		getInactiveParticle().start(x, y, z, vx, vy, vz, gravity, color);
	}
	
	public static void drawParticles(Camera c) {
		shader.bind();
		for (Particle p:particles) {
			if (p.visible) {
				p.draw(c, shader);
			}
		}
	}
	
	public static ArrayList<Particle> particles = new ArrayList<Particle>();
	public static Particle getInactiveParticle() {
		for (Particle p:particles) {
			if (!p.inAir) {
				return p;
			}
		}
		
		Particle newParticle = new Particle();
		particles.add(newParticle);
		return newParticle;
	}
}
