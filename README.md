AppDynamics Monitoring Extension for use with Atlassian Confluence
===================================================================

An AppDynamics extension to be used with a stand alone Java machine agent to provide metrics for Atlassian Confluence


## Use Case ##

Atlassian Confluence is a wiki used by more than half of Fortune 100 companies to connect people with the content and co-workers they need to get their jobs done, faster. Connect your entire business in one place online to collaborate and capture knowledge – create, share, and discuss your documents, ideas, minutes, and projects.

## Prerequisites ##

This extension extracts the metrics from Confluence using the JMX protocol.
By default, Confluence starts with local or remote JMX disabled. Please follow the below link to enable JMX

https://confluence.atlassian.com/doc/live-monitoring-using-the-jmx-interface-150274182.html

To know more about JMX, please follow the below link

 http://docs.oracle.com/javase/6/docs/technotes/guides/management/agent.html


## Troubleshooting steps ##
Before configuring the extension, please make sure to run the below steps to check if the set up is correct.

1. Telnet into your Confluence server from the box where the extension is deployed.
       ```
          telnet <hostname> <port>

          <port> - It is the jmxremote.port specified.
          <hostname> - IP address
       ```


    If telnet works, it confirm the access to the Confluence server.


2. Start jconsole. Jconsole comes as a utility with installed jdk. After giving the correct host and port , check if Confluence
mbean shows up.

3. It is a good idea to match the mbean configuration in the config.yml against the jconsole. JMX is case sensitive so make
sure the config matches exact.

## Metrics Provided ##

In addition to the metrics exposed by Confluence, we also add a metric called "Metrics Collection Successful" with a value 0 when an error occurs and 1 when the metrics collection is successful.

Note : By default, a Machine agent or a AppServer agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).
For eg.
```
    java -Dappdynamics.agent.maxMetrics=2500 -jar machineagent.jar
```


## Installation ##

1. Run "mvn clean install" and find the ConfluenceMonitor.zip file in the "target" folder. You can also download the ConfluenceMonitor.zip from [AppDynamics Exchange][].
2. Unzip as "ConfluenceMonitor" and copy the "ConfluenceMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`


# Configuration ##

Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the Confluence instances by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/ConfluenceMonitor/`.
2. Below is the default config.yml which has metrics configured already
   For eg.

   ```
      ### ANY CHANGES TO THIS FILE DOES NOT REQUIRE A RESTART ###

      #This will create this metric in all the tiers, under this path
      metricPrefix: Custom Metrics|Confluence

      #This will create it in specific Tier/Component. Make sure to replace <COMPONENT_ID> with the appropriate one from your environment.
      #To find the <COMPONENT_ID> in your environment, please follow the screenshot https://docs.appdynamics.com/display/PRO42/Build+a+Monitoring+Extension+Using+Java
      #metricPrefix: Server|Component:<COMPONENT_ID>|Custom Metrics|Confluence

      # List of Confluence Instances
      instances:
        - host: "localhost"
          port: 8889
          username:
          password:
          #encryptedPassword:
          #encryptionKey:
          displayName: "LocalConfluence"  #displayName is a REQUIRED field for  level metrics.


      # number of concurrent tasks.
      # This doesn't need to be changed unless many instances are configured
      numberOfThreads: 10


      # The configuration of different metrics from various mbeans of Confluence server
      # For most cases, the mbean configuration does not need to be changed.
      mbeans:
        - objectName: "Confluence:name=IndexingStatistics"
          #aggregation: true #uncomment this only if you want the extension to do aggregation for all the metrics in this mbean for a cluster
          metrics:
            include:
              - Flushing : "Flushing"
                convert : {
                  true : "1",
                  false : "2"
                }
              - LastElapsedMilliseconds : "LastElapsedMilliseconds"
              - LastElapsedReindexing : "LastElapsedReindexing"
              - TaskQueueLength : "TaskQueueLength"

        - objectName: "Confluence:name=MailTaskQueue"
          metrics:
            include:
              - ErrorQueueSize : "ErrorQueueSize"
              - Flushing : "Flushing"
                convert : {
                  true : "1",
                  false : "2"
                }
              - RetryCount : "RetryCount"
              - TasksSize : "TasksSize"

   ```

3. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/ConfluenceMonitor/` directory. Below is the sample
   For Windows, make sure you enter the right path.
     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/ConfluenceMonitor/config.yml" />
          ....
     </task-arguments>
    ```


## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppDynamics Exchange][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 1.0.0
**Controller Compatibility:** 3.7+
**Confluence Versions Tested On:** 5.10.4

[Github]: https://github.com/Appdynamics/confluence-monitoring-extension
[AppDynamics Exchange]: http://community.appdynamics.com/t5/AppDynamics-eXchange/idb-p/extensions
[AppDynamics Center of Excellence]: mailto:help@appdynamics.com
