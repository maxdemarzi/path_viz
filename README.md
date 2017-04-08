# Path Viz

POC Visualizing Neo4j


Instructions
-------------

1. Start Neo4j, login, create some data
2. Edit "application.conf" properties with your Neo4j settings:

        DBHOST = localhost
        neo4j.url = "bolt://"${DBHOST}":7687"
        neo4j.username = "neo4j"
        neo4j.password = "swordfish"
        
3. Edit "application.conf" properties with your visualization settings:

        viz.display_properties = ["username","name", "title", "status"]
        viz.search_label="User"
        viz.search_property="username"
        
* display_properties is a list of node properties to try to use as the text label of the nodes.
* search_label is the Label of the type of node searched in the autocomplete query
* search_property is the property to consider in the autocomplete search query        

4. Run mvn jooby:run and visit http://localhost:8080

It will try to use an icon based on the Label of the node from public/assets/img/
You can add icons here or copy them to match your labels.