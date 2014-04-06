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

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
	implements ListSelectionListener, ActionListener
	{
		private static final long serialVersionUID = 1L;

		private JList<Unit> list;
		private Animation anim;
		private int size;
		private Timer timer; 
		protected int tick;

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

		public void paint(Graphics g) {
			if (anim != null) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				Rectangle2D bounds = anim.getBounds();
				anim.setPosition((size - bounds.getWidth())/2 - bounds.getX(),
						(size - bounds.getHeight())/2 - bounds.getY());
				anim.drawFrame(tick % anim.getNumFrames(), g2);
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent ev) {
			anim = null;
			timer.stop();
			tick = 0;

			Unit unit = list.getSelectedValue();
			if (unit != null) {
				try {
					anim = unit.getBackAnimation();
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

		@Override
		public void actionPerformed(ActionEvent e) {
			tick++;
			repaint();
		}

		public void saveCurrentAsGif() {
			JFileChooser fileChooser = new JFileChooser();
			int userOption = fileChooser.showSaveDialog(this);
			if (userOption == JFileChooser.APPROVE_OPTION) {
				try {
					writeGif(fileChooser.getSelectedFile());
				}
				catch (IOException e) {
					JOptionPane.showMessageDialog(null,
							"Error writing file.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
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
			anim.drawFrame(0, g);

			for (int i = 1; i < frames; i++) {
				frame = out.getFrame(i);
				g = frame.createGraphics();
				anim.drawFrame(i, g);
			}

			out.write(file);
		}
	}

	public static void main(String[] args)
	{
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
		JMenuItem exportGifItem = new JMenuItem("Export to Gif");

		exportGifItem.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				img.saveCurrentAsGif();
			}
		});

		fileMenu.add(exportGifItem);
		menuBar.add(fileMenu);
		frame.setJMenuBar(menuBar);

		frame.pack();
		frame.setVisible(true);
	}

}
