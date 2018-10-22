package com.jhl.dds.querybuilder;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class InsertQueryTest {

    @Test
    public void testInsert() {
        Map<String, Object> obj = new HashMap<String, Object>() {{
            put("id", 1);
            put("name", "James");
        }};
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
                    put("name", "James Test xxx");
                    put("id", 2);
                }}
        )).build();

        System.out.println("sql = " + sql);
    }
}
