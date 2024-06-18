return document.querySelector('downloads-manager')
               .shadowRoot
               .querySelector('#downloadsList')
               .items
               .filter(e => e.fileName.match(arguments[0]))
               .sort((a, b) => b.started - a.started)
               .map(e => e.filePath)[0]
