/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.confluence.metrics;

import static com.appdynamics.extensions.confluence.ConfigConstants.EXCLUDE;
import static com.appdynamics.extensions.confluence.ConfigConstants.INCLUDE;
import static com.appdynamics.extensions.confluence.ConfigConstants.METRICS;
import static com.appdynamics.extensions.confluence.ConfigConstants.OBJECT_NAME;
import static com.appdynamics.extensions.confluence.Util.convertToString;

import com.appdynamics.extensions.confluence.JMXConnectionAdapter;
import com.appdynamics.extensions.confluence.filters.ExcludeFilter;
import com.appdynamics.extensions.confluence.filters.IncludeFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.LoggerFactory;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DomainMetricsProcessor {

    static final org.slf4j.Logger logger = LoggerFactory.getLogger(DomainMetricsProcessor.class);

    private final JMXConnectionAdapter jmxAdapter;
    private final JMXConnector jmxConnection;


    private final MetricKeyFormatter keyFormatter = new MetricKeyFormatter();
    private final MetricValueTransformer valueConverter = new MetricValueTransformer();

    public DomainMetricsProcessor(JMXConnectionAdapter jmxAdapter, JMXConnector jmxConnection) {
        this.jmxAdapter = jmxAdapter;
        this.jmxConnection = jmxConnection;
    }

    public List<Metric> getNodeMetrics(Map aConfigMBean, Map<String, MetricProperties> metricPropsMap) throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException, MalformedObjectNameException {
        List<Metric> nodeMetrics = Lists.newArrayList();
        String configObjectName = convertToString(aConfigMBean.get(OBJECT_NAME), "");
        Set<ObjectInstance> objectInstances = jmxAdapter.queryMBeans(jmxConnection, ObjectName.getInstance(configObjectName));
        for (ObjectInstance instance : objectInstances) {
            List<String> metricNamesDictionary = jmxAdapter.getReadableAttributeNames(jmxConnection, instance);
            List<String> metricNamesToBeExtracted = applyFilters(aConfigMBean, metricNamesDictionary);
            List<Attribute> attributes = jmxAdapter.getAttributes(jmxConnection, instance.getObjectName(), metricNamesToBeExtracted.toArray(new String[metricNamesToBeExtracted.size()]));
            collect(nodeMetrics, attributes, instance, metricPropsMap);
        }
        return nodeMetrics;
    }

    private List<String> applyFilters(Map aConfigMBean, List<String> metricNamesDictionary) throws IntrospectionException, ReflectionException, InstanceNotFoundException, IOException {
        Set<String> filteredSet = Sets.newHashSet();
        Map configMetrics = (Map) aConfigMBean.get(METRICS);
        List includeDictionary = (List) configMetrics.get(INCLUDE);
        List excludeDictionary = (List) configMetrics.get(EXCLUDE);
        new ExcludeFilter(excludeDictionary).apply(filteredSet, metricNamesDictionary);
        new IncludeFilter(includeDictionary).apply(filteredSet, metricNamesDictionary);
        return Lists.newArrayList(filteredSet);
    }

    private void collect(List<Metric> nodeMetrics, List<Attribute> attributes, ObjectInstance instance, Map<String, MetricProperties> metricPropsPerMetricName) {
        for (Attribute attr : attributes) {
            try {
                String attrName = attr.getName();
                MetricProperties props = metricPropsPerMetricName.get(attrName);
                if (props == null) {
                    logger.error("Could not find metric props for {}", attrName);
                    continue;
                }
                //get metric value by applying conversions if necessary
                BigDecimal metricValue = valueConverter.transform(attrName, attr.getValue(), props);
                if (metricValue != null) {
                    Metric nodeMetric = new Metric();
                    nodeMetric.setMetricName(attrName);
                    String metricName = nodeMetric.getMetricNameOrAlias();
                    nodeMetric.setProperties(props);
                    String domainName = keyFormatter.getKeyProperty(instance, "name");
                    nodeMetric.setMetricKey(domainName + "|" + metricName);
                    nodeMetric.setMetricValue(metricValue);
                    nodeMetrics.add(nodeMetric);
                }


            } catch (Exception e) {
                logger.error("Error collecting value for {} {}", instance.getObjectName(), attr.getName(), e);
            }
        }
    }
}
