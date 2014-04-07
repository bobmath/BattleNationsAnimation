package ui;

import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import bn.Building;
import bn.GameFiles;
import bn.Timeline;
import bn.Unit;

public class AnimationTree {
	public static void main(String[] args) throws IOException {
		GameFiles.load();

		JFrame frame = new JFrame("Tree Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();
		JTree tree = new JTree(buildTree());
		JScrollPane scroller = new JScrollPane(tree);
		scroller.setPreferredSize(new Dimension(300, 300));
		content.add(scroller);
		frame.pack();
		frame.setVisible(true);
	}

	public static DefaultMutableTreeNode buildTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Animations");
		root.add(buildingsTree());
		for (UnitGroup group : UnitGroup.getGroups())
			root.add(group.getTreeNode());
		root.add(allAnimTree());
		return root;
	}

	private static DefaultMutableTreeNode buildingsTree() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Buildings");
		for (Building build : Building.getAll()) {
			DefaultMutableTreeNode buildNode = new DefaultMutableTreeNode(build);
			node.add(buildNode);
		}
		return node;
	}

	private static DefaultMutableTreeNode allAnimTree() {
		Map<Character,DefaultMutableTreeNode> nodes = new HashMap<Character,DefaultMutableTreeNode>();
		for (String name : Timeline.getAllNames()) {
			Character firstChar = Character.valueOf(name.charAt(0));
			DefaultMutableTreeNode node = nodes.get(firstChar);
			if (node == null) {
				node = new DefaultMutableTreeNode(firstChar);
				nodes.put(firstChar, node);
			}
			node.add(new DefaultMutableTreeNode(name));
		}
		Character[] chars = new Character[nodes.size()];
		chars = nodes.keySet().toArray(chars);
		Arrays.sort(chars);
		DefaultMutableTreeNode all = new DefaultMutableTreeNode("All");
		for (Character ch : chars)
			all.add(nodes.get(ch));
		return all;
	}

	protected static class UnitGroup implements Comparable<UnitGroup> {
		private Map<String,List<Unit>> subgroups;
		private String side;
		protected static UnitGroup[] getGroups() {
			Map<String,UnitGroup> groups = new HashMap<String,UnitGroup>();
			for (Unit unit : Unit.getAll()) {
				String side = unit.getSide();
				UnitGroup group = groups.get(side);
				if (group == null) {
					group = new UnitGroup(side);
					groups.put(side, group);
				}
				group.add(unit);
			}
			UnitGroup[] array = new UnitGroup[groups.size()];
			array = groups.values().toArray(array);
			Arrays.sort(array);
			return array;
		}
		protected DefaultMutableTreeNode getTreeNode() {
			String[] names = new String[subgroups.size()];
			names = subgroups.keySet().toArray(names);
			Arrays.sort(names);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(side + " Units");
			for (String name : names) {
				List<Unit> unitList = subgroups.get(name);
				DefaultMutableTreeNode subnode = new DefaultMutableTreeNode(name);
				if (unitList.size() > 1) {
					for (Unit unit : unitList) {
						DefaultMutableTreeNode subsubnode = new DefaultMutableTreeNode(unit.getTag());
						subnode.add(subsubnode);
					}
				}
				node.add(subnode);
			}
			return node;
		}
		protected UnitGroup(String side) {
			this.side = side;
			subgroups = new HashMap<String,List<Unit>>();
		}
		protected void add(Unit unit) {
			String name = unit.getName();
			List<Unit> unitList = subgroups.get(name);
			if (unitList == null) {
				unitList = new ArrayList<Unit>();
				subgroups.put(name, unitList);
			}
			unitList.add(unit);
		}
		@Override
		public int compareTo(UnitGroup that) {
			return this.side.compareTo(that.side);
		}
	}
}
