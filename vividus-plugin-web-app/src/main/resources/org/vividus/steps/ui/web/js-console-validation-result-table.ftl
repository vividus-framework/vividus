[#ftl strip_whitespace=true]
<!doctype html>
<html lang="en" ng-app="allure">
<head>
    <meta charset="utf-8">
    <title>JS error result table</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/5.3.1/css/bootstrap.min.css"/>
</head>
<body>
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
                <tr class="[#switch jsLogEntry.level]
                    [#on "SEVERE"]
                        table-danger
                    [#on "WARNING"]
                        table-warning
                    [#default]
                        table-light
                    [/#switch]">
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
