package ch.unil.doplab.geodabs.distance;

import ch.unil.doplab.geodabs.model.Point;

import static java.lang.Math.*;

/**
 * A utility class for computing score distances.
 */
public class Distance {


    private static final double R = 6371E3;

    /**
     * Returns the score dfd separating a pair of points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the score dfd
     */
    public static double distance(Point p1, Point p2) {
        double lat1 = toRadians(p1.lat);
        double lat2 = toRadians(p2.lat);
        double lon1 = toRadians(p1.lon);
        double lon2 = toRadians(p2.lon);
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = pow(sin(dlat / 2), 2) + cos(lat1) * cos(lat2) * pow(sin(dlon / 2), 2);
        double c = 2 * asin(min(1, sqrt(a)));
        double d = R * c;
        return d;
    }

}
