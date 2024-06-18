var input = window.document.createElement('INPUT');
input.setAttribute('type', 'file');
input.setAttribute('id', 'downloadedFileContent');
input.hidden = true;
input.onchange = function (e) { e.stopPropagation() };
return window.document.documentElement.appendChild(input);
