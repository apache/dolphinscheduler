package org.apache.dolphinscheduler.alert.api;

import org.junit.Assert;
import org.junit.Test;

public class AlertResultTest {

    //Testing that two AlertResult objects are equal when they have same messages and status
    @Test
    public void testAlertResultEqualsTrue(){
        AlertResult ar1 = new AlertResult("Status", "Message");
        AlertResult ar2 = new AlertResult("Status", "Message");
        Assert.assertTrue(ar1.equals(ar2));
    }

    //Testing that two AlertResult objects are not equal when they have different messages and statuses
    @Test
    public void testAlertResultEqualsFalse(){
        AlertResult ar1 = new AlertResult("Status1","Message1");
        AlertResult ar2 = new AlertResult("Status2","Message2");
        Assert.assertFalse(ar1.equals(ar2));
    }

    //Testing that equals() method will be false when comparing an AlertResult to a different type object
    @Test
    public void testAlertResultEqualsDifferentInstanceFalse(){
        AlertResult ar1 = new AlertResult("Message1", "Status1");
        String s2 = "Message1Status1";
        Assert.assertFalse(ar1.equals(s2));
    }

}
