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
    // Even though the id attribute is expected to be unique according to specification, not everyone follows it. Detection
    // of non-unique id attributes is not a goal of CSS selector factory, please consider usage of accessibility analyzers.
    var elementId = element.id;
    if (elementId.length > 0 && isUniqueId(elementId)) {
        return buildByIdCssSelector(elementId);
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

function isUniqueId(elementId) {
    return document.querySelectorAll(buildByIdCssSelector(elementId)).length == 1;
}

function buildByIdCssSelector(elementId) {
    return '#' + escapeSpecialChars(elementId);
}

function escapeSpecialChars(string) {
    var result = '';
    var first = string.charCodeAt(0);
    if (first >= 0x0030 && first <= 0x0039) {
        result += '\\' + first.toString(16) + ' ';
        string = string.substring(1);
    }
    result += string.replace(/[:; '".!#?,()$%&*+/<=>@^`{|}~[\\\]]/g, '\\$&');
    return result;
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
