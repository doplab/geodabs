package ch.unil.doplab.geodabs.model;

public class BBox {

    public final Point p1;
    public final Point p2;
    public final double width;
    public final double height;

    public BBox(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.width = p2.lon - p1.lon;
        this.height = p2.lat - p1.lat;
    }

    /**
     * Returns true if the BBox contains the provided point.
     *
     * @param p the point
     * @return
     */
    public boolean contains(Point p) {
        return p1.lon <= p.lon && p1.lat <= p.lat && p2.lon >= p.lon && p2.lat >= p.lat;
    }

    /**
     * Returns true if the BBox overlap with the provided point.
     *
     * @param bbox
     * @return
     */
    public boolean overlap(BBox bbox) {
        return p1.lon <= bbox.p1.lon + bbox.width
                && p1.lon + width >= bbox.p1.lon
                && p1.lat <= bbox.p1.lat + bbox.height
                && p1.lat + height >= bbox.p1.lat;
    }

    @Override
    public String toString() {
        return "BBox(" + p1 + ", " + p2 + ")";
    }
}
