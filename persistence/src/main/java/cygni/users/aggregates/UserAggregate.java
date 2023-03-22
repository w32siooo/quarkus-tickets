package cygni.users.aggregates;

import cygni.es.AggregateRoot;
import cygni.es.Event;
import lombok.Getter;

import java.util.List;

public class UserAggregate extends AggregateRoot {
    public static final String AGGREGATE_TYPE = "User";

    private String name;

    private long balance;

    private List<String> ownedExperiences;

    @Override
    public void when(Event event) {


    }


}
