package com.jhl.mds.controllers.api;

import com.jhl.mds.dto.ApiResponse;
import com.jhl.mds.dto.migration.MigrationDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.jsclientgenerator.JsClientController;
import com.jhl.mds.services.migration.mysql2mysql.StructureMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@JsClientController(className = "ToolApiClient", fileName = "tool-api-client")
@RequestMapping("/api/tool")
public class ToolApiController {

    private MigrationDTO.Converter fullMigrationDTOConverter;
    private StructureMigrationService structureMigrationService;

    @Autowired
    public ToolApiController(
            MigrationDTO.Converter fullMigrationDTOConverter,
            StructureMigrationService structureMigrationService
    ) {
        this.fullMigrationDTOConverter = fullMigrationDTOConverter;
        this.structureMigrationService = structureMigrationService;
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse exceptionHandler(Exception e) {
        return ApiResponse.error(e);
    }

    @PostMapping("/sync-structure")
    public ApiResponse<Boolean> syncStructure(@RequestBody TaskDTO dto) throws Exception {
        MigrationDTO migrationDTO = fullMigrationDTOConverter.from(dto);
        structureMigrationService.execute(migrationDTO, null, null, null);
        return ApiResponse.success(true);
    }
}
