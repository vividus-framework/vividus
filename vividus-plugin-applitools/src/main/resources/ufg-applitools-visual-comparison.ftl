<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Visual tests result table</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/vividus/style.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap-toggle/2.2.2/css/bootstrap-toggle.min.css">
    <style>
        tbody:hover {
            background-color: #f5f5f5
        }

        .inner-cell {
            border-top: none !important;
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <h3>Baseline name: ${result.baselineName}</h3>
        <table class="table">
            <thead>
                <tr>
                    <th scope="col">Test</th>
                    <th scope="col">OS</th>
                    <th scope="col">Browser</th>
                    <th scope="col">Viewport</th>
                    <th scope="col">Device</th>
                </tr>
            </thead>
            <#list result.testResults as testResult>
                <#assign visualStatus = testResult.isPassed()?then('success', 'failed')>
                <tbody>
                    <tr>
                        <td>${testResult.getName()}</td>
                        <td>${testResult.getOs()}</td>
                        <td>${testResult.getBrowser()}</td>
                        <td>${testResult.getViewport()}</td>
                        <td>${testResult.getDevice()}</td>
                    </tr>
                    <tr>
                        <td class="inner-cell"></td>
                        <td colspan="4" class="inner-cell">
                            <table class="table table-hover">
                                <tr class="${visualStatus}">
                                    <td class="inner-cell">
                                        Visual check is ${testResult.getStatus()}, see <a href="${testResult.getUrl()}" role="button">Applitools report</a> for details.
                                    </td>
                                </tr>
                                <#if testResult.getAccessibilityCheckResult()??>
                                    <#assign accessibilityResult = testResult.getAccessibilityCheckResult()>
                                    <#assign accessibilityStatus = accessibilityResult.isPassed()?then('success', 'failed')>
                                    <tr class="${accessibilityStatus}">
                                        <td class="inner-cell">
                                            ${accessibilityResult.getGuideline()} accessibility check is ${accessibilityResult.getStatus()}, see <a href="${accessibilityResult.getUrl()}" role="button">Applitools report</a> for details.
                                        </td>
                                    </tr>
                                </#if>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </#list>
        </table>
    </div>
</body>
</html>
