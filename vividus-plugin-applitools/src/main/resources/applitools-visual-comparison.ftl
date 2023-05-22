<#include "base-report.ftl">
<#macro custom_css>
.applitools {
  background-color: #1DA69B;
  box-shadow: 0px 8px 15px rgba(0, 0, 0, 0.1);
}

.dot {
  height: 13px;
  width: 13px;
  border-radius: 50%;
  display: inline-block;
}
</#macro>

<#macro custom_section>
 <#if result.applitoolsTestResults.accessibilityCheckResult??>
     <#assign checkResult = result.applitoolsTestResults.accessibilityCheckResult>
     <#assign statusColor = checkResult.isPassed()?then('green', 'red')>
     <div style="margin-top: 25px; margin-bottom: 25px">
         <h5>
             <span class="dot" style="background: ${statusColor}"></span>
             ${checkResult.getGuideline()} accessibility check is ${checkResult.getStatus()}<#if checkResult.getUrl()?hasContent>, see <a href="${checkResult.getUrl()}">Accessibility Report</a></#if>
         </h5>
     </div>
 </#if>
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
