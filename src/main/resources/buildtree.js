var traverse = require('traverse')
var currentTree = undefined

function buildTree() {
    var input = document.forms['input']['expression'].value
    var xhr = new XMLHttpRequest()
    xhr.open('POST', 'buildtree', false)
    xhr.setRequestHeader("Content-Type", "application/octet-stream")
    xhr.send(input)
    var response = JSON.parse(xhr.responseText)
    traverse(response).forEach(function(x) {
        if (this.key == 'text')
            this.update({'name': x})
    })
    response.collapsed = true
    if (currentTree !== undefined)
        currentTree.destroy()
    currentTree = new Treant({
        chart: {
            container: "#parse-tree",
            rootOrientation: "WEST",
            connectors: {
                type: "step"
            },
            node: {
                collapsable: true
            }
        },
        nodeStructure: response
    })
}
