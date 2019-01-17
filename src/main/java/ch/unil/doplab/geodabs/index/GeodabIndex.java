package ch.unil.doplab.geodabs.index;

import ch.unil.doplab.geodabs.Util;
import ch.unil.doplab.geodabs.distance.JaccardDistance;
import ch.unil.doplab.geodabs.geohash.Geohash;
import ch.unil.doplab.geodabs.model.*;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;

public class GeodabIndex implements TrajectoryIndex {

    public static final HashFunction hashFunction = Hashing.murmur3_32();

    public static final Funnel<Point> funnel = (Funnel<Point>) (point, sink) -> {
        sink.putDouble(point.lon).putDouble(point.lat);
    };

    public final int bits;
    public final int t;
    public final int k;

    public HashMap<Integer, Set<Record>> index = new HashMap<>();
    public HashMap<Record, RoaringBitmap> dataset = new HashMap<>();

    public GeodabIndex(int bits, int t, int k) {
        this.bits = bits;
        this.t = t;
        this.k = k;
    }

    @Override
    public void add(List<Record> records) {
        records.forEach(record -> {
            List<Point> points = Util.normalize(Arrays.asList(record.trajectory.points), bits);
            RoaringBitmap fingerprints = extract(points);
            fingerprints.forEach((IntConsumer) f -> {
                if (!index.containsKey(f)) index.put(f, new HashSet<>());
                index.get(f).add(record);
            });
            dataset.put(record, fingerprints);
        });
    }

    @Override
    public Response query(Query query) {
        List<Point> queryPoints = Util.normalize(Arrays.asList(query.record.trajectory.points), bits);
        RoaringBitmap queryFingerprints = extract(queryPoints);
        Set<Record> records = new HashSet<>();
        List<Result> results = new ArrayList<>();
        queryFingerprints.forEach((IntConsumer) queryFingerprint -> {
            Set<Record> matches = index.getOrDefault(queryFingerprint, new HashSet<>());
            for (Record record : matches) {
                if (!records.contains(record)) {
                    records.add(record);
                    RoaringBitmap resultFingerprints = dataset.get(record);
                    double distance = JaccardDistance.distance(queryFingerprints, resultFingerprints);
                    if (distance <= query.distance) {
                        results.add(new Result(record, distance));
                    }
                }
            }
        });
        Collections.sort(results);
        return new Response(query, results);
    }

    public RoaringBitmap extract(List<Point> points) {
        RoaringBitmap rr = new RoaringBitmap();

        // Build the list of k-gram fingerprints
        ArrayList<Fingerprint> fingerprints = new ArrayList<>();
        for (int i = 0; i <= points.size() - k; i++) {
            Hasher hasher = hashFunction.newHasher();
            double lon = 0;
            double lat = 0;
            for (int j = i; j < i + k; j++) {
                Point p = points.get(j);
                hasher.putObject(p, funnel);
                lat += p.lat;
                lon += p.lon;
            }
            int right = hasher.hash().asInt();
            lat /= k;
            lon /= k;
            int left = (int) (Geohash.encode(lat, lon, 16) & 0xffff);
            int hash = (left << 16) | (right & 0xffff);
            fingerprints.add(new Fingerprint(hash, i));
        }

        // Winnow the list of k-gram fingerprints
        int w = t - k + 1;
        for (int i = 0; i <= fingerprints.size() - w; i++) {
            int m = i;
            for (int j = i + 1; j < i + w; j++) {
                if (fingerprints.get(j).hash < fingerprints.get(m).hash) {
                    m = j;
                }
            }
            rr.add(fingerprints.get(m).hash);
        }

        return rr;
    }

}
