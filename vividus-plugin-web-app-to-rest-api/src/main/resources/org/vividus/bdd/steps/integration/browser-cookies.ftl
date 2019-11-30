<#ftl strip_whitespace=true>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Recources' check result table</title>
    <link rel="stylesheet" href="../../css/external.css"/>
    <link rel="stylesheet" href="../../styles.css"/>
</head>
<body>
    <style>
        table {
            border-collapse: collapse;
            width: 100%;
        }
        table th {
            text-align: center;
            font-family: Arial, Helvetica, sans-serif;
        }
        thead th {
            position: sticky;
            position: -webkit-sticky;
            top: 0;
            z-index: 10;
            background: #dfe2e4;
        }
    </style>

    <table class="table table-hover table-bordered table-condensed fixedHeader">
        <thead>
            <tr>
                <th width="10%">
                    Name
                </th>
                <th width="50%">
                    Value
                </th>
                <th width="10%">
                    Domain
                </th>
                <th width="7%">
                    Path
                </th>
                <th width="13%">
                    Expires
                </th>
                <th width="5%">
                    Persistent
                </th>
                <th width="5%">
                    Secure
                </th>
            </tr>
        </thead>
        <tbody>
            <#list cookies as cookie>
                <tr>
                    <td>
                        ${cookie.name}
                    </td>
                    <td>
                        ${cookie.value}
                    </td>
                    <td>
                        ${cookie.domain}
                    </td>
                    <td>
                        ${cookie.path}
                    </td>
                    <td>
                        <#if cookie.expiryDate??>
                            ${cookie.expiryDate?datetime}
                        <#else>
                            Session
                        </#if>
                    </td>
                    <td>
                        ${cookie.persistent?c}
                    </td>
                    <td>
                        ${cookie.secure?c}
                    </td>
                </tr>
            </#list>
        </tbody>
    </table>
    <script src="../../webjars/bootstrap/3.3.6/js/bootstrap.min.js"></script>
</body>
</html>
