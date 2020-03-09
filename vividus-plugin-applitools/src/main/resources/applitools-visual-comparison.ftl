<#include "base-report.ftl">
<#macro custom_css>
.applitools {
  background-color: #1DA69B;
  box-shadow: 0px 8px 15px rgba(0, 0, 0, 0.1);
}
</#macro>
<#macro custom_controls>
<#if result.stepUrl?hasContent>
    <a href="${result.stepUrl}" target="_blank" class="btn btn-info applitools" role="button">Step editor
        <span class="glyphicon glyphicon-wrench"></span>
    </a>
</#if>
<#if result.batchUrl?hasContent>
    <a href="${result.batchUrl}" target="_blank" class="btn btn-info applitools" role="button">Batch
        <span class="glyphicon glyphicon-eye-open"></span>
    </a>
</#if>
</#macro>

<@report />
