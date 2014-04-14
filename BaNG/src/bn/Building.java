package bn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import util.FileFormatException;

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
			switch (menu.getString("title", "")) {
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
		initStructure(configs.getJsonObject("StructureMenu"));
		initAnimation(configs.getJsonObject("Animation"));
		if (name == null) name = tag;
	}

	private void initStructure(JsonObject json) {
		if (json == null) return;
		name = Text.get(json.getString("name", null));
	}

	private void initAnimation(JsonObject json) {
		if (json == null) return;
		json = json.getJsonObject("animations");
		if (json == null) return;

		busyAnimationName = json.getString("Active", null);

		idleAnimationName = json.getString("Default", null);
		if (idleAnimationName == null)
			idleAnimationName = json.getString("Idle", null);
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
		Animation anim = Animation.get(idleAnimationName);
		if (anim == null)
			anim = Animation.get(busyAnimationName);
		return anim;
	}

	public Animation getBusyAnimation() throws IOException {
		Animation anim = Animation.get(busyAnimationName);
		if (anim == null)
			anim = Animation.get(idleAnimationName);
		return anim;
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
