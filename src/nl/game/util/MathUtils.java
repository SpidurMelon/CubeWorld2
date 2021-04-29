package nl.game.util;

import java.util.HashMap;
import java.util.Random;

public class MathUtils {
	private static HashMap<Random, Long> seeds = new HashMap<Random, Long>(); 
	public static int randInt(int low, int high, long seed) {
		for (Random r:seeds.keySet()) {
			if (seeds.get(r).longValue() == seed) {
				return low + r.nextInt(high-low+1);
			}
		}
		
		Random newRandom = new Random(seed);
		seeds.put(newRandom, seed);
		return low + newRandom.nextInt(high-low+1);
	}
	
	public static int randInt(int low, int high) {
		return (int)(low+Math.random()*((high-low)+1));
	}
}
