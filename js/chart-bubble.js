function drawBubble(id, prop, data, c1, c2, c3, c4, mode) {
    // set the dimensions and margins of the graph
    let margin = prop.margin, width = prop.width, height = prop.height;
    const rawData = data["raw"]
    let minx = 0, maxx = 0, miny = 0, maxy = 0, minr = Number.MAX_SAFE_INTEGER, maxr = Number.MIN_SAFE_INTEGER;
    let domain = new Set();
    const distinctY = new Set();
    const distinctX = new Set();
    rawData.forEach(function(d) {
        if (mode > 1) { // both are numerical axis
            minx = Math.min(minx, d[c1]);
            maxx = Math.max(maxx, d[c1]);
            miny = Math.min(miny, d[c2]);
            maxy = Math.max(maxy, d[c2]);
        } else { // both are categorical axis
            distinctY.add(d[c2]);
            distinctX.add(d[c1]);
        }
        minr = Math.min(minr, d[c3]);
        maxr = Math.max(maxr, d[c3]);
        domain.add(d[c4]);
    });
    domain = Array.from(domain);
    if (mode == 1) {
        height = Math.max(height, distinctY.size * 10);
        width = Math.max(width, distinctX.size * 10);
    }
    const div = d3.select("#" + id);
    const svg = div
                .append("svg")
                    .attr("width", width + margin.left + margin.right)
                    .attr("height", height + margin.top + margin.bottom)
                .append("g")
                    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    let x;
    let y;
    // Add X axis
    if (mode > 1) {
        x = d3.scaleLinear().domain([minx, maxx]).range([0, width]);
        y = d3.scaleLinear().domain([miny, maxy]).range([height, 0]);
    } else {
        x = d3
            .scalePoint()
            .rangeRound([0, width])
            .padding(0.1)
            .domain(rawData.map(function (d) {
                return d[c1];
            }))

        y = d3
            .scalePoint()
            .rangeRound([0, height])
            .padding(0.1)
            .domain(rawData.map(function (d) {
                return d[c2];
            }))
    }

    // Add a scale for bubble size
    const z = d3.scaleLinear().range([2.5, 10]).domain([minr, maxr]);
    const tooltip = createTooltip(div);
    svg
        .selectAll("dot")
        .data(rawData)
        .enter()
        .append("circle")
            .attr("cx", function (d) { return x(d[c1]); } )
            .attr("cy", function (d) { return y(d[c2]); } )
            .attr("datapoint", "negcolored")
            .attr("r", function (d) {  if (mode > 1 && typeof c3 == "undefined") return 4; else return z(d[c3]); })
            .on("mouseover",  function f (d) { showTooltip(data, d, tooltip) })
            .on("mousemove",  function f (d) { moveTooltip(d, tooltip, svg) })
            .on("mouseleave", function f (d) { hideTooltip(d, tooltip) });

    appendXaxis(svg, x, height);
    appendXlabel(svg, c1, width, height);
    appendYaxis(svg, y, x);
    appendYlabel(svg, c2, width, height, x);
    // appendLegend(data, svg, domain, 20, -40, color);
    return [height, width];
}