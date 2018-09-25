package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.RedisServer;
import com.jhl.mds.dao.repositories.RedisServerRepository;
import com.jhl.mds.dto.ApiResponse;
import com.jhl.mds.dto.RedisServerDTO;
import com.jhl.mds.jsclientgenerator.JsClientController;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/redis")
@JsClientController(className = "RedisApiClient", fileName = "redis-api-client")
public class RedisApiController {

    private RedisServerRepository redisServerRepository;
    private RedisServerDTO.Converter redisServerDTOConverter;

    public RedisApiController(
            RedisServerRepository redisServerRepository,
            RedisServerDTO.Converter redisServerDTOConverter
    ) {

        this.redisServerRepository = redisServerRepository;
        this.redisServerDTOConverter = redisServerDTOConverter;
    }

    @GetMapping("/")
    public ApiResponse<List<RedisServerDTO>> list() {
        List<RedisServer> servers = redisServerRepository.findAll(Sort.by(Sort.Direction.DESC, "serverId"));
        List<RedisServerDTO> serverDTOs = servers.stream().map(server -> redisServerDTOConverter.from(server)).collect(Collectors.toList());
        return ApiResponse.success(serverDTOs);
    }

    @PutMapping("/")
    public ApiResponse<RedisServerDTO> create(@RequestBody RedisServerDTO dto) {
        return ApiResponse.success(dto);
    }
}
