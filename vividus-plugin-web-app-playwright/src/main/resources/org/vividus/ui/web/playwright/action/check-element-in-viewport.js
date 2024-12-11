(element) => {
    const rect = element.getBoundingClientRect();
    const windowHeight = window.innerHeight;

    return (rect.top >= 0 && rect.top <= windowHeight) || (rect.bottom > 0 && rect.bottom <= windowHeight);
}
