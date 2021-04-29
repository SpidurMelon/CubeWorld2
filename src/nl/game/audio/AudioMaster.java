package nl.game.audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.newdawn.slick.openal.WaveData;

public class AudioMaster {
	
	private static long device;
	public static void init() {
		String defaultDeviceName = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
		device = ALC10.alcOpenDevice(defaultDeviceName);
		
		int[] attributes = {0};
		long context = ALC10.alcCreateContext(device, attributes);
		ALC10.alcMakeContextCurrent(context);
		
		ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
	}
	
	public static void setListenerData() {
		AL10.alListener3f(AL10.AL_POSITION, 0, 0, 0);
	}
	
	private static ArrayList<Integer> buffers = new ArrayList<Integer>();
	public static int loadSound(String path) {
		int buffer = AL10.alGenBuffers();
		buffers.add(buffer);
		AudioInputStream ais = null;
		try {
			ais = AudioSystem.getAudioInputStream(new File(path));
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		WaveData waveFile = WaveData.create(ais);
		AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		return buffer;
	}
	
	public static int createSource() {
		return AL10.alGenSources();
	}
	
	private static ArrayList<Integer> sources = new ArrayList<Integer>();
	private static int getAvailableSource() {
		for (Integer sourceId:sources) {
			if (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) {
				return sourceId;
			}
		}
		
		int newSource = AL10.alGenSources();
		AL10.alSource3f(newSource, AL10.AL_POSITION, 0, 0, 0);
		
		sources.add(newSource);
		return newSource;
	}
	
	public static void playSound(int buffer) {
		int source = getAvailableSource();
		playSound(source, buffer, 1, 1);
	}
	
	public static void playSound(int source, int buffer) {
		playSound(source, buffer, 1, 1);
	}
	
	public static void playSound(int buffer, float volume, float pitch) {
		int source = getAvailableSource();
		playSound(source, buffer, volume, pitch);
	}
	
	public static float volumeModifier = 0.3f;
	public static void playSound(int source, int buffer, float volume, float pitch) {
		AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
		AL10.alSourcef(source, AL10.AL_GAIN, volume*volumeModifier);
		AL10.alSourcef(source, AL10.AL_PITCH, pitch);
		AL10.alSourcePlay(source);
	}
	
	public static void stopSound(int source) {
		AL10.alSourceStop(source);
	}
	
	public static void cleanUp() {
		for (int buffer:buffers) {
			AL10.alDeleteBuffers(buffer);
		}
		for (int source:sources) {
			AL10.alDeleteSources(source);
		}
		ALC10.alcCloseDevice(device);
		ALC.destroy();
	}
}
