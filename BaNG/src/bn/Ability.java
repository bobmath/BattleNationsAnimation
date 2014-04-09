package bn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class Ability {

	public static final Ability NO_ABILITY = new Ability();

	private static Map<String,Ability> abilities;

	private String tag, name;
	private String frontAnimationName, backAnimationName;

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
		String animType = json.getString("damageAnimationType", null);
		if (animType != null) {
			JsonObject dmg = dmgAnim.getJsonObject(animType);
			if (dmg != null) {
				frontAnimationName = dmg.getString("front", null);
				backAnimationName = dmg.getString("back", null);
			}
		}
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

}
