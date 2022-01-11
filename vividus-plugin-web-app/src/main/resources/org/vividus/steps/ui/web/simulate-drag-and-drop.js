function simulateDragDrop(sourceNode, destinationNode) {
    var EVENT_TYPES = {
        DRAG_START: 'dragstart',
        MOUSEOVER: 'mouseover',
        DROP: 'drop',
        DRAG_END: 'dragend'
    }

    function createCustomEvent(type) {
        var event = document.createEvent('CustomEvent')
        event.initCustomEvent(type, true, true, null)
        event.dataTransfer = {
            data: {
            },
            setData: function(type, val) {
                this.data[type] = val
            },
            getData: function(type) {
                return this.data[type]
            }
        }
        return event
    }

    function dispatchEvent(node, type, event) {
        if (node.dispatchEvent) {
            return node.dispatchEvent(event)
        }
        if (node.fireEvent) {
            return node.fireEvent('on' + type, event)
        }
    }

    var event = createCustomEvent(EVENT_TYPES.DRAG_START)
    dispatchEvent(sourceNode, EVENT_TYPES.DRAG_START, event)

    var mouseoverEvent = document.createEvent('MouseEvent');
    mouseoverEvent.initMouseEvent(EVENT_TYPES.MOUSEOVER, true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
    dispatchEvent(destinationNode, EVENT_TYPES.MOUSEOVER, mouseoverEvent)

    var dropEvent = createCustomEvent(EVENT_TYPES.DROP)
    dropEvent.dataTransfer = event.dataTransfer
    dispatchEvent(destinationNode, EVENT_TYPES.DROP, dropEvent)

    var dragEndEvent = createCustomEvent(EVENT_TYPES.DRAG_END)
    dragEndEvent.dataTransfer = event.dataTransfer
    dispatchEvent(sourceNode, EVENT_TYPES.DRAG_END, dragEndEvent)
}
simulateDragDrop(arguments[0], arguments[1])
