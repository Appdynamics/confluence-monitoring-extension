/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.confluence;


import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.Map;

public class ConfluenceMonitorTest {

    @Test
    public void test() throws TaskExecutionException {
        ConfluenceMonitor monitor = new ConfluenceMonitor();
        Map<String, String> taskArgs = Maps.newHashMap();
        taskArgs.put("config-file", "src/test/resources/conf/config.yml");
        monitor.execute(taskArgs, null);
    }
}
