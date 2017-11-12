import java.util.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Node {

	private PApplet parent;
	public PVector pos;
	public List<Node> neighbors;
	private float r;

	public Node(PApplet parent, float x, float y) {
		this.parent = parent;
		pos = new PVector(x, y);
		neighbors = new ArrayList<Node>();
		r = 15;
	}

	public void toggleNeighbor(Node other) {
		if (other == null || other == this)
			return;
		if (neighbors.contains(other)) {
			neighbors.remove(other);
			other.neighbors.remove(this);
		} else {
			neighbors.add(other);
			other.neighbors.add(this);
		}
	}

	public void addNeighbor(Node other) {
		if (!neighbors.contains(other)) {
			neighbors.add(other);
			other.neighbors.add(this);
		}
	}

	public void removeNeighbor(Node other) {
		if (neighbors.contains(other)) {
			neighbors.remove(other);
			other.neighbors.remove(this);
		}
	}

	public void drawConnections() {
		for (Node n : neighbors)
			parent.line(pos.x, pos.y, n.pos.x, n.pos.y);
	}

	public void drawSelf(boolean clue, boolean unclue, boolean poss) {
		parent.fill(255);
		parent.ellipse(pos.x, pos.y, 2 * r, 2 * r);
		if (clue) {
			parent.fill(216, 221, 57);
			parent.arc(pos.x, pos.y, 2 * r, 2 * r, -PConstants.HALF_PI, PConstants.HALF_PI, PConstants.CHORD);
		} else if (unclue) {
			parent.fill(100);
			parent.arc(pos.x, pos.y, 2 * r, 2 * r, -PConstants.HALF_PI, PConstants.HALF_PI, PConstants.CHORD);
		}
		if (poss) {
			parent.fill(255,0,0);
			parent.arc(pos.x, pos.y, 2 * r, 2 * r, PConstants.HALF_PI, PConstants.PI+PConstants.HALF_PI, PConstants.CHORD);
		}
		parent.textAlign(PConstants.CENTER, PConstants.CENTER);
	}

	public boolean pointInNode(float x, float y) {
		return pointInNode(new PVector(x, y));
	}

	public boolean pointInNode(PVector p) {
		return p.dist(pos) < r;
	}

}