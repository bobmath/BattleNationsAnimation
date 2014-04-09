package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import util.GifAnimation;
import bn.Animation;

public class AnimationBox extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final Color defaultColor =
			new Color(0xf2, 0xf2, 0xf2); // wiki light gray

	private Animation anim, hitAnim;
	private Timer timer;
	protected int tick;
	private Color backgroundColor;
	private double scale = 1;
	private BufferedImage backgroundImage;
	private int hitDelay;

	public AnimationBox() {
		timer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tick++;
				repaint();
			}
		});
		buildPopup();
	}

	private void buildPopup() {
		final JPopupMenu popup = new JPopupMenu();

		JMenuItem export = new JMenuItem("Export Raw Bitmap");
		export.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCurrentRawBitmap();
			}
		});
		popup.add(export);

		JMenuItem replace = new JMenuItem("Replace Raw Bitmap");
		replace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				replaceRawBitmap();
			}
		});
		popup.add(replace);

		JMenuItem restore = new JMenuItem("Restore Raw Bitmap");
		restore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (anim == null) return;
				anim.getBitmap().restoreTexture();
				repaint();
			}
		});
		popup.add(restore);

		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	public void paint(Graphics g) {
		if (anim == null) return;
		anim.setScale(scale);
		Graphics2D g2 = (Graphics2D) g;
		Dimension dim = getSize();
		Rectangle2D bounds = anim.getBounds();
		anim.setPosition((dim.width - bounds.getWidth())/2 - bounds.getX(),
				(dim.height - bounds.getHeight())/2 - bounds.getY());
		drawBackground(g2, true, dim.width, dim.height);
		setHints(g2);
		anim.drawFrame(tick % anim.getNumFrames(), g2);
	}

	private static void setHints(Graphics2D g) {
		// This makes drawing a background image outrageously slow the first time.
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}

	private void drawBackground(Graphics2D g, boolean transparent, int width, int height) {
		if (backgroundImage == null) {
			Color bg = backgroundColor;
			if (bg == null) {
				if (transparent) return;
				bg = defaultColor;
			}
			g.setColor(bg);
			g.fillRect(0, 0, width, height);
		}
		else {
			AffineTransform oldTrans = g.getTransform();
			g.translate(anim.getX(), anim.getY());
			g.scale(scale, scale);
			g.translate(-0.5*backgroundImage.getWidth(),
					-0.5*backgroundImage.getHeight());
			g.drawImage(backgroundImage, 0, 0, null);
			g.setTransform(oldTrans);
		}
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

	public void setHitAnimation(Animation hitAnim, int hitDelay) {
		if (hitAnim == this.hitAnim) return;
		this.hitAnim = hitAnim;
		this.hitDelay = hitDelay;
		repaint();
	}

	public File selectOutputFile(String suggest, String ext) {
		if (suggest == null) suggest = anim.getName();
		String uc = ext.toUpperCase();
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter(uc + " Images", ext));
		fileChooser.setSelectedFile(new File(suggest + "." + ext));
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
		File file = selectOutputFile(anim.getName(), "gif");
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
		File file = selectOutputFile(anim.getName(), "png");
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

	public void saveCurrentRawBitmap() {
		if (anim == null) return;
		File file = selectOutputFile(anim.getBitmap().getName(), "png");
		if (file == null) return;
		try {
			BufferedImage im = anim.getBitmap().getTexture().getImage();
			ImageIO.write(im, "png", file);
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
		drawBackground(g, false, width, height);
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
		drawBackground(g, true, width, height);
		setHints(g);
		anim.drawFrame(0, g);

		file.delete();
		ImageIO.write(frame, "png", file);
	}

	private void replaceRawBitmap() {
		if (anim == null) return;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
		fileChooser.setDialogTitle("Load PNG");
		int userOption = fileChooser.showOpenDialog(this);
		if (userOption != JFileChooser.APPROVE_OPTION) return;
		File file = fileChooser.getSelectedFile();
		if (file == null) return;

		try {
			BufferedImage im = ImageIO.read(file);
			anim.getBitmap().replaceTexture(im);
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Error reading file.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}

		repaint();
	}

	public void setBackgroundColor(Color color) {
		backgroundColor = color;
		backgroundImage = null;
		repaint();
	}

	public void setBackgroundImage(BufferedImage image) {
		backgroundColor = null;
		backgroundImage = image;
		repaint();
	}

	public void setScale(double scale) {
		if (scale <= 0 || scale > 2)
			throw new IllegalArgumentException("Invalid scale");
		this.scale  = scale;
		repaint();
	}

}