<div class="card card-body">
   <div class="container">
      <ul class="nav nav-tabs">
         <li class="nav-item"><a class="nav-link active" data-bs-toggle="tab" href="#pretty">Pretty</a></li>
         <li class="nav-item"><a class="nav-link" data-bs-toggle="tab" href="#origin">Original</a></li>
      </ul>
      <div class="tab-content border rounded">
         <#assign contentType = bodyContentType?split('/')?last>
         <div id="pretty" class="tab-pane container active show">
            <pre><code id = "pretty-code" class="${contentType}"><#outputformat "HTML">${body}</#outputformat></code></pre>
         </div>
         <div id="origin" class="tab-pane container fade">
            <pre><code id = "original-code" class="${contentType}"><#outputformat "HTML">${body}</#outputformat></code></pre>
         </div>
      </div>
   </div>
</div>
