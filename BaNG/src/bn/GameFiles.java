package bn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.stream.JsonParsingException;

import util.FileFormatException;
import util.GlobFilter;

public class GameFiles {
	private static boolean initialized;
	private static File updateDir, installDir;

	public static boolean init() {
		initialized = true;
		String os = System.getProperty("os.name");
		String homeDir = System.getProperty("user.home");
		if (os == null || homeDir == null) return false;
		if (os.equals("Mac OS X")) {
			installDir = new File("/Applications/BattleNations.app/Contents/Resources/bundle");
			updateDir = new File(homeDir, "Library/Containers/com.z2live.battlenations-mac/Data/Library/Caches/jujulib/remoteData");
		}
		else if (os.startsWith("Windows")) {
			final String steamPath = "Steam/SteamApps/common/BattleNations/assets";
			installDir = new File("C:/Program Files", steamPath);
			if (!installDir.isDirectory())
				installDir = new File("C:/Program Files (x86)", steamPath);
			updateDir = new File(homeDir, "Local Settings/Application Data/Z2/Battle Nations/cache/remoteData");
		}
		else
			return false;
		return updateDir.isDirectory() && installDir.isDirectory();
	}

	public static FileInputStream open(String filename)
			throws IOException {
		if (!initialized) init();
		try {
			return new FileInputStream(new File(updateDir, filename));
		}
		catch (IOException e) {
			return new FileInputStream(new File(installDir, filename));
		}
	}

	public static JsonStructure readJson(String filename)
			throws IOException {
		return readJson(open(filename));
	}

	public static JsonStructure readJson(InputStream in)
			throws IOException {
		JsonReader reader = Json.createReader(new BufferedReader(
				new InputStreamReader(in, "utf-8")));
		try {
			return reader.read();
		}
		catch (JsonParsingException e) {
			throw new FileFormatException("Json parse error", e);
		}
		catch (JsonException e) {
			throw new FileFormatException("Json error", e);
		}
		finally {
			reader.close();
		}
	}

	public static File[] glob(String pat) {
		if (!initialized) init();
		FilenameFilter filter = new GlobFilter(pat);
		Map<String,File> files = new HashMap<String,File>();
		addFiles(files, installDir.listFiles(filter));
		addFiles(files, updateDir.listFiles(filter));
		String[] names = new String[files.size()];
		names = files.keySet().toArray(names);
		Arrays.sort(names);
		File[] result = new File[names.length];
		for (int i = 0; i < names.length; i++)
			result[i] = files.get(names[i]);
		return result;
	}

	private static void addFiles(Map<String,File> dest, File[] src) {
		if (src != null)
			for (File file : src)
				dest.put(file.getName().toLowerCase(), file);
	}

	public static void load() throws IOException {
		Text.load();  // must load first
		Ability.load();  // must load before unit
		Unit.load();
		Building.load();
		Timeline.load();
	}

	public static File getFile(String filename) {
		File file = new File(updateDir, filename);
		if (file.exists()) return file;
		return new File(installDir, filename);
	}

}
