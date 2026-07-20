<#ftl strip_whitespace=true>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>HTTP resources validation results</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/5.3.1/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        .passed {
            --bs-table-bg: #DFF0D8;
            color: #3C763D;
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
                    Check Status
                </th>
                <th>
                    Response
                </th>
            </tr>
        </thead>
        <tbody>
            <#list results as result>
            <#assign checkStatus = result.checkStatus>
                <tr class="${checkStatus?lower_case}">
                    <td>
                        ${result.uriOrError.getLeft()}
                    </td>
                    <td>
                        ${checkStatus}
                    </td>
                    <td>
                        Status code: ${result.statusCode.getAsInt()}
                        <#include "resources-response-body-fragment.ftl">
                    </td>
                </tr>
                </#list>
        </tbody>
    </table>
    <script src="../../webjars/bootstrap/5.3.1/js/bootstrap.min.js"></script>
</body>
</html>
