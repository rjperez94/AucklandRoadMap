# AucklandRoadMap

## Compiling Java files using Eclipse IDE

1. Download this repository as ZIP
2. Create new `Java Project` in `Eclipse`
3. Right-click on your `Java Project` --> `Import`
4. Choose `General` --> `Archive File`
5. Put directory where you downloaded ZIP in `From archive file`
6. Put `ProjectName/src` in `Into folder`
7. Click `Finish`

## Running the program

1. Right-click on your `Java Project` --> `Run As` --> `Java Application` --> `Mapper`
2. To load the map, click `Load` and find `ProjectName/src/data`. It's actually the `data` directory
3. Use the UI controls to zoom in/out and navigate through the map

## Build an executable using IntelliJ IDEA

1. Go to **File** → **Project Structure** → **Artifacts**.
2. Click the green plus (**+**) button, select **JAR**, and choose **From modules with dependencies...**
3. In the **Main Class** field, click the folder icon and select the application's entry point class.
4. Under **JAR files from libraries**, select **extract to the target JAR** (this creates the single Fat JAR).
5. Click **OK**, then click **Apply**.
6. From the top menu bar, go to **Build** → **Build Artifacts...** and click **Build**.
7. The executable jar file will be generated inside the project directory under `out/artifacts/`.

### Run the executable JAR file using the command line:

```bash
java -jar path/to/executable.jar
```

## Live Demo

You can run this application directly in your web browser via the link below:

**[Launch Live Demo](https://rjperez94.github.io/AucklandRoadMap/)**

### Loading Local Data

If you are trying to pick a file from your physical hard drive, you cannot browse your local folders through the Java window. You must use the bridge upload feature.

1. Look at the very top right of the Java window's title bar for a small **Up Arrow (Upload)** button.
2. Click it to trigger your **native browser file picker** (this one can see your real computer folders).
3. Select your local file. The app will silently drop it into the virtual folder named `/files/uploads/`.
4. Now, inside your Java file picker, type `/files/uploads/` into the file path bar and press **Enter** to find your uploaded file.


## Overview

A program that lets a user view and search the Auckland, New Zealand Road system. This is a program that will read a collection of files containing information about the roads in the Auckland region, display the information visually, and let the user view and search the data in several ways. It can also find and display shortest
routes between intersections, identify critical intersections for disaster management, and calculate total capacity of the road system between two points. The program uses several large data structures.

### The data

<strong>Nodes</strong>

These are locations where roads end, join, or intersect. The node data is in the nodeID-lat-lon.tab
file: a tab separated text file with one line for each node, specifying the ID of the node, and the
latitude and longitude of the node. Note that latitude and longitude are specified in degrees,
not distances. One degree of latitude corresponds to 111.0 kilometers. One degree of longitude
varies, depending on the latitude, but it is reasonable to assume that for the whole region of
Auckland, one degree of longitude is 88.649 kilometers. This means that when you are computing
distances between two points, you must scale the latitude difference by 111.0 and scale the
longitude difference by 88.649.

<strong>Road Segments</strong>

These are a part of a road between two intersections (nodes). The only intersections on a
road segment are at its ends. The data includes the length of each segment. The road segment
data is in the roadSeg-roadID-length-nodeID-nodeID-coords.tab file: a tab separated text file
with one line for each road segment, specifying the ID of the road object this segment belongs
to, the length of the segment and the ID’s of the nodes at each end of the segment, followed by
the coordinates of the road segment for drawing it. The first line of the file specifies the fields
in each line. The coordinates are given as a sequence of latitude and longitude coordinates of
points along the centerline of the road segment. The coordinates consist of an even number of
floating point numbers, and there are always at least two pairs of numbers (for each end of the
road segment); some segments have a lot more coordinates. 

<strong>Roads</strong>

These are a sequences of segments, with a name and other properties. These need not be an entire
road - a real road that has different properties for some parts will be represented in the data 
by several road objects, all with the same name. A very important property of roads is whether
they are one-way or not. * The road data is in the roadID-roadInfo.tab file, with one line for
each road object. The first value on the line is the roadID. The columns are specified at the top
of the file.

## Features

- Navigable map
- Selectable intersection with the mouse
  - The program will highlight intersection, and print out the ID of the intersection and the names of all the roads at the intersection.
- Allowa user to select a road by entering its name
  - Highlight the road with name matching their input by drawing all the segments in all the Road objects that have the given name in a highlighted colour
  - This includes the name of all roads that start with the prefix typed into the search box
- Route finding
  - Allows the user to specify two intersections on the map and will then find using A* search and display the shortest route between those two locations
  - Highlight the route on the map (by colouring all the road segments along the route) and output a list of all the roads along the route, along with the lengths of each part of the route and the total length of the route
- Critical intersections
  -  This is an analysis tool that might be used by emergency services planners who want to identify every intersection that would have bad consequences for emergency services if it were blocked or disabled in some way. An intersection that is the only entrance way into some part of the map is a critical intersection
