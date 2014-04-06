package bn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class Unit implements Comparable<Unit> {

	private static Map<String,Unit> units;

	private String tag;
	private String name;
	private String side;
	private String backAnimName, frontAnimName;

	public static void load() throws IOException
	{
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
	
	public static Unit[] getPlayer() {
		List<Unit> list = new ArrayList<Unit>();
		for (Unit unit : units.values())
			if (unit.isPlayer())
				list.add(unit);
		Unit[] array = list.toArray(new Unit[list.size()]);
		Arrays.sort(array);
		return array;
	}

	private Unit(String tag, JsonObject json) {
		this.tag = tag;
		name = Text.get(json.getString("name"));
		if (name == null) name = tag;
		if (name.startsWith("Speciment "))
			name = "Specimen" + name.substring(9);
		side = json.getString("side");
		backAnimName = json.getString("backIdleAnimation");
		frontAnimName = json.getString("frontIdleAnimation");
	}
	
	public String getTag() {
		return tag;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	public String getSide() {
		return side;
	}
	
	private boolean isPlayer() {
		return side.equals("Player");
	}
	
	public Animation getBackAnimation() throws IOException {
		return Animation.get(backAnimName);
	}
	
	public Animation getFrontAnimation() throws IOException {
		return Animation.get(frontAnimName);
	}

	@Override
	public int compareTo(Unit that) {
		return this.name.compareTo(that.name);
	}
	
}
