<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Application source code</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        pre {
            white-space: pre-wrap;
            word-break: normal;
        }
    </style>

    <div id="pretty" class="tab-pane">
    <#if format == "html">
        <pre><code id = "pretty-code" class="html"><#outputformat "HTML">${sourceCode}</#outputformat></code></pre>
    <#else>
        <pre><code id = "pretty-code" class="xml"><#outputformat "XML">${sourceCode}</#outputformat></code></pre>
    </#if>
    </div>

    <script src="../../webjars/highlight.js/11.7.0/highlight.min.js"></script>
    <script src="../../webjars/js-beautify/1.13.4/beautify-html.min.js"></script>
    <script type="text/javascript">
        (function() {
            let code = document.querySelector('#pretty-code');
            let format = "html" === "${format}"
            if (format) {
                <#include "html-formatter-fragment.ftl">
            }
            hljs.highlightElement(code);
        })();
    </script>
</body>
</html>
