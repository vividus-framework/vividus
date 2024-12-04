function getComputedStyleAsMap(element) {
    var computedStyle = getComputedStyle(element);
    var computedStyleMap = new Map(Object.entries(computedStyle));
    for (const [key] of computedStyleMap) {
      if (/^\d+$/.test(key)) {
        computedStyleMap.delete(key);
      }
    }
    return Object.fromEntries(computedStyleMap);
}

return getComputedStyleAsMap(arguments[0]);
