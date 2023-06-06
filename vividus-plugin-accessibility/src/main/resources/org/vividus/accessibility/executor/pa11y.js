// This file is part of pa11y.
//
// pa11y is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// pa11y is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with pa11y.  If not, see <https://www.gnu.org/licenses/>.
//
// Sources:
// - https://github.com/pa11y/pa11y/blob/master/lib/runner.js
// - https://github.com/pa11y/pa11y/blob/master/lib/runners/htmlcs.js

'use strict';

/* eslint-disable max-statements */
window.injectAccessibilityCheck = function(window, options, rootElement, elementsToCheck, elementsToIgnore, done) {

	if (options.verifyPage) {
		const windowHtml = window.document.documentElement.outerHTML;
		if (!windowHtml.match(new RegExp(options.verifyPage))) {
			return reportError('Page not verified - HTML did not contain: "' + options.verifyPage + '"');
		}
	}

	const issueTypeMap = {
		1: 'error',
		2: 'warning',
		3: 'notice'
	};

	setTimeout(runCodeSniffer, options.wait);

	function runCodeSniffer() {
		try {
			window.HTMLCS.process(options.standard, window.document, onCodeSnifferComplete);
		} catch (error) {
			reportError('HTML CodeSniffer: ' + error.message);
		}
	}

	function onCodeSnifferComplete() {
		done(JSON.stringify(processIssues(window.HTMLCS.getMessages())));
	}

	function processIssues(issues) {
		if (rootElement) {
			issues = issues.filter(issue => isElementInTestArea(issue.element));
		}

		if (elementsToIgnore.length !== 0) {
			issues = issues.filter(issue => isElementOutsideHiddenArea(issue.element));
		}

		if (elementsToCheck.length !== 0) {
			issues = issues.filter(issue => isElementInsideArea(issue.element));
		}
		return issues.map(processIssue).filter(isIssueNotIgnored);
	}

	function processIssue(issue) {
		return {
			code: issue.code,
			context: processIssueHtml(issue.element),
			message: issue.msg,
			type: (issueTypeMap[issue.type] || 'unknown'),
			typeCode: issue.type,
			selector: getElementSelector(issue.element)
		};
	}

	function processIssueHtml(element) {
		let outerHTML = null;
		let innerHTML = null;
		if (!element.outerHTML) {
			return outerHTML;
		}
		outerHTML = element.outerHTML;
		if (element.innerHTML.length > 31) {
			innerHTML = element.innerHTML.substr(0, 31) + '...';
			outerHTML = outerHTML.replace(element.innerHTML, innerHTML);
		}
		if (outerHTML.length > 251) {
			outerHTML = outerHTML.substr(0, 250) + '...';
		}
		return outerHTML;
	}

	function getElementSelector(element, selectorParts = []) {
		if (isElementNode(element)) {
			const identifier = buildElementIdentifier(element);
			selectorParts.unshift(identifier);
			if (!element.id && element.parentNode) {
				return getElementSelector(element.parentNode, selectorParts);
			}
		}
		return selectorParts.join(' > ');
	}

	function buildElementIdentifier(element) {
		if (element.id) {
			return `#${element.id}`;
		}
		let identifier = element.tagName.toLowerCase();
		if (!element.parentNode) {
			return identifier;
		}
		const siblings = getSiblings(element);
		const childIndex = siblings.indexOf(element);
		if (!isOnlySiblingOfType(element, siblings) && childIndex !== -1) {
			identifier += `:nth-child(${childIndex + 1})`;
		}
		return identifier;
	}

	function getSiblings(element) {
		return [...element.parentNode.childNodes].filter(isElementNode);
	}

	function isOnlySiblingOfType(element, siblings) {
		const siblingsOfType = siblings.filter(sibling => {
			return (sibling.tagName === element.tagName);
		});
		return (siblingsOfType.length <= 1);
	}

	function isElementNode(element) {
		return (element.nodeType === window.Node.ELEMENT_NODE);
	}

	function isElementInTestArea(element) {
		return (rootElement ? rootElement.contains(element) : true);
	}

	function isElementOutsideHiddenArea(element) {
		return !elementsToIgnore.some(elementToIgnore => {
			return elementToIgnore.contains(element);
		});
	}

	function isElementInsideArea(element) {
		return elementsToCheck.some(elementToCheck => {
			return elementToCheck.contains(element);
		});
	}

	function isIssueNotIgnored(issue) {
		if (options.include) {
			return options.include.indexOf(issue.code) !== -1;
		}
		// NOTE: Original code has issue.code.toLowerCase()
		if (options.ignore.indexOf(issue.code) !== -1) {
			return false;
		}
		if (options.ignore.indexOf(issue.type) !== -1) {
			return false;
		}
		return true;
	}

	function reportError(message) {
		done({
			error: message
		});
	}

}
/* eslint-enable max-statements */

/* istanbul ignore next */
if (typeof module !== 'undefined' && module.exports) {
	module.exports = injectAccessibilityCheck;
}
