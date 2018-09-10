package ch.unil.doplab.geodabs.index;

import ch.unil.doplab.geodabs.distance.JaccardDistance;
import ch.unil.doplab.geodabs.geohash.Geohash;
import ch.unil.doplab.geodabs.model.*;
import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;

public class GeohashIndex implements TrajectoryIndex {

    public final int bits;

    public HashMap<Integer, Set<Record>> index = new HashMap<>();
    public HashMap<Record, RoaringBitmap> dataset = new HashMap<>();

    public GeohashIndex(int bits) {
        this.bits = bits;
    }

    @Override
    public void add(List<Record> records) {
        records.forEach(record -> {
            RoaringBitmap fingerprints = extract(Arrays.asList(record.trajectory.points));
            fingerprints.forEach((IntConsumer) f -> {
                if (!index.containsKey(f)) index.put(f, new HashSet<>());
                index.get(f).add(record);
            });
            dataset.put(record, fingerprints);
        });
    }

    @Override
    public Response query(Query query) {
        RoaringBitmap queryFingerprints = extract(Arrays.asList(query.record.trajectory.points));
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
        for (Point p : points) {
            int geohash = (int) (Geohash.encode(p.lat, p.lon, bits) & 0xfffffff);
            rr.add(geohash);
        }
        return rr;
    }

}
