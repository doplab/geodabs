package ch.unil.doplab.geodabs.index;

import ch.unil.doplab.geodabs.model.Query;
import ch.unil.doplab.geodabs.model.Record;
import ch.unil.doplab.geodabs.model.Response;

import java.util.List;

public interface TrajectoryIndex {

    void add(List<Record> records);

    Response query(Query query);

}
