Description: Integration tests for SshSteps class (requires enabling of SSH connection to host)

Meta:
    @epic vividus-plugin-ssh

Scenario: Verify step 'When I execute commands `$commands` on $server over $protocol'
When I execute commands `cd /; pwd` on localhost over <protocol>
When I execute commands `cd /Users` on localhost over <protocol>
Examples:
|protocol|
|SSH     |
|SFTP    |

Scenario: Verify step 'When I execute commands `$commands` on $server over SSH and save $stream stream to $scopes variable `$variableName`'
When I execute commands `cd /Users; pwd` on localhost over SSH
Then `${ssh-stdout}` matches `/Users\s+`
Then `${ssh-exit-status}` is equal to `0`
When I execute commands `cd /Users/any-non-existing-dir` on localhost over SSH
Then `${ssh-stderr}` matches `bash: line 0: cd: /Users/any-non-existing-dir: No such file or directory\s+`
Then `${ssh-exit-status}` is equal to `1`

Scenario: Verify step 'When I execute commands `$commands` on $server over SFTP and save result to $scopes variable `$variableName`'
When I execute commands `cd /Users; pwd` on localhost over SFTP and save result to scenario variable `pwd`
Then `${pwd}` is equal to `/Users`

Scenario: Verify step 'When I execute commands `$commands` on $server over SFTP and save result to $scopes variable `$variableName`'
When I execute commands `get .bash_profile` on localhost over SFTP and save result to scenario variable `bash-profile`
When I create file with content ` ${bash-profile}` at path `.bash_profile-copy` on localhost over SFTP
