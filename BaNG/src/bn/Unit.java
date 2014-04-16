package bn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import util.FileFormatException;

public class Unit implements Comparable<Unit> {

	private static Map<String,Unit> units;

	private String tag, name, shortName, side;
	private String backAnimName, frontAnimName;
	private Rank[] ranks;
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
		name = Text.get(json.getString("name", null));
		if (name == null) name = tag;
		if (name.startsWith("Speciment ")) // fix game file typo
			name = "Specimen" + name.substring(9);
		shortName = Text.get(json.getString("shortName", null));
		if (shortName == null) shortName = name;
		side = json.getString("side", "Other");
		backAnimName = json.getString("backIdleAnimation", null);
		frontAnimName = json.getString("frontIdleAnimation", null);
		initWeapons(json.getJsonObject("weapons"));
		initRanks(json.getJsonArray("stats"));
	}

	private void initRanks(JsonArray json) {
		ranks = new Rank[json.size()];
		for (int i = 0; i < ranks.length; i++)
			ranks[i] = new Rank(json.getJsonObject(i));
	}

	private void initWeapons(JsonObject json) {
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

	public int getMaxRank() {
		return ranks.length;
	}

	public Rank getRank(int rank) {
		return ranks[rank-1];
	}

	public int getPower(int rank) {
		return ranks[rank-1].getPower();
	}

	public class Rank {
		private int power;
		private Prerequisites prereq;
		protected Rank(JsonObject json) {
			power = json.getInt("power", 0);
			prereq = Prerequisites.create(
					json.getJsonObject("prereqsForLevel"));
		}
		public int getPower() {
			return power;
		}
		public int getMinLevel() {
			return prereq == null ? 0 : prereq.getMinLevel();
		}
	}

	public class Weapon {
		private String name, tag;
		private String frontAnimationName, backAnimationName;
		private Attack[] attacks;
		private int hitDelay;
		private int minDamage, maxDamage;
		protected Weapon() {
			name = "(None)";
			tag = "none";
			attacks = new Attack[0];
		}
		protected Weapon(String tag, JsonObject json) {
			this.tag = tag;
			name = Text.get(json.getString("name", null));
			if (name == null) name = tag;
			frontAnimationName = json.getString("frontattackAnimation", null);
			backAnimationName = json.getString("backattackAnimation", null);
			hitDelay = json.getInt("damageAnimationDelay", 0)
					+ json.getInt("firesoundFrame", 0);
			initStats(json.getJsonObject("stats"));
			JsonArray abilities = json.getJsonArray("abilities");
			attacks = new Attack[abilities.size()];
			for (int i = 0; i < attacks.length; i++)
				attacks[i] = new Attack(abilities.getString(i, null), this);
		}
		private void initStats(JsonObject json) {
			if (json == null) return;
			minDamage = json.getInt("base_damage_min", 0);
			maxDamage = json.getInt("base_damage_max", 0);
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
		public Attack[] getAttacks() {
			Attack[] array = new Attack[attacks.length+1];
			array[0] = new Attack(null, this);
			for (int i = 0; i < attacks.length; i++)
				array[i+1] = attacks[i];
			return array;
		}
		public int getHitDelay() {
			return hitDelay;
		}
		public int getMinDamage() {
			return minDamage;
		}
		public int getMaxDamage() {
			return maxDamage;
		}
		public String toString() {
			return name;
		}
	}

	public class Attack {
		private Ability ability;
		private Weapon weapon;
		private Prerequisites prereq;
		protected Attack(String tag, Weapon weapon) {
			ability = Ability.get(tag);
			if (ability == null)
				ability = Ability.NO_ABILITY;
			this.weapon = weapon;
			prereq = ability.getPrereqs(Unit.this.getTag());
		}
		public String getName() {
			return ability.getName();
		}
		public String getTag() {
			return ability.getTag();
		}
		public String toString() {
			return ability.toString();
		}
		public Animation getFrontAnimation() throws IOException {
			return ability.getFrontAnimation();
		}
		public Animation getBackAnimation() throws IOException {
			return ability.getBackAnimation();
		}
		public int getMinDamage(int rank) {
			return ability.adjustDamage(weapon.getMinDamage(),
					getPower(rank));
		}
		public int getMaxDamage(int rank) {
			return ability.adjustDamage(weapon.getMaxDamage(),
					getPower(rank));
		}
		public double getAverageDamage(int rank) {
			return 0.5*(getMinDamage(rank) + getMaxDamage(rank));
		}
		public int getMinRank() {
			return prereq == null ? 1
					: Math.max(prereq.getMinRank(), 1);
		}
		public Ability getAbility() {
			return ability;
		}
		public Weapon getWeapon() {
			return weapon;
		}
	}

}
