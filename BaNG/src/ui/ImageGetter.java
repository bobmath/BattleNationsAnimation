package ui;

import java.awt.Color;
import java.awt.Container;
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
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.GifAnimation;
import bn.Animation;
import bn.GameFiles;
import bn.Unit;

public class ImageGetter {

	public static class ImageBox extends JComponent
	implements ListSelectionListener
	{
		private static final long serialVersionUID = 1L;

		private JList<Unit> list;
		private Animation anim;
		private int size;
		private Timer timer; 
		protected int tick;
		private boolean front;

		public ImageBox(int size, JList<Unit> list) {
			this.size = size;
			this.list = list;
			list.addListSelectionListener(this);
			timer = new Timer(50, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tick++;
					repaint();
				}
			});
		}

		public Dimension getPreferredSize() {
			return new Dimension(size, size);
		}

		public void setFront(boolean front) {
			this.front = front;
			valueChanged(null);
		}

		public void paint(Graphics g) {
			if (anim != null) {
				Graphics2D g2 = (Graphics2D) g;
				setHints(g2);
				Rectangle2D bounds = anim.getBounds();
				anim.setPosition((size - bounds.getWidth())/2 - bounds.getX(),
						(size - bounds.getHeight())/2 - bounds.getY());
				anim.drawFrame(tick % anim.getNumFrames(), g2);
			}
		}

		private static void setHints(Graphics2D g2) {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}

		@Override
		public void valueChanged(ListSelectionEvent ev) {
			anim = null;
			timer.stop();
			tick = 0;

			Unit unit = list.getSelectedValue();
			if (unit != null) {
				try {
					anim = front ? unit.getFrontAnimation()
							: unit.getBackAnimation();
					timer.start();
				}
				catch (IOException ex) {
					JOptionPane.showMessageDialog(null,
							"Unable to load animation",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}

			repaint();
		}

		public void saveCurrentAsGif() {
			if (anim == null) return;
			if (anim == null) return;
			File file = selectOutputFile("Save as GIF");
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

		public void writeGif(File file) throws IOException {
			Rectangle2D bounds = anim.getBounds();
			int width = (int) Math.ceil(bounds.getWidth());
			int height = (int) Math.ceil(bounds.getHeight());
			anim.setPosition((width - bounds.getWidth())/2 - bounds.getX(),
					(height - bounds.getHeight())/2 - bounds.getY());
			int frames = anim.getNumFrames();
			GifAnimation out = new GifAnimation(width, height, frames, 5);

			BufferedImage frame = out.getFrame(0);
			Graphics2D g = frame.createGraphics();
			g.setColor(new Color(242, 242, 242)); // wiki light gray
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

		public File selectOutputFile(String title) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(title);
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

		public void saveCurrentAsPng() {
			if (anim == null) return;
			File file = selectOutputFile("Save as PNG");
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

		private void writePng(File file) throws IOException {
			Rectangle2D bounds = anim.getBounds(0);
			int width = (int) Math.ceil(bounds.getWidth());
			int height = (int) Math.ceil(bounds.getHeight());
			anim.setPosition((width - bounds.getWidth())/2 - bounds.getX(),
					(height - bounds.getHeight())/2 - bounds.getY());

			BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = frame.createGraphics();
			setHints(g);
			anim.drawFrame(0, g);

			file.delete();
			ImageIO.write(frame, "png", file);
		}

	}

	public static void main(String[] args) {
		if (!GameFiles.init()) {
			JOptionPane.showMessageDialog(null,
					"Unable to find Battle Nations directory.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			GameFiles.load();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Unable to read Battle Nations game files.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JList<Unit> list = new JList<Unit>(Unit.getPlayer());
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(300, 300));

		final ImageBox img = new ImageBox(300, list);

		JFrame frame = new JFrame("ImageGetter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.LINE_AXIS));
		content.add(listScroller);
		content.add(img);

		JMenuBar menuBar = new JMenuBar(); 
		JMenu fileMenu = new JMenu("File");
		JMenuItem exportGifItem = new JMenuItem("Export to GIF");
		JMenuItem exportPngItem = new JMenuItem("Export to PNG");

		exportGifItem.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				img.saveCurrentAsGif();
			}
		});

		exportPngItem.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				img.saveCurrentAsPng();
			}
		});

		fileMenu.add(exportGifItem);
		fileMenu.add(exportPngItem);
		menuBar.add(fileMenu);

		JMenu viewMenu = new JMenu("View");

		JRadioButtonMenuItem viewFront = new JRadioButtonMenuItem("Front", false);
		JRadioButtonMenuItem viewBack = new JRadioButtonMenuItem("Back", true);
		viewFront.setActionCommand("true");
		viewBack.setActionCommand("back");
		ButtonGroup group = new ButtonGroup();
		group.add(viewFront);
		group.add(viewBack);

		ActionListener switchView = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				img.setFront(Boolean.parseBoolean(e.getActionCommand()));
			}
		};
		viewFront.addActionListener(switchView);
		viewBack.addActionListener(switchView);

		viewMenu.add(viewFront);
		viewMenu.add(viewBack);

		menuBar.add(viewMenu);
		frame.setJMenuBar(menuBar);

		frame.pack();
		frame.setVisible(true);
	}

}
