<#macro violation_panel container level>
	<div class="card-group" id="accordion">
	    <div class="card ${level}">
	        <div class="card-header">
	            <h4 class="card-title">
	                <#assign violationsCount = 0>
	                <#list container?keys as violationCode>
	                    <#assign violationsCount = violationsCount + container[violationCode]?size>
	                </#list>
	                <button data-bs-toggle="collapse" data-bs-target="#collapse${level}" href="#collapse${level}" class="btn btn-accordion collapsed ${level}"><span>${level}s (${violationsCount})</span></button>
	            </h4>
	        </div>
	        <div id="collapse${level}" class="collapse collapse-container">
	            <#list container?keys as violationCode>
	                <#assign violations = container[violationCode]>
	                <div class="card-group-level2" id="accordion">
	                    <div class="card ${level}">
	                        <#assign violationId = violationCode?replace(".", "-")?replace(",", "-") >
	                        <div class="card-header">
	                            <h4 class="card-title">
	                                <button data-bs-toggle="collapse" data-bs-target="#${violationId}" href="#${violationId}" class="btn btn-accordion collapsed ${level}"><span>${violationCode} (${violations?size})</span></button>
	                            </h4>
	                        </div>
	                        <div id="${violationId}" class="collapse">
	                            <div class="card-header ${(violations[0].type)!}">
	                                <button class="btn btn-link" target="_blank" style="padding: 0;" href="https://squizlabs.github.io/HTML_CodeSniffer/Standards/${violationCode?starts_with("WCAG2")?then('WCAG2/">WCAG 2.0','Section508/">Section 508')} Standard</button>
	                                <h4 class="card-title">Message Name: ${(violations[0].message)!?html}</h4>
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
    <link rel="stylesheet" href="../../webjars/bootstrap/5.3.0-alpha3/css/bootstrap.min.css"/>
    <style>
        .Error {
            background-color: var(--bs-danger-bg-subtle) !important;
        }
        .Warning {
            background-color: var(--bs-warning-bg-subtle) !important;
        }
        .Notice {
            background-color: var(--bs-info-bg-subtle) !important;
        }

        button.Error {
            color: var(--bs-danger) !important;
            font-size: 18px;
        }
        button.Warning {
            color: var(--bs-warning) !important;
            font-size: 18px;
        }
        button.Notice {
            color: var(--bs-info) !important;
            font-size: 18px;
        }

        pre {
            display: block;
            padding: 8.5px;
            margin: 0 0 10px;
            font-size: 13px;
            line-height: 1.42857143;
            color: #333;
            word-break: break-all;
            word-wrap: break-word;
            background-color: #f5f5f5;
            border: 1px solid #ccc;
            border-radius: 4px;
            white-space: pre-wrap;
        }

        .card-title {
            margin-top: 0px;
            margin-bottom: 0px;
            font-size: 16px;
            color: inherit;
        }
        .card-title:hover {
             cursor: pointer;
        }
        .card-group-level2 {
            margin-top: 10px;
            margin-bottom: 10px;
            margin-left: 5px;
        }
        .card-group {
            margin-bottom: 20px;
        }

        .collapse-container {
            background: #ffffff;
        }
    </style>
    <link rel="stylesheet" href="../../webjars/vividus/buttons.css"/>
</head>
<body>
    <@violation_panel container=Error level="Error" />
    <@violation_panel container=Warning level="Warning" />
    <@violation_panel container=Notice level="Notice" />
    <script src="../../webjars/jquery/3.6.4/jquery.min.js"></script>
    <script src="../../webjars/bootstrap/5.3.0-alpha3/js/bootstrap.min.js"></script>
</body>
</html>
