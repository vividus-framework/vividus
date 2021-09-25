[
    <#list country as c>
    {
        "country": "${c}",
        "capital": "${capital[c?index]}"
    }<#sep>,</#sep>
    </#list>
]
