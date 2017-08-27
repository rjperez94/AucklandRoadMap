import java.awt.Graphics;
import java.awt.Point;

/**
 * A Segment is the most interesting class making up our graph, and represents
 * an edge between two Nodes. It knows the Road it belongs to as well as the
 * Nodes it joins, and contains a series of Locations that make up the length of
 * the Segment and can be used to render it.
 * 
 */
public class Segment {

	public final Road road;
	public final Node start, end;
	public final double length;
	public final Location[] points;
	
	public boolean highlight = false;

	public Segment(Graph graph, int roadID, double length, int node1ID,
			int node2ID, double[] coords) {

		this.road = graph.roads.get(roadID);
		this.start = graph.nodes.get(node1ID);
		this.end = graph.nodes.get(node2ID);
		this.length = length;

		points = new Location[coords.length / 2];
		for (int i = 0; i < points.length; i++) {
			points[i] = Location
					.newFromLatLon(coords[2 * i], coords[2 * i + 1]);
		}

		this.road.addSegment(this);	// add this to road object
		this.start.outNeighbours.add(this);	//add this to out neigh of start node
		this.end.inNeighbours.add(this);	//add this to in neigh of end node
		
		//do the reverse if road this segment belongs to is two way
		if (road.oneWay == 0){
			Segment revSeg = this.reverse();
			end.outNeighbours.add(revSeg);
			start.inNeighbours.add(revSeg);
		}
	}

	public Segment(Road road, double length, Node end, Node start,	Location[] points) {
		this.road = road;
		this.start = end;
		this.end = start;
		this.length = length;
		this.points = points;
	}

	public void draw(Graphics g, Location origin, double scale) {
		for (int i = 1; i < points.length; i++) {
			Point p = points[i - 1].asPoint(origin, scale);
			Point q = points[i].asPoint(origin, scale);
			g.drawLine(p.x, p.y, q.x, q.y);
		}
	}
	
	//make a reverse segment -- put start node as end node and end node as start node
	//this depends whether road is one way or two way
	public Segment reverse(){
		Segment seg =  new Segment(road, length, end, start, points);
		return seg;
	}
	
	public String toString(){
		return String.format("%d: %4.2fkm from %d to %d", road.roadID, length, start.nodeID,end.nodeID);
	}
}