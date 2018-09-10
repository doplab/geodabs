package ch.unil.doplab.geodabs.model;

import java.util.List;

public class Response {

    public final Query query;
    public final List<Result> results;

    public Response(Query query, List<Result> results) {
        this.query = query;
        this.results = results;
    }

}
