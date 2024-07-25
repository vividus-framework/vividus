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

    const safariBrowser = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
    if (safariBrowser)
    {
        currentWindow.addEventListener('scroll', clearTimeoutAndWait, false);
    }
    else
    {
        // https://developer.mozilla.org/en-US/docs/Web/API/Document/scrollend_event
        currentWindow.addEventListener("scrollend", scrollEndEventListener);
    }

    currentWindow.scrollBy(0, elementToScroll.getBoundingClientRect().top - currentWindow.innerHeight * stickyHeaderSize);
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

function scrollEndEventListener(event) {
    currentWindow.removeEventListener("scrollend", scrollEndEventListener);
    exit(true);
}

function isElementInsideOverflowContainer(element) {
    let container = element.parentElement ?? element.getRootNode().host;
    while (container) {
        const style = window.getComputedStyle(container);
        if (style.overflow !== 'visible' && (style.overflowX !== 'visible' || style.overflowY !== 'visible')) {
            return true;
        }
        container = container.parentElement ?? container.getRootNode().host;
    }
    return false;
}

wait();
