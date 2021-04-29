package nl.game.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class IOUtils {
	public static String readFile(String path) {
		StringBuilder string = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line;
			while((line = br.readLine()) != null) {
				string.append(line);
				string.append("\n");
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return string.toString();
	}
}
