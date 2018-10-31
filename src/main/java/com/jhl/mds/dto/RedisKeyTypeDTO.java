package com.jhl.mds.dto;

import com.jhl.mds.jsclientgenerator.JsClientDTO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsClientDTO(fileName = "redis-key-type-dto", className = "RedisKeyTypeDTO")
public class RedisKeyTypeDTO {

    private int serverId;
    private String keyType;
}
