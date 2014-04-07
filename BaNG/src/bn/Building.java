package bn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public class Building implements Comparable<Building> {

	private static Map<String,Building> buildings;

	private String tag, name, menu;
	private String idleAnimationName, busyAnimationName;

	public static void load() throws IOException {
		buildings = new HashMap<String,Building>();
		try {
			loadBuildings();
			loadMenus();
		}
		catch (ClassCastException e) {
			throw new FileFormatException("Json type error", e);
		}
	}

	private static void loadBuildings() throws IOException {
		JsonObject json = (JsonObject) GameFiles.readJson("Compositions.json");
		for (Map.Entry<String,JsonValue> item : json.entrySet()) {
			String key = item.getKey();
			Building build = new Building(key, (JsonObject) item.getValue());
			buildings.put(key, build);
		}
	}

	private static void loadMenus() throws IOException {
		JsonArray json = (JsonArray) GameFiles.readJson("StructureMenu.json");
		for (JsonValue item : json) {
			JsonObject menu = (JsonObject) item;
			String title;
			switch (menu.getString("title")) {
			case "bmCat_houses": title = "Housing"; break;
			case "bmCat_shops": title = "Shops"; break;
			case "bmCat_military": title = "Military"; break;
			case "bmCat_resources": title = "Resources"; break;
			case "bmCat_decorations": title = "Decorations"; break;
			default: continue;
			}
			JsonArray bldList = menu.getJsonArray("options");
			for (JsonValue bldName : bldList) {
				String name = ((JsonString) bldName).getString();
				Building bld = buildings.get(name);
				if (bld != null)
					bld.menu = title;
			}
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

		JsonObject obj = configs.getJsonObject("StructureMenu");
		if (obj != null) {
			name = Text.get(obj.getString("name"));
		}

		obj = configs.getJsonObject("Animation");
		if (obj != null) {
			obj = obj.getJsonObject("animations");
			if (obj != null) {
				if (obj.containsKey("Active"))
					busyAnimationName = obj.getString("Active");
				if (obj.containsKey("Default"))
					idleAnimationName = obj.getString("Default");
				else if (obj.containsKey("Idle"))
					idleAnimationName = obj.getString("Idle");
				else
					System.out.println(tag);
			}
		}

		if (name == null) name = tag;
	}

	public String getTag() {
		return tag;
	}

	public String getName() {
		return name;
	}

	public String getBuildMenu() {
		return menu;
	}

	public boolean hasAnimation() {
		return idleAnimationName != null;
	}

	public Animation getIdleAnimation() throws IOException {
		return Animation.get(idleAnimationName);
	}

	public Animation getBusyAnimation() throws IOException {
		return Animation.get(busyAnimationName);
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
