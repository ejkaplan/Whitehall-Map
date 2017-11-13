import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.Table;
import processing.data.TableRow;

public class WhitehallMap extends PApplet {

	private List<Integer> keysPressed;
	public List<Node> nodes;
	private Node selected;
	private int turn;
	private List<List<Node>> visited;
	private List<List<Node>> unvisited;
	private List<Integer> moveType;
	private int viewMode;
	private PImage board;
	private boolean showBoard;
	private List<Node> poss;
	private boolean dirty;
	private boolean connectionMode = false;
	private boolean allowDrag = false;
	private boolean hideEdges;

	public static void main(String[] args) {
		PApplet.main("WhitehallMap");
	}

	public void settings() {
		size(800, 900);
	}

	public void setup() {
		hideEdges = true;
		dirty = true;
		board = loadImage("data/board.jpg");
		board.resize(width, width);
		showBoard = true;
		visited = new ArrayList<List<Node>>();
		unvisited = new ArrayList<List<Node>>();
		moveType = new ArrayList<Integer>();
		for (int i = 0; i < 16; i++) {
			visited.add(new ArrayList<Node>());
			unvisited.add(new ArrayList<Node>());
			moveType.add(0);
		}
		keysPressed = new ArrayList<Integer>();
		nodes = new ArrayList<Node>();
		turn = 0;
		viewMode = 0;
		loadBoard("nodes.csv");
	}

	public void draw() {
		if (dirty) {
			poss = possLocations();
			dirty = false;
		}
		background(200);
		if (showBoard) {
			image(board, 0, 0);
		}
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			n.drawSelf(visited.get(turn).contains(n), unvisited.get(turn).contains(n), poss.contains(n), i + 1,
					selected == n, true);
		}
		if (!hideEdges) {
			if (selected != null) {
				if (allowDrag)
					selected.pos.set(mouseX, mouseY);
				if (connectionMode)
					selected.drawConnections(viewMode, true);
			}
			if (!connectionMode) {
				for (Node n : nodes) {
					n.drawConnections(viewMode, false);
				}
			}
		}
		drawTimeline(17 * height / 18, 9 * width / 10, turn);
	}

	public void drawTimeline(float y, float w, int turn) {
		float x = (width - w) / 2;
		line(x, y, x + w, y);
		for (int i = 0; i <= 15; i++) {
			line(x + i * w / 15, y - 10, x + i * w / 15, y + 10);
		}
		switch (moveType.get(turn)) {
		case 0:
			fill(255);
			break;
		case 1:
			fill(255, 216, 150);
			break;
		case 2:
			fill(153, 192, 255);
			break;
		}
		ellipse(x + turn * w / 15, y, 20, 20);
		textAlign(CENTER, CENTER);
		fill(0);
		text(turn, x + turn * w / 15, y);
	}

	public List<Node> possLocations() {
		List<Node> poss = new ArrayList<Node>();
		if (visited.get(0).size() != 1)
			return poss;
		Node start = visited.get(0).get(0);
		List<List<Node>> paths = allPaths(start, turn);
		for (List<Node> path : paths) {
			Node end = path.get(path.size() - 1);
			if (!poss.contains(end))
				poss.add(end);
		}
		return poss;
	}

	public List<List<Node>> allPaths(Node start, int turns) {
		List<List<Node>> expanded = new ArrayList<List<Node>>();
		List<List<Node>> toExpand = new ArrayList<List<Node>>();
		List<Node> first = new ArrayList<Node>();
		first.add(start);
		toExpand.add(first);
		for (int i = 1; i <= turns; i++) {
			// All paths
			for (List<Node> path : toExpand) {
				List<Node> next;
				if (moveType.get(i) == 1)
					next = path.get(path.size() - 1).alleyNeighbors;
				else if (moveType.get(i) == 2)
					next = path.get(path.size() - 1).boatNeighbors;
				else
					next = path.get(path.size() - 1).neighbors;
				for (Node n : next) {
					List<Node> newPath = new ArrayList<Node>(path);
					newPath.add(n);
					expanded.add(newPath);
				}
			}
			// Filter the impossible ones
			List<Node> turnClues = visited.get(i);
			List<Node> turnUnclues = unvisited.get(i);
			a: for (int j = 0; j < expanded.size(); j++) {
				List<Node> path = expanded.get(j);
				for (Node n : turnClues) {
					if (!path.contains(n)) {
						expanded.remove(j);
						j--;
						continue a;
					}
				}
				for (Node n : turnUnclues) {
					if (path.contains(n)) {
						expanded.remove(j);
						j--;
						continue a;
					}
				}
			}
			toExpand = expanded;
			expanded = new ArrayList<List<Node>>();
		}
		return toExpand;
	}

	public void saveBoard() {
		Table t = new Table();
		t.addColumn("x");
		t.addColumn("y");
		t.addColumn("neighbors");
		t.addColumn("alley");
		t.addColumn("boat");
		for (Node n : nodes) {
			TableRow r = t.addRow();
			r.setFloat("x", n.pos.x);
			r.setFloat("y", n.pos.y);
			String neighbors = "";
			for (Node x : n.neighbors) {
				neighbors += nodes.indexOf(x) + 1 + ",";
			}
			r.setString("neighbors", neighbors);
			neighbors = "";
			for (Node x : n.alleyNeighbors) {
				neighbors += nodes.indexOf(x) + 1 + ",";
			}
			r.setString("alley", neighbors);
			neighbors = "";
			for (Node x : n.boatNeighbors) {
				neighbors += nodes.indexOf(x) + 1 + ",";
			}
			r.setString("boat", neighbors);
		}
		saveTable(t, "data/nodes.csv");
		System.out.println("SAVED");
	}

	public void loadBoard(String filename) {
		Table t = loadTable(filename, "header");
		nodes.clear();
		for (TableRow r : t.rows()) {
			float x = r.getFloat("x");
			float y = r.getFloat("y");
			Node n = new Node(this, x, y);
			nodes.add(n);
		}
		for (int i = 0; i < t.getRowCount(); i++) {
			String[] neigh = t.getString(i, "neighbors").split(",");
			for (String elem : neigh) {
				if (elem.length() == 0)
					continue;
				int j = Integer.parseInt(elem) - 1;
				nodes.get(i).addNeighbor(nodes.get(j), 0);
			}
			neigh = t.getString(i, "alley").split(",");
			for (String elem : neigh) {
				if (elem.length() == 0)
					continue;
				int j = Integer.parseInt(elem) - 1;
				nodes.get(i).addNeighbor(nodes.get(j), 1);
			}
			neigh = t.getString(i, "boat").split(",");
			for (String elem : neigh) {
				if (elem.length() == 0)
					continue;
				int j = Integer.parseInt(elem) - 1;
				nodes.get(i).addNeighbor(nodes.get(j), 2);
			}
		}
	}

	public void deleteNode(Node n) {
		for (Node node : nodes) {
			node.removeNeighbor(n);
		}
		nodes.remove(n);
	}

	public Node getMousedOverNode() {
		for (Node n : nodes) {
			if (n.pointInNode(mouseX, mouseY)) {
				return n;
			}
		}
		return null;
	}

	public void mousePressed() {
		dirty = true;
		if (!keyPressed)
			selected = getMousedOverNode();
		if (mouseButton == LEFT) {
			if (selected != null) {
				Node mousedOver = getMousedOverNode();
				if (mousedOver != null) {
					if (keyPressed(67)) { // c
						selected.toggleNeighbor(mousedOver, 0);
					} else if (keyPressed(65)) {
						selected.toggleNeighbor(mousedOver, 1);
					} else if (keyPressed(66)) {
						selected.toggleNeighbor(mousedOver, 2);
					}
				}
			} else {
				nodes.add(new Node(this, mouseX, mouseY));
			}
		} else if (mouseButton == RIGHT && selected != null) {
			List<Node> turnVisitClues = visited.get(turn);
			List<Node> turnUnvisitClues = unvisited.get(turn);
			if (turnVisitClues.contains(selected)) {
				turnVisitClues.remove(selected);
				turnUnvisitClues.add(selected);
			} else if (turnUnvisitClues.contains(selected)) {
				turnUnvisitClues.remove(selected);
			} else {
				turnVisitClues.add(selected);
			}
			selected = null;
		}
	}

	public boolean hasClueAbout(Node n) {
		for (List<Node> turnClue : visited) {
			if (turnClue.contains(n))
				return true;
		}
		return false;
	}

	public void keyPressed() {
		dirty = true;
		if (keyCode == TAB)
			showBoard = !showBoard;
		else if (key == 'h')
			hideEdges = !hideEdges;
		else if (key == 'v')
			connectionMode = !connectionMode;
		else if (key == 'x') {
			if (selected != null)
				deleteNode(selected);
		} else if (key == 'd')
			allowDrag = !allowDrag;
		else if (key == 's')
			saveBoard();
		else if (key == 'l')
			loadBoard("nodes.csv");
		else if (keyCode == LEFT)
			turn = max(turn - 1, 0);
		else if (keyCode == RIGHT)
			turn = min(turn + 1, 15);
		else if (key == '1') {
			if (keyPressed(CONTROL))
				viewMode = 0;
			else
				moveType.set(turn, 0); // normal
		} else if (key == '2') {
			if (keyPressed(CONTROL))
				viewMode = 1;
			else
				moveType.set(turn, 1); // alley
		} else if (key == '3') {
			if (keyPressed(CONTROL))
				viewMode = 2;
			else
				moveType.set(turn, 2); // boat
		}
		if (!keysPressed.contains(keyCode)) {
			keysPressed.add(keyCode);
		}
	}

	public void keyReleased() {
		while (keysPressed.contains(keyCode))
			keysPressed.remove(Integer.valueOf(keyCode));
	}

	public boolean keyPressed(int code) {
		return keysPressed.contains(code);
	}

}
