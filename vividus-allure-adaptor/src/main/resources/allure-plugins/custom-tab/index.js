'use strict';

(function() {
    function showCustomTab() {
        var content = document.getElementById('content');
        if (!content) return;

        content.innerHTML = '<div id="custom-tab-container" style="display:block;position:relative;overflow:auto;height:100vh;padding:15px;"></div>';

        var container = document.getElementById('custom-tab-container');
        var zeroMd = document.createElement('zero-md');
        zeroMd.setAttribute('src', './plugin/custom-tab/custom-tab.md');

        var template = document.createElement('template');
        template.innerHTML = '<style>:host{display:block;position:relative;contain:content;}</style>';
        zeroMd.appendChild(template);
        container.appendChild(zeroMd);

        var script = document.createElement('script');
        script.type = 'module';
        script.textContent = "import ZeroMd from './webjars/zero-md.3.1.7/zero-md.min.js';if(!customElements.get('zero-md')){customElements.define('zero-md',ZeroMd);}";
        container.appendChild(script);
    }

    function addCustomTabLink() {
        var menu = document.querySelector('.side-nav__menu');
        if (!menu) return;

        var li = document.createElement('li');
        li.className = 'side-nav__item';
        li.setAttribute('data-tooltip', 'Custom');

        var a = document.createElement('a');
        a.href = '#custom-tab';
        a.className = 'side-nav__link';

        var iconLane = document.createElement('span');
        iconLane.className = 'side-nav__icon-lane';
        iconLane.innerHTML = '<svg class="side-nav__icon" viewBox="0 0 24 24" width="24" height="24"><path fill="currentColor" d="M6 2a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6H6zm0 2h7v5h5v11H6V4zm2 7v2h8v-2H8zm0 4v2h5v-2H8z"/></svg>';

        var text = document.createElement('span');
        text.className = 'side-nav__text';
        text.textContent = 'Custom';

        a.appendChild(iconLane);
        a.appendChild(text);
        li.appendChild(a);
        menu.appendChild(li);
    }

    function onHashChange() {
        if (window.location.hash === '#custom-tab') {
            showCustomTab();
        }
    }

    window.addEventListener('hashchange', onHashChange);

    Promise.resolve(window.__allureCoreLoaded).then(function() {
        addCustomTabLink();
        onHashChange();
    });
})();
