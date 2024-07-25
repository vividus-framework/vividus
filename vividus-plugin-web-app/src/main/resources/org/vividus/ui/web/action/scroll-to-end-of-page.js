const exit = arguments[arguments.length-1];
const maxNumberOfScrolls = 10;
let currentScrollIteration = 0;
(scrollToEndOfPage = function() {
    bottom = document.body.scrollHeight;
    current = window.innerHeight + Math.round(window.scrollY);
    if((bottom - current) > 0 && (currentScrollIteration < maxNumberOfScrolls)) {
        window.scrollTo(0, bottom);
        currentScrollIteration++;
        setTimeout('scrollToEndOfPage()', 500);
    } else {
        exit();
    }
}) ();
