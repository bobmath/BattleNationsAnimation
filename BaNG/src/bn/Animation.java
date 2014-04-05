package bn;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public class Animation {

	private static Map<String,String> packageIndex;
	private static Map<String,Animation> animCache;
	
	private String packageName, name;
	private int xMin, xMax, yMin, yMax;
	private Frame[] frames;
	private double scale;
	
	public static Animation get(String name) throws IOException {
		if (packageIndex == null) buildIndex();
		String lc = name.toLowerCase();
		if (!animCache.containsKey(lc)) {
			String packname = packageIndex.get(lc);
			if (packname == null) return null;
			animCache.put(lc, null);
			readTimeline(packname);
		}
		return animCache.get(lc);
	}
	
	private Animation(String name) {
		packageName = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getName() {
		return name;
	}
	
	public Rectangle getBounds() {
		return new Rectangle(xMin, yMin, xMax-xMin+1, yMax-yMin+1);
	}
	
	public double getScale() {
		return scale;
	}

	public int getNumFrames() {
		return frames.length;
	}
	
	public Frame getFrame(int num) {
		return frames[num];
	}

	public void drawFrame(int num, Graphics2D g) {
		frames[num].draw(g);
	}

	private static void buildIndex() throws IOException {
		packageIndex = new HashMap<String,String>();
		animCache = new HashMap<String,Animation>();
		try {
			JsonObject packs = (JsonObject) GameFiles.readJson("AnimationPacks.json");
			for (JsonValue packname : packs.getJsonArray("animationPacks")) {
				String packstr = ((JsonString) packname).getString();
				JsonObject meta = (JsonObject) GameFiles.readJson(packstr + "_Metadata.json");
				for (JsonValue animname : meta.getJsonArray("animationNames")) {
					String animstr = ((JsonString) animname).getString();
					packageIndex.put(animstr.toLowerCase(), packstr);
				}
			}
		}
		catch (ClassCastException e) {
			throw new FileFormatException("Json type error", e);
		}
	}

	private static void readTimeline(String name) throws IOException {
		LittleEndianInputStream in = new LittleEndianInputStream(
				GameFiles.open(name + "_Timeline.bin"), 256);
		try {
			int ver = in.readShort();
			if (ver != 4 && ver != 6 && ver != 8)
				throw new FileFormatException("Unknown version");
			in.readByte();
			int num = in.readShort();
			in.readShort();
			for (int i = 0; i < num; i++)
				new Animation(name).read(in, ver);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			throw new FileFormatException("Invalid array index", e);
		}
		finally {
			in.close();
		}
	}
	
	private void read(LittleEndianInputStream in, int ver) throws IOException
	{
		name = in.readCString(256);
		animCache.put(name.toLowerCase(), this);
		in.readShort();
		readFrames(in, ver);
		if (ver > 4)
			readSequence(in);
		for (int i = 0; i < frames.length; i++) {
			scale = frames[i].getScale();
			if (scale != 0) break;
		}
	}
	
	private void readFrames(LittleEndianInputStream in, int ver) throws IOException
	{
		int numCoords = in.readShort() * 4;
		int extra = 0;
		if (ver == 8) {
			switch (in.readShort()) {
			case 0: break;
			case 1: extra = 4; break;
			case 0x101: extra = 16; break;
			default: throw new FileFormatException("Unknown point size");
			}
		}
		
		int[] coords = new int[numCoords];
		for (int i = 0; i < numCoords; i += 4) {
			coords[i] = in.readShort();
			coords[i+1] = in.readShort();
			if (ver == 4) in.readShort();
			coords[i+2] = in.readShort();
			coords[i+3] = in.readShort();
			for (int j = 0; j < extra; j++)
				in.readByte();
		}
		
		xMin = in.readShort();
		xMax = in.readShort();
		yMin = in.readShort();
		yMax = in.readShort();
		
		int numFrames = in.readShort();
		frames = new Frame[numFrames];
		for (int i = 0; i < numFrames; i++) {
			frames[i] = new Frame();
			frames[i].read(in, ver, coords);
		}
	}

	private void readSequence(LittleEndianInputStream in) throws IOException
	{
		int numSeq = in.readShort();
		if (numSeq <= 0)
			throw new FileFormatException("Invalid sequence count");
		in.readShort();
		Frame[] sequence = new Frame[numSeq];
		for (int i = 0; i < numSeq; i++)
			sequence[i] = frames[in.readShort()];
		frames = sequence;
	}

}
