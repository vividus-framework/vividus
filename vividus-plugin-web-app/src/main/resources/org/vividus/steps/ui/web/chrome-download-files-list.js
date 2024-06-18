return document.querySelector('downloads-manager')
               .shadowRoot
               .querySelector('#downloadsList')
               .items
               .map(e => e.fileName)
