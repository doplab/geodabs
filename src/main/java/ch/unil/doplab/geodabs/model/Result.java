package ch.unil.doplab.geodabs.model;

public class Result implements Comparable<Result> {

    public final Record record;
    public final double score;

    public Result(Record record, double score) {
        this.record = record;
        this.score = score;
    }

    @Override
    public int compareTo(Result o) {
        return Double.compare(score, o.score);
    }

}
