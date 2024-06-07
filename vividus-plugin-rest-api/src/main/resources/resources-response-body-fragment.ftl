<#if result.responseBody??>
<#assign hash = result.hashCode()?c>
<button type="button" class="btn btn-primary" data-toggle="modal" data-target="#modal_${hash}">
Show HTTP response
</button>
<div class="modal fade" id="modal_${hash}" tabindex="-1" role="dialog" aria-labelledby="modalLabel">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modalLabel">Response body</h4>
            </div>
            <div class="modal-body">
                <pre><code id="pretty-code" class="html"><#outputformat "HTML">${result.responseBody}</#outputformat></code></pre>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
</#if>

<script src="../../webjars/jquery/3.6.4/jquery.min.js"></script>
<script src="../../webjars/highlight.js/11.7.0/highlight.min.js"></script>
<script src="../../webjars/js-beautify/1.13.4/beautify-html.min.js"></script>
<script type="text/javascript">
    (function() {
            let code = document.querySelector('#pretty-code');
            <#include "templates/html-formatter-fragment.ftl">
        hljs.highlightElement(code);
    })();
</script>
