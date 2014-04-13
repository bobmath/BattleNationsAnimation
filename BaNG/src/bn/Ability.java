package bn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class Ability {

	public static final Ability NO_ABILITY = new Ability();

	private static Map<String,Ability> abilities;

	private String tag, name;
	private String frontAnimationName, backAnimationName;
	private double damageFromWeapon, damageFromUnit;
	private int damageBonus;
	private int aoeDelay;
	private String targetType;
	private boolean randomTarget;
	private TargetSquare[] targetArea, damageArea;

	public static void load() throws IOException {
		abilities = new HashMap<String,Ability>();
		try {
			JsonObject damageAnim = (JsonObject)
					GameFiles.readJson("DamageAnimConfig.json");
			JsonObject json = (JsonObject)
					GameFiles.readJson("BattleAbilities.json");
			for (Map.Entry<String,JsonValue> item : json.entrySet()) {
				String key = item.getKey();
				Ability abil = new Ability(key,
						(JsonObject) item.getValue(), damageAnim);
				abilities.put(key, abil);
			}
		}
		catch (ClassCastException e) {
			throw new FileFormatException("Json type error", e);
		}
	}

	private Ability() {
		tag = "none";
		name = "(None)";
	}

	private Ability(String tag, JsonObject json, JsonObject dmgAnim) {
		this.tag = tag;
		name = Text.get(json.getString("name", null));
		if (name == null) name = tag;
		initAnimation(json, dmgAnim);
		initStats(json.getJsonObject("stats"));
	}

	private void initAnimation(JsonObject json, JsonObject dmgAnim) {
		String animType = json.getString("damageAnimationType", null);
		if (animType == null) return;
		JsonObject dmg = dmgAnim.getJsonObject(animType);
		if (dmg == null) return;
		frontAnimationName = dmg.getString("front", null);
		backAnimationName = dmg.getString("back", null);
	}

	private void initStats(JsonObject stats) {
		if (stats == null) return;

		damageBonus = stats.getInt("damage", 0);
		damageFromWeapon = getDouble(stats, "damageFromWeapon", 1);
		damageFromUnit = getDouble(stats, "damageFromUnit", 1);

		damageArea = initArea(stats.getJsonObject("damageArea"), false);
		JsonObject targ = stats.getJsonObject("targetArea");
		if (targ != null) {
			targetType = targ.getString("type", null);
			randomTarget = targ.getBoolean("random");
			aoeDelay = (int) Math.round(
					getDouble(targ, "aoeOrderDelay", 0) * 20);
			targetArea = initArea(targ, randomTarget);
		}
	}

	private static TargetSquare[] initArea(JsonObject area,
			boolean random) {
		if (area == null) return null;
		JsonArray data = area.getJsonArray("data");
		if (data == null) return null;

		double weight = 0;
		if (random) {
			for (JsonValue item : data)
				weight += getDouble((JsonObject) item, "weight", 0);
		}

		TargetSquare[] squares = new TargetSquare[data.size()];
		for (int i = 0; i < squares.length; i++)
			squares[i] = new TargetSquare(data.getJsonObject(i), weight);
		return squares;
	}

	protected static double getDouble(JsonObject json, String name,
			double defaultVal) {
		JsonNumber val = json.getJsonNumber(name);
		if (val == null)
			return defaultVal;
		return val.doubleValue();
	}

	public static Ability get(String tag) {
		return abilities.get(tag);
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

	public Animation getFrontAnimation() throws IOException {
		return Animation.get(frontAnimationName);
	}

	public Animation getBackAnimation() throws IOException {
		return Animation.get(backAnimationName);
	}

	public int adjustDamage(int damage, int power) {
		return (int) ((Math.floor(damage * damageFromWeapon)
				+ damageBonus)
				* (1 + power * damageFromUnit / 50));
	}

	public int getAoeDelay() {
		return aoeDelay;
	}

	public boolean getRandomTarget() {
		return randomTarget;
	}

	public String getTargetType() {
		return targetType;
	}

	public TargetSquare[] getDamageArea() {
		return damageArea.clone();
	}

	public TargetSquare[] getTargetArea() {
		return targetArea.clone();
	}

	public static class TargetSquare {
		private double value;
		private int x, y, order;
		protected TargetSquare(JsonObject json, double weight) {
			if (weight == 0) {
				value = getDouble(json, "damagePercent", 0) / 100;
				order = json.getInt("order", 0);
			}
			else
				value = getDouble(json, "weight", 0) / weight;
			JsonObject pos = json.getJsonObject("pos");
			if (pos != null) {
				x = pos.getInt("x", 0);
				y = pos.getInt("y", 0);
			}
		}
		public double getValue() {
			return value;
		}
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		public int getOrder() {
			return order;
		}
	}

}
