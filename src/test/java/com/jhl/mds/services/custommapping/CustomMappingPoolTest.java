package com.jhl.mds.services.custommapping;

import com.jhl.mds.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class CustomMappingPoolTest extends BaseTest {

    @Autowired
    private CustomMappingPool customMappingPool;

    @Test
    public void testResolve() throws Exception {
        HashMap<String, Object> data = new HashMap<String, Object>() {{
            put("id", 5);
        }};
        Assert.assertEquals("test_5", customMappingPool.resolve("'test_' + id", data).get());
        Assert.assertEquals("abc_xyz_5", customMappingPool.resolve("'abc_' + 'xyz_' + id", data).get());
        Assert.assertEquals("id_5", customMappingPool.resolve("'id_' + id", data).get());
    }
}
