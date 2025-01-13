$(document).ready(function() {
   $("code[id='pretty-code']").each(function(i, e) {
       if(e.className.includes("json")){
           var text = $(this).text();
           var pretty =  JSON.stringify(JSON.parse(text), null, 2);
           $(this).text(pretty);
       }
       hljs.highlightElement(e);
   });
});
