<#ftl strip_whitespace=true>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>HTTP resources validation results</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.3.6/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        .passed {
            background-color: #DFF0D8;
            color: #3C763D;
        }
        .failed {
            background-color: #F2DEDE;
            color: #A94442;
        }
        .broken {
            background-color: #CCB3FF;
            color: #661AFF;
        }
        .skipped {
            background-color: #D6D8DB;
        }
        .value-failed:nth-child(even) {
            background-color: #F2DEDE;
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

    <table class="table table-hover table-bordered table-condensed fixedHeader">
        <thead>
            <tr>
                <th/>
                    Checked URL
                </th>
                <th>
                    Check Status
                </th>
                <th/>
                    Status code
                </th>
            </tr>
        </thead>
        <tbody>
            <#list results as result>
            <#assign checkStatus = result.checkStatus>
                <tr class="${checkStatus?lower_case}">
                    <td>
                        ${result.uri}
                    </td>
                    <td>
                        ${checkStatus}
                    </td>
                    <td>
                        ${result.statusCode}
                    </td>
                </tr>
                </#list>
        </tbody>
    </table>
    <script src="../../webjars/bootstrap/3.3.6/js/bootstrap.min.js"></script>
</body>
</html>
