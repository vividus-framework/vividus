<#ftl strip_whitespace=true>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Resources check result table</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/5.3.1/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        .passed {
            --bs-table-bg: #DFF0D8;
            color: #3C763D;
        }
        .blocked {
            --bs-table-bg: #F2DEDE;
            color: #A94442;
        }
        .failed {
            --bs-table-bg: #F2DEDE;
            color: #A94442;
        }
        .broken {
            --bs-table-bg: #CCB3FF;
            color: #661AFF;
        }
        .skipped {
            --bs-table-bg: #D6D8DB;
        }
        .value-failed:nth-child(even) {
            --bs-table-bg: #F2DEDE;
            color: #A94442;
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

    <table class="table table-hover table-bordered table-sm fixedHeader">
        <thead>
            <tr>
                <th>
                    Checked URL
                </th>
                <th>
                    CSS selector
                </th>
                <th>
                    Check Status
                </th>
                <th>
                    Response
                </th>
                <th>
                    Checked on page
                </th>
            </tr>
        </thead>
        <tbody>
            <#list results as result>
            <#assign checkStatus = result.checkStatus>
                <tr class="${checkStatus?lower_case}">
                    <td>
                        <#if result.uriOrError.getLeft()??>
                            <#assign uri = result.uriOrError.getLeft()>
                            <a class="link" target="_blank" href="${uri}">${uri}</a>
                        <#else>
                            ${result.uriOrError.getRight()}
                        </#if>
                    </td>
                    <td>
                        ${result.cssSelector}
                    </td>
                    <td>
                        ${checkStatus}
                    </td>
                    <td>
                        Status code: ${(result.statusCode.isPresent())?then(result.statusCode.getAsInt(),'N/A')}
                        <#include "org/vividus/http/steps/attachment/resources-response-body-fragment.ftl">
                    </td>
                    <td>
                        <#assign pageURL = result.pageURL>
                        <#if pageURL != 'N/A'>
                            <a class="link" target="_blank" href="${pageURL}">${pageURL}</a>
                        <#else>
                            ${pageURL}
                        </#if>
                    </td>
                </tr>
                </#list>
        </tbody>
    </table>
    <script src="../../webjars/bootstrap/5.3.1/js/bootstrap.min.js"></script>
</body>
</html>
