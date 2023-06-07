<#--
Report source: https://lpelypenko.github.io/axe-html-reporter/
-->

<#macro render_checks key checks>
    <#if checks?size != 0>
        <p>${key} of the following should pass:</p>
        <ul class="text-muted">
            <#list checks as check>
                <li>[${check.getImpact()}] <#outputformat 'HTML'>${check.getMessage()}</#outputformat></li>
            </#list>
        </ul>
    </#if>
</#macro>

<!DOCTYPE html>
<html lang="en">
    <head>
        <!-- Required meta tags -->
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
        <link rel="stylesheet" href="../../webjars/bootstrap/5.3.0-alpha3/css/bootstrap.min.css"/>
        <style>
            body, html {
              font-size: 16px !important;
            }

            .violationCard {
                width: 100%;
                margin-bottom: 1rem;
            }

            .violationCardLine {
                display: flex;
                justify-content: space-between;
                align-items: start;
            }

            .learnMore {
                margin-bottom: 0.75rem;
                white-space: nowrap;
                color: #2557a7;
            }

            .card-link {
                color: #2557a7;
            }

            .violationNode {
                font-size: 0.75rem;
            }

            .wrapBreakWord {
                word-break: break-word;
            }

            .summary {
                font-size: 1rem;
            }

            .summarySection {
                margin: 0.5rem 0;
            }

            .hljs {
                white-space: pre-wrap;
                width: 100%;
                background: #f6f6f6 !important;
            }

            p {
                margin-top: 0.3rem;
            }

            li {
                line-height: 1.618;
            }

            .card {
                margin-top: 5px;
            }

            table {
                background-color: #ffffff !important;
            }

            .failed {
                background-color: var(--bs-danger-bg-subtle) !important;
            }

            .failed button {
                color: var(--bs-danger) !important;
            }

            .incompleted {
                background-color: var(--bs-warning-bg-subtle) !important;
            }

            .incompleted button {
                color: var(--bs-warning) !important;
            }

            .passed {
                background-color: var(--bs-success-bg-subtle) !important;
            }

            .passed button {
                color: var(--bs-success) !important;
            }

            .inapplicable {
                background-color: var(--bs-gray-300) !important;
            }

            .inapplicable button {
                color: var(--bs-secondary) !important;
            }
        </style>
        <link rel="stylesheet" href="../../styles.css"/>
        <link rel="stylesheet" href="../../webjars/vividus/buttons.css"/>
        <script src="../../webjars/bootstrap/5.3.0-alpha3/js/bootstrap.min.js"></script>
        <script src="../../webjars/jquery/3.6.4/jquery.min.js"></script>
        <script src="../../webjars/highlight.js/11.7.0/highlight.min.js"></script>
        <script>
            window.addEventListener('DOMContentLoaded', (event) => {
                document.querySelectorAll('pre code').forEach((event) => {
                      hljs.highlightElement(event);
                });

                new bootstrap.Collapse(document.querySelector('#failed'), {
                    toggle: true
                });
            });
        </script>
        <title>AXE Accessibility Results</title>
    </head>
    <body>
        <div style="padding: 2rem">
            <h3>
                Accessibility Report
            </h3>
            <div class="summarySection">
                <div class="summary">
                    <br>
                    Engine: <a href="https://www.deque.com/axe/" target="_blank" class="card-link">Axe Core</a>
                    <br>
                    <#if run.getType() == 'tag'>
                        Standard: ${run.getValues()[0]?upper_case}
                    <#else>
                        Rules: ${run.getValues()?join(', ')}
                    </#if>
                    <br>
                    Page URL: <a href="${url}" target="_blank" class="card-link">${url}</a>
                    <br>
                    <br>
                </div>
            </div>
            <#list entries as entry>
                <#if entry.getResults()?size != 0>
                    <#assign type = entry.getType()?lower_case >
                    <div id="accordion-${type}">
                        <div class="${type} card">
                            <div class="card-header" id="heading-${type}">
                                <button
                                    class="btn btn-accordion collapsed"
                                    data-bs-toggle="collapse"
                                    data-bs-target="#${type}"
                                    aria-expanded="false"
                                    aria-controls="${type}"
                                >
                                    <span>${type?capitalize} checks: ${entry.getResults()?size}</span>
                                </button>
                            </div>
                            <div
                                id="${type}"
                                class="collapse"
                                aria-labelledby="heading-${type}"
                                data-parent="#accordion-${type}"
                            >
                                <div class="card-body">
                                    <table class="table table-striped table-bordered">
                                        <thead>
                                            <tr>
                                                <th style="width: 5%">#</th>
                                                <th style="width: 45%">Description</th>
                                                <th style="width: 15%">Rule ID</th>
                                                <th style="width: 23%">Tags</th>
                                                <th style="width: 7%">Impact</th>
                                                <th style="width: 5%">Count</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <#list entry.getResults() as result>
                                                <tr>
                                                    <th scope="row"><a href="#${result?index + 1}-${type}" class="card-link">${result?index + 1}</a></th>
                                                    <td><#outputformat 'HTML'>${result.getDescription()}</#outputformat></td>
                                                    <td>${result.getId()}</td>
                                                    <td>${result.getTags()?join(', ')}</td>
                                                    <td>${(result.getImpact())!"-"}</td>
                                                    <td>${result.getNodes()?size}</td>
                                                </tr>
                                            </#list>
                                        </tbody>
                                    </table>
                                    <h5>Details</h5>
                                    <#list entry.getResults() as result>
                                        <div class="card violationCard">
                                            <div class="card-body">
                                                <div class="violationCardLine">
                                                    <h5 class="card-title violationCardTitleItem">
                                                        <a id="${result?index + 1}-${type}">${result?index + 1}.</a> <#outputformat 'HTML'>${result.getHelp()}</#outputformat>
                                                    </h5>
                                                    <a
                                                        href="${result.getHelpUrl()}"
                                                        target="_blank"
                                                        class="card-link violationCardTitleItem learnMore"
                                                        >Learn more</a
                                                    >
                                                </div>
                                                <div class="violationCardLine">
                                                    <h6 class="card-subtitle mb-2 text-muted">${result.getId()}</h6>
                                                </div>
                                                <div class="violationCardLine">
                                                    <p class="card-text"><#outputformat 'HTML'>${result.getDescription()}</#outputformat></p>
                                                    <h6 class="card-subtitle mb-2 text-muted violationCardTitleItem">
                                                        ${(result.getImpact())!"-"}
                                                    </h6>
                                                </div>
                                                <div class="violationCardLine">
                                                    <h6 class="card-subtitle mb-2 text-muted violationCardTitleItem">
                                                        Issue Tags: 
                                                        <#list result.getTags() as tag>
                                                            <span class="badge bg-light text-dark"> ${tag} </span>
                                                        </#list>
                                                    </h6>
                                                </div>
                        
                                                <div class="violationNode">
                                                    <table class="table table-sm table-bordered">
                                                        <thead>
                                                            <tr>
                                                                <th style="width: 2%">#</th>
                                                                <th style="width: 49%">Issue Description</th>
                                                                <th style="width: 49%">
                                                                    Checks
                                                                </th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <#list result.getNodes() as node>
                                                                <tr>
                                                                    <td>${node?index}</td>
                                                                    <td>
                                                                        <p><strong>Element location</strong></p>
                                                                        <#list node.getTarget() as target>
                                                                            <pre><code class="css text-wrap">${target}</code></pre>
                                                                        </#list>
                                                                        <p><strong>Element source</strong></p>
                                                                        <pre><code class="html text-wrap"><#outputformat 'HTML'>${node.getHtml()}</#outputformat></code></pre>
                                                                    </td>
                                                                    <td>
                                                                        <div class="wrapBreakWord">
                                                                            <@render_checks key="all" checks=node.getAll() />
                                                                            <@render_checks key="any" checks=node.getAny() />
                                                                            <@render_checks key="none" checks=node.getNone() />
                                                                        </div>
                                                                    </td>
                                                                </tr>
                                                            </#list>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                            </div>
                        </div>
                    </div>
                </#if>
            </#list>
        </div>
    </body>
</html>
