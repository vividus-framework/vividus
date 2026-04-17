Scenario: Validate step "When I wait `$timeout` with `$pollingTimeout` polling until file matching `$fileNameRegex` appears in directory `$directoryPath` and save path to $scopes variable `$variableName`"
When I create temporary file with name `mydata.txt` and content `Hello World!` and put path to scenario variable `filePath`
Given I initialize scenario variable `folderName` with value `#{eval(stringUtils:substringBefore(filePath, 'mydata'))}`
When I wait `PT30S` with `PT1S` polling until file matching `mydata.*.txt` appears in directory `${folderName}` and save path to scenario variable `textFile`
Then `${textFile}` is equal to `${filePath}`
