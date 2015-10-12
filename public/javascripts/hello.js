var $success =$("#success");
$success.hide();

$.get("/inputLines", function (dataSol) {
    //console.log(dataSol);
    if (dataSol.head > -1) {
        $("#parentDiv").hide();

        // Fill dialog with solution ranges.
        {
        var text = "";
        for (var i = 0, len = dataSol.maxUsageList.length; i < len; i++) {
            text += dataSol.maxUsageList[i] + ";";
        }
        $("#successTA").val(text);
        $success.dialog({
            resizable: false,
            dialogClass: 'no-close success-dialog',
            buttons: [{
                    text: "OK",
                    click: function() {
                        $(this).dialog( "close" );
                    }
            }]
        });
        $success.show();
        }

        // Draw graph
        {
        var margin = {top: 20, right: 20, bottom: 30, left: 50},
            width = document.getElementById("middle").offsetWidth - margin.left - margin.right,
            height = document.getElementById("middle").offsetHeight - margin.top - margin.bottom;

        var x = d3.scale.linear()
            .range([0, width]);

        var y = d3.scale.linear()
            .range([height, 0]);

        var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom");

        var yAxis = d3.svg.axis()
            .scale(y)
            .orient("left")
            .tickFormat(function(d) {
                if (d % 1 == 0) {
                    return d3.format('.f')(d)
                }
            });

        var line = d3.svg.line()
            .x(function(d) { return x(d.date); })
            .y(function(d) { return y(d.close); });

        var svg = d3.select("#graph").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        var data = [];
        var count = dataSol.head;
        dataSol.solY.forEach(function() {
            data.push({ date: count, close: dataSol.solY[count - dataSol.head] });
            count++;
        });

        x.domain(d3.extent(data, function(d) { return d.date; }));
        y.domain([0, d3.max(data, function (d) { return d.close; })]);

        svg.append("path")
            .datum(data)
            .attr("class", "line")
            .attr("d", line);

        svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis)
            .append("text")
            .attr("class", "x label")
            .attr("text-anchor", "end")
            .attr("x", width - 6)
            .attr("y", -6)
            .text("Time");

        svg.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .append("text")
            .attr("class", "y label")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text("Usage (number of users)");
        }
    }
});
