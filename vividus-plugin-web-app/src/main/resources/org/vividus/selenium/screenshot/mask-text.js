const targetValue = arguments[0];
const replacement = arguments[1];

const escapedTarget = targetValue.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
const regex = new RegExp(escapedTarget, 'g');

const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
let node;
while (node = walker.nextNode()) {
  if (node.nodeValue.includes(targetValue)) {
    node.nodeValue = node.nodeValue.replace(regex, replacement);
  }
}

document.querySelectorAll('input,textarea').forEach(input => {
  if (input.value.includes(targetValue)) {
    input.value = input.value.replace(regex, replacement);
  }
});
