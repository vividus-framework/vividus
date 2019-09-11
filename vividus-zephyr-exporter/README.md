# Vividus Zephyr Exporter

## Description

Vividus Zephyr Exporter is a tool that exports Test Results of a given project test run into Zephyr.

## How to use

### Prerequisites
* Install [JDK 12](https://jdk.java.net/12/)
* Add JSON format in the following property for your test project:
  ```
  bdd.configuration.formats=JSON
  ```
* Run tests you want to export, after that you can find reports in .json format in directory: .../output/reports/jbehave/
* Download vividus-zephyr-exporter jar from [Artifactory](https://oss.jfrog.org/artifactory/oss-snapshot-local/org/vividus/vividus-zephyr-exporter/0.1.0-SNAPSHOT/)

### Run
* With property file
    * Create file application.properties with content like in the following example (can be created in the same directory as vividus-zephyr-exporter jar):
      ```
      #jira account user name
      org.vividus.facade.jira.model.JiraConfiguration.RGN.username=xxx

      #jira account password
      org.vividus.facade.jira.model.JiraConfiguration.RGN.password=***

      #jira project key
      zephyr-exporter.jira-project-key=HSM

      #actual json files location
      zephyr-exporter.source-directory=/reports/jbehave/
      ```
    * To run vividus-zephyr-exporter jar do as follows, pointing path to application.properties file:
      ```
      java -jar vividus-zephyr-exporter-xxx.jar  --spring.config.location=classpath:/application.properties, ./application.properties
      ```

* With command line arguments
    * Run with command line properties
      ```
      java -jar vividus-zephyr-exporter-xxx.jar --org.vividus.facade.jira.model.JiraConfiguration.RGN.username=xxx --org.vividus.facade.jira.model.JiraConfiguration.RGN.password=*** --zephyr-exporter.jira-project-key=HSM --zephyr-exporter.source-directory=/reports/jbehave/
      ```
