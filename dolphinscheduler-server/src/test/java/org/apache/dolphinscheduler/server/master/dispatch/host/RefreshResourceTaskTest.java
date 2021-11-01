package org.apache.dolphinscheduler.server.master.dispatch.host;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * RefreshResourceTask test
 */
@RunWith(MockitoJUnitRunner.class)
public class RefreshResourceTaskTest {

  @Mock
  private LowerWeightHostManager.RefreshResourceTask refreshResourceTask;

  @Test
  public void testGetHostWeightWithResult() {
    Assert.assertEquals(refreshResourceTask.getHostWeight(
        "192.168.1.1:22", "default", null), null);
  }
}
