/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.jobexecutor;

import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
public class JobExecutorAcquireJobsDefaultTest extends AbstractJobExecutorAcquireJobsTest {

  @Parameterized.Parameter(0)
  public boolean ensureJobDueDateSet;

  @Parameterized.Parameter(1)
  public Date currentTime;

  @Parameterized.Parameters(name = "Job DueDate is set: {0}")
  public static Collection<Object[]> scenarios() throws ParseException {
    return Arrays.asList(new Object[][] {
      { false, null },
      { true, ClockTestUtil.setClockToDateWithoutMilliseconds() }
    });
  }

  @Before
  public void setUp() {
    rule.getProcessEngineConfiguration().setEnsureJobDueDateNotNull(ensureJobDueDateSet);
  }

  @Test
  public void testProcessEngineConfiguration() {
    assertFalse(configuration.isJobExecutorPreferTimerJobs());
    assertFalse(configuration.isJobExecutorAcquireByDueDate());
    assertEquals(ensureJobDueDateSet, configuration.isEnsureJobDueDateNotNull());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml")
  public void testJobDueDateValue() {
    // when
    runtimeService.startProcessInstanceByKey("simpleAsyncProcess");
    List<JobEntity> jobList = findAcquirableJobs();

    // then
    assertEquals(1, jobList.size());
    assertEquals(currentTime, jobList.get(0).getDuedate());
  }
}
