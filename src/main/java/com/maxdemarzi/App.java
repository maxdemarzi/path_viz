package com.maxdemarzi;

import com.typesafe.config.Config;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.json.Jackson;
import org.jooby.rocker.Rockerby;
import org.jooby.whoops.Whoops;

import java.util.*;

import static com.maxdemarzi.Cypher.params;

public class App extends Jooby {
    private static ArrayList<String> keywords;
    {

    // Debug friendly error messages
    on("dev", () -> use(new Whoops()));

    // Configure Jackson
    use(new Jackson().doWith(mapper -> {
      mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
    }));

    // Setup Template Engine
    use(new Rockerby());

    use(new Neo4j());

    // Setup Keywords
    onStart(registry -> {
            Config conf = require(Config.class);
            keywords = new ArrayList<>(conf.getStringList("viz.display_properties"));
     });

    assets("/assets/**");

    get("/", req -> views.index.template(req.param("neoid").longValue(1)));

    get("/search", req -> {
        Config conf = require(Config.class);
        Cypher cypher = require(Cypher.class);
        String query =
                "MATCH (u:" + conf.getString("viz.search_label") +  ") " +
                "WHERE u." + conf.getString("viz.search_property") +  " CONTAINS {term} " +
                "RETURN u." + conf.getString("viz.search_property") +  " AS label, ID(u) AS value " +
                "ORDER BY u." + conf.getString("viz.search_property") + " " +
                "LIMIT 15";

        return cypher.query(query, params("term", req.param("term").value()));

    }).produces("json");

    // Get a random set of Nodes
    get("/edges/", req -> {
        Cypher cypher = require(Cypher.class);
        String query =
                "MATCH (me)--(target) " +
                "RETURN ID(me) AS source, LABELS(me)[0] AS source_label, me AS source_data, " +
                "ID(target) AS target, LABELS(target)[0] AS target_label, target AS target_data " +
                "LIMIT 10";

        ArrayList<HashMap<String, Object>> results = prepareResults(req, cypher, query);
        return results;
    });

    // Get the neighborhood of a particular Node
    get("/edges/{id}", req -> {
        Cypher cypher = require(Cypher.class);
        String query =
                "MATCH (me)--(target) " +
                "WHERE ID(me) = {id} " +
                    "RETURN ID(me) AS source, LABELS(me)[0] AS source_label, me AS source_data, " +
                    "ID(target) AS target, LABELS(target)[0] AS target_label, target AS target_data";

        ArrayList<HashMap<String, Object>> results = prepareResults(req, cypher, query);
        return results;
    });

  }

    private ArrayList<HashMap<String, Object>> prepareResults(Request req, Cypher cypher, String query) {
        ArrayList<HashMap<String, Object>> results = new ArrayList<>();
        Iterator<Map<String,Object>> related = cypher.query(query, params("id", req.param("id").longValue(1)));
        while (related.hasNext()) {
            HashMap<String, Object> result = new HashMap<>();
            Map<String, Object> row = related.next();

            HashMap<String, Object> source_data = new HashMap<>();
            for(Map.Entry<String, Object> entry : ((Map<String, Object>)row.get("source_data")).entrySet()) {
                source_data.put(entry.getKey(), entry.getValue());
            }
            source_data.put("label", row.get("source_label"));
            addDisplayProperty(source_data);


            HashMap<String, Object> target_data = new HashMap<>();
            for(Map.Entry<String, Object> entry : ((Map<String, Object>)row.get("target_data")).entrySet()) {
                target_data.put(entry.getKey(), entry.getValue());
            }
            target_data.put("label", row.get("target_label"));
            addDisplayProperty(target_data);

            result.put("source", row.get("source"));
            result.put("source_data", source_data);
            result.put("target", row.get("target"));
            result.put("target_data", target_data);
            results.add(result);
        }
        return results;
    }

    // From the list of possible display properties, add it to text if it finds it otherwise use the label.
    private  void addDisplayProperty(HashMap<String, Object> source_data) {
        source_data.put("text", source_data.get("label"));
        for (String key : keywords) {
            if (source_data.containsKey(key)) {
                source_data.put("text", source_data.get(key));
                break;
            }
        }
    }

    public static void main(final String[] args) {
    run(App::new, args);
  }

}
