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
    currentWindow.addEventListener('scroll', cleanTimeoutAndWait, false);
}
catch(e) {
    // swallow error quietly
}

function wait() {
waitForScroll = setTimeout(() => {
        currentWindow.removeEventListener('scroll', cleanTimeoutAndWait);
        exit(true);
    }, 50);
}

function cleanTimeoutAndWait(event) {
    currentWindow.clearTimeout(waitForScroll);
    wait();
}

wait();
