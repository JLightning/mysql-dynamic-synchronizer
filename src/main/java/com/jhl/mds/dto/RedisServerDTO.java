package com.jhl.mds.dto;

import com.jhl.mds.dao.entities.RedisServer;
import com.jhl.mds.jsclientgenerator.JsClientDTO;
import lombok.*;
import org.springframework.stereotype.Component;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"serverId", "name"})
public class RedisServerDTO {
    private int serverId;
    private String name;
    private String host;
    private String port;
    private String username;
    private String password;

    @Component
    public static class Converter {

        public RedisServerDTO from(RedisServer server) {
            return RedisServerDTO.builder()
                    .serverId(server.getServerId())
                    .name(server.getName())
                    .host(server.getHost())
                    .port(server.getPort())
                    .username(server.getUsername())
                    .password(server.getPassword())
                    .build();
        }

        public RedisServer toDAO(RedisServerDTO dto) {
            return RedisServer.builder()
                    .serverId(dto.getServerId())
                    .name(dto.getName())
                    .host(dto.getHost())
                    .port(dto.getPort())
                    .username(dto.getUsername())
                    .password(dto.getPassword())
                    .build();
        }
    }
}
