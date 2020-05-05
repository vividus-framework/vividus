var elementToScroll = arguments[0];
var exit = arguments[arguments.length-1];
(scrollElementIntoViewportCenter = function() {
    var currentWindow = window;
    try {
        // Check, if we have an access to top level document
        window.top.document;
        while (currentWindow !== window.top) {
            elementToScroll = currentWindow.frameElement;
            currentWindow = currentWindow.parent;
        }
        currentWindow.scrollBy(0, elementToScroll.getBoundingClientRect().top - currentWindow.innerHeight * 0.25);
        setTimeout(scrollElementIntoViewportCenter, 500);
    }
    catch(e) {
        // swallow error quietly
    }
    exit();
}) ();

