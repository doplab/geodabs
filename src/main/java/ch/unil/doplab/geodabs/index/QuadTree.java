package ch.unil.doplab.geodabs.index;

import ch.unil.doplab.geodabs.distance.Distance;
import ch.unil.doplab.geodabs.model.BBox;
import ch.unil.doplab.geodabs.Util;
import ch.unil.doplab.geodabs.model.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A simplistic quadtree implementation optimized for the GISCUP.
 * This implementation is not thread safe and must be used with caution.
 * In the context of the challenge insertions are performed sequentially.
 * Search operations can safely be performed in parallel since the index
 * is not updated after its creation.
 *
 * @param <T>
 */
public class QuadTree<T> {

    private class Entry {

        final Point point;
        final T object;

        private Entry(Point p, T e) {
            this.point = p;
            this.object = e;
        }

    }

    private class Node {

        final BBox bbox;
        Node nw, ne, sw, se;
        HashSet<Point> points = new HashSet<>(capacity);
        ArrayList<Entry> entries = new ArrayList<Entry>(capacity);

        Node(BBox bbox) {
            this.bbox = bbox;
        }

        void insert(Entry entry) {
            if (bbox.contains(entry.point)) {
                if (points != null) {
                    insertOrSplit(entry);
                } else {
                    nw.insert(entry);
                    ne.insert(entry);
                    sw.insert(entry);
                    se.insert(entry);
                }
            }
        }

        void insertOrSplit(Entry entry) {
            // When a node reaches full capacity,
            // it must be splitted into sub nodes.
            if (points.size() < capacity) {
                points.add(entry.point);
                entries.add(entry);
            } else {
                double x1 = bbox.p1.lon;
                double x2 = bbox.p1.lon + bbox.width / 2;
                double x4 = bbox.p2.lon;
                double x3 = Math.nextAfter(x2, x4);
                double y1 = bbox.p1.lat;
                double y2 = bbox.p1.lat + bbox.height / 2;
                double y4 = bbox.p2.lat;
                double y3 = Math.nextAfter(y2, y4);
                nw = new Node(new BBox(new Point(x1, y3), new Point(x2, y4)));
                ne = new Node(new BBox(new Point(x3, y3), new Point(x4, y4)));
                sw = new Node(new BBox(new Point(x1, y1), new Point(x2, y2)));
                se = new Node(new BBox(new Point(x3, y1), new Point(x4, y2)));
                ArrayList<Entry> temp = entries;
                points = null;
                entries = null;
                for (Entry e : temp) {
                    insert(e);
                }
                insert(entry);
            }
        }

        void search(BBox bbox, HashSet<T> results) {
            if (bbox.overlap(this.bbox)) {
                if (entries != null) {
                    for (Entry e : entries) {
                        if (bbox.contains(e.point)) {
                            results.add(e.object);
                        }
                    }
                } else {
                    nw.search(bbox, results);
                    ne.search(bbox, results);
                    sw.search(bbox, results);
                    se.search(bbox, results);
                }
            }
        }

        void search(Point point, double distance, BBox bbox, HashSet<T> results) {
            if (bbox.overlap(this.bbox)) {
                if (entries != null) {
                    for (Entry e : entries) {
                        if (bbox.contains(e.point) && Distance.distance(point, e.point) <= distance) {
                            results.add(e.object);
                        }
                    }
                } else {
                    nw.search(point, distance, bbox, results);
                    ne.search(point, distance, bbox, results);
                    sw.search(point, distance, bbox, results);
                    se.search(point, distance, bbox, results);
                }
            }
        }

    }

    private final Node root;
    private final int capacity;

    /**
     * Initializes a QuadTree.
     *
     * @param bbox the span of the quadtree
     * @param capacity the capacity of the nodes of the quadtree
     */
    public QuadTree(BBox bbox, int capacity) {
        this.root = new Node(bbox);
        this.capacity = capacity;
    }

    /**
     * Inserts an object associated to a point in the index.
     *
     * @param p the point
     * @param o the object
     */
    public void insert(Point p, T o) {
        root.insert(new Entry(p, o));
    }

    /**
     * Searches the index with a bounding box
     *
     * @param bbox the bounding box
     * @return the matching objects
     */
    public Set<T> search(BBox bbox) {
        HashSet<T> results = new HashSet<>();
        root.search(bbox, results);
        return results;
    }

    /**
     * Searches the index with a point and a range.
     *
     * @param point the point
     * @param range the range
     * @return the matching objects
     */
    public Set<T> search(Point point, double range) {
        HashSet<T> results = new HashSet<>();
        BBox bbox = new BBox(
                new Point(point.lon - range, point.lat - range),
                new Point(point.lon + range, point.lat + range));
        root.search(point, range, bbox, results);
        return results;
    }

}
