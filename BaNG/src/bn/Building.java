package bn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class Building implements Comparable<Building> {

	private static Map<String,Building> buildings;

	private String tag, name;

	public static void load() throws IOException {
		buildings = new HashMap<String,Building>();
		try {
			JsonObject json = (JsonObject) GameFiles.readJson("Compositions.json");
			for (Map.Entry<String,JsonValue> item : json.entrySet()) {
				String key = item.getKey();
				Building build = new Building(key, (JsonObject) item.getValue());
				buildings.put(key, build);
			}
		}
		catch (ClassCastException e) {
			throw new FileFormatException("Json type error", e);
		}
	}

	public static Building get(String tag) {
		return buildings.get(tag);
	}

	public static Building[] getAll() {
		Building[] array = new Building[buildings.size()];
		array = buildings.values().toArray(array);
		Arrays.sort(array);
		return array;
	}

	private Building(String tag, JsonObject json) {
		this.tag = tag;
		JsonObject configs = json.getJsonObject("componentConfigs");
		if (configs == null) return;

		JsonObject struct = configs.getJsonObject("StructureMenu");
		if (struct != null) {
			name = Text.get(struct.getString("name"));
		}

		if (name == null) name = tag;
	}

	public String getKey() {
		return tag;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Building that) {
		int cmp = this.name.compareTo(that.name);
		if (cmp == 0)
			cmp = this.tag.compareTo(that.tag);
		return cmp;
	}

}
