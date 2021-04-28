<#macro violation_panel container level panelClass>
	<div class="panel-group" id="accordion">
	    <div class="panel panel-${panelClass}">
	        <div class="panel-heading">
	            <h4 class="panel-title">
	                <#assign violationsCount = 0>
	                <#list container?keys as violationCode>
	                    <#assign violationsCount = violationsCount + container[violationCode]?size>
	                </#list>
	                <a data-toggle="collapse" data-target="#collapse${level}" href="#collapse${level}" class="collapsed panel-accordion">${level}s (${violationsCount})</a>
	            </h4>
	        </div>
	        <div id="collapse${level}" class="panel-collapse collapse">
	            <#list container?keys as violationCode>
	                <#assign violations = container[violationCode]>
	                <div class="panel-group-level2" id="accordion">
	                    <div class="panel panel-${panelClass}">
	                        <div class="panel-heading">
	                            <h4 class="panel-title">
	                                <a data-toggle="collapse" data-target="#collapse${violationCode?replace(".", "\\.")?replace(",", "\\,")}" href="#collapse${violationCode}" class="collapsed panel-accordion">${violationCode} (${violations?size})</a>
	                            </h4>
	                        </div>
	                        <div id="collapse${violationCode}" class="panel-collapse collapse">
	                            <div class="panel-heading ${(violations[0].type)!}">
	                                <a target="_blank" href="https://squizlabs.github.io/HTML_CodeSniffer/Standards/${violationCode?starts_with("WCAG2")?then('WCAG2/">WCAG 2.0','Section508/">Section 508')} Standard</a>
	                                <h4 class="panel-title">Message Name: ${(violations[0].message)!?html}</h4>
	                            </div>
	                            <table class="table">
	                                <tbody>
	                                    <#assign counter = 0>
	                                    <#list violations as violation>
	                                        <#assign counter = counter + 1>
	                                        <tr class="${(violation.type)!}">
	                                            <td>
	                                                ${counter!}
	                                            </td>
	                                            <td>
	                                                <b>Selector: </b><pre>${(violation.selector)!?html}</pre>
	                                                <b>Source: </b><pre>${(violation.context)!?html}</pre>
	                                            </td>
	                                        </tr>
	                                    </#list>
	                                </tbody>
	                            </table>
	                        </div>
	                    </div>
	                </div>
	            </#list>
	        </div>
	    </div>
	</div>
</#macro>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Accessibility check result table</title>
    <link rel="stylesheet" href="../../css/external.css"/>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.3.6/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        .Error {
            background-color: #F2DEDE;
        }
        .Warning {
            background-color: #FFFFE5;
        }
        .Notice {
            background-color: #F2F2F2;
        }
        pre {
            white-space: pre-wrap;
            word-break: normal;
        }
        a[data-toggle='collapse'] {
            display: inline-block;
            width: 100%;
            height: 100%;
        }
        .panel-title:hover {
             cursor: pointer;
        }
        .panel-accordion:after {
            font-family:'FontAwesome';
            content:"\F107";
            float: right;
            color: grey;
        }
        .panel-accordion.collapsed:after {
            content:"\F105";
        }
        .panel-group-level2 {
            margin-top: 20px;
            margin-left: 5px;
        }
    </style>
    <@violation_panel container=Error level="Error" panelClass="danger" />
    <@violation_panel container=Warning level="Warning" panelClass="warning" />
    <@violation_panel container=Notice level="Notice" panelClass="info" />
    <script src="../../webjars/jquery/2.1.1/jquery.min.js"></script>
    <script src="../../webjars/bootstrap/3.3.6/js/bootstrap.min.js"></script>
</body>
</html>
