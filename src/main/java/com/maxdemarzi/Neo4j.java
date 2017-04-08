package com.maxdemarzi;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

public class Neo4j implements Jooby.Module {

    @Override
    public void configure(Env env, Config config, Binder binder) throws Throwable {
        Cypher cypher = new Cypher(config);

        binder.bind(Cypher.class).toInstance(cypher);
    }

}
