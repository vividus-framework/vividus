<#ftl output_format="HTML">
<!DOCTYPE html>
<html lang="en-us">
<head>
    <meta charset="utf-8" />
    <link rel="stylesheet" href="../../webjars/highlight.js.11.7.0/highlight.min.js" />
    <link
            rel="stylesheet"
            type="text/css"
            href="../../webjars/diff2html/css/diff2html.min.css"
    />
    <script type="text/javascript" src="../../webjars/diff2html/js/diff2html-ui.min.js"></script>
</head>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        let targetElement = document.getElementById('myDiffElement');
        if(${udiff?length} === 0)
        {
            targetElement.style.fontSize = "large";
            targetElement.innerHTML = "Expected and actual string values are identical!";
            return;
        }
        const fileLine = `
            <span class="d2h-file-name-wrapper">
                {{>fileIcon}}
                <span class="d2h-file-name">Expected<=>Actual</span>
                {{>fileTag}}
            </span>
            <label class="d2h-file-collapse">
                <input class="d2h-file-collapse-input" type="checkbox" name="viewed" value="viewed">
                Viewed
            </label>`;
        let configuration = {
            drawFileList: false,
            fileListToggle: false,
            fileListStartVisible: false,
            fileContentToggle: false,
            matching: 'lines',
            outputFormat: 'side-by-side',
            synchronisedScroll: true,
            highlight: true,
            renderNothingWhenEmpty: false,
            rawTemplates: { "generic-file-path": fileLine, "tag-file-changed": "<span/>"},
        };
        let diff2htmlUi = new Diff2HtmlUI(targetElement, `${udiff?replace("${", "\\${")?replace("`", "\\`")?no_esc}`, configuration);
        diff2htmlUi.draw();
        diff2htmlUi.highlightCode();
    });
</script>
<body>
<div id="myDiffElement"></div>
</body>
</html>
