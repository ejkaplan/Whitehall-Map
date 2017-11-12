import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.Table;
import processing.data.TableRow;

public class WhitehallMap extends PApplet {

	private List<Integer> keysPressed;
	public List<Node> nodes;
	private Node selected;
	private boolean makeEdge;
	private int turn;
	private List<List<Node>> visited;
	private List<List<Node>> unvisited;

	public static void main(String[] args) {
		PApplet.main("WhitehallMap");
	}

	public void settings() {
		fullScreen();
	}

	public void setup() {
		visited = new ArrayList<List<Node>>();
		unvisited = new ArrayList<List<Node>>();
		for (int i = 0; i < 16; i++) {
			visited.add(new ArrayList<Node>());
			unvisited.add(new ArrayList<Node>());
		}
		keysPressed = new ArrayList<Integer>();
		nodes = new ArrayList<Node>();
		turn = 0;
	}

	public void draw() {
		background(200);
		List<Node> poss = possLocations();
		if (selected != null) {
			if (makeEdge)
				line(selected.pos.x, selected.pos.y, mouseX, mouseY);
			else
				selected.pos.set(mouseX, mouseY);
		}
		for (Node n : nodes) {
			n.drawConnections();
		}
		for (Node n : nodes) {
			n.drawSelf(visited.get(turn).contains(n), unvisited.get(turn).contains(n), poss.contains(n));
		}
		drawTimeline(9 * height / 10, 9 * width / 10, turn);
	}

	public void drawTimeline(float y, float w, int turn) {
		float x = (width - w) / 2;
		line(x, y, x + w, y);
		for (int i = 0; i <= 15; i++) {
			line(x + i * w / 15, y - 10, x + i * w / 15, y + 10);
		}
		fill(255);
		ellipse(x + turn * w / 15, y, 20, 20);
		textAlign(CENTER, CENTER);
		fill(0);
		text(turn, x + turn * w / 15, y);
	}

	public List<Node> possLocations() {
		List<Node> poss = new ArrayList<Node>();
		if (visited.get(0).size() == 0)
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
				for (Node n : path.get(path.size() - 1).neighbors) {
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
		for (Node n : nodes) {
			TableRow r = t.addRow();
			r.setFloat("x", n.pos.x);
			r.setFloat("y", n.pos.y);
			String neighbors = "";
			for (Node x : n.neighbors) {
				neighbors += nodes.indexOf(x) + ",";
			}
			r.setString("neighbors", neighbors);
		}
		saveTable(t, "data/nodes.csv");
	}

	public void loadBoard() {
		Table t = loadTable("data/nodes.csv", "header");
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
				System.out.println(elem);
				int j = Integer.parseInt(elem);
				System.out.println(j);
				nodes.get(i).addNeighbor(nodes.get(j));
			}

		}
	}

	public void deleteNode(Node n) {
		for (Node node : nodes) {
			node.removeNeighbor(n);
		}
		nodes.remove(n);
	}

	public Node getMousedOver() {
		for (Node n : nodes) {
			if (n.pointInNode(mouseX, mouseY)) {
				return n;
			}
		}
		return null;
	}

	public void mousePressed() {
		selected = getMousedOver();
		if (mouseButton == LEFT) {
			if (selected != null) {
				if (keyPressed(88)) { // x
					deleteNode(selected);
					selected = null;
				} else if (keyPressed(16)) // shift
					makeEdge = true;
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

	public void mouseReleased() {
		if (makeEdge && selected != null) {
			Node end = getMousedOver();
			if (end != null)
				selected.toggleNeighbor(end);
		}
		makeEdge = false;
		selected = null;
	}

	public void keyPressed() {
		if (key == 's')
			saveBoard();
		else if (key == 'l')
			loadBoard();
		else if (keyCode == LEFT)
			turn = max(turn - 1, 0);
		else if (keyCode == RIGHT)
			turn = min(turn + 1, 15);
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
