import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Node represents an intersection in the road graph. It stores its ID and its
 * location, as well as all the segments that it connects to. It knows how to
 * draw itself, and has an informative toString method.
 * 
 */
public class Node {

	public final int nodeID;
	public final Location location;
	//public final Collection<Segment> segments;
	
	//added fields
	//used for A* and articulation points
	public List<Segment> outNeighbours = new ArrayList<Segment>(2);
	public List<Segment> inNeighbours = new ArrayList<Segment>(2);
	public Node parent;	//parent of this node, even though used in both algorithms, it will be reseted everytime. No worries
	
	//used for route finding
	public double g_score;	// g is distance from the start
	public double h_score;	// h is the heuristic to goal destination
	public double f_score;		// f = g + h 
	public boolean highlight = false; 	//part of route???
	
	//used for articulation points - first four fields for recursive, all fields for iterative
	public int depth = Integer.MAX_VALUE;
	public int reachBack = 0; 		//the minimum depth that the subtree node that the subtree can reach back
	public boolean critical = false;	//articulation point???
	public boolean visited = false;
	public Queue <Node> children = null;		//children nodes of this node

	public Node(int nodeID, double lat, double lon) {
		this.nodeID = nodeID;
		this.location = Location.newFromLatLon(lat, lon);
		//this.segments = new HashSet<Segment>();
	}

	/*public void addSegment(Segment seg) {
		segments.add(seg);
	}*/

	public void draw(Graphics g, Dimension area, Location origin, double scale) {
		Point p = location.asPoint(origin, scale);

		// for efficiency, don't render nodes that are off-screen.
		if (p.x < 0 || p.x > area.width || p.y < 0 || p.y > area.height)
			return;

		int size = (int) (Mapper.NODE_GRADIENT * Math.log(scale) + Mapper.NODE_INTERCEPT);
		g.fillRect(p.x - size / 2, p.y - size / 2, size, size);
	}
	
	//returns all neighbours, in and out
	public List<Segment> allNeighbours() {
		List<Segment> allNeigh = new ArrayList<Segment>();
		allNeigh.addAll(inNeighbours);
		allNeigh.addAll(outNeighbours);
		return allNeigh;
	}

	public String toString() {
		Set<String> edges = new HashSet<String>();
		for (Segment s : outNeighbours) {
			if (!edges.contains(s.road.name))
				edges.add(s.road.name);
		}

		String str = "ID: " + nodeID + "  loc: " + location + "\nroads: ";
		for (String e : edges) {
			str += e + ", ";
		}
		return str.substring(0, str.length() - 2);
	}
}