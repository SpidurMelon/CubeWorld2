package nl.game.map;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import nl.game.util.IOUtils;
import nl.game.world.World;

public class Structure {
	public Integer[][][] structureData;
	public int depth, height, width;
	public Structure(String path) {
		structureData = loadStructure(path);
	}
	private Integer[][][] loadStructure(String path) {
		String data = IOUtils.readFile(path);
		data = data.replaceAll("\n", " ").replaceAll(" +", " ").replaceAll(";[^0-9-]+", ";");
		String[] layers = data.split(";");
		
		String[] dimensions = layers[0].split(" ");
		depth = Integer.valueOf(dimensions[2]);
		height = Integer.valueOf(dimensions[1]);
		width = Integer.valueOf(dimensions[0]);
		
		Integer[][][] structure = new Integer[depth][height][width];
		
		for (int by = 0; by < height; by++) {
			String[] currentLayer = layers[by+1].split(" ");
			for (int bz = 0; bz < depth; bz++) {
				for (int bx = 0; bx < width; bx++) {
					structure[bz][by][bx] = Integer.valueOf(currentLayer[bx+bz*width]);
				}
			}
		}
		
		return structure;
	}
	public static void writeStructure(Integer[][][] data, String name) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("res/structures/" + name + ".str"));
			writer.write(data[0][0].length + " " + data[0].length + " " + data.length + ";");
			writer.newLine();
			for (int y = 0; y < data[0].length; y++) {
				for (int z = 0; z < data.length; z++) {
					for (int x = 0; x < data[0][0].length; x++) {
						writer.write(data[z][y][x] + " ");
					}
					writer.newLine();
				}
				writer.write(";");
				writer.newLine();
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
