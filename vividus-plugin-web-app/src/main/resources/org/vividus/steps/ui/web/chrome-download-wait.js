return document.querySelector('downloads-manager')
               .shadowRoot
               .querySelector('#downloadsList')
               .items
               .find(e => e.filePath === arguments[0])
               .state === 2
