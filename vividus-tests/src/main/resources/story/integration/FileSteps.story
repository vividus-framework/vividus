Scenario: Validate file expressions & step "When I wait `$timeout` with `$pollingTimeout` polling until file matching `$fileNameRegex` appears in directory `$directoryPath` and save path to $scopes variable `$variableName`"
Given I initialize scenario variable `testContent` with value `Hello World!`

When I create temporary file with name `mydata.txt` and content `${testContent}` and put path to scenario variable `filePath`
Given I initialize scenario variable `folderName` with value `#{eval(stringUtils:substringBefore(filePath, 'mydata'))}`
When I wait `PT30S` with `PT1S` polling until file matching `mydata.*.txt` appears in directory `${folderName}` and save path to scenario variable `textFile`
Then `${textFile}` is equal to `${filePath}`

Given I initialize scenario variable `fileContent` with value `#{loadFile(${filePath})}`
Then `${fileContent}` is equal to `${testContent}`

Given I initialize scenario variable `fileBinaryContent` with value `#{loadBinaryFile(${filePath})}`
Then `#{evalGroovy(new String(fileBinaryContent, "UTF-8") )}` is equal to `${testContent}`
