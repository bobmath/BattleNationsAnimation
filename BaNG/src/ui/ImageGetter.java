package ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

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

import bn.Animation;
import bn.GameFiles;
import bn.GifFrame;
import bn.ImageUtil;
import bn.Unit;

public class ImageGetter {
	
	private static JFileChooser fileChooser = new JFileChooser();
	private static ImageBox img;
	
	public static class ImageBox extends JComponent implements ListSelectionListener {
		private static final long serialVersionUID = 1L;

		private JList<Unit> list;
		private static Animation anim;
		private int size;
		public int tick = 0;
		public Timer timer; 

		public ImageBox(int size, JList<Unit> list) {
			this.size = size;
			this.list = list;
			list.addListSelectionListener(this);
		}
		
		public Dimension getPreferredSize() {
			return new Dimension(size, size);
		}
		
		public void paint(Graphics g) {
			if (anim != null) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.translate(0.5*size, 0.5*size);
				Animation.Frame frame = anim.getFrame(0);
				frame.scale(size, g2);
				frame.center(g2);
				anim.drawFrame(tick, g2);

				if (timer != null) {
					timer.stop();
				}
				//Change the number in the line below to modify anim speed
				timer = new Timer(50, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {

						tick += 1;

						repaint(0, 0, 0, size, size);
					}
				});
				timer.start();
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent ev) {
			if (anim != null) {
				anim.freeBitmap();
				if (timer != null) {
					timer.stop();
					tick = 0;
				}
				anim = null;
			}
			
			Unit unit = list.getSelectedValue();
			if (unit != null) {
				try {
					anim = unit.getFrontAnimation();
					if (anim != null)
						anim.loadBitmap();
				}
				catch (IOException ex) { }
			}
			
			repaint(0, 0, 0, size, size);
		}
		
		public static void saveCurrentAsGif () {
			
			ArrayList<GifFrame> gifFrames = anim.getGifFrames() ;
			
			int userOption = fileChooser.showSaveDialog(img);
			if (userOption == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				try {
					
					OutputStream out = new BufferedOutputStream (new FileOutputStream(file));
					ImageUtil.saveAnimatedGIF(out, gifFrames, 0);
					out.flush();
					out.close();
					
				} catch (FileNotFoundException e) {
					Logger.getLogger("UI").severe("File not found..." + file.getName());
					System.exit(1);
				} catch ( IOException e) {
					Logger.getLogger("UI").severe("Error while writing to file..." + file.getName());
					System.exit(1);
				} catch (Exception e) {
					Logger.getLogger("UI").severe("An unexpected error occured...");
					e.printStackTrace();
				}
			}
			
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
        listScroller.setPreferredSize(new Dimension(400, 300));
		
		img = new ImageBox(200, list);
		
        JFrame frame = new JFrame("ImageGetter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = frame.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.LINE_AXIS));
        content.add(listScroller);
        content.add(img);
        
        // Adding Menu, and Export to Gif
        JMenuBar menuBar = new JMenuBar (); 
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportGifItem = new JMenuItem("Export to Gif");
        
        
        exportGifItem.addActionListener(new ActionListener () {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		ImageBox.saveCurrentAsGif();
        	}
        });

        fileMenu.add(exportGifItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
        
        frame.pack();
        frame.setVisible(true);
	}

}
