package com.jhl.mds.services.customfilter;

import com.jhl.mds.BaseTest;
import com.jhl.mds.services.customefilter.CustomFilterPool;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class CustomFilterPoolTest extends BaseTest {

    @Autowired
    private CustomFilterPool customFilterPool;

    @Test
    public void resolveTest() throws ExecutionException, InterruptedException {
        Assert.assertEquals(true, customFilterPool.resolve("id > 5 && name.length > 3", new HashMap<String, Object>() {{
            put("id", 6);
            put("name", "Minh");
        }}).get());
    }
}
