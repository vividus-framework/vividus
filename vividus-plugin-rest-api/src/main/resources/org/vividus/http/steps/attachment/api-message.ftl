<!doctype html>
<html lang="en" ng-app="allure">
<head>
    <meta charset="utf-8">
    <title>API message</title>
    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon">
    <link rel="icon" href="img/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/5.3.1/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        pre {
            white-space: pre-wrap;
            word-break: normal;
        }
        a[data-bs-toggle='collapse'] {
            display: inline-block;
            width: 100%;
            height: 100%;
        }
        .card-header:hover {
             cursor: pointer;
        }
        .card-header a:after {
            font-family:'FontAwesome';
            content:"\F107";
            float: right;
            color: grey;
        }
        .card-header a.collapsed:after {
            content:"\F105";
        }
        .button-info {
            color: #31708f;
            background-color: #d9edf7;
            border-color: #bce8f1;
        }
        .tab-content {
            background-color: #f5f5f5;
        }
        #copy-toast
        {
            display: none;
            position: absolute;
            background-color: grey;
            color: white;
            padding: 6px 12px;
            border-radius: 4px;
            font-size: 14px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.2);
            z-index: 9999;
        }
    </style>

    <div class="card" id="accordion">
        <#if statusCode != -1>
            <h5 class="card-header button-info" style="cursor: default">Status code: ${statusCode}</h4>
        </#if>

        <h4 class="card-header">
            <a class="btn button-info text-start" data-bs-toggle="collapse" href="#collapse-header" role="button" aria-expanded="false" aria-controls="collapse-header">Headers</a>
        </h4>
        <div id="collapse-header" class="collapse multi-collapse border rounded">
            <div class="card card-body">
                <table class="table">
                    <tbody>
                        <#list headers as header>
                            <tr>
                                <td>
                                    ${header.getName()}
                                </td>
                                <td>
                                    ${header.getValue()}
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>

        <#if body??>
            <h4 class="card-header">
                <a class="btn button-info text-start" data-bs-toggle="collapse" href="#collapse-body" role="button" aria-expanded="false" aria-controls="collapse-body">Body</a>
            </h4>
            <div id="collapse-body" class="collapse multi-collapse">
                <#include "/templates/http-body-container-fragment.ftl">
            </div>
        </#if>

        <#if curlCommand??>
            <h4 class="card-header">
                <a class="btn button-info text-start" data-bs-toggle="collapse" href="#collapse-curl" role="button" aria-expanded="false" aria-controls="collapse-curl">cURL command</a>
            </h4>
            <div id="collapse-curl" class="collapse multi-collapse">
                <div class="card card-body">
                   <div class="container">
                      <ul class="nav nav-tabs">
                         <li class="nav-item"><a class="nav-link active" data-bs-toggle="tab" href="#origin">Original</a></li>
                      </ul>
                      <div class="tab-content border rounded" id="curl-copy-block">
                         <div id="origin" class="tab-pane container active show">
                            <pre><code class="language-shell" id="curl-content">${curlCommand}</code></pre>
                         </div>
                      </div>
                   </div>
                </div>
            </div>
        </#if>
    </div>

    <div id="copy-toast">Copied!</div>

    <script src="../../webjars/jquery/3.6.4/jquery.min.js"></script>
    <script src="../../webjars/bootstrap/5.3.1/js/bootstrap.min.js"></script>
    <script src="../../webjars/highlight.js/11.7.0/highlight.min.js"></script>
    <script src="../../webjars/vividus/js/code-prettifier.js"></script>

    <script>
        document.addEventListener("DOMContentLoaded", function () {
            const copyBlock = document.getElementById("curl-copy-block");
            const curlText = document.getElementById("curl-content");
            const toast = document.getElementById("copy-toast");

            if (copyBlock && curlText && toast)
            {
                copyBlock.addEventListener("click", function (event)
                {
                    const text = curlText.textContent;
                    navigator.clipboard.writeText(text).then(() =>
                        {
                            toast.style.left = (event.pageX + 10) + 'px';
                            toast.style.top = (event.pageY + 10) + 'px';
                            toast.style.display = "block";

                            setTimeout(() => {toast.style.display = "none";}, 250);
                        })
                        .catch(err => {console.error("Failed to copy text: ", err);});
                });
            }
        });
    </script>
</body>
</html>
