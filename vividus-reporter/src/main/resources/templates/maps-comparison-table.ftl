<#ftl strip_whitespace=true>
<#setting boolean_format="true ,false ">
<#assign valueLimit = 10>
<#assign visibilityThreshold = 13>
<#assign abbreviation = '...'>
<#assign more = 'more'>
<#assign less = 'less'>
<#assign animationDuration = 600>
<#function createCell input>
  <#if visibilityThreshold < (input?length)>
      <#assign visiblePart = input[0..valueLimit]>
      <#assign hiddenPart = input[valueLimit + 1..]>
      <#return visiblePart + '<span class="abbreviation">' + abbreviation
                    + '</span><span class="hiddenContent"><span>' + hiddenPart
                    + '</span><a href="" class="exceed">' + more + '</a></span>'>
  </#if>
  <#return input>
</#function>

<#function getClassName input>
    <#if input == "null">
        <#return input>
    </#if>
  <#return input?keep_before(".class")?keep_after_last(".")>
</#function>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Analytics result table</title>
    <link rel="stylesheet" href="../../styles.css"/>
    <link rel="stylesheet" href="../../webjars/bootstrap/3.3.6/css/bootstrap.min.css"/>
</head>
<body>
    <style>
        .passed {
            background-color: #DFF0D8;
            color: #3C763D;
        }
        .failed {
            background-color: #F2DEDE;
            color: #A94442;
        }
        .value-failed:nth-child(even) {
            background-color: #F2DEDE;
            color: #A94442;
        }
        table {
            border-collapse: collapse;
            width: 100%;
        }
        table th {
            text-align: center;
            font-family: Arial, Helvetica, sans-serif;
        }
        .hiddenContent span {
            display: none;
        }
        .exceed {
            display: block;
        }
        .floating{
            position:fixed;
            width:30px;
            height:30px;
            bottom:20px;
            right:20px;
            background-color:#4FD5D6;
            color:#400D12;
            border-radius:20px;
            text-align:center;
            box-shadow: 2px 2px 3px #999;
        }

        .icon{
            margin-top: 10px;
        }
    </style>

    <table class="table table-hover table-bordered table-condensed">
        <a class="floating" id="expander" href="#">
            <i class="fa fa-plus icon"></i>
        </a>
        <thead>
            <#assign headCell = (results?size > 0)?then(results[0], '') >
            <#if headCell?has_content >
            <tr>
                <th>Line</th>
                <#list results[0] as cell>
                    <th colspan="2">
                        ${(cell.key)!}
                    </th>
                </#list>
            </tr>
            <tr>
                <th/>
                <#list results[0] as cell>
                    <th>Left (${getClassName(cell.getLeftClassName())})</th>
                    <th>Right (${getClassName(cell.getRightClassName())})</th>
                </#list>
            </tr>
            <#else>
            <tr>
                <th>Both tables are empty</th>
            </tr>
            </#if>
        </thead>
        <tbody>
            <#assign lineNumber = 1>
            <#list results as row>
                <tr>
                    <td>
                        ${lineNumber}
                    </td>
                <#assign cellNumber = 0>
                <#list row as cell>
                    <#assign class>
                        <#if cell.passed>
                            ${"passed"}
                        <#else>
                            ${"failed"}
                        </#if>
                    </#assign>
                    <#assign left = (cell.left)!"null">
                    <#assign right = (cell.right)!"null">
                    <td class="${class}">
                        <span class="exceedable">${createCell(left)}</span>
                    </td>
                    <td class="${class}">
                        <span class="exceedable">${createCell(right)}</span>
                    </td>
                    <#assign cellNumber++>
                </#list>
                <#assign lineNumber++>
                </tr>
            </#list>
        </tbody>
    </table>
    <script src="../../webjars/jquery/2.1.1/jquery.min.js"></script>
    <script src="../../webjars/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script type="text/javascript">
        $(document).ready(function()
        {
            $(".exceed").click(function()
            {
                if($(this).hasClass('${less}'))
                {
                   $(this).removeClass('${less}');
                   $(this).html('${more}');
                }
                else
                {
                    $(this).addClass('${less}');
                    $(this).html('${less}');
                }
                $(this).parent().prev().toggle(${animationDuration});
                $(this).prev().toggle(${animationDuration});
                return false;
           });

           $("#expander").click(function()
           {
               var icon = $(this).find('i');
               var expand = icon.hasClass('fa-plus')
               $(".exceed").each(function()
               {
                   var expanded = $(this).hasClass('${less}');
                   if ((!expanded && expand) || (expanded && !expand))
                   {
                       $(this).click()
                   };
               });
               if(expand)
               {
                   icon.removeClass('fa-plus');
                   icon.addClass('fa-minus');
               }
               else
               {
                   icon.removeClass('fa-minus');
                   icon.addClass('fa-plus');
               }
           });
        });
    </script>
</body>
</html>