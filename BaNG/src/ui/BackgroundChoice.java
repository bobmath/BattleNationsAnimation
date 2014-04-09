package ui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import util.ImageCache;
import bn.GameFiles;

public class BackgroundChoice {

	private String name, file;
	private Color color;

	public static BackgroundChoice[] getBackgrounds() {
		List<BackgroundChoice> items = new ArrayList<BackgroundChoice>();
		items.add(new BackgroundChoice("Default Background"));
		items.add(new BackgroundChoice("Black", 0, 0, 0));
		items.add(new BackgroundChoice("Critter Tan", 0xdd, 0xcc, 0xaa));
		items.add(new BackgroundChoice("Player/Civ Orange", 0xeb, 0x81, 0x00));
		items.add(new BackgroundChoice("Raider Brown", 0xbb, 0x99, 0x66));
		items.add(new BackgroundChoice("Rebel/Wolf Gray", 0xcc, 0xcc, 0xcc));
		items.add(new BackgroundChoice("Sky Blue", 0x87, 0xce, 0xfa));
		items.add(new BackgroundChoice("White", 0xff, 0xff, 0xff));
		for (String file : GameFiles.glob("BattleMap*.png")) {
			String name = file.replaceFirst("(?i:\\.png)$", "");
			name = name.replaceFirst("^(?i:battle)", "");
			items.add(new BackgroundChoice(name, file));
		}
		BackgroundChoice[] array = new BackgroundChoice[items.size()];
		return items.toArray(array);
	}

	public void setBackground(AnimationBox animBox) {
		if (file == null)
			animBox.setBackgroundColor(color);
		else {
			try {
				animBox.setBackgroundImage(ImageCache.read(
						GameFiles.getFile(file)));
			}
			catch (IOException e) {
				JOptionPane.showMessageDialog(null,
						"Unable to load background",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private BackgroundChoice(String name) {
		this.name = name;
	}

	private BackgroundChoice(String name, String file) {
		this.name = name;
		this.file = file;
	}

	private BackgroundChoice(String name, int r, int g, int b) {
		this.name = name;
		this.color = new Color(r, g, b);
	}

	public String getFile() {
		return file;
	}

	public Color getColor() {
		return color;
	}

	public String toString() {
		return name;
	}

}