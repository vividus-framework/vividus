<#ftl strip_whitespace=true>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
    <title>Data set</title>
    <link rel="stylesheet" href="../../webjars/bootstrap/5.3.1/css/bootstrap.min.css"/>
    <style>
        body {
            padding: 1rem;
        }
        table {
            background-color: #ffffff;
        }
        th.line-number, td.line-number {
            width: 1%;
            white-space: nowrap;
            text-align: center;
            color: var(--bs-secondary);
        }
        td {
            word-break: break-word;
        }
        td.null-value {
            color: var(--bs-secondary);
            font-style: italic;
        }
    </style>
</head>
<body>
    <#outputformat "HTML">
    <div class="container-fluid">
        <#assign columns = data[0]?keys>
        <p class="text-muted mb-2">Total rows: <strong>${data?size}</strong></p>
        <div class="table-responsive">
            <table class="table table-striped table-bordered table-hover table-sm align-middle">
                <thead class="table-light">
                    <tr>
                        <th scope="col" class="line-number">#</th>
                        <#list columns as column>
                            <th scope="col">${column}</th>
                        </#list>
                    </tr>
                </thead>
                <tbody>
                    <#list data as row>
                        <tr>
                            <th scope="row" class="line-number">${row?index + 1}</th>
                            <#list columns as column>
                                <#if row[column]??>
                                    <td>${row[column]?string}</td>
                                <#else>
                                    <td class="null-value">null</td>
                                </#if>
                            </#list>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </div>
    </#outputformat>
</body>
</html>
