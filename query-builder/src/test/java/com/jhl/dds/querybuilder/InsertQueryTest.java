package com.jhl.dds.querybuilder;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class InsertQueryTest {

    @Test
    public void testInsert() {
        String sql = new QueryBuilder().insertInto("test").values(Arrays.asList(
                new LinkedHashMap<String, Object>() {{
                    put("id", 1);
                    put("name", "James");
                }},
                new HashMap<String, Object>() {{
                    put("name", "James Test");
                    put("id", 2);
                }},
                new LinkedHashMap<String, Object>() {{
                    put("name", "James Test 2");
                    put("id", 2);
                }}
        )).build();

        Assert.assertEquals("INSERT INTO `test`(`id`, `name`) VALUES ('1', 'James'), ('2', 'James Test'), ('2', 'James Test 2')", sql);
    }
}
