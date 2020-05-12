var elementToScroll = arguments[0];
var exit = arguments[arguments.length-1];
var waitForScroll;
var currentWindow = window;

try {
    // Check, if we have an access to top level document
    window.top.document;
    while (currentWindow !== window.top) {
        elementToScroll = currentWindow.frameElement;
        currentWindow = currentWindow.parent;
    }
    currentWindow.addEventListener('scroll', clearTimeoutAndWait, false);
    currentWindow.scrollBy(0, elementToScroll.getBoundingClientRect().top - currentWindow.innerHeight * 0.25);
}
catch(e) {
    // swallow error quietly
}

function wait() {
    waitForScroll = setTimeout(function() {
        currentWindow.removeEventListener('scroll', clearTimeoutAndWait);
        exit(true);
    }, 50);
}

function clearTimeoutAndWait(event) {
    currentWindow.clearTimeout(waitForScroll);
    wait();
}

wait();
