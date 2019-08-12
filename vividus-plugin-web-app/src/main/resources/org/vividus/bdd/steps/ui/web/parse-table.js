var table = arguments[0];
var columnNames = Array.prototype.slice.call(table.querySelectorAll('th')).map(
        function(element) {
            return element.textContent
        })
var columns = columnNames.map(
        function(columnName) {
            return document.evaluate(
                    '//td[position() = count(//tr/th[following-sibling::th[contains(text(),"'
                            + columnName + '")]]) + 1]', table, null,
                    XPathResult.ORDERED_NODE_ITERATOR_TYPE, null)
        }).map(function(columnIterator) {
    var cells = [];
    var cell = columnIterator.iterateNext();
    while (cell) {
        var cellText = cell.textContent;
        if (cellText.match(/^(\d+(,|\.)?)+(?<!%)$/g)) {
            cellText = parseInt(cellText.replace(/[,.]/g, ''))
        }
        cells.push(cellText);
        cell = columnIterator.iterateNext();
    }
    return cells;
})

var rows = [];
while (columns.every(function(column) {return column.length})) {
    var row = {};
    columnNames.forEach(function(columnName, index) {
        var item = row[columnName] = columns[index].shift()
    })
    rows.push(row);
}
return rows;