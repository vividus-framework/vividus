$(document).ready(function() {
   $("code[id='pretty-code']").each(function(i, e) {
       if(e.className.includes("json")){
           var text = $(this).text();
           var pretty =  LosslessJSON.stringify(LosslessJSON.parse(text), null, 2);
           $(this).text(pretty);
       }
       hljs.highlightElement(e);
   });
});
