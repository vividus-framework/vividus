<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Data Sources Statistics</title>
    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon">
    <link rel="icon" href="img/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/5.3.1/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        pre {
            white-space: pre-wrap;
            word-break: normal;
        }
        .accordion-item-info .accordion-button {
            background-color: #d9edf7;
            color: #31708f;
        }
        .accordion-item-info .accordion-button:not(.collapsed) {
            background-color: #bce8f1;
            color: #31708f;
        }
        .accordion-item-info .accordion-button::after {
            filter: invert(35%) sepia(30%) saturate(500%) hue-rotate(155deg);
        }
    </style>

    <#outputformat "HTML">
    <#assign left = statistics.left>
    <#assign right = statistics.right>
    <div class="accordion" id="accordion">
        <#if left.query?has_content>
            <div class="accordion-item accordion-item-info">
                <h4 class="accordion-header">
                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse-left-sql" aria-expanded="false" aria-controls="collapse-left-sql">
                        Left query
                    </button>
                </h4>
                <div id="collapse-left-sql" class="accordion-collapse collapse">
                    <div class="accordion-body">
                        <pre><code class="sql">${left.query}</code></pre>
                    </div>
                </div>
            </div>
        </#if>

        <#if right.query?has_content>
            <div class="accordion-item accordion-item-info">
                <h4 class="accordion-header">
                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse-right-sql" aria-expanded="false" aria-controls="collapse-right-sql">
                        Right query
                    </button>
                </h4>
                <div id="collapse-right-sql" class="accordion-collapse collapse">
                    <div class="accordion-body">
                        <pre><code class="sql">${right.query}</code></pre>
                    </div>
                </div>
            </div>
        </#if>

        <div class="card mt-3">
            <div class="card-body">
                <table class="table table-bordered table-striped table-hover">
                    <thead>
                        <tr>
                            <th>Parameter</th>
                            <th>Left data source</th>
                            <th>Right data source</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#assign emptyTime="00:00:00.000">
                        <#if !(left.getExecutionTime() == emptyTime && right.getExecutionTime() == emptyTime)>
                        <tr>
                            <td>Execution time hh:mm:ss:SSS</td>
                            <td>${left.getExecutionTime()}</td>
                            <td>${right.getExecutionTime()}</td>
                        </tr>
                        </#if>
                        <tr>
                            <td>Rows Quantity</td>
                            <td>${left.rowsQuantity}</td>
                            <td>${right.rowsQuantity}</td>
                        </tr>
                        <tr>
                            <td>No pair found</td>
                            <td>${(left.noPair)!'N/A'}</td>
                            <td>${(right.noPair)!'N/A'}</td>
                        </tr>
                        <tr>
                            <td>Connection</td>
                            <td>${(left.url)!'N/A'}</td>
                            <td>${(right.url)!'N/A'}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="container-fluid">
                <div class="row">
                    <div class="col-6">
                        <h4 style='text-align: center'>Total unique rows: ${statistics.totalRows}</h4>
                    </div>
                    <div class="col-6">
                        <h4 style='text-align: center'>Counts difference: ${(left.rowsQuantity - right.rowsQuantity)?abs}</h4>
                    </div>
                </div>
                <p align="center">
                    <canvas id="statistics-pie-chart" style="max-width: 360px; max-height: 360px;"></canvas>
                </p>
            </div>
        </div>
    </div>
    </#outputformat>

    <script src="../../webjars/jquery/3.6.4/jquery.min.js"></script>
    <script src="../../webjars/bootstrap/5.3.1/js/bootstrap.min.js"></script>
    <script src="../../webjars/highlight.js/11.7.0/highlight.min.js"></script>
    <script src="../../webjars/chart.js/3.2.1/chart.min.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $(".sql").each(function(i,e) {
                hljs.highlightElement(e);
            });
        });
    </script>

    <script type="text/javascript">
        var ctx = document.getElementById("statistics-pie-chart").getContext('2d');
        var myPieChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ["Mismatched", "Matched"],
            datasets: [{
                data: [${statistics.mismatched?long?c}, ${statistics.getMatched()?long?c}],
                backgroundColor: ["#F7464A", "#46BFBD"],
                hoverBackgroundColor: ["#FF5A5E", "#5AD3D1"]
            }]
        }
        });
    </script>

</body>
</html>
