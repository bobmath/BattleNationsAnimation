package bn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class Unit implements Comparable<Unit> {

	private static Map<String,Unit> units;

	private String tag, name, shortName, side;
	private String backAnimName, frontAnimName;
	private Weapon[] weapons;

	public static void load() throws IOException {
		units = new HashMap<String,Unit>();
		try {
			JsonObject json = (JsonObject) GameFiles.readJson("BattleUnits.json");
			for (Map.Entry<String,JsonValue> item : json.entrySet()) {
				String key = item.getKey();
				units.put(key, new Unit(key, (JsonObject) item.getValue()));
			}
		}
		catch (ClassCastException e) {
			throw new FileFormatException("Json type error", e);
		}
	}

	public static Unit get(String tag) {
		return units.get(tag);
	}

	public static Unit[] getAll() {
		Unit[] array = units.values().toArray(new Unit[units.size()]);
		Arrays.sort(array);
		return array;
	}

	private Unit(String tag, JsonObject json) {
		this.tag = tag;
		name = Text.get(json.getString("name"));
		if (name == null) name = tag;
		if (name.startsWith("Speciment ")) // fix game file typo
			name = "Specimen" + name.substring(9);
		shortName = Text.get(json.getString("shortName"));
		if (shortName == null) shortName = name;
		side = json.getString("side");
		backAnimName = json.getString("backIdleAnimation");
		frontAnimName = json.getString("frontIdleAnimation");
		this.buildWeapons(json.getJsonObject("weapons"));
	}

	private void buildWeapons(JsonObject json) {
		if (json == null || json.isEmpty()) {
			this.weapons = new Weapon[0];
			return;
		}
		Map<String,Weapon> weapons = new HashMap<String,Weapon>();
		for (Map.Entry<String,JsonValue> item : json.entrySet()) {
			String name = item.getKey();
			Weapon weap = new Weapon(name, (JsonObject) item.getValue());
			switch (name) {
			case "primary": name = "1primary"; break;
			case "secondary": name = "2secondary"; break;
			}
			weapons.put(name, weap);
		}
		String[] names = new String[weapons.size()];
		names = weapons.keySet().toArray(names);
		Arrays.sort(names);
		this.weapons = new Weapon[names.length];
		for (int i = 0; i < names.length; i++)
			this.weapons[i] = weapons.get(names[i]);
	}

	public String getTag() {
		return tag;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public String toString() {
		return name;
	}

	public String getSide() {
		return side;
	}

	public Animation getBackAnimation() throws IOException {
		return Animation.get(backAnimName);
	}

	public Animation getFrontAnimation() throws IOException {
		return Animation.get(frontAnimName);
	}

	public Weapon[] getWeapons() {
		Weapon[] copy = new Weapon[weapons.length+1];
		copy[0] = new Weapon();
		for (int i = 0; i < weapons.length; i++)
			copy[i+1] = weapons[i];
		return copy;
	}

	@Override
	public int compareTo(Unit that) {
		int cmp = this.name.compareTo(that.name);
		if (cmp == 0)
			cmp = this.tag.compareTo(that.tag);
		return cmp;
	}

	public class Weapon {
		String name, tag;
		String frontAnimationName, backAnimationName;
		protected Weapon() {
			this.name = "(None)";
			this.tag = "none";
		}
		protected Weapon(String tag, JsonObject json) {
			this.tag = tag;
			name = Text.get(json.getString("name"));
			if (name == null) name = tag;
			frontAnimationName = json.getString("frontattackAnimation");
			backAnimationName = json.getString("backattackAnimation");
		}
		public String getName() {
			return name;
		}
		public String getTag() {
			return tag;
		}
		public Animation getFrontAnimation() throws IOException {
			return Animation.get(frontAnimationName);
		}
		public Animation getBackAnimation() throws IOException  {
			return Animation.get(backAnimationName);
		}
		public String toString() {
			return name;
		}
	}

}
