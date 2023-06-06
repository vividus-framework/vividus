'use strict';

window.injectAccessibilityCheck = function(window, options, rootElement, elementsToCheck, elementsToIgnore, done) {

	setTimeout(runAxeCore, options.wait);

	function runAxeCore() {
		const context = {
			include: elementsToCheck,
			exclude: elementsToIgnore
		};

		axe.run(context, options, (err, output) => {
			if (err) {
				done(err.message)
			} else {
				done(JSON.stringify([
					{
						type: 'failed',
						results: output.violations
					},
					{
						type: 'incompleted',
						results: output.incomplete
					},
					{
						type: 'passed',
						results: output.passes
					},
					{
						type: 'inapplicable',
						results: output.inapplicable
					}
				]));
			}
		});
	}
}
