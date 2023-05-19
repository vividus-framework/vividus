<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Visual tests result table</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/vividus/style.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap-toggle/2.2.2/css/bootstrap-toggle.min.css">
</head>
<body>
    <div class="container-fluid">
        <h3>Baseline name: ${result.baselineName}</h3>
        <table class="table table-hover">
            <thead>
            <tr>
                <th scope="col">Status</th>
                <th scope="col">Test</th>
                <th scope="col">OS</th>
                <th scope="col">Browser</th>
                <th scope="col">Viewport</th>
                <th scope="col">Device</th>
                <th scope="col">Link</th>
            </tr>
            </thead>
            <tbody>
                <#list result.testResults as testResult>
                    <#if testResult.isPassed()>
                        <tr class="passed">
                    <#else>
                        <tr class="failed">
                    </#if>
                        <td>${testResult.getStatus()}</td>
                        <td>${testResult.getName()}</td>
                        <td>${testResult.getOs()}</td>
                        <td>${testResult.getBrowser()}</td>
                        <td>${testResult.getViewport()}</td>
                        <td>${testResult.getDevice()}</td>
                        <td>
                            <a href="${testResult.getUrl()}" class="btn btn-default btn-sm active" role="button">View in Applitools</a>
                        </td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
</body>
</html>
