import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 * 
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;

	Node highlightedNode;
	Collection<Road> highlightedRoads = new HashSet<>();

	public Graph(File nodes, File roads, File segments, File polygons) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
	}

	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		for (Segment s : segments) {
			if (s.highlight) {		// highlighted segment used for route finder
				g2.setColor(Color.green.darker());
				s.draw(g2, origin, scale);
			} else if (s.road.notForCar == 1) {	//permanently highlight segments that are not for cars
				g2.setColor(Color.magenta.darker());
				s.draw(g2, origin, scale);
			} else if (s.road.oneWay == 1) {	//permanently highlight segments that belong to one way roads as red
				g2.setColor(Color.red.darker());
				s.draw(g2, origin, scale);
			}  else {	//draw using original colour
				g2.setColor(Mapper.SEGMENT_COLOUR);
				s.draw(g2, origin, scale);
			}	
		}

		// draw the segments of all highlighted roads -- used for search query
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Road road : highlightedRoads) {
			for (Segment seg : road.components) {
				seg.draw(g2, origin, scale);
			}
		}

		// draw all the nodes.
		for (Node n : nodes.values()) {
			if (!n.highlight && !n.critical) {	//draw using original colour
				g2.setColor(Mapper.NODE_COLOUR);
				n.draw(g2, screen, origin, scale);
			} else if (n.highlight) {		// highlighted node used for route finder
				g2.setColor(Color.green.darker());
				n.draw(g2, screen, origin, scale);
			} else if (n.critical) {		// permanently highlight node used for articulation points
				g.setColor(Color.CYAN);
				n.draw(g, screen, origin, scale);
			}
		}

		// draw the highlighted node, if it exists -- used in node select
		if (highlightedNode != null) {
			g2.setColor(Mapper.HIGHLIGHT_COLOUR);
			highlightedNode.draw(g2, screen, origin, scale);
		}
		
		for (Node n: nodes.values()) {	//reset node highlight from route finding
			n.highlight = false;
		}
		
		for (Road r: roads.values()) {		//reset segment highlight from route finding
			for (Segment s: r.components)
				s.highlight = false;
		}
	}

	public void setHighlight(Node node) {
		this.highlightedNode = node;
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}
	
	public void AstarSearch(Node start, Node goal){
		//initilise all nodes parent to null and calculate heuristic score using Eucleadian distance 
		for (Node n: nodes.values()) {
			n.h_score = n.location.distance(goal.location);
			n.f_score = 0;
			n.parent = null;
		}
		
		//maintain both a visited set and a parent field in node
		Set<Node> explored = new HashSet<Node>();
		//fringe == priority queue ordered by f_score
		PriorityQueue<Node> fringe = new PriorityQueue<Node>(20,	new NodeComparator());

		//cost from start
		start.g_score = 0;

		//add start to fringe prioritised based on f_score
		fringe.add(start);

		boolean found = false;

		//repeat until goal not found
		while((!fringe.isEmpty())&&(!found)){
			//the node in having the lowest f_score value
			Node current = fringe.poll();
			
			//add to explored set
			explored.add(current);

			//goal found
			if(current.equals(goal)){
				found = true;
			}

			//check every child i.e. outNeighbour of current node
			for(Segment s : current.outNeighbours){
				Node child = s.end;		//node at other end of segment
				double cost = s.length;	
				double temp_g_scores = current.g_score + cost;		//current distance + segment length
				double temp_f_scores = temp_g_scores + child.h_score;	//current distance + segment length + heuristic

				//if child node has been evaluated and	 the newer f_score is higher, skip
				if((explored.contains(child)) &&	(temp_f_scores >= child.f_score)){
					continue;
				} else if((!fringe.contains(child)) || (temp_f_scores < child.f_score)){
					//if child node is not in queue or newer f_score is lower
					child.parent = current;		//parent of current node is this child
					child.g_score = temp_g_scores;		//update g_score and f_score of this child
					child.f_score = temp_f_scores;

					//remove child from fringe
					if(fringe.contains(child)){
						fringe.remove(child);
					}
					//add child to fringe
					//remember: priority based on NEW f_score of child
					fringe.add(child);
				}
			}
		}

	}
	
	//compares nodes based f_score: smallest --> largest
	static class NodeComparator implements Comparator<Node> {            
		//override compare method
		public int compare(Node n1, Node n2) {
			if(n1.f_score > n2.f_score){
				return 1;
			} else if (n1.f_score < n2.f_score){
				return -1;
			} else {
				return 0;
			}
		}
     }

	//returns  a list of nodes in shortest path from start --> goal
	public List<Node> printPath(Node target){
		List<Node> path = new ArrayList<Node>();
		//goal to start i.e. follow node.parent
		for(Node node = target; node!=null; node = node.parent){
			path.add(node);
		}
		
		//from: goal --> start; to: start --> goal
		Collections.reverse(path);
		return path;
	} 
	
	//this will only be called once when the data is first loaded
	public void articulation (Node start, Set<Node> artPn) {
		//for all nodes
		for (Node n: nodes.values()) {
			//set depth to infinity, reachBack to 0 and parent to null
			n.depth = Integer.MAX_VALUE;
			n.reachBack = 0;
			n.parent = null;
		}
		
		//depth of start node is 0 and subtree is 0
		start.depth = 0;
		int subTree = 0;
		
		//for all neighbours of start node , inNeigh+outNeigh
		for (Segment s: start.allNeighbours()) {
			/*if (s.road.notForCar == 1) {		//include forCar roads only
				continue;
			}*/
			
			Node neigh = s.end;		//node at other end of segment
			//if neighbour depth  is set to infinity
			if (neigh.depth ==  Integer.MAX_VALUE) {
				start.visited = true;
				neigh.visited = true;
				iterArtPts(neigh, start, artPn);
				subTree++;
			}
		}
		
		if (subTree > 1) {
			artPn.add(start);
		}
	}

	private void iterArtPts(Node firstNode, Node root, Set<Node> set) {
		//maintain a stack
		Stack<Node> stack = new Stack <Node>();
		//put firstNode in stack
		firstNode.depth = 1;
		firstNode.parent = root;
		stack.push(firstNode);
		firstNode.visited = true;
		
		//repeat until stack is empty
		while (!stack.isEmpty()) {
			//make a copy of node at top of stack
			Node elem  = stack.peek();
			Node node = elem;
			
			//first visit
			if (elem.children == null) {
				node.depth = elem.depth;
				elem.reachBack = elem.depth;
				node.visited = true;

				elem.children  = new LinkedList<Node>();
				//for all neighbours, inNeigh+outNeigh of node
				for (Segment s: node.allNeighbours()) {
					/*if (s.road.notForCar == 1) {		//include forCar roads only
						continue;
					}*/
					
					Node neigh = s.end;		//node at other end of segment
					if  (!neigh.equals(elem.parent) ) { 
						//if end node != parent of this node, add end node to children of this node
						elem.children.add(neigh);
					}
				}
			//process children of node
			} else if (!elem.children.isEmpty()) {
				Node child = elem.children.poll();
				child.visited = true;
				if (child.depth < Integer.MAX_VALUE) {
					elem.reachBack = Math.min(elem.reachBack, child.depth);
				} else {
					//add child to stack
					child.depth = node.depth+1;
					child.parent = elem;
					stack.push(child);
				}
			//last visit
			} else {
				if  (!node.equals(firstNode) ) {   
					if  (elem.reachBack >= elem.parent.depth) {  
						//add as articulation point
						elem.parent.visited = true;
						set.add (elem.parent);
					}
					elem.parent.reachBack =  Math.min (elem.parent.reachBack,   elem.reachBack);
				}
				//remove from stack
				stack.pop();
			}
			
		}
	}
	
	//recursive Articulation points
	/*public void articulation (Node start,  Set<Node> artPn) {

		for (Node n: nodes.values()) {
			n.depth = Integer.MAX_VALUE;
			n.reachBack = 0;
		}
		
		int subTree = 0;
		for (Segment s: start.allNeighbours()) {
			Node neigh = s.end;
			if (neigh.depth ==  Integer.MAX_VALUE) { 
				neigh.visited = true;
				start.visited = true;
				recArtPts(neigh, 1, start, artPn);
				subTree++;
			}
		}
		
		if (subTree > 1) 
			artPn.add(start);
		
	}

	private int recArtPts(Node node, int depth, Node from, Set<Node> set) {
		node.depth = depth;
		int reachBack = depth; 
		for (Segment s: node.allNeighbours()) {
			Node thisNeigh = s.end;
			thisNeigh.visited = true;
			if (thisNeigh.nodeID == from.nodeID) {
				continue;
			}
			
			if  (thisNeigh.depth <Integer.MAX_VALUE) {
				reachBack = Math.min(thisNeigh.depth, reachBack);
			} else { 
				int childReach = recArtPts(thisNeigh, depth +1, node, set);
				if (childReach  >= depth) {
					set.add(node);
				}
				reachBack = Math.min(childReach, reachBack);
			}
		}
		return  reachBack;

	}*/

}