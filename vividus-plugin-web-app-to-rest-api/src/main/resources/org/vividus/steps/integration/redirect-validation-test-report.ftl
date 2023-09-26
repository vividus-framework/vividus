<#ftl strip_whitespace=true>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Redirect validation test report</title>
    <link rel="stylesheet" href="../../webjars/vividus/style.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/5.3.1/css/bootstrap.min.css"/>
    <style>
        .passed {
            --bs-table-bg: #DFF0D8;
        }
        .failed {
            --bs-table-bg: #F2DEDE;
        }
        table {
            border-collapse: collapse;
            width: 100%;
        }
        table th {
            text-align: center;
            font-family: Arial, Helvetica, sans-serif;
        }
    </style>
</head>
<body>
    <#list results as result>
    <table class="table table-hover table-bordered">
        <#assign startURL = result.expectedRedirect.startUrl>
        <#assign endURL = result.expectedRedirect.endUrl>
        <tr>
            <h5 align="center"><u>Redirects validation test report from '${startURL}' to '${endURL}'</u></h5>
            <th>Start URL</th>
            <th>Expected end URL</th>
            <th>Actual end URL</th>
            <#if (result.expectedRedirect.redirectsNumber)?? >
                <th>Expected redirects number</th>
                <th>Actual redirects number</th>
                <#assign fullTable = true>
            </#if>
            <th>Result</th>
        </tr>
        <#assign passed = "${(result.passed)?then('passed', 'failed')}">
        <tr class="${passed}">
            <td><a href="${(result.expectedRedirect.startUrl)!}">${startURL}</a></td>
            <td><a href="${(result.expectedRedirect.endUrl)!}">${endURL}</a></td>
            <td><a href="${(result.actualEndUrl)!}">${(result.actualEndUrl)!}</a></td>
            <#if fullTable?? >
                <td>${(result.expectedRedirect.redirectsNumber)!}</td>
                <td>${(result.actualRedirectsNumber)!}</td>
            </#if>
            <td>${result.resultMessage!}</td>
        </tr>
    </table>
    <table class="table table-hover table-bordered">
        <thead>
            <tr>
                <th/>Request path</th>
            </tr>
        </thead>
        <tr>
            <td>${(result.expectedRedirect.startUrl)!}</td>
        </tr>
        <#list result.redirects>
            <#items as url>
                <tr>
                    <td>${url}</td>
                </tr>
            </#items>
        </#list>
    </table>
    </#list>
</body>
</html>
