package bn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import util.FileFormatException;

public class Ability {

	public static final Ability NO_ABILITY = new Ability();

	private static Map<String,Ability> abilities;

	private String tag, name;
	private String frontAnimationName, backAnimationName;
	private double damageFromWeapon, damageFromUnit;
	private int damageBonus;
	private int minRange, maxRange;
	private int aoeDelay;
	private String targetType;
	private boolean randomTarget;
	private TargetSquare[] targetArea, damageArea;
	private Map<String,Prerequisites> prereqs;

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
		initPrereqs(json.getJsonObject("reqs"));
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
		minRange = stats.getInt("minRange", 1);
		maxRange = stats.getInt("maxRange", 1);

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

	private void initPrereqs(JsonObject json) {
		if (json == null || json.isEmpty()) return;
		prereqs = new HashMap<String,Prerequisites>();
		for (Map.Entry<String,JsonValue> item : json.entrySet()) {
			JsonObject reqs = (JsonObject) item.getValue();
			Prerequisites pre = Prerequisites.create(
					reqs.getJsonObject("prereq"));
			if (pre != null)
				prereqs.put(item.getKey(), pre);
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

	public Prerequisites getPrereqs(String unit) {
		return (prereqs == null) ? null : prereqs.get(unit);
	}

	public int adjustDamage(int damage, int power) {
		return (int) ((Math.floor(damage * damageFromWeapon)
				+ damageBonus)
				* (1 + power * damageFromUnit / 50));
	}

	public int getMinRange() {
		return minRange;
	}

	public int getMaxRange() {
		return maxRange;
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
		return (damageArea == null) ? null : damageArea.clone();
	}

	public TargetSquare[] getTargetArea() {
		return (targetArea == null) ? null : targetArea.clone();
	}

	public static class TargetSquare implements Comparable<TargetSquare> {
		public static final TargetSquare SINGLE_TARGET = new TargetSquare();
		private double value;
		private int x, y, order;
		private TargetSquare() {
			value = 1;
		}
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
		private TargetSquare(TargetSquare a, TargetSquare b) {
			x = a.x + b.x;
			y = a.y + b.y;
			order = a.order + b.order;
			value = a.value * b.value;
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
		@Override
		public int compareTo(TargetSquare that) {
			// sort ascending y, descending x, ascending order
			if (this.y != that.y) return that.y - this.y;
			if (this.x != that.x) return this.x - that.x;
			return that.order - this.order;
		}

		public static TargetSquare[] convolution(TargetSquare[] in1,
				TargetSquare[] in2) {
			TargetSquare[] out = new TargetSquare[in1.length * in2.length];
			int i = 0;
			for (int j = 0; j < in1.length; j++)
				for (int k = 0; k < in2.length; k++)
					out[i++] = new TargetSquare(in1[j], in2[k]);

			Arrays.sort(out);
			i = 1;
			TargetSquare a = out[0];
			for (int j = 1; j < out.length; j++) {
				TargetSquare b = out[j];
				if (a.x == b.x && a.y == b.y)
					a.value += b.value;
				else
					out[i++] = a = b;
			}

			if (i == out.length)
				return out;
			TargetSquare[] out2 = new TargetSquare[i];
			for (int j = 0; j < i; j++)
				out2[j] = out[j];
			return out2;
		}
	}

}
