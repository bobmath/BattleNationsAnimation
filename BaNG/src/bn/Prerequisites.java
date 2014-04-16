package bn;

import javax.json.JsonObject;
import javax.json.JsonValue;

public class Prerequisites {

	public static Prerequisites create(JsonObject json) {
		return (json == null || json.isEmpty()) ? null
				: new Prerequisites(json);
	}

	private int minLevel, minRank;

	protected Prerequisites(JsonObject json) {
		for (JsonValue val : json.values()) {
			JsonObject prereq = (JsonObject) val;
			switch (prereq.getString("_t", "")) {
			case "LevelPrereqConfig":
				minLevel = prereq.getInt("level", 0);
				break;
			case "UnitLevelPrereqConfig":
				minRank = prereq.getInt("level", 0);
				break;
			}
		}
	}

	public int getMinLevel() {
		return minLevel;
	}

	public int getMinRank() {
		return minRank;
	}

}
