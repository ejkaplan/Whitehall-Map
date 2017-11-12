import java.util.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Node {

	private PApplet parent;
	public PVector pos;
	public List<Node> neighbors;
	public List<Node> alleyNeighbors;
	public List<Node> boatNeighbors;
	private float r;

	public Node(PApplet parent, float x, float y) {
		this.parent = parent;
		pos = new PVector(x, y);
		neighbors = new ArrayList<Node>();
		boatNeighbors = new ArrayList<Node>();
		alleyNeighbors = new ArrayList<Node>();
		r = 10;
	}

	public void toggleNeighbor(Node other, int edgeMode) {
		if (other == null || other == this)
			return;
		if (edgeMode == 0) {
			if (neighbors.contains(other)) {
				neighbors.remove(other);
				other.neighbors.remove(this);
			} else {
				neighbors.add(other);
				other.neighbors.add(this);
			}
		} else if (edgeMode == 1) {
			if (alleyNeighbors.contains(other)) {
				alleyNeighbors.remove(other);
				other.alleyNeighbors.remove(this);
			} else {
				alleyNeighbors.add(other);
				other.alleyNeighbors.add(this);
			}
		} else if (edgeMode == 2) {
			if (boatNeighbors.contains(other)) {
				boatNeighbors.remove(other);
				other.boatNeighbors.remove(this);
			} else {
				boatNeighbors.add(other);
				other.boatNeighbors.add(this);
			}
		}
	}

	public void addNeighbor(Node other, int mode) {
		if (mode == 0) {
			if (!neighbors.contains(other)) {
				neighbors.add(other);
				other.neighbors.add(this);
			}
		}
		if (mode == 1) {
			if (!alleyNeighbors.contains(other)) {
				alleyNeighbors.add(other);
				other.alleyNeighbors.add(this);
			}
		}
		if (mode == 2) {
			if (!boatNeighbors.contains(other)) {
				boatNeighbors.add(other);
				other.boatNeighbors.add(this);
			}
		}
	}

	public void removeNeighbor(Node other) {
		if (neighbors.contains(other)) {
			neighbors.remove(other);
			other.neighbors.remove(this);
		}
	}

	public void drawConnections(int viewMode) {
		parent.pushStyle();
		if (viewMode == 0)
			parent.stroke(0);
		else
			parent.stroke(0, 50);
		for (Node n : neighbors)
			parent.line(pos.x, pos.y, n.pos.x, n.pos.y);
		if (viewMode == 1)
			parent.stroke(109, 51, 0);
		else
			parent.stroke(109, 51, 0, 50);
		for (Node n : alleyNeighbors)
			parent.line(pos.x, pos.y, n.pos.x, n.pos.y);
		if (viewMode == 2)
			parent.stroke(0, 10, 150);
		else
			parent.stroke(0, 10, 150, 50);
		for (Node n : boatNeighbors) {
			parent.line(pos.x, pos.y, n.pos.x, n.pos.y);

		}
		parent.popStyle();
	}

	public void drawSelf(boolean clue, boolean unclue, boolean poss, int label) {
		parent.pushStyle();
		parent.fill(255);
		if (clue) {
			parent.fill(252, 255, 183);
		} else if (unclue) {
			parent.fill(150);
		}
		if (poss) {
			parent.stroke(255, 0, 0);
			parent.strokeWeight(4);
		}
		parent.ellipse(pos.x, pos.y, 2 * r, 2 * r);
		parent.textAlign(PConstants.CENTER, PConstants.CENTER);
		parent.fill(0);
		parent.textSize(10);
		parent.text(label, pos.x, pos.y);
		parent.popStyle();
	}

	public boolean pointInNode(float x, float y) {
		return pointInNode(new PVector(x, y));
	}

	public boolean pointInNode(PVector p) {
		return p.dist(pos) < r;
	}

}