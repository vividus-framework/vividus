async () => {
    while (true) {
        var bottom = document.body.scrollHeight;
        var current = window.innerHeight + Math.round(window.scrollY);
        if ((bottom - current) > 0) {
            window.scrollTo(0, bottom);
            await new Promise(resolve => setTimeout(resolve, 500));
        } else {
            break;
        }
    }
}
