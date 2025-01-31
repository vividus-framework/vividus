'use strict';

const template = `
    <script type="module">
        import ZeroMd from './webjars/zero-md.3.1.7/zero-md.min.js';
        customElements.define('zero-md', ZeroMd);
    </script>
    <zero-md src="./plugin/custom-tab/custom-tab.md">
        <template>
            <style>
                :host {
                    display: block; position: relative; contain: content; overflow:scroll; height:100vh;
                    padding-left: 15px; padding-right: 15px;
                }
                :host([hidden]) { display: none; }
            </style>
        </template>
    </zero-md>
`;

var MyView = Backbone.Marionette.View.extend({
    template: template,

    render: function () {
        this.$el.html(this.template);
        return this;
    }
})

class MyLayout extends allure.components.AppLayout {
    getContentView() {
        return new MyView();
    }
}

allure.api.addTab('custom-tab', {
    title: 'tab.custom-tab.name', icon: 'fa fa-book',
    route: 'custom-tab',
    onEnter: (function () {
        return new MyLayout();
        })
});
