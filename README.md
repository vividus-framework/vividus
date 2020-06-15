[![Download](https://api.bintray.com/packages/vividus/maven-releases/org.vividus.vividus/images/download.svg)](https://bintray.com/vividus/maven-releases/org.vividus.vividus/_latestVersion)
[![Vividus CI](https://github.com/vividus-framework/vividus/workflows/Vividus%20CI/badge.svg)](https://github.com/vividus-framework/vividus/actions?query=workflow%3A%22Vividus+CI%22)
[![codecov](https://codecov.io/gh/vividus-framework/vividus/branch/master/graph/badge.svg)](https://codecov.io/gh/vividus-framework/vividus)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=vividus-framework_vividus&metric=alert_status)](https://sonarcloud.io/dashboard?id=vividus-framework_vividus)
[![Stackoverflow](https://img.shields.io/badge/stackoverflow-vividus-green.svg)](http://stackoverflow.com/questions/tagged/vividus)
[![BrowserStack Status](https://automate.browserstack.com/badge.svg?badge_key=VjFGZEtSdWpWZ3QrNkNBeHphOUpLbWFsL2VLMW15eUxySGEwamovU1ArTT0tLVN3dlROQWpVbnlNWWdsOGFxdDYwRGc9PQ==--4afc60c487e3a7d23e327c7b430c81e34277a35e)](https://automate.browserstack.com/public-build/VjFGZEtSdWpWZ3QrNkNBeHphOUpLbWFsL2VLMW15eUxySGEwamovU1ArTT0tLVN3dlROQWpVbnlNWWdsOGFxdDYwRGc9PQ==--4afc60c487e3a7d23e327c7b430c81e34277a35e)
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](code_of_conduct.md)


## Support/Community
[**Slack chat**](https://vividus-support.herokuapp.com/)

## Prerequisites
#### Java
- The baseline is JDK 11. Latest [JDK 14](https://jdk.java.net/14/) is supported as well
- Configure `JAVA_HOME` environment variable
- Update `PATH` environment variable: add `$JAVA_HOME/bin`

#### Eclipse
- Install [Eclipse IDE 2020-03](https://www.eclipse.org/downloads/packages/release/2020-03/r/eclipse-ide-java-developers)
- Add [JBehave plugin](https://jbehave.org/eclipse-integration.html)


## How to build the project?
1. Clone main [Vividus project](https://github.com/vividus-framework/vividus.git)

    ```shell
    git clone --recursive https://github.com/vividus-framework/vividus.git
    ```

2. Build the project (run commands from the project root):

    ```shell
    ./gradlew build eclipse
    ```

4. [Import project to Eclipse](https://help.eclipse.org/2020-03/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-importproject.htm)

## Acknowledgements
[<img src="https://saucelabs.com/images/logo-saucelabs.png" width="180px" alt="SauceLabs">](https://saucelabs.com)

[SauceLabs](https://saucelabs.com/) has provided us with the tools and infrastructure necessary to build a high quality tool by testing our components on a range of browsers and platforms. Thank you.


[![BrowserStack](https://www.browserstack.com/images/mail/browserstack-logo-footer.png)](https://www.browserstack.com)

[BrowserStack](https://www.browserstack.com/) has provided us with the tools and infrastructure necessary to build a high quality tool by testing our components on a range of browsers and platforms. Thank you.

[<img src="https://resources.jetbrains.com/storage/products/intellij-idea/img/meta/intellij-idea_logo_300x300.png" width="100px" alt="IntelliJ IDEA">](https://www.jetbrains.com/?from=Vividus)

[JetBrains](https://www.jetbrains.com/?from=Vividus) has provided us with the tools and IDEs necessary to build a high quality tool. Thank you.
