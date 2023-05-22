<#macro report>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Visual tests result table</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.4.1/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap-toggle/2.2.2/css/bootstrap-toggle.min.css">
    <link rel="stylesheet" href="../../webjars/vividus/style.css"/>
</head>
<body>
    <style>
    h3, p {
        font-weight: bold;
    }
    <@custom_css />
    </style>
    <div class="container-fluid">
    <#assign compare = result.actionType.name() != "ESTABLISH">
    <#assign hasBaseline = result.baseline?hasContent>
        <h3>Baseline name: ${result.baselineName}</h3>
        <@custom_section />
        <#if compare>
            <div class="col-md">
                <#if hasBaseline>
                    <label class="checkbox-inline">
                        <input id="diffCheckBox" type="checkbox" data-toggle="toggle" data-on="Diff" data-off="Checkpoint" data-onstyle="danger" data-offstyle="success">
                    </label>
                </#if>
                <@custom_controls />
            </div>
            <div class="col-md-6">
                <p>Baseline</p>
                <#if hasBaseline>
                    <img class="img-responsive" src="data:image/png;base64,${freemarkerMethodCompressImage(result.baseline)}" />
                <#else>
                    <span>No baseline image</span>
                </#if>
            </div>
            <div class="col-md-6">
                <p>Checkpoint</p>
                <#if hasBaseline>
                    <#if result.diff?hasContent>
                        <img id="diff" class="img-responsive" src="data:image/png;base64,${freemarkerMethodCompressImage(result.diff)}" />
                    <#else>
                        <span>No diff image</span>
                    </#if>
                </#if>
                <#if result.checkpoint?hasContent>
                    <img id="checkpoint" class="img-responsive" src="data:image/png;base64,${freemarkerMethodCompressImage(result.checkpoint)}" />
                <#else>
                    <span>No checkpoint image</span>
                </#if>
            </div>
        <#else>
            <div class="col-md">
                <p>Baseline</p>
                <@custom_controls />
                <#if result.checkpoint?hasContent>
                    <img id="checkpoint" class="img-responsive" src="data:image/png;base64,${freemarkerMethodCompressImage(result.checkpoint)}" />
                <#else>
                    <span>No checkpoint image</span>
                </#if>
            </div>
        </#if>
    </div>

    <script src="../../webjars/jquery/3.6.4/jquery.min.js"></script>
    <script src="../../webjars/bootstrap/3.4.1/js/bootstrap.min.js"></script>
    <script src="../../webjars/bootstrap-toggle/2.2.2/js/bootstrap-toggle.min.js"></script>
    <#if compare>
    <script type="text/javascript">
        $(document).ready(function(){
            var diffImage = $("#diff");
            var checkpointImage = $("#checkpoint");
            checkpointImage.show();
            diffImage.hide();
            $("#diffCheckBox").change(function(){
                if($(this).is(":checked"))
                {
                    diffImage.show();
                    checkpointImage.hide()
                }
                else
                {
                    diffImage.hide();
                    checkpointImage.show()
                }
            });
        });
    </script>
    </#if>
</body>
</html>
</#macro>

<#macro custom_css>
</#macro>
<#macro custom_section>
</#macro>
<#macro custom_controls>
</#macro>
