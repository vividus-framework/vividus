var exit = arguments[arguments.length-1];
(scrollToEndOfPage = function() {
    bottom = document.body.scrollHeight;
    current = window.innerHeight + Math.round(window.scrollY);
    if((bottom - current) > 0) { 
        window.scrollTo(0, bottom);
        setTimeout('scrollToEndOfPage()', 500);
    } else {
        exit();
    }
}) ();
