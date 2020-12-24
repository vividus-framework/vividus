<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Queries statistics</title>
    <link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon">
    <link rel="icon" href="img/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.3.6/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        pre {
            white-space: pre-wrap;
            word-break: normal;
        }
        a[data-toggle='collapse'] {
            display: inline-block;
            width: 100%;
            height: 100%;
        }
        .toggleable:hover {
             cursor: pointer;
        }
        .panel-heading a:after {
            font-family:'FontAwesome';
            content:"\F103";
            float: right;
            color: grey;
        }
        .panel-heading a.collapsed:after {
            content:"\F101";
        }
    </style>

    <#assign source = statistics.source>
    <#assign target = statistics.target>
    <div class="panel-group" id="accordion">
        <#if source.query?has_content>
        <div class="panel panel-info">
            <div class="panel-heading">
                <h4 class="panel-title toggleable">
                    <a data-toggle="collapse" data-target="#collapse-source-sql" href="#collapse-source-sql" class="collapsed">Source query</a>
                </h4>
            </div>
            <div id="collapse-source-sql" class="panel-collapse collapse">
                <pre><code class="sql">${source.query}</code></pre>
            </div>
        </div>
        </#if>

        <#if target.query?has_content>
        <div class="panel panel-info">
            <div class="panel-heading">
                <h4 class="panel-title toggleable">
                    <a data-toggle="collapse" data-target="#collapse-target-sql" href="#collapse-target-sql" class="collapsed">Target query</a>
                </h4>
            </div>
            <div id="collapse-target-sql" class="panel-collapse collapse">
                <pre><code class="sql">${target.query}</code></pre>
            </div>
        </div>
        </#if>

        <div class="panel panel-info">
            <div class="panel-heading">
                <h4 class="panel-title toggleable">
                    <a data-toggle="collapse" data-target="#collapse-statistics" href="#collapse-statistics" class="collapsed">Statistics</a>
                </h4>
            </div>
            <div id="collapse-statistics" class="panel-collapse collapse in">
                <div class="container">
                    <table class="table table-bordered table-stripped table-hover">
                        <thead>
                            <tr>
                                <th>Parameter</th>
                                <th>Source</th>
                                <th>Target</th>
                            </tr>
                        </thead>
                        <tbody>
                            <#assign emptyTime="00:00:00.000">
                            <#if !(source.getExecutionTime() == emptyTime && target.getExecutionTime() == emptyTime)>
                            <tr>
                                <td>Execution time hh:mm:ss:SSS</td>
                                <td>${source.getExecutionTime()}</td>
                                <td>${target.getExecutionTime()}</td>
                            </tr>
                            </#if>
                            <tr>
                                <td>Rows Quantity</td>
                                <td>${source.rowsQuantity}</td>
                                <td>${target.rowsQuantity}</td>
                            </tr>
                            <tr>
                                <td>No pair found</td>
                                <td>${source.noPair}</td>
                                <td>${target.noPair}</td>
                            </tr>
                            <tr>
                                <td>Connection</td>
                                <td>${(source.url)!'N/A'}</td>
                                <td>${(target.url)!'N/A'}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                 <div class="container-fluid">
                     <div class="row">
                         <div class="col-xs-6">
                             <h4 style='text-align: center'>Total unique rows: ${statistics.totalRows}</h4>
                         </div>
                         <div class="col-xs-6">
                            <h4 style='text-align: center'>Counts diffrence: ${(source.rowsQuantity - target.rowsQuantity)?abs}</h4>
                         </div>
                     </div>
                     <p align="center">
                         <canvas id="statistics-pie-chart" style="max-width: 360px;"></canvas>
                     </p>
                </div>
            </div>
        </div>
    </div>

    <script src="../../webjars/jquery/2.1.1/jquery.min.js"></script>
    <script src="../../webjars/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="../../webjars/highlight.js/9.12.0/highlight.min.js"></script>
    <script src="../../webjars/chart.js/2.4.0/Chart.min.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $(".sql").each(function(i,e) {
                hljs.highlightBlock(e);
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
