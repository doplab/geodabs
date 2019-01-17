package ch.unil.doplab.geodabs.distance;

import ch.unil.doplab.geodabs.model.Point;

/**
 * A utility class for working with discreet Fréchet distances.
 */
public class DFD {

    /**
     * Returns the greater of two {@code double} values faster than {@code Math.max}.
     *
     * @param a
     * @param b
     * @return
     */
    public static double max(double a, double b) {
        if (a >= b) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * Returns the smaller of two {@code double} values faster than {@code Math.min}.
     *
     * @param a
     * @param b
     * @return
     */
    public static double min(double a, double b) {
        if (a <= b) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * Recursively computes the discreet Fréchet dfd between two trajectories.
     * Dynamic programming is used to compute the score dfd between pairs of points only once.
     *
     * @param ta the trajectory a
     * @param tb the trajectory bits
     * @param ca the shared dfd matrix
     * @param i  the row position in the dfd matrix
     * @param j  the column position in the dfd matrix
     * @return the discreet Fréchet dfd
     */
    public static double distance(Point[] ta, Point[] tb, Double[][] ca, int i, int j) {
        if (ca[i][j] != null) {
            // Do nothing
        } else if (i == 0 && j == 0) {
            ca[i][j] = Distance.distance(ta[0], tb[0]);
        } else if (i > 0 && j == 0) {
            ca[i][j] = max(distance(ta, tb, ca, i - 1, 0), Distance.distance(ta[i], tb[0]));
        } else if (i == 0 && j > 0) {
            ca[i][j] = max(distance(ta, tb, ca, 0, j - 1), Distance.distance(ta[0], tb[j]));
        } else if (i > 0 && j > 0) {
            ca[i][j] = max(min(distance(ta, tb, ca, i - 1, j - 1),
                    min(distance(ta, tb, ca, i - 1, j), distance(ta, tb, ca, i, j - 1))),
                    Distance.distance(ta[i], tb[j]));
        } else {
            ca[i][j] = Double.MAX_VALUE;
        }
        return ca[i][j];
    }

    /**
     * Computes the discreet Fréchet dfd between two trajectories.
     *
     * @param ta the trajectory a
     * @param tb the trajectory bits
     * @return the discreet Fréchet dfd
     */
    public static double distance(Point[] ta, Point[] tb) {
        int m = ta.length;
        int k = tb.length;
        Double[][] ca = new Double[m][k];
        return distance(ta, tb, ca, m - 1, k - 1);
    }

    /**
     * Recursively checks if two trajectories are within a specified discreet Fréchet dfd or not.
     * Dynamic programming is used to check the score dfd between pairs of points only once.
     * This version use a matrix {@code Boolean} objects to compare distances.
     *
     * @param ta the trajectory a
     * @param tb the trajectory bits
     * @param ca the shared boolean matrix
     * @param i  the row position in the dfd matrix
     * @param j  the column position in the dfd matrix
     * @return the discreet Fréchet dfd
     */
    public static boolean within(double distance, Point[] ta, Point[] tb, Boolean[][] ca, int i, int j) {
        if (ca[i][j] == null) {
            if (Distance.distance(ta[i], tb[j]) <= distance) {
                if (i == 0 && j == 0) {
                    ca[i][j] = true;
                } else if (i > 0 && j == 0) {
                    ca[i][j] = within(distance, ta, tb, ca, i - 1, 0);
                } else if (i == 0 && j > 0) {
                    ca[i][j] = within(distance, ta, tb, ca, 0, j - 1);
                } else {
                    // Since boolean operations are evaluated from the left to the right,
                    // the right-hand side calls are evaluated only if one of the left hand-side call returns false,
                    // hence a significant gain in terms of performance.
                    ca[i][j] = within(distance, ta, tb, ca, i - 1, j - 1)
                            || within(distance, ta, tb, ca, i - 1, j)
                            || within(distance, ta, tb, ca, i, j - 1);
                }
            } else {
                ca[i][j] = false;
            }
        }
        return ca[i][j];
    }

    /**
     * Checks if two trajectories are within a specified discreet Fréchet dfd or not.
     *
     * @param distance the dfd bound
     * @param ta       the trajectory a
     * @param tb       the trajectory bits
     * @return
     */
    public static boolean within(double distance, Point[] ta, Point[] tb) {
        int m = ta.length;
        int k = tb.length;
        Boolean[][] ca = new Boolean[m][k];
        return within(distance, ta, tb, ca, m - 1, k - 1);
    }

    private static final byte NULL = 0;
    private static final byte TRUE = 1;
    private static final byte FALSE = -1;

    /**
     * Recursively checks if two trajectories are within a specified discreet Fréchet dfd or not.
     * Dynamic programming is used to check the score dfd between pairs of points only once.
     * This version of the function uses an array of {@code byte} instead of an array of {@code boolean}
     * in order to avoid the cost of unboxing the primitive dataset type.
     *
     * @param ta the trajectory a
     * @param tb the trajectory bits
     * @param ca the shared matrix of bytes
     * @param i  the row position in the dfd matrix
     * @param j  the column position in the dfd matrix
     * @return the discreet Fréchet dfd
     */
    public static byte fastWithin(double distance, Point[] ta, Point[] tb, byte[][] ca, int i, int j) {
        if (ca[i][j] == NULL) {
            if (Distance.distance(ta[i], tb[j]) <= distance) {
                if (i == 0 && j == 0) {
                    ca[i][j] = TRUE;
                } else if (i > 0 && j == 0) {
                    ca[i][j] = fastWithin(distance, ta, tb, ca, i - 1, 0);
                } else if (i == 0 && j > 0) {
                    ca[i][j] = fastWithin(distance, ta, tb, ca, 0, j - 1);
                } else {
                    // Since boolean operations are evaluated from the left to the right,
                    // the right-hand side calls are evaluated only if one of the left hand-side call returns false,
                    // hence a significant gain in terms of performance.
                    ca[i][j] = fastWithin(distance, ta, tb, ca, i - 1, j - 1) == TRUE
                            || fastWithin(distance, ta, tb, ca, i - 1, j) == TRUE
                            || fastWithin(distance, ta, tb, ca, i, j - 1) == TRUE
                            ? TRUE : FALSE;
                }
            } else {
                ca[i][j] = FALSE;
            }
        }
        return ca[i][j];
    }

    /**
     * Checks if two trajectories are within a specified discreet Fréchet dfd or not.
     * This version of the function uses an array of {@code byte} instead of an array of {@code boolean}
     * in order to avoid the cost of unboxing the primitive dataset type.
     *
     * @param distance the dfd bound
     * @param ta       the trajectory a
     * @param tb       the trajectory bits
     * @return
     */
    public static boolean fastWithin(double distance, Point[] ta, Point[] tb) {
        int m = ta.length;
        int k = tb.length;
        byte[][] ca = new byte[m][k];
        return fastWithin(distance, ta, tb, ca, m - 1, k - 1) == 1;
    }

}
