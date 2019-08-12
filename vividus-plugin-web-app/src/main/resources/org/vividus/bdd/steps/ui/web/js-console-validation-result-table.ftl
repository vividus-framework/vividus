[#ftl strip_whitespace=true]
<!doctype html>
<html lang="en" ng-app="allure">
<head>
    <meta charset="utf-8">
    <title>JS error result table</title>
    <link rel="stylesheet" href="../../css/external.css"/>
    <link rel="stylesheet" href="../../styles.css"/>
</head>
<body>
    <style>
        .SEVERE {
            background-color: #F2DEDE;
            color: #A94442;
        }
        .WARNING {
            background-color: #FCF8E3;
            color: #8A6D3B;
        }
    </style>
    <table class="table table-hover table-bordered">
        <thead>
            <tr>
                <th>#</th>
                <th>Page URL</th>
                <th>Level</th>
                <th>Log Entry</th>
            </tr>
        </thead>
        <tbody>
            [#assign counter = 0]
            [#list results?keys as pageResult]
                [#list results[pageResult] as jsLogEntry]
                [#assign counter = counter + 1]
                <tr class="${jsLogEntry.level}">
                    <td>
                        ${counter!}
                    </td>
                    <td>
                        ${pageResult}
                    </td>
                    <td>
                        ${jsLogEntry.level}
                    </td>
                    <td>
                        ${jsLogEntry}
                    </td>
                </tr>
                [/#list]
            [/#list]
        </tbody>
    </table>
</body>
</html>
