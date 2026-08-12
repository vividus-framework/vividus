Meta:
    @epic vividus-plugin-winrm

Scenario: Verify step: 'When I execute batch command `$command` on server `$connectionKey` using WinRM and save result to $scopes variable `$variableName`'
When I execute batch command `echo Hello` on server `winrm-server` using WinRM and save result to SCENARIO variable `result`
Then `${result.exit-status}` is equal to `0`
Then `${result.stderr}` is equal to ``
Then `${result.stdout}` matches `(?s).*Hello.*`

Scenario: Verify step: 'When I execute PowerShell command `$command` on server `$connectionKey` using WinRM and save result to $scopes variable `$variableName`'
When I execute PowerShell command `Write-Output 'Hello from PS'` on server `winrm-server` using WinRM and save result to SCENARIO variable `result`
Then `${result.exit-status}` is equal to `0`
Then `${result.stdout}` matches `(?s).*Hello from PS.*`

Scenario: Verify step: 'When I configure WinRM connection with key `$connectionKey` and parameters:$connectionParameters'
When I configure WinRM connection with key `dynamic-server` and parameters:
|address                      |username                      |password                      |authentication-scheme                      |disable-certificate-checks|
|${winrm-server-address}      |${winrm-server-username}      |${winrm-server-password}      |${winrm-server-authentication-scheme}      |true                      |
When I execute PowerShell command `Write-Output 'Dynamic connection works'` on server `dynamic-server` using WinRM and save result to SCENARIO variable `result`
Then `${result.exit-status}` is equal to `0`
Then `${result.stdout}` matches `(?s).*Dynamic connection works.*`
