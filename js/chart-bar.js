function drawBar(id, prop, data, d1, d2, c, mode, selectedModel, selectedComponent, highlightColor) {
    // https://bl.ocks.org/tlfrd/187e45e0629711c4560cf6bcd0767b27
    let rawData = data["raw"].sort(function (a, b) { return b[d2] - a[d2] })
    let miny = 0, maxy = 0;
    let M = new Set();
    let count = 0
    for (var d in rawData) {
        count += 1
        d = rawData[d];
        miny = Math.min(miny, d[d2]);
        maxy = Math.max(maxy, d[d2]);
        M.add(d[c]);
    }

    M = Array.from(M);
    // set the dimensions and margins of the graph
    let margin = prop.margin, width = prop.width, height = prop.height;
    height = Math.max(height, count * 10);
    let div = d3.select("#" + id);
    let svg = div
        .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
        .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    var tooltip = createTooltip(div);
    // Add X axis
    var x = d3.scaleLinear().domain([miny, maxy]).range([0, width]);
    // Y axis
    var y = d3.scaleBand().range([0, height]).domain(rawData.map(function(d) { return d[d1]; })).padding(.1);
    //Bars
    svg.selectAll("myRect")
        .data(rawData)
        .enter()
        .append("rect")
        .attr("x", function (d) {
            return x(Math.min(0, d[d2]));
        })
        .attr("y", function (d) {
            return y(d[d1]);
        })
        .attr("width", function (d) {
            return Math.abs(x(d[d2]) - x(0));
        })
        .attr("height", y.bandwidth())
        .attr("datapoint", "colored")
        .on("mouseover",  function f (d) { showTooltip(data, d, tooltip) })
        .on("mousemove",  function f (d) { moveTooltip(d, tooltip) })
        .on("mouseleave", function f (d) { hideTooltip(d, tooltip) });

    appendXaxis(svg, x, height);
    appendXlabel(svg, d2, width, height);
    // appendYaxis(svg, y, x);
    appendYlabel(svg, d1, width, height, x);

    svg.append("g")
        .attr("class", "axis")
        .attr("transform", "translate(" + x(0) + ",0)")
        .call(d3.axisLeft(y).tickFormat("")) //.ticks(null, "s"));

    var yAxis = svg.append("g")
        .attr("class", "axis")
        .attr("transform", "translate(" + x(0) + ",0)")
        .append("line")
        .attr("y1", 0)
        .attr("y2", height);

    var cfg = {
        labelMargin: 5,
        xAxisMargin: 10,
        legendRightMargin: 0
    }
    const labels = svg.append("g").attr("class", "labels");
    labels
        .selectAll("text")
        .data(rawData)
        .enter()
        .append("text")
        .attr("class", "axis")
        .attr("x", x(0))
        .attr("y", function(d) { return y(d[d1])})
        .attr("dx", function(d) {
            return d[d2] < 0 ? cfg.labelMargin : -cfg.labelMargin;
        })
        .attr("dy", y.bandwidth())
        .attr("text-anchor", function(d) {
            return d[d2] < 0 ? "start" : "end";
        })
        .text(function(d) { return d[d1] })
        .style("font-size", "10px");
    // appendLegend(data, svg, M, 20, -40, color);
    return [height, width]
}