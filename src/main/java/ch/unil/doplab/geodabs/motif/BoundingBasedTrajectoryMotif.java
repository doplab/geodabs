package ch.unil.doplab.geodabs.motif;

import ch.unil.doplab.geodabs.distance.DFD;
import ch.unil.doplab.geodabs.distance.Distance;
import ch.unil.doplab.geodabs.model.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BoundingBasedTrajectoryMotif {

    public static class CandidateSet implements Comparable<CandidateSet> {

        public final int i;
        public final int j;
        public final double lb;

        public CandidateSet(int i, int j, double lb) {
            this.i = i;
            this.j = j;
            this.lb = lb;
        }

        @Override
        public int compareTo(CandidateSet that) {
            return Double.compare(this.lb, that.lb);
        }

    }

    public static MotifPair execute(final Point[] ta, final Point[] tb, final int epsilon) {
        final int s = ta.length;
        final int t = tb.length;

        double[][] dG = new double[s][t];
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < t; j++) {
                dG[i][j] = Distance.distance(ta[i], tb[j]);
            }
        }

        double[][] dF = new double[s][t];

        double[] cMin = new double[s];
        Arrays.fill(cMin, Double.MAX_VALUE);
        for (int i = 0; i < s - 1; i++) {
            for (int j = 0; j < t; j++) {
                double d = dG[i + 1][j];
                if (d < cMin[i]) cMin[i] = d;
            }
        }

        double[] rMin = new double[t];
        Arrays.fill(rMin, Double.MAX_VALUE);
        for (int i = 0; i < s; i++) {
            for (int j = 0; j < t - 1; j++) {
                double d = dG[i][j + 1];
                if (d < rMin[j]) rMin[j] = d;
            }
        }

        double bsf = Double.MAX_VALUE;
        MotifPair bpair = null;

        // Create the candidate set
        ArrayList<CandidateSet> A = new ArrayList<CandidateSet>();
        for (int i = 0; i < s - epsilon + 1; i++) {
            for (int j = 0; j < t - epsilon + 1; j++) {
                double lbCell = dG[i][j];

                double rlbStartCross = DFD.max(cMin[i], rMin[j]);

                double rlbRowBand = -Double.MAX_VALUE;
                for (int jj = j; jj < j + epsilon - 1; jj++) {
                    if (rMin[jj] > rlbRowBand)
                        rlbRowBand = rMin[jj];
                }

                double rlbColBand = -Double.MAX_VALUE;
                for (int ii = i; ii < i + epsilon - 1; ii++) {
                    if (cMin[ii] > rlbColBand)
                        rlbColBand = cMin[ii];
                }

                double lb = DFD.max(lbCell, DFD.max(rlbStartCross, DFD.max(rlbRowBand, rlbColBand)));
                A.add(new CandidateSet(i, j, lb));
            }
        }
        Collections.sort(A);

        // Find the best motif
        for (CandidateSet a : A) {
            if (bsf <= a.lb) break;

            int iEnd = s;
            int jEnd = t;

            for (int ie = a.i + 1; ie < iEnd; ie++) {
                for (int je = a.j + 1; je < jEnd; je++) {
                    dF[ie][je] = DFD.max(dG[ie][je], DFD.min(dF[ie - 1][je - 1], DFD.min(dF[ie][je - 1], dF[ie - 1][je])));
                    if (ie >= a.i + epsilon - 1 && je >= a.j + epsilon - 1 && dF[ie][je] < bsf) {
                        bsf = dF[ie][je];
                        bpair = new MotifPair(a.i, a.j, ie, je, bsf);
                    }
                }
                if (bpair != null && bsf <= DFD.max(cMin[bpair.ie], rMin[bpair.je])) {
                    iEnd = bpair.ie;
                    jEnd = bpair.je;
                }
            }
        }

        return bpair;
    }


}

