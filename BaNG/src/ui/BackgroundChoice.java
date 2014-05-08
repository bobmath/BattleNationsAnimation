package ui;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import util.ImageCache;
import bn.GameFiles;

public class BackgroundChoice {

	public static BackgroundChoice[] getBackgrounds() {
		List<BackgroundChoice> items = new ArrayList<BackgroundChoice>();
		items.add(new ColorBackground("Default Background"));
		for (File file : GameFiles.glob("BattleMap*.png")) {
			String name = file.getName();
			name = name.replaceFirst("(?i:\\.png)$", "");
			name = name.replaceFirst("^(?i:battle)", "");
			items.add(new ImageBackground(name, file));
		}
		items.add(new ChooseImage("Other Image..."));
		items.add(new ColorBackground("Black", 0, 0, 0));
		items.add(new ColorBackground("Critter Tan", 0xdd, 0xcc, 0xaa));
		items.add(new ColorBackground("Neutral Beige", 0xe0, 0xdc, 0xd0));
		items.add(new ColorBackground("Player/Civ Orange", 0xeb, 0x81, 0x00));
		items.add(new ColorBackground("Raider Brown", 0xbb, 0x99, 0x66));
		items.add(new ColorBackground("Rebel/Wolf Gray", 0xcc, 0xcc, 0xcc));
		items.add(new ColorBackground("Sky Blue", 0x87, 0xce, 0xfa));
		items.add(new ColorBackground("White", 0xff, 0xff, 0xff));
		items.add(new ChooseColor("Other Color..."));
		BackgroundChoice[] array = new BackgroundChoice[items.size()];
		return items.toArray(array);
	}

	private String name;

	protected BackgroundChoice(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public void setBackground(AnimationBox animBox) {
	}

	protected static class ColorBackground extends BackgroundChoice {
		private Color color;
		protected ColorBackground(String name) {
			super(name);
		}
		protected ColorBackground(String name, int r, int g, int b) {
			super(name);
			this.color = new Color(r, g, b);
		}
		public void setBackground(AnimationBox animBox) {
			animBox.setBackgroundColor(color);
		}
	}

	protected static class ChooseColor extends BackgroundChoice {
		private Color color;
		protected ChooseColor(String name) {
			super(name);
			this.color = Color.WHITE;
		}
		public void setBackground(AnimationBox animBox) {
			Color c = JColorChooser.showDialog(animBox,
					"Choose Background Color", color);
			if (c == null) return;
			color = c;
			animBox.setBackgroundColor(color);
		}
	}

	protected static class ImageBackground extends BackgroundChoice {
		private File file;
		protected ImageBackground(String name, File file) {
			super(name);
			this.file = file;
		}
		public void setBackground(AnimationBox animBox) {
			try {
				animBox.setBackgroundImage(ImageCache.read(file));
			}
			catch (IOException e) {
				showError(animBox);
			}
		}
	}

	protected static class ChooseImage extends BackgroundChoice {
		protected ChooseImage(String name) {
			super(name);
		}
		public void setBackground(AnimationBox animBox) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "png", "gif", "jpg", "jpeg"));
			fileChooser.setDialogTitle("Load Background");
			int userOption = fileChooser.showOpenDialog(animBox);
			if (userOption != JFileChooser.APPROVE_OPTION) return;
			File img = fileChooser.getSelectedFile();
			if (img == null) return;
			try {
				animBox.setBackgroundImage(ImageIO.read(img));
			}
			catch (IOException e) {
				showError(animBox);
			}
		}
	}

	protected static void showError(Component comp) {
		JOptionPane.showMessageDialog(comp,
				"Unable to load background",
				"Error", JOptionPane.ERROR_MESSAGE);
	}

}
