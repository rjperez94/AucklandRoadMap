import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Mapper extends GUI {
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;
	private Trie trie;
	
	//two selected nodes
	Node start = null;
	Node goal = null;

	//shortest path
	List<Node> path;

	//the map of road lengths in path, indexed by the road name
	Map<String,Double> roadsAlongPath;

	//the list of articulation points
	Set<Node> artNodes = new HashSet<Node>();

	@Override
	protected void redraw(Graphics g) {
		if (graph != null) {		// don't draw if no graph structure i.e. if no data loaded or at start of program	
			textOutput();		//output text when necessary
			
			//if no path as determined by path list field, no start, and no goal.. just do this
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
		}
	}
	
	private void textOutput () {
		if (start != null && goal != null && path != null) {	//if there is path and start and goal
			//highlight each node in path
			for (Node n : path) {
				n.highlight = true;
			}

			//initialise map of road lengths to road names
			roadsAlongPath = new HashMap<String,Double>();
			//look at each node in path list
			for (Node n: path) {
				//look at each out segment in this node
				for (Segment s: n.outNeighbours) {
					//if start and end node of this segment is highlighted, then this segment is included in the path
					//highlight segment and put appropriate road length in the map
					if (s.start.highlight && s.end.highlight) {
						s.highlight = true;
						if (!roadsAlongPath.containsKey(s.road.toString())) {		//new road in map
							roadsAlongPath.put(s.road.toString(), s.length);
						} else {		//existing road in map
							double currentLength = roadsAlongPath.get(s.road.toString());
							roadsAlongPath.put(s.road.toString(), currentLength+s.length);
						}
						
					}
				}
			}
			
			//output text start and goal node
			getTextOutputArea().setText("From Node "+start.nodeID+" to Node "+goal.nodeID);
			double totLength = 0;
			for (String name: roadsAlongPath.keySet()) {
				//output text road and length
				//increment total length
				getTextOutputArea().append("\n"+name+" -- "+roadsAlongPath.get(name)+" km");
				totLength+= roadsAlongPath.get(name);
			}
			//output text total length of route
			getTextOutputArea().append("\n Total length is: "+totLength+" km");				
			
		}
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}
		
		//used to select two nodes --  for route finder
		if (start ==  null && goal ==  null) {	//if no start node, means first pick
			start = closest;
		} else if (start !=  null && goal ==  null) {	//if there's start node but no goal node, means second pick
			goal = closest;
		} else if (start !=  null && goal !=  null) { //if there's start and goal, go back to first pick
			start = closest;
			goal = null;
		}

		// if it's close enough, highlight it and show some information.
		if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());
		}
		
		//if there's start and goal nodes, run a* search, put result in path list field
		if (start != null && goal != null) {
			graph.AstarSearch(start, goal);
			path = graph.printPath(goal);
		}
	}

	@Override
	protected void onSearch() {
		if (trie == null)
			return;

		// get the search query and run it through the trie.
		String query = getSearchBox().getText();
		Collection<Road> selected = trie.get(query);

		// figure out if any of our selected roads exactly matches the search
		// query. if so, as per the specification, we should only highlight
		// exact matches. there may be (and are) many exact matches, however, so
		// we have to do this carefully.
		boolean exactMatch = false;
		for (Road road : selected)
			if (road.name.equals(query))
				exactMatch = true;

		// make a set of all the roads that match exactly, and make this our new
		// selected set.
		if (exactMatch) {
			Collection<Road> exactMatches = new HashSet<>();
			for (Road road : selected)
				if (road.name.equals(query))
					exactMatches.add(road);
			selected = exactMatches;
		}

		// set the highlighted roads.
		graph.setHighlight(selected);

		// now build the string for display. we filter out duplicates by putting
		// it through a set first, and then combine it.
		Collection<String> names = new HashSet<>();
		for (Road road : selected)
			names.add(road.name);
		String str = "";
		for (String name : names)
			str += name + "; ";

		if (str.length() != 0)
			str = str.substring(0, str.length() - 2);
		getTextOutputArea().setText(str);
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		trie = new Trie(graph.roads.values());
		origin = new Location(-250, 250); // close enough
		scale = 1;
				
		List <Integer> artComponents = new ArrayList<Integer>();
		int artNodesPrevSize = 0;
		
		//run articulation points method and put set of nodes in artNodes set field
		for (Node n: graph.nodes.values()) {
			if (!n.visited) {
				graph.articulation(n, artNodes);
				artComponents.add(artNodes.size()-artNodesPrevSize);
				artNodesPrevSize = artNodes.size();
			}
		}
		
		getTextOutputArea().setText("");
		for (int i = 0; i < artComponents.size(); i++) {
			getTextOutputArea().append("Component "+ (i+1) +" has "+artComponents.get(i)+" articulation points\n");
		}
		getTextOutputArea().append("\nThis data has this total of articulation points: "+artNodes.size());
		
		//highlight every node in artNodes list
		for (Node n: artNodes) {
			n.critical = true;
		}
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		new Mapper();
	}
}