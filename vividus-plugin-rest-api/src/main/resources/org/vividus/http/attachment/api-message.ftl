<!doctype html>
<html lang="en" ng-app="allure">
<head>
    <meta charset="utf-8">
    <title>API message</title>
    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon">
    <link rel="icon" href="img/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        pre {
            white-space: pre-wrap;
            word-break: normal;
        }
        a[data-toggle='collapse'] {
            display: inline-block;
            width: 100%;
            height: 100%;
        }
        .toggleable:hover {
             cursor: pointer;
        }
        .panel-heading a:after {
            font-family:'FontAwesome';
            content:"\F107";
            float: right;
            color: grey;
        }
        .panel-heading a.collapsed:after {
            content:"\F105";
        }
        .container {
            position: relative;
        }
        .copy-button {
            position: absolute;
            top: 0px;
            right: 15px;
        }
        #copy-toast {
            position: absolute;
            display: none;
            top: 35px;
            right: 0px;
            font-size: 13px;
            background-color: #666362;
            color: #fff;
        }
    </style>

    <div class="panel-group" id="accordion">

        <#if statusCode != -1>
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h4 class="panel-title">Status code: ${statusCode}</h4>
                </div>
            </div>
        </#if>

        <div class="panel panel-info">
            <div class="panel-heading">
                <h4 class="panel-title toggleable">
                    <a data-toggle="collapse" data-target="#collapse-header" href="#collapse-header" class="collapsed">Headers</a>
                </h4>
            </div>
            <div id="collapse-header" class="panel-collapse collapse">
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
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h4 class="panel-title toggleable">
                        <a data-toggle="collapse" data-target="#collapse-body" href="#collapse-body" class="collapsed">Body</a>
                    </h4>
                </div>
                <div id="collapse-body" class="panel-collapse collapse in">
                    <div class="container">
                        <ul class="nav nav-tabs">
                            <li class="active"><a data-toggle="tab" href="#pretty">Pretty</a></li>
                            <li><a data-toggle="tab" href="#origin">Original</a></li>
                        </ul>
                        <div class="tab-content">
                             <#assign contentType = bodyContentType?split('/')?last>
                             <div id="pretty" class="tab-pane fade  in active">
                               <pre><code id = "pretty-code" class="${contentType}"><#outputformat "HTML">${body}</#outputformat></code></pre>
                             </div>
                             <div id="origin" class="tab-pane fade">
                               <pre><code id = "original-code" class="${contentType}"><#outputformat "HTML">${body}</#outputformat></code></pre>
                             </div>
                        </div>
                    </div>
                </div>
            </div>
        </#if>

        <#if curlCommand??>
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h4 class="panel-title toggleable">
                        <a data-toggle="collapse" data-target="#collapse-curl" href="#collapse-curl" class="collapsed">cURL command</a>
                    </h4>
                </div>
                <div id="collapse-curl" class="panel-collapse collapse">
                    <div class="container">
                        <pre><code class="language-shell">${curlCommand}</code></pre>
                        <button class="copy-button" title="Copy to clipboard" onclick="copyCurlCommand()">
                        	<img src="../../webjars/bootstrap/3.4.1/fonts/clipboard.svg">
                        </button>
                        <span id="copy-toast">Copied!</span>
                    </div>
                </div>
            </div>
        </#if>
    </div>

    <script src="../../webjars/jquery/3.6.4/jquery.min.js"></script>
    <script src="../../webjars/bootstrap/3.4.1/js/bootstrap.min.js"></script>
    <script src="../../webjars/highlight.js/11.7.0/highlight.min.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $("code[id='pretty-code']").each(function(i, e) {
                if(e.className.includes("json")){
                    var text = $(this).text();
                    var pretty =  JSON.stringify(JSON.parse(text), null, 2);
                    $(this).text(pretty);
                }
                hljs.highlightElement(e);
            });
        });

        function copyCurlCommand() {
            var text = document.querySelector('#collapse-curl code').textContent;
            navigator.clipboard.writeText(text);
            document.getElementById("copy-toast").style.display = "inline";
            setTimeout( function() {
                document.getElementById("copy-toast").style.display = "none";
            }, 1000);
        }
    </script>
</body>
</html>
