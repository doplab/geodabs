package ch.unil.doplab.geodabs.distance;

import ch.unil.doplab.geodabs.index.Fingerprint;
import org.roaringbitmap.RoaringBitmap;

import java.util.HashSet;
import java.util.Set;

public class JaccardDistance {

    public static double distance(RoaringBitmap a, RoaringBitmap b) {
        double intersection = RoaringBitmap.andCardinality(a, b);
        double union = RoaringBitmap.orCardinality(a, b);
        return 1 - (intersection / union);
    }

    public static double distance(Set<Fingerprint> a, Set<Fingerprint> b) {
        HashSet<Fingerprint> intersection = new HashSet<>();
        intersection.addAll(a);
        intersection.retainAll(b);
        HashSet<Fingerprint> union = new HashSet<>();
        union.addAll(a);
        union.addAll(b);
        double i = intersection.size();
        double u = union.size();
        return 1 - (i / u);
    }

}
