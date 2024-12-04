<#ftl strip_whitespace=true>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>CSS properties validation results table</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
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
        tr.pass {
            background-color: #d4edda;
        }
        tr.fail {
            background-color: #f8d7da;
        }
        .icon {
            display: inline-block;
            width: 16px;
            height: 16px;
            text-align: center;
            vertical-align: middle;
        }
        .icon.pass {
            color: #28a745;
        }
        .icon.fail {
            color: #dc3545;
        }
    </style>

    <table class="table table-hover table-bordered table-condensed fixedHeader">
        <thead>
            <tr>
                <th>Css Property</th>
                <th>Actual Value</th>
                <th>Comparison Rule</th>
                <th>Expected</th>
                <th>Result</th>
            </tr>
        </thead>
        <tbody>
            <#list cssResults as cssResult>
                <tr class="${cssResult.passed?string('pass', 'fail')}">
                    <td>${cssResult.cssName}</td>
                    <td>${cssResult.cssActualValue!""}</td>
                    <td>${cssResult.comparisonRule}</td>
                    <td>${cssResult.cssExpectedValue}</td>
                    <td>
                        <span class="icon ${cssResult.passed?string('pass', 'fail')}">
                            ${cssResult.passed?string('✔', '✖')}
                        </span>
                    </td>
                </tr>
            </#list>
        </tbody>
    </table>
    <script src="../../webjars/bootstrap/3.4.1/js/bootstrap.min.js"></script>
</body>
</html>
