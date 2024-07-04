var input = arguments[0], callback = arguments[1];
var reader = new FileReader();
reader.onload = function (ev) { callback(reader.result) };
reader.onerror = function (ex) { callback(ex.message) };
reader.readAsDataURL(input.files[0]);
input.remove();
