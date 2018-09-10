package ch.unil.doplab.geodabs.motif;

import ch.unil.doplab.geodabs.distance.DFD;
import ch.unil.doplab.geodabs.model.Point;

import static ch.unil.doplab.geodabs.distance.DFD.distance;

import java.util.Arrays;

public class BruteForce {

    public static MotifPair execute(Point[] ta, Point[] tb, int e) {
        final int s = ta.length;
        final int t = tb.length;

        double bsf = Double.MAX_VALUE;
        MotifPair bpair = null;

        for (int i = 0; i <= s - e; i++) {
            for (int j = 0; j <= t - e; j++) {
                for (int ie = i + e; ie <= s; ie++) {
                    for (int je = j + e; je <= t; je++) {
                        Point[] ps = Arrays.copyOfRange(ta, i, ie);
                        Point[] qs = Arrays.copyOfRange(tb, j, je);
                        double d = DFD.distance(ps, qs);
                        if (d < bsf) {
                            bsf = d;
                            bpair = new MotifPair(i, j, ie, je, bsf);
                        }
                    }
                }
            }
        }

        return bpair;
    }

}
