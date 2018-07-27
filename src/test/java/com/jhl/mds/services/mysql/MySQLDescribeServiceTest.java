package com.jhl.mds.services.mysql;

import com.jhl.mds.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MySQLDescribeServiceTest extends BaseTest {

    @Autowired
    private MySQLDescribeService mySQLDescribeService;

    @Test
    public void beautifyFilterTest() {
        Assert.assertEquals("id > 5", mySQLDescribeService.beautifyFilter("id>5"));
        Assert.assertEquals("id > 5", mySQLDescribeService.beautifyFilter("id> 5"));
        Assert.assertEquals("id > 5", mySQLDescribeService.beautifyFilter("id >5"));
        Assert.assertEquals("id > 5 && random_number % 5 == 6", mySQLDescribeService.beautifyFilter("id>5&&random_number%5==6"));
        Assert.assertEquals("id > 5 && random_number % 5 != 6", mySQLDescribeService.beautifyFilter("id >5 &&random_number %5 !=6"));
        Assert.assertEquals("(id + random_number) % 100 <= 5", mySQLDescribeService.beautifyFilter("(id+random_number)%100<=5"));
        Assert.assertEquals("(id + random_number) % 100 > 5", mySQLDescribeService.beautifyFilter("(id+random_number)%100>5"));
        Assert.assertEquals("(a + b) == 5", mySQLDescribeService.beautifyFilter("( a+b )==5"));
    }
}
