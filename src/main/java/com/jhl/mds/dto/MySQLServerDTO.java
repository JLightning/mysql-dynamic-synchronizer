package com.jhl.mds.dto;

import com.jhl.mds.dao.entities.MySQLServer;
import lombok.*;
import org.springframework.stereotype.Component;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"serverId", "name"})
public class MySQLServerDTO {
    private int serverId;
    private String name;
    private String host;
    private String port;
    private String username;
    private String password;

    @Component
    public static class Converter {

        public MySQLServerDTO from(MySQLServer server) {
            return MySQLServerDTO.builder()
                    .serverId(server.getServerId())
                    .name(server.getName())
                    .host(server.getHost())
                    .port(server.getPort())
                    .username(server.getUsername())
                    .password(server.getPassword())
                    .build();
        }
    }
}
