package bn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import util.FileFormatException;

public class Text {

	private static Map<String,String> text;

	public static void load() throws IOException {
		text = new HashMap<String,String>();
		try {
			loadJson("BattleNations_en.json");
			loadJson("Delta_en.json");
		}
		catch (ClassCastException e) {
			throw new FileFormatException("Json type error", e);
		}
	}

	private static void loadJson(String file) throws IOException {
		JsonObject json = (JsonObject) GameFiles.readJson(file);
		for (Map.Entry<String,JsonValue> item : json.entrySet()) {
			String str = ((JsonString) item.getValue()).getString();
			text.put(item.getKey().toLowerCase(), str);
		}
	}

	public static String get(String key) {
		if (key == null) return null;
		return text.get(key.toLowerCase());
	}

	private Text() {
	}

}
