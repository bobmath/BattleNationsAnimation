package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import bn.Animation;
import bn.Building;
import bn.GameFiles;
import bn.Unit;
import bn.Unit.Weapon;

public class ImageGetter {

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
		ImageGetter ui = new ImageGetter();
		ui.buildUI();
		ui.showUI();
	}

	public ImageGetter() {
	}

	private Object source;
	private JFrame frame;
	private AnimationBox animBox;
	private JTree tree;
	private JPanel rightPanel;
	private JPanel unitPanel;
	private JComboBox<String> frontCtrl;
	private JComboBox<Weapon> weaponCtrl;
	private JPanel buildingPanel;
	private JComboBox<String> busyCtrl;

	public void buildUI() {
		frame = new JFrame("ImageGetter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		tree = new JTree(AnimationTree.buildTree());
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				updateSource();
			}
		});
		JScrollPane scroller = new JScrollPane(tree);
		scroller.setPreferredSize(new Dimension(300, 400));
		scroller.setMinimumSize(new Dimension(100, 100));

		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(300, 400));
		animBox = new AnimationBox();
		animBox.setMinimumSize(new Dimension(100, 100));
		rightPanel.add(animBox);

		buildControls();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scroller, rightPanel);
		splitPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		frame.setContentPane(splitPane);

		frame.pack();
	}

	private void buildControls() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));

		ActionListener update = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateAnimation();
			}
		};

		controlPanel.add(buildUnitControls(update));
		controlPanel.add(buildBuildingControls(update));
		controlPanel.add(buildAnimationControls());

		rightPanel.add(controlPanel, BorderLayout.PAGE_END);
	}

	private JPanel buildAnimationControls() {
		JPanel animPanel = new JPanel();
		animPanel.setLayout(new BoxLayout(animPanel, BoxLayout.LINE_AXIS));
		final JComboBox<BackgroundChoice> backgroundCtrl =
				new JComboBox<BackgroundChoice>(
						BackgroundChoice.getBackgrounds());
		backgroundCtrl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BackgroundChoice bkg = (BackgroundChoice) backgroundCtrl.getSelectedItem();
				bkg.setBackground(animBox);
			}
		});
		backgroundCtrl.setMaximumSize(backgroundCtrl.getPreferredSize());
		animPanel.add(backgroundCtrl);

		SpinnerModel scaleRange = new SpinnerNumberModel(100, 5, 200, 5);
		final JSpinner scaleCtrl = new JSpinner(scaleRange);
		scaleCtrl.setMaximumSize(scaleCtrl.getPreferredSize());
		scaleCtrl.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Number value = (Number) scaleCtrl.getValue();
				animBox.setScale(value.doubleValue() / 100);
			}
		});
		animPanel.add(scaleCtrl);

		final JPopupMenu exportPopup = new JPopupMenu();

		JMenuItem exportGif = new JMenuItem("Export Animation");
		exportGif.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				animBox.saveCurrentAsGif();
			}
		});
		exportPopup.add(exportGif);

		JMenuItem exportPng = new JMenuItem("Export Image");
		exportPng.addActionListener(new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				animBox.saveCurrentAsPng();
			}
		});
		exportPopup.add(exportPng);

		final JButton exportBtn = new JButton("Export");
		exportBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportPopup.show(exportBtn, 0, 0);
			}
		});
		animPanel.add(Box.createHorizontalGlue());
		animPanel.add(exportBtn);
		return animPanel;
	}

	private JPanel buildBuildingControls(ActionListener update) {
		buildingPanel = new JPanel();
		buildingPanel.setLayout(new BoxLayout(buildingPanel, BoxLayout.LINE_AXIS));
		busyCtrl = new JComboBox<String>(new String[] { "Busy", "Idle" });
		busyCtrl.addActionListener(update);
		busyCtrl.setMaximumSize(busyCtrl.getPreferredSize());
		buildingPanel.add(busyCtrl);
		buildingPanel.add(Box.createHorizontalGlue());
		buildingPanel.setVisible(false);
		return buildingPanel;
	}

	private JPanel buildUnitControls(ActionListener update) {
		unitPanel = new JPanel();
		unitPanel.setLayout(new BoxLayout(unitPanel, BoxLayout.LINE_AXIS));
		frontCtrl = new JComboBox<String>(new String[] { "Front", "Back" });
		frontCtrl.addActionListener(update);
		frontCtrl.setMaximumSize(frontCtrl.getPreferredSize());
		unitPanel.add(frontCtrl);

		weaponCtrl = new JComboBox<Weapon>();
		weaponCtrl.addActionListener(update);
		unitPanel.add(weaponCtrl);

		unitPanel.add(Box.createHorizontalGlue());
		unitPanel.setVisible(false);
		return unitPanel;
	}

	private void updateSource() {
		AnimationTree.TreeNode node = (AnimationTree.TreeNode)
				tree.getLastSelectedPathComponent();
		Object src = (node == null) ? null : node.getValue();
		if (src != source) {
			source = src;
			updateControls();
			updateAnimation();
		}
	}

	private void updateControls() {
		if (source instanceof Unit) {
			Unit unit = (Unit) source;
			unitPanel.setVisible(true);
			buildingPanel.setVisible(false);

			weaponCtrl.setModel(new DefaultComboBoxModel<Weapon>(unit.getWeapons()));
			weaponCtrl.setMaximumSize(weaponCtrl.getPreferredSize());

			switch (unit.getSide()) {
			case "Player": case "Hero":
				frontCtrl.setSelectedItem("Back");
				break;
			case "Hostile": case "Villain":
				frontCtrl.setSelectedItem("Front");
				break;
			}
		}
		else if (source instanceof Building) {
			unitPanel.setVisible(false);
			buildingPanel.setVisible(true);
		}
		else {
			unitPanel.setVisible(false);
			buildingPanel.setVisible(false);
		}
		rightPanel.revalidate();
	}

	private void updateAnimation() {
		Animation anim = null;
		try {
			if (source instanceof Unit) {
				Unit unit = (Unit) source;
				boolean front = "Front".equals(frontCtrl.getSelectedItem());
				if (weaponCtrl.getSelectedIndex() <= 0) {
					anim = front ? unit.getFrontAnimation()
							: unit.getBackAnimation();
				}
				else {
					Weapon weap = (Weapon) weaponCtrl.getSelectedItem();
					anim = front ? weap.getFrontAnimation()
							: weap.getBackAnimation();
				}
			}
			else if (source instanceof Building) {
				Building bld = (Building) source;
				boolean busy = "Busy".equals(busyCtrl.getSelectedItem());
				if (busy)
					anim = bld.getBusyAnimation();
				if (anim == null)
					anim = bld.getIdleAnimation();
			}
			else if (source instanceof String) {
				anim = Animation.get((String) source);
			}
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Unable to load animation",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		animBox.setAnimation(anim);
	}

	public void showUI() {
		frame.setVisible(true);
	}

}
