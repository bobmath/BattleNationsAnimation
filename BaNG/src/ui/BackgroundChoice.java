package ui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

import util.ImageCache;
import bn.GameFiles;

public class BackgroundChoice {

	public static BackgroundChoice[] getBackgrounds() {
		List<BackgroundChoice> items = new ArrayList<BackgroundChoice>();
		items.add(new BackgroundChoice("Default Background", TYPE_COLOR));
		items.add(new BackgroundChoice("Black", 0, 0, 0));
		items.add(new BackgroundChoice("Critter Tan", 0xdd, 0xcc, 0xaa));
		items.add(new BackgroundChoice("Player/Civ Orange", 0xeb, 0x81, 0x00));
		items.add(new BackgroundChoice("Raider Brown", 0xbb, 0x99, 0x66));
		items.add(new BackgroundChoice("Rebel/Wolf Gray", 0xcc, 0xcc, 0xcc));
		items.add(new BackgroundChoice("Sky Blue", 0x87, 0xce, 0xfa));
		items.add(new BackgroundChoice("White", 0xff, 0xff, 0xff));
		items.add(new BackgroundChoice("Other Color...", TYPE_CHOOSE));
		for (File file : GameFiles.glob("BattleMap*.png")) {
			String name = file.getName();
			name = name.replaceFirst("(?i:\\.png)$", "");
			name = name.replaceFirst("^(?i:battle)", "");
			items.add(new BackgroundChoice(name, file));
		}
		BackgroundChoice[] array = new BackgroundChoice[items.size()];
		return items.toArray(array);
	}

	private String name;
	private File file;
	private Color color;
	private int type;

	private static final int TYPE_COLOR = 1;
	private static final int TYPE_CHOOSE = 2;
	private static final int TYPE_IMAGE = 3;

	public void setBackground(AnimationBox animBox) {
		switch (type) {
		case TYPE_COLOR:
			animBox.setBackgroundColor(color);
			break;
		case TYPE_CHOOSE:
			Color c = JColorChooser.showDialog(animBox,
					"Choose Background Color", color);
			if (c != null) {
				color = c;
				animBox.setBackgroundColor(color);
			}
			break;
		case TYPE_IMAGE:
			try {
				animBox.setBackgroundImage(ImageCache.read(file));
			}
			catch (IOException e) {
				JOptionPane.showMessageDialog(null,
						"Unable to load background",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
			break;
		}
	}

	private BackgroundChoice(String name, int type) {
		this.name = name;
		this.type = type;
		color = Color.WHITE;
	}

	private BackgroundChoice(String name, File file) {
		this.name = name;
		this.file = file;
		type = TYPE_IMAGE;
	}

	private BackgroundChoice(String name, int r, int g, int b) {
		this.name = name;
		this.color = new Color(r, g, b);
		type = TYPE_COLOR;
	}

	public File getFile() {
		return file;
	}

	public Color getColor() {
		return color;
	}

	public String toString() {
		return name;
	}

}