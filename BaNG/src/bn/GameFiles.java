package bn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.stream.JsonParsingException;

import util.GlobFilter;

public class GameFiles {
	private static boolean initialized;
	private static File updateDir, installDir;

	public static boolean init()
	{
		initialized = true;
		switch (System.getProperty("os.name"))
		{
		case "Mac OS X":
			String homeDir = System.getProperty("user.home");
			if (homeDir == null) return false;
			updateDir = new File(homeDir + "/Library/Containers/com.z2live.battlenations-mac/Data/Library/Caches/jujulib/remoteData");
			installDir = new File("/Applications/BattleNations.app/Contents/Resources/bundle");
			break;
		default:
			return false;
		}
		return updateDir.isDirectory() && installDir.isDirectory();
	}

	public static FileInputStream open(String filename) throws IOException
	{
		if (!initialized) init();
		try {
			return new FileInputStream(new File(updateDir, filename));
		}
		catch (IOException e) {
			return new FileInputStream(new File(installDir, filename));
		}
	}

	public static JsonStructure readJson(String filename) throws IOException
	{
		JsonReader reader = Json.createReader(new BufferedReader(
				new InputStreamReader(open(filename), "utf-8")));
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

	public static String[] glob(String pat) {
		if (!initialized) init();
		FilenameFilter filter = new GlobFilter(pat);
		String[] uFiles = updateDir.list(filter);
		String[] iFiles = installDir.list(filter);
		String[] result;
		if (uFiles == null || uFiles.length == 0)
			result = iFiles;
		else if (iFiles == null || iFiles.length == 0)
			result = uFiles;
		else {
			int uLen = uFiles.length;
			int iLen = iFiles.length;
			result = new String[uLen + iLen];
			for (int i = 0; i < uLen; i++)
				result[i] = uFiles[i];
			for (int i = 0; i < iLen; i++)
				result[i+uLen] = iFiles[i];
		}
		if (result != null)
			Arrays.sort(result, String.CASE_INSENSITIVE_ORDER);
		return result;
	}

	public static void load() throws IOException {
		Text.load();
		Unit.load();
	}

}
