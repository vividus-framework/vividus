let elementToScroll = arguments[0];
const exit = arguments[arguments.length-1];
let waitForScroll;
let currentWindow = window;
const stickyHeaderSize = arguments[1] / 100

try {
    if (isElementInsideOverflowContainer(elementToScroll)) {
        elementToScroll.scrollIntoView(true);
        exit(true);
    }

    // Check, if we have an access to top level document
    window.top.document;
    while (currentWindow !== window.top) {
        elementToScroll = currentWindow.frameElement;
        currentWindow = currentWindow.parent;
    }
    currentWindow.addEventListener('scroll', clearTimeoutAndWait, false);
    currentWindow.scrollBy(0, elementToScroll.getBoundingClientRect().top - currentWindow.scrollY - currentWindow.innerHeight * stickyHeaderSize);
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

function isElementInsideOverflowContainer(element) {
    let container = element.parentElement;
    while (container) {
        const style = window.getComputedStyle(container);
        if (style.overflow !== 'visible' && (style.overflowX !== 'visible' || style.overflowY !== 'visible')) {
            return true;
        }
        container = container.parentElement;
    }
    return false;
}

wait();
