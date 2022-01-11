const t = window.performance.timing;

return {
  'TIME_TO_FIRST_BYTE': t.responseStart - t.navigationStart,
  'DNS_LOOKUP_TIME': t.domainLookupEnd - t.domainLookupStart,
  'DOM_CONTENT_LOAD_TIME': t.domComplete - t.domLoading,
  'PAGE_LOAD_TIME': t.loadEventEnd - t.navigationStart
};
