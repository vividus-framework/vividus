[![Build Status](https://travis-ci.com/vividus-framework/vividus.svg?branch=master)](https://travis-ci.com/vividus-framework/vividus)
[![codecov](https://codecov.io/gh/vividus-framework/vividus/branch/master/graph/badge.svg)](https://codecov.io/gh/vividus-framework/vividus)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=vividus-framework_vividus&metric=alert_status)](https://sonarcloud.io/dashboard?id=vividus-framework_vividus)
[![BrowserStack Status](https://automate.browserstack.com/badge.svg?badge_key=Mm9sbnBHa3FpaGttYmliQnArRmZ1ZWdsa3VsbkxGYjVrV24rL2JyV3RmTT0tLW1NYjFuM2o0UkRDbWZMM2phWm56Z3c9PQ==--3e7e38e3d87990e56aea8eb882d5e22821fdf36b)](https://automate.browserstack.com/public-build/Mm9sbnBHa3FpaGttYmliQnArRmZ1ZWdsa3VsbkxGYjVrV24rL2JyV3RmTT0tLW1NYjFuM2o0UkRDbWZMM2phWm56Z3c9PQ==--3e7e38e3d87990e56aea8eb882d5e22821fdf36b)

## Prerequisites
#### Java
- Install [JDK 12](https://jdk.java.net/12/)
- Configure `JAVA_HOME` environment variable
- Update `PATH` environment variable: add `$JAVA_HOME/bin`

#### Eclipse
- Install [Eclipse IDE 2019-06](https://www.eclipse.org/downloads/packages/release/2019-06/r/eclipse-ide-java-developers)
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

4. [Import project to Eclipse](https://help.eclipse.org/2019-06/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Ftasks-importproject.htm)

## Acknowledgements
[<img src="https://saucelabs.com/images/logo-saucelabs.png" width="180px" alt="SauceLabs">](https://saucelabs.com)

[SauceLabs](https://saucelabs.com/) has provided us with the tools and infrastructure necessary to build a high quality tool by testing our components on a range of browsers and platforms. Thank you.


[![BrowserStack](https://www.browserstack.com/images/mail/browserstack-logo-footer.png)](https://www.browserstack.com)

[BrowserStack](https://www.browserstack.com/) has provided us with the tools and infrastructure necessary to build a high quality tool by testing our components on a range of browsers and platforms. Thank you.
