<#ftl strip_whitespace=true>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>CSS properties validation results table</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/5.3.1/css/bootstrap.min.css"/>
</head>
<body>
    <style>
	    .result-icon {
            display: inline-block;
            width: 16px;
            height: 16px;
            text-align: center;
        }
        .result-icon.pass {
            color: #28a745;
        }
        .result-icon.fail {
            color: #dc3545;
        }
        table {
            border-collapse: collapse;
            width: 100%;
        }
		table td:nth-child(1) {
            width: 20px;
            text-align: center;
        }
        table td:nth-child(2) {
            width: 25%;
        }
        table td:nth-child(3) {
            width: 30%;
        }
        table td:nth-child(4) {
            width: 15%;
        }
        table td:nth-child(5) {
            width: 30%;
        }
        table th {
            text-align: center;
            font-family: Arial, Helvetica, sans-serif;
        }
    </style>

    <table class="table table-primary table-hover table-bordered table-sm">
        <thead>
            <tr>
                <th />
                <th>CSS Property</th>
                <th>Actual Value</th>
                <th>Rule</th>
                <th>Expected Value</th>
            </tr>
        </thead>
        <tbody>
            <#list cssResults as cssResult>
                <tr class="${cssResult.passed?string('table-success', 'table-danger')}">
                    <td>
                        <span class="result-icon ${cssResult.passed?string('pass', 'fail')}">
                            ${cssResult.passed?string('<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-check-circle" viewBox="0 0 16 16">
                                                        <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14m0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16"/>
                                                        <path d="m10.97 4.97-.02.022-3.473 4.425-2.093-2.094a.75.75 0 0 0-1.06 1.06L6.97 11.03a.75.75 0 0 0 1.079-.02l3.992-4.99a.75.75 0 0 0-1.071-1.05"/>
                                                       </svg>',
                                                       '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-exclamation-triangle" viewBox="0 0 16 16">
                                                        <path d="M7.938 2.016A.13.13 0 0 1 8.002 2a.13.13 0 0 1 .063.016.15.15 0 0 1 .054.057l6.857 11.667c.036.06.035.124.002.183a.2.2 0 0 1-.054.06.1.1 0 0 1-.066.017H1.146a.1.1 0 0 1-.066-.017.2.2 0 0 1-.054-.06.18.18 0 0 1 .002-.183L7.884 2.073a.15.15 0 0 1 .054-.057m1.044-.45a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767z"/>
                                                        <path d="M7.002 12a1 1 0 1 1 2 0 1 1 0 0 1-2 0M7.1 5.995a.905.905 0 1 1 1.8 0l-.35 3.507a.552.552 0 0 1-1.1 0z"/>
                                                       </svg>')}
                        </span>
                    </td>
                    <td>${cssResult.cssProperty}</td>
                    <td>${cssResult.actualValue!""}</td>
                    <td>${cssResult.comparisonRule}</td>
                    <td>${cssResult.expectedValue}</td>
                </tr>
            </#list>
        </tbody>
    </table>
    <script src="../../webjars/bootstrap/5.3.1/js/bootstrap.min.js"></script>
</body>
</html>
