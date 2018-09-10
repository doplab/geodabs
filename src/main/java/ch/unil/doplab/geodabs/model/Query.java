package ch.unil.doplab.geodabs.model;

public class Query {

    public final Record record;
    public final double distance;

    public Query(Record record, double score) {
        this.record = record;
        this.distance = score;
    }

}
