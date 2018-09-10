package ch.unil.doplab.geodabs.model;

import java.nio.file.Path;

public class Record {

    public final Path path;
    public final Trajectory trajectory;
    private final int hashCode;

    public Record(Path path, Trajectory trajectory) {
        this.path = path;
        this.trajectory = trajectory;
        this.hashCode = path.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

}
