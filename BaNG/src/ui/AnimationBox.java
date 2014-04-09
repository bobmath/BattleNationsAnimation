package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import util.GifAnimation;
import bn.Animation;

public class AnimationBox extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final Color defaultColor = new Color(0xf2, 0xf2, 0xf2); // wiki light gray

	private Animation anim;
	private Timer timer;
	protected int tick;
	private Color backgroundColor;
	private double scale = 1;

	public AnimationBox() {
		timer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tick++;
				repaint();
			}
		});
	}

	public void paint(Graphics g) {
		if (anim == null) return;
		anim.setScale(scale);
		Graphics2D g2 = (Graphics2D) g;
		setHints(g2);
		Dimension dim = getSize();
		Rectangle2D bounds = anim.getBounds();
		anim.setPosition((dim.width - bounds.getWidth())/2 - bounds.getX(),
				(dim.height - bounds.getHeight())/2 - bounds.getY());
		anim.drawFrame(tick % anim.getNumFrames(), g2);
	}

	private static void setHints(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}

	public void setAnimation(Animation anim) {
		if (anim == this.anim) return;
		this.anim = anim;
		timer.stop();
		tick = 0;
		if (anim != null)
			timer.start();
		repaint();
	}

	public File selectOutputFile(String ext) {
		String uc = ext.toUpperCase();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter(uc + " Images", ext));
		fileChooser.setSelectedFile(new File(anim.getName() + "." + ext));
		fileChooser.setDialogTitle("Save as " + uc);
		int userOption = fileChooser.showSaveDialog(this);
		if (userOption != JFileChooser.APPROVE_OPTION)
			return null;
		File file = fileChooser.getSelectedFile();
		if (file.exists()) {
			userOption = JOptionPane.showConfirmDialog(this,
					file + "\nalready exists. Overwrite?",
					"File Exists",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (userOption != JOptionPane.YES_OPTION)
				return null;
		}
		return file;
	}

	public void saveCurrentAsGif() {
		if (anim == null) return;
		if (anim == null) return;
		File file = selectOutputFile("gif");
		if (file == null) return;
		try {
			writeGif(file);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Error writing file.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void saveCurrentAsPng() {
		if (anim == null) return;
		File file = selectOutputFile("png");
		if (file == null) return;
		try {
			writePng(file);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Error writing file.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void writeGif(File file) throws IOException {
		Rectangle2D bounds = anim.getBounds();
		int width = (int) Math.ceil(bounds.getWidth()) + 2;
		int height = (int) Math.ceil(bounds.getHeight()) + 2;
		anim.setPosition((width - bounds.getWidth())/2 - bounds.getX(),
				(height - bounds.getHeight())/2 - bounds.getY());
		int frames = anim.getNumFrames();
		GifAnimation out = new GifAnimation(width, height, frames, 5);

		BufferedImage frame = out.getFrame(0);
		Graphics2D g = frame.createGraphics();
		Color bg = backgroundColor;
		if (bg == null) bg = defaultColor;
		g.setColor(bg);
		g.fillRect(0, 0, width, height);
		out.copyBackground();
		setHints(g);
		anim.drawFrame(0, g);

		for (int i = 1; i < frames; i++) {
			frame = out.getFrame(i);
			g = frame.createGraphics();
			setHints(g);
			anim.drawFrame(i, g);
		}

		file.delete();
		out.write(file);
	}

	private void writePng(File file) throws IOException {
		Rectangle2D bounds = anim.getBounds(0);
		int width = (int) Math.ceil(bounds.getWidth()) + 2;
		int height = (int) Math.ceil(bounds.getHeight()) + 2;
		anim.setPosition((width - bounds.getWidth())/2 - bounds.getX(),
				(height - bounds.getHeight())/2 - bounds.getY());

		BufferedImage frame = new BufferedImage(width, height,
				backgroundColor == null ? BufferedImage.TYPE_INT_ARGB
						: BufferedImage.TYPE_INT_RGB);
		Graphics2D g = frame.createGraphics();
		if (backgroundColor != null) {
			g.setColor(backgroundColor);
			g.fillRect(0, 0, width, height);
		}
		setHints(g);
		anim.drawFrame(0, g);

		file.delete();
		ImageIO.write(frame, "png", file);
	}

	public void setBackgroundColor(Color color) {
		backgroundColor = color;
		if (color == null)
			setOpaque(false);
		else {
			setOpaque(true);
			setBackground(color);
		}
		repaint();
	}

	public void setScale(double scale) {
		if (scale <= 0 || scale > 1) return;
		this.scale  = scale;
		repaint();
	}

}