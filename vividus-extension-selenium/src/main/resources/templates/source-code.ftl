<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Application source code</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.3.6/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        pre {
            white-space: pre-wrap;
            word-break: normal;
        }
    </style>

    <div id="pretty" class="tab-pane">
    <#if format == "HTML">
        <pre><code id = "pretty-code" class="html"><#outputformat "HTML">${sourceCode}</#outputformat></code></pre>
    <#else>
        <pre><code id = "pretty-code" class="xml"><#outputformat "XML">${sourceCode}</#outputformat></code></pre>
    </#if>
    </div>

    <script src="../../webjars/highlight.js/9.12.0/highlight.min.js"></script>
    <script src="../../webjars/js-beautify/1.13.4/beautify-html.min.js"></script>
    <script type="text/javascript">
        (function() {
            let code = document.querySelector('#pretty-code');
            let format = "HTML" === "${format}"
            if (format) {
                code.textContent = html_beautify(code.textContent, {
                        'indent_size': 4,
                        'indent_char': ' ',
                        'max_char': 78,
                        'unformatted': ['?', '?=', '?php', 'a', 'span', 'bdo', 'em', 'strong', 'dfn', 'code', 'samp', 'kbd', 'var', 'cite', 'abbr', 'acronym', 'q', 'sub', 'sup', 'tt', 'i', 'b', 'big', 'small', 'u', 's', 'strike', 'font', 'ins', 'del', 'pre', 'address', 'dt', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6'],
                        'extra_liners': []
                    });
            }
            hljs.highlightBlock(code);
        })();
    </script>
</body>
</html>
