<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Application source code</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/5.2.3/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        .content {
            max-height: 0;
            overflow: hidden;
            transition: max-height 0.3s ease;
        }
        .down-arrow {
            margin-right: 5px;
            width: 18px;
            height: 18px;
            transition: transform 0.3s ease;
        }
        .collapsible {
          cursor: pointer;
        }
        .collapsible.active .indicator {
            transform: rotate(180deg);
        }
        .collapsible.active {
            background-color: gainsboro;
        }
        .collapsible:hover {
            background-color: gainsboro;
        }
        .content pre {
            overflow: auto;
        }
        pre {
            white-space: pre-wrap;
            word-break: normal;
        }
    </style>

    <div id="pretty" class="tab-pane">
        <#list shadowDomSources as key, value>
            <div class="collapsible">
                <svg class="indicator down-arrow" viewBox="0 0 20 20" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 111.06 1.06l-4.24 4.24a.75.75 0 01-1.06 0L5.23 8.27a.75.75 0 01.02-1.06z" clip-rule="evenodd" />
                </svg>
                ${key}
            </div>
            <div class="content">
                <pre><code id="pretty-code" class="html"><#outputformat "HTML">${value}</#outputformat></code></pre>
            </div>
        </#list>
    </div>

    <script src="../../webjars/highlight.js/11.7.0/highlight.min.js"></script>
    <script src="../../webjars/js-beautify/1.13.4/beautify-html.min.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            let codes = document.querySelectorAll('#pretty-code');
            codes.forEach(function(code) {
                <#include "html-formatter-fragment.ftl">
                hljs.highlightElement(code);
            });
        });
    </script>
    <script>
        var coll = document.getElementsByClassName("collapsible");
        var i;
        for (i = 0; i < coll.length; i++) {
          coll[i].addEventListener("click", function() {
            this.classList.toggle("active");
            var content = this.nextElementSibling;
            if (content.style.maxHeight){
              content.style.maxHeight = null;
            } else {
              content.style.maxHeight = content.scrollHeight + "px";
            }
          });
        }
    </script>
</body>
</html>
