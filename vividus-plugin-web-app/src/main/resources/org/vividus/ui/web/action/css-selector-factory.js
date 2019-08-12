function getCssSelectorForElement(element, selectorParts) {
    selectorParts = Array.isArray(selectorParts) ? selectorParts : [];
    if (isElementNode(element)) {
        var identifier = buildElementIdentifier(element);
        selectorParts.unshift(identifier);
        if (!element.id && element.parentNode) {
            return getCssSelectorForElement(element.parentNode, selectorParts);
        }
    }
    return selectorParts.join(' > ');
}

function buildElementIdentifier(element) {
    if (element.id) {
        return '#' + escapeSpecialChars(element.id);
    }
    var identifier = escapeColon(element.tagName.toLowerCase());
    if (!element.parentNode) {
        return identifier;
    }
    var siblings = getSiblings(element);
    var childIndex = siblings.indexOf(element);
    if (!isOnlySiblingOfType(element, siblings) && childIndex !== -1) {
        identifier += ':nth-child(' + (childIndex + 1) + ')';
    }
    return identifier;
}

function escapeSpecialChars(str) {
    return str.replace(/(:| |'|\.|!)/g, '\\$1')
}

function escapeColon(str) {
    return str.replace(/:/g, '\\:');
}

function getSiblings(element) {
    return Array.prototype.slice.call(element.parentNode.childNodes).filter(
            isElementNode);
}

function isOnlySiblingOfType(element, siblings) {
    var siblingsOfType = siblings.filter(function(sibling) {
        return (sibling.tagName === element.tagName);
    });
    return (siblingsOfType.length <= 1);
}

function isElementNode(element) {
    return (element.nodeType === 1);
}

var source = arguments[0];
if (Array.isArray(source)) {
    return Array.prototype.slice.call(source).map(getCssSelectorForElement)
}
else {
    return getCssSelectorForElement(source);
}
