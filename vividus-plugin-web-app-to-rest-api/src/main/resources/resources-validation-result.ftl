<#ftl strip_whitespace=true>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Recources' check result table</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
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
                <th/>
                    CSS selector
                </th>
                <th>
                    Check Status
                </th>
                <th/>
                    Status code
                </th>
                <th/>
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
                        ${(result.statusCode.isPresent())?then(result.statusCode.getAsInt(),'N/A')}
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
    <script src="../../webjars/bootstrap/3.4.1/js/bootstrap.min.js"></script>
</body>
</html>
