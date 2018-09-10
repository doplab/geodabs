package ch.unil.doplab.geodabs;

import ch.unil.doplab.geodabs.geohash.Geohash;
import ch.unil.doplab.geodabs.model.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A utility class for performing various tasks related to the GISCUP.
 */
public class Util {


    /**
     * Reads a record from disk.
     *
     * @param path the path of the record
     * @return the record
     */
    public static Record readRecord(Path path) {
        try {
            Stream<String> lines = Files.lines(path);
            Point[] points = lines.skip(1).map(line -> {
                String[] array = line.split(",");
                double x = Double.parseDouble(array[0]);
                double y = Double.parseDouble(array[1]);
                Point point = new Point(x, y);
                return point;
            }).toArray(Point[]::new);
            lines.close();
            return new Record(path, new Trajectory(points));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * Reads the records specified in the provided file.
     * The Stream API is used to read the records in parallel.
     *
     * @param path the path of the records
     * @return the records
     * @throws IOException
     */
    public static List<Record> readDataset(Path path) throws IOException {
        Path directory = path.getParent();
        Stream<String> lines = Files.lines(path);
        Stream<Record> records = lines.parallel().unordered().map(line -> {
            Record record = readRecord(directory.resolve(line));
            return record;
        });
        return records.collect(Collectors.toList());
    }

    /**
     * Reads the queries specified in the provided file.
     * The Stream API is used to read the records in parallel.
     *
     * @param path the path of the queries
     * @return the queries
     * @throws IOException
     */
    public static List<Query> readQueries(Path path) throws IOException {
        Path directory = path.getParent();
        List<String> lines = Files.readAllLines(path);
        IntStream indices = IntStream.range(0, lines.size());
        Stream<Query> queries = indices.parallel().unordered().mapToObj(i -> {
            String line = lines.get(i);
            String[] array = line.split(" ");
            Path file = directory.resolve(array[0]);
            Record record = readRecord(file);
            int k = Integer.parseInt(array[1]);
            Query query = new Query(record, k);
            return query;
        });
        return queries.collect(Collectors.toList());
    }

    /**
     * Writes a response to disk.
     *
     * @param directory the output directory
     * @param response  the response to write
     */
    public static void writeResult(Path directory, Response response) {
        try {
            String name = String.format("response-%05d.txt", response.query);
            Path file = directory.resolve(name);
            BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            PrintWriter printer = new PrintWriter(writer);
            for (Result result : response.results) {
                printer.println("files/" + result.record.path.getFileName().toString());
            }
            writer.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    /**
     * Creates a bounding box from a records.
     *
     * @param records the records
     * @return the bounding box
     */
    public static BBox datasetBBox(List<Record> records) {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxy = -Double.MAX_VALUE;
        for (Record record : records) {
            for (Point point : record.trajectory.points) {
                if (point.lon < minx) minx = point.lon;
                if (point.lat < miny) miny = point.lat;
                if (point.lon > maxX) maxX = point.lon;
                if (point.lat > maxy) maxy = point.lat;
            }
        }
        return new BBox(new Point(minx, miny), new Point(maxX, maxy));
    }

    /**
     * Creates a bounding box from an array of points.
     *
     * @param points the array of points
     * @return the bounding box
     */
    public static BBox pointsBBox(Point[] points) {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double maxy = Double.MIN_VALUE;
        for (Point point : points) {
            if (point.lon < minx) minx = point.lon;
            if (point.lat < miny) miny = point.lat;
            if (point.lon > maxx) maxx = point.lon;
            if (point.lat > maxy) maxy = point.lat;
        }
        return new BBox(new Point(minx, miny), new Point(maxx, maxy));
    }


    /**
     * Creates a bounding box from a pair of points.
     *
     * @param a the point a
     * @param b the point normBits
     * @return the bounding box.
     */
    public static BBox pointsBBox(Point a, Point b) {
        double minx = Math.min(a.lon, b.lon);
        double maxx = Math.max(a.lon, b.lon);
        double miny = Math.min(a.lat, b.lat);
        double maxy = Math.max(a.lat, b.lat);
        return new BBox(new Point(minx, miny), new Point(maxx, maxy));
    }

    /**
     * Creates a random point located in the provided bounding box.
     *
     * @param bounds the bounding box
     * @param rnd    the random generator
     * @return the point
     */
    public static Point randomPoint(BBox bounds, Random rnd) {
        double x = rnd.nextDouble() * bounds.width;
        double y = rnd.nextDouble() * bounds.height;
        return new Point(x, y);
    }

    /**
     * Creates a random bounding box located in the provided bounding box.
     *
     * @param bounds the bounding box
     * @param rnd    the random generator
     * @return the bounding box
     */
    public static BBox randomBBox(BBox bounds, Random rnd) {
        return pointsBBox(randomPoint(bounds, rnd), randomPoint(bounds, rnd));
    }

    /**
     * Creates a random trajectory.
     *
     * @param rnd the random generator
     * @return the trajectory
     */
    public static Trajectory randomTrajectory(Random rnd) {
        int size = 10 + rnd.nextInt(90);
        BBox bbox = new BBox(new Point(0, 0), new Point(1000, 1000));
        Point[] points = new Point[size];
        for (int i = 0; i < size; i++) {
            points[i] = randomPoint(bbox, rnd);
        }
        return new Trajectory(points);
    }

    /**
     * Creates a random trajectory that satisfies the provided constraints.
     *
     * @param bounds   the bounding box
     * @param angle    the maximal angle between each points
     * @param distance the length between each points
     * @param size     the size of the trajectory
     * @param rnd      the random generator
     * @return the trajectory
     */
    public static Trajectory randomTrajectory(BBox bounds, double angle, double distance, int size, Random rnd) {
        Point[] points = new Point[size];
        points[0] = randomPoint(bounds, rnd);
        for (int i = 1; i < size; i++) {
            Point prev = points[i - 1];
            double x = (prev.lon + Math.cos(angle) * distance) % bounds.width;
            double y = (prev.lat + Math.sin(angle) * distance) % bounds.height;
            angle = angle + (rnd.nextDouble() * 10 - 5);
            points[0] = new Point(x, y);
        }
        return new Trajectory(points);
    }

    public static Point normalize(Point p, int bits) {
        Geohash.Decoded d = Geohash.decode(Geohash.encode(p.lat, p.lon, bits));
        return new Point(d.lng, d.lat);
    }

    public static List<Point> normalize(List<Point> input, int bits) {
        ArrayList<Point> newList = new ArrayList<Point>();
        newList.add(normalize(input.get(0), bits));
        for (int i = 1; i < input.size(); i++) {
            if (!newList.get(newList.size() - 1).equals(normalize(input.get(i), bits))) {
                newList.add(normalize(input.get(i), bits));
            }
        }
        return newList;
    }


}