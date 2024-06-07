code.textContent = html_beautify(code.textContent, {
        'indent_size': 4,
        'indent_char': ' ',
        'max_char': 78,
        'unformatted': ['?', '?=', '?php', 'a', 'span', 'bdo', 'em', 'strong', 'dfn', 'code', 'samp', 'kbd', 'var', 'cite', 'abbr', 'acronym', 'q', 'sub', 'sup', 'tt', 'i', 'b', 'big', 'small', 'u', 's', 'strike', 'font', 'ins', 'del', 'pre', 'address', 'dt', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6'],
        'extra_liners': []
});
