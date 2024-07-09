async (elementToScroll, stickyHeaderSize) => {
    let waitForScroll;
    let currentWindow = window;
    stickyHeaderSize = stickyHeaderSize / 100;

    try {
        if (isElementInsideOverflowContainer(elementToScroll)) {
            elementToScroll.scrollIntoView(true);
            return true;
        }

        window.top.document;
        while (currentWindow !== window.top) {
            elementToScroll = currentWindow.frameElement;
            currentWindow = currentWindow.parent;
        }

        const safariBrowser = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
        if (safariBrowser) {
            currentWindow.addEventListener('scroll', clearTimeoutAndWait, false);
        } else {
            currentWindow.addEventListener("scrollend", scrollEndEventListener);
        }

        currentWindow.scrollBy(0, elementToScroll.getBoundingClientRect().top - currentWindow.innerHeight * stickyHeaderSize);
    } catch(e) {
        // swallow error quietly
    }

    function wait() {
        waitForScroll = setTimeout(function() {
            currentWindow.removeEventListener('scroll', clearTimeoutAndWait);
            return true;
        }, 50);
    }

    function clearTimeoutAndWait(event) {
        currentWindow.clearTimeout(waitForScroll);
        wait();
    }

    function scrollEndEventListener(event) {
        currentWindow.removeEventListener("scrollend", scrollEndEventListener);
        return true;
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

    return wait();
}
