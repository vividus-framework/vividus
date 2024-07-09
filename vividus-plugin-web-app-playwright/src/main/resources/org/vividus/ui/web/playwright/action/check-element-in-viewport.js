(element) => {
    var elementYCoordinate = element.getBoundingClientRect().y;
    var windowScrollY = Math.floor(window.scrollY);
    return windowScrollY <= elementYCoordinate && elementYCoordinate <= (windowScrollY + window.innerHeight);
}
