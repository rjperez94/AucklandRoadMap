import java.util.Collection;
import java.util.HashSet;

/**
 * Road represents ... a road ... in our graph, which is some metadata and a
 * collection of Segments. We have lots of information about Roads, but don't
 * use much of it.
 * 
 */
public class Road {
	public final int roadID, oneWay, notForCar;
	public final String name, city;
	public final Collection<Segment> components;

	public Road(int roadID, int type, String label, String city, int oneway,
			int speed, int roadclass, int notforcar, int notforpede,
			int notforbicy) {
		//added field
		this.oneWay = oneway;
		this.notForCar = notforcar;
		
		this.roadID = roadID;
		this.city = city;
		this.name = label;
		this.components = new HashSet<Segment>();
	}

	public void addSegment(Segment seg) {
		components.add(seg);
	}
	
	public String toString(){
		return "Road: "+this.name;
	}
}