package ch.unil.doplab.geodabs.distance;

import ch.unil.doplab.geodabs.model.Point;

public class DTW {

    public static double distance(Point[] x, Point[] y) {
        double[][] dtw = new double[x.length][y.length];
        dtw[0][0] = 0;
        for (int i = 1; i < x.length; i++) {
            dtw[i][0] = Double.MAX_VALUE;
        }
        for (int j = 1; j < y.length; j++) {
            dtw[0][j] = Double.MAX_VALUE;
        }
        for (int i = 1; i < x.length; i++) {
            for (int j = 1; j < y.length; j++) {
                double cost = Distance.distance(x[i], y[j]);
                dtw[i][j] = cost + Math.min(dtw[i-1][j], Math.min(dtw[i][j-1], dtw[i-1][j-1]));
            }
        }
        return dtw[x.length-1][y.length-1];
    }
    
}
