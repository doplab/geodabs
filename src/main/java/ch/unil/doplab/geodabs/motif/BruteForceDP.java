package ch.unil.doplab.geodabs.motif;

import ch.unil.doplab.geodabs.distance.DFD;
import ch.unil.doplab.geodabs.distance.Distance;
import ch.unil.doplab.geodabs.model.Point;

public class BruteForceDP {

    public static MotifPair execute(Point[] ta, Point[] tb, int e) {
        final int s = ta.length;
        final int t = tb.length;

        double[][] dG = new double[s][t];
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < t; j++) {
                dG[i][j] = Distance.distance(ta[i], tb[j]);
            }
        }

        double[][] dF = new double[s][t];

        double bsf = Double.MAX_VALUE;
        MotifPair bpair = null;

        for (int i = 0; i < s - e + 1; i++) {
            for (int j = 0; j < t - e + 1; j++) {

                dF[i][j] = dG[i][j];
                for (int k = j + 1; k < t; k++) {
                    dF[i][k] = DFD.max(dG[i][k], dF[i][k - 1]);
                }
                for (int k = i + 1; k < s; k++) {
                    dF[k][j] = DFD.max(dG[k][j], dF[k - 1][j]);
                }

                for (int ie = i + 1; ie < s; ie++) {
                    for (int je = j + 1; je < t; je++) {
                        double tmp = DFD.min(dF[ie - 1][je - 1], DFD.min(dF[ie][je - 1], dF[ie - 1][je]));
                        dF[ie][je] = DFD.max(dG[ie][je], tmp);
                        if (ie >= i + e - 1 && je >= j + e - 1 && dF[ie][je] < bsf) {
                            bsf = dF[ie][je];
                            bpair = new MotifPair(i, j, ie, je, bsf);
                        }
                    }
                }
            }
        }

        return bpair;
    }


}
