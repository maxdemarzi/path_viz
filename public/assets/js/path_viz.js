function addNeo(graph, data) {
    function addNode(id, label) {
        if (!id || typeof id == "undefined") return null;
        var node = graph.getNode(id);
        if (!node) node = graph.addNode(id, label);
        return node;
    }

    for (n in data.edges) {
        if (data.edges[n].source) {
            console.log(data.edges[n].source);
            console.log(data.edges[n].source_data);
            addNode(data.edges[n].source, data.edges[n].source_data );
        }
        if (data.edges[n].target) {
            console.log(data.edges[n].target);
            console.log(data.edges[n].target_data);
            addNode(data.edges[n].target, data.edges[n].target_data);
        }
    }

    for (n in data.edges) {
        var edge=data.edges[n];
        var found=false;
        graph.forEachLinkedNode(edge.source, function (node, link) {
            if (node.id==edge.target) found=true;
        });
        if (!found && edge.source && edge.target) graph.addLink(edge.source, edge.target);
    }
}

function loadData(graph,id) {
    console.log("in loadData");
    console.log(id);
    $.ajax(id ? "/edges/" + id : "/edges", {
        type:"GET",
        dataType:"json",
        success:function (res) {
            addNeo(graph, {edges:res});
        }
    })
}
var graph = Viva.Graph.graph();

function onLoad() {
    // Step 1. Create a graph:
    //var graph = Viva.Graph.graph();

    var layout = Viva.Graph.Layout.forceDirected(graph, {
        springLength:75,
        springCoeff:0.0005,
        dragCoeff:0.02,
        gravity:-1.2
    });


    var graphics = Viva.Graph.View.svgGraphics(),
        nodeSize = 24,
        // we use this method to highlight all related links
        // when user hovers mouse over a node:
        highlightRelatedNodes = function(nodeId, isOn) {
            // just enumerate all realted nodes and update link color:
            graph.forEachLinkedNode(nodeId, function(node, link){
                var linkUI = graphics.getLinkUI(link.id);
                if (linkUI) {
                    // linkUI is a UI object created by graphics below
                    linkUI.attr('stroke', isOn ? 'red' : 'gray');
                }
            });
        };

    graphics.node(function(node) {
        var ui = Viva.Graph.svg('g'),
            svgText = Viva.Graph.svg('text').attr('y', '-4px').text(node.data.text),
            img = Viva.Graph.svg('image')
                .attr('width', 32)
                .attr('height', 32)
                .link('/assets/img/' + node.data.label + '.png');

        ui.append(svgText);
        ui.append(img);

        $(ui).hover(function() { // mouse over
            highlightRelatedNodes(node.id, true);
            $('#explanation').html("<h2>" + node.data.text + "</h2>");
            $.each(node.data, function(key, value){
                if(key != "text") {
                    $('#explanation').append("<p>" + key + " : " + value + "</p>");
                }
            });

            //$('#explanation').html(node.data.username ? : node.data.status);
        }, function() { // mouse out
            highlightRelatedNodes(node.id, false);
        });
        $(ui).click(function(){
            renderer.rerender();
            loadData(graph,node.id);
        });
        return ui;
    }).placeNode(function(nodeUI, pos) {
        nodeUI.attr('transform',
            'translate(' +
            (pos.x - 16) + ',' + (pos.y - 16) +
            ')');
    });

    graphics.link(function(link){
        return Viva.Graph.svg('path')
            .attr('stroke', 'gray');
    }).placeLink(function(linkUI, fromPos, toPos) {
        var data = 'M' + fromPos.x + ',' + fromPos.y +
            'L' + toPos.x + ',' + toPos.y;

        linkUI.attr("d", data);
    })

    // Finally render the graph with our customized graphics object:
    var renderer = Viva.Graph.View.renderer(graph, {
        graphics : graphics,
        container:document.getElementById('graph'),
        renderLinks:true

    });
    renderer.run();

    var neoid = window.location.search.substring(1).split("=")[1];
    if ( neoid == "") {
        neoid = document.getElementById("neoid").value;
    };
    if ( neoid == "") {
        neoid = 1;
    };

    loadData(graph,neoid);

}