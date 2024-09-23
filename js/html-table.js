var _table_ = document.createElement('table'), _tr_ = document.createElement('tr'), _th_ = document.createElement('th'), _td_ = document.createElement('td');

// Check if a string is a measure
function isMeasure(s) {
    return s.includes("(") && !s.includes("zscore") && !s.includes("surprise");
}

// HTML attributes cannot contain brakets, replace them
function header2att(s) {
    return s.replace("(", "--").replace(")", "-");
}

function att2header(s) {
    return s.replace("--", "(").replace("-", ")");
}

// Builds the HTML Table out of myList json data from Ivy restful service.
function buildHtmlPivot(arr, measures) {
    var table = _table_.cloneNode(false);
    if (typeof arr === 'undefined') { return table; }
    for (var i=0, maxi=arr.length; i < maxi; ++i) {
        var tr = _tr_.cloneNode(false);
        for (var j=0, maxj=arr[i].length; j < maxj; ++j) {
            var jsonObj = arr[i][j];
            var td = _td_.cloneNode(false);
            if (typeof jsonObj["type"] !== 'undefined') {
                td.setAttribute("type", jsonObj["type"]);
                if (jsonObj["type"] === "header") {
                    td.appendChild(document.createTextNode(jsonObj["attribute"]));
                    if (j == 0) { // do not make the "horizontal" members sticky! only make the "vertical" ones (i.e., the one in the columns and not in the rows)
                        td.setAttribute("class", "sticky-col");
                    }
                    if (i == 0) { // do not make the "horizontal" members sticky! only make the "vertical" ones (i.e., the one in the columns and not in the rows)
                        td.setAttribute("class", "sticky-row");
                    }
                } else {
                    var txt = ""
                    measures.forEach(function(m) { txt += jsonObj[m] + "; "; });
                    Object.keys(jsonObj).sort(function(a, b) { return a.localeCompare(b); }).forEach(function(key) {
                        if (key.includes("model")) {
                            td.setAttribute(header2att(key), jsonObj[key]);
                        } else if (key.includes("label")) {
                            td.setAttribute("label", jsonObj[key]);
                        }
                    });
                    td.appendChild(document.createTextNode(txt.substr(0, txt.length - 2))); 
                }
            } else {
                td.appendChild(document.createTextNode(""));
                if (i == 0 && j == 0) {
                    td.setAttribute("class", "sticky-top");
                }
            }
            // td.onclick = function(e) { update(e.target.getAttribute("content")); };
            tr.appendChild(td);
        }
        table.appendChild(tr);
    }
    return table;
}

function componentToString(model, component) {
    if (model.includes("clustering")) {
        return "cluster " + component;
    } else if (model.includes("label")) {
        return component;
    } else {
        var text = model.replace("model_", "").replace("_", " ");
        if (component == "True" || component == "true") {
            return text;
        } else {
            return "not " + text;
        }
    }
}

// Builds the HTML Table out of myList json data from Ivy restful service.
function buildHtmlTable(arr, behavior) {
    var table = _table_.cloneNode(false);
    if (typeof arr === 'undefined') { return table; }
    var columns = addAllColumnHeaders(arr, table);
    for (var i=0, maxi=arr.length; i < maxi; ++i) {
        var tr = _tr_.cloneNode(false);
        var jsonObj = arr[i];
        
        for (var j=0, maxj=columns.length; j < maxj ; ++j) {
            var td = _td_.cloneNode(false);
            // console.log(columns[j]);
            if (columns[j] == "component" || columns[j] == "label") {
                var input = (jsonObj["component"] ? jsonObj["component"] : jsonObj["label"]).split("=");
                var model = input[0];
                var component = input[1];
                td.appendChild(document.createTextNode(componentToString(model, component)));
            } else {
                td.appendChild(document.createTextNode(
                    JSON.stringify(arr[i][columns[j]]).replace(/"|\(|\)|{|}/gi, function(x) { return '' }).replace(/,|:/gi, function(x) { return ' = ' })
                ));            }
            Object.keys(jsonObj).forEach(function(key) {
                if (behavior == "raw") {
                    if (key.includes("model")) {
                        td.setAttribute(header2att(key), jsonObj[key]);
                        if (key == columns[j]) {
                            td.setAttribute("content", header2att(key) + "=" + jsonObj[key]);
                        }
                    } else if (key.includes("label")) {
                        td.setAttribute("label", jsonObj[key]);
                    }
                } else {
                    var input = (jsonObj["component"] ? jsonObj["component"] : jsonObj["label"]).split("=");
                    var model = header2att(input[0]);
                    var component = input[1];
                    td.setAttribute(model, component);
                    td.setAttribute("content", model + "=" + component);
                    td.setAttribute("label", component);
                }
                td.onclick = function(e) { update(e.target.getAttribute("content")); };
            });
            tr.appendChild(td);
        }
        table.appendChild(tr);
    }
    return table;
}
    
// Adds a header row to the table and returns the set of columns.
// Need to do union of keys from all records as some records may not contain
// all records
function addAllColumnHeaders(arr, table) {
    var columnSet = [], tr = _tr_.cloneNode(false);
    for (var i=0, l=arr.length; i < l; i++) {
        for (var key in arr[i]) {
            if (arr[i].hasOwnProperty(key) && columnSet.indexOf(key)===-1) {
                columnSet.push(key);
                var th = _th_.cloneNode(false);
                th.appendChild(document.createTextNode(key));
                tr.appendChild(th);
            }
        }
    }
    table.appendChild(tr);
    return columnSet;
}

function buildKeyValueTable(obj) {
    var table = _table_.cloneNode(false);
    Object.keys(obj).forEach(function(key) {
        var tr = _tr_.cloneNode(false);
        var td = _td_.cloneNode(false);
        td.appendChild(document.createTextNode(key));
        tr.appendChild(td);
        var td = _td_.cloneNode(false);
        td.appendChild(document.createTextNode(obj[key]));
        tr.appendChild(td);
        table.appendChild(tr);
    });
    return table;
}
    