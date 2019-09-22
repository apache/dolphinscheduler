package cn.escheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class IpUtilsTest {

    @Test
    public void ipToLong() {

        String ip = "192.168.110.1";
        String ip2 = "0.0.0.0";
        long longNumber = IpUtils.ipToLong(ip);
        long longNumber2 = IpUtils.ipToLong(ip2);
        System.out.println(longNumber);
        Assert.assertEquals(longNumber, 3232263681L);
        Assert.assertEquals(longNumber2, 0L);

        String ip3 = "255.255.255.255";
        long longNumber3 = IpUtils.ipToLong(ip3);
        System.out.println(longNumber3);
        Assert.assertEquals(longNumber3, 4294967295L);

    }

    @Test
    public void longToIp() {

        String ip = "192.168.110.1";
        String ip2 = "0.0.0.0";
        long longNum = 3232263681L;
        String i1 = IpUtils.longToIp(longNum);

        String i2 = IpUtils.longToIp(0);

        Assert.assertEquals(ip, i1);
        Assert.assertEquals(ip2, i2);
    }
}