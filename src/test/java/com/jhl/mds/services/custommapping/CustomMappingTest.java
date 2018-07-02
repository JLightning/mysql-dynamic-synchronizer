package com.jhl.mds.services.custommapping;

import com.jhl.mds.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class CustomMappingTest extends BaseTest {

    @Autowired
    private CustomMapping customMapping;

    @Test
    public void testResolve() throws Exception {
        HashMap<String, Object> data = new HashMap<String, Object>() {{
            put("id", 5);
        }};
        Assert.assertEquals("test_5", customMapping.resolve("'test_' + id", data));
        Assert.assertEquals("abc_xyz_5", customMapping.resolve("'abc_' + 'xyz_' + id", data));
        Assert.assertEquals("id_5", customMapping.resolve("'id_' + id", data));
    }
}
