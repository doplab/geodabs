package ch.unil.doplab.geodabs.model;

public class Point {

    public final double lon;
    public final double lat;

    public Point(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point p = (Point) obj;
            return (lon == p.lon) && (lat == p.lat);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(lon);
        bits ^= Double.doubleToLongBits(lat) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    @Override
    public String toString() {
        return "Point(" + lon + ", " + lat + ")";
    }

}
