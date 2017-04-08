package com.maxdemarzi;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Cypher {

    private static org.neo4j.driver.v1.Driver driver;

    public Cypher(com.typesafe.config.Config config) {
        driver = GraphDatabase.driver( config.getString("neo4j.url"), AuthTokens.basic( config.getString("neo4j.username"), config.getString("neo4j.password") ) );
    }

    public Iterator<Map<String, Object>> query(String query, Map<String, Object> params) {
        try (Session session = driver.session()) {
            List<Map<String, Object>> list = session.run(query, params)
                    .list( r -> r.asMap(Cypher::convert));
            return list.iterator();
        }
    }

    static Object convert(Value value) {
        switch (value.type().name()) {
            case "PATH":
                return value.asList(Cypher::convert);
            case "NODE":
            case "RELATIONSHIP":
                return value.asMap();
        }
        return value.asObject();
    }

    public static <K, V> Map<K, V> genericMap( Object... objects )
    {
        return genericMap( new HashMap<K, V>(), objects );
    }

    public static <K, V> Map<K, V> genericMap( Map<K, V> targetMap, Object... objects )
    {
        int i = 0;
        while ( i < objects.length )
        {
            targetMap.put( (K) objects[i++], (V) objects[i++] );
        }
        return targetMap;
    }

    public static Map<String, Object> params( Object... objects )
    {
        return genericMap( objects );
    }



}
