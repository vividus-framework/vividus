<#ftl strip_whitespace=true>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Archive entries</title>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        table {
            border-collapse: collapse;
            width: 100%;
            counter-reset: entryCounter;
        }
        table th {
            text-align: center;
            font-family: Arial, Helvetica, sans-serif;
        }
        table tbody tr:before {
            counter-increment: entryCounter;
            content: counter(entryCounter);
            display: table-cell;
            vertical-align: middle;
            text-align: center;
        }
    </style>

    <#if entryNames?has_content>
      <table class="table table-hover table-bordered table-condensed fixedHeader">
         <thead>
            <tr>
                <th>
                    #
                </th>
                <th>
                    Name
                </th>
            </tr>
         </thead>
         <tbody>
            <#list entryNames as entry>
                <tr>
                    <td>
                        ${entry}
                    </td>
                </tr>
            </#list>
         </tbody>
      </table>
    <#else>
      There are no files in the archive
    </#if>
</body>
</html>
