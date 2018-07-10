package com.jhl.mds.services.migration.mysql2mysql;

import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import com.jhl.mds.dao.repositories.MySQLServerRepository;
import com.jhl.mds.dao.repositories.TaskFieldMappingRepository;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.services.mysql.MySQLWriteService;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogListener;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogPool;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogService;
import com.jhl.mds.util.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class IncrementalMigrationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private TaskRepository taskRepository;
    private TaskFieldMappingRepository taskFieldMappingRepository;
    private MySQLServerRepository mySQLServerRepository;
    private MySQLServerDTO.Converter serverDTOConverter;
    private MySQLBinLogPool mySQLBinLogPool;
    private MySQLBinLogService mySQLBinLogService;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private MySQLWriteService mySQLWriteService;
    private Set<Integer> runningTask = new HashSet<>();

    @Autowired
    public IncrementalMigrationService(
            TaskRepository taskRepository,
            TaskFieldMappingRepository taskFieldMappingRepository,
            MySQLServerRepository mySQLServerRepository,
            MySQLServerDTO.Converter serverDTOConverter,
            MySQLBinLogPool mySQLBinLogPool,
            MySQLBinLogService mySQLBinLogService,
            MigrationMapperService.Factory migrationMapperServiceFactory,
            MySQLWriteService mySQLWriteService
    ) {
        this.taskRepository = taskRepository;
        this.taskFieldMappingRepository = taskFieldMappingRepository;
        this.mySQLServerRepository = mySQLServerRepository;
        this.serverDTOConverter = serverDTOConverter;
        this.mySQLBinLogPool = mySQLBinLogPool;
        this.mySQLBinLogService = mySQLBinLogService;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.mySQLWriteService = mySQLWriteService;
    }

    @PostConstruct
    // TODO: clean FullMigrationDTO generation, support bin log server restart
    private void init() {
        List<Task> tasks = taskRepository.findByIncrementalMigrationActive(true);
        for (Task task : tasks) {
            List<TaskFieldMapping> mapping = taskFieldMappingRepository.findByFkTaskId(task.getTaskId());

            List<SimpleFieldMappingDTO> mappingDTOs = mapping.stream().map(m -> new SimpleFieldMappingDTO(m.getSourceField(), m.getTargetField())).collect(Collectors.toList());

            MySQLServer sourceServer = mySQLServerRepository.findByServerId(task.getFkSourceServer());
            MySQLServer targetServer = mySQLServerRepository.findByServerId(task.getFkTargetServer());

            TableInfoDTO sourceTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(sourceServer), task.getSourceDatabase(), task.getSourceTable());
            TableInfoDTO targetTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(targetServer), task.getTargetDatabase(), task.getTargetTable());

            FullMigrationDTO fullMigrationDTO = FullMigrationDTO.builder()
                    .taskId(task.getTaskId())
                    .mapping(mappingDTOs)
                    .source(sourceTableInfoDTO)
                    .target(targetTableInfoDTO)
                    .build();

            run(fullMigrationDTO);
        }
    }

    public void run(FullMigrationDTO dto) {
        logger.info("Run incremental migration for: " + dto);
        if (runningTask.contains(dto.getTaskId())) {
            throw new RuntimeException("Task has already been running");
        }
        runningTask.add(dto.getTaskId());

        mySQLBinLogPool.addListener(dto.getSource(), new MySQLBinLogListener() {
            @Override
            public void insert(WriteRowsEventData eventData) {
                executor.submit(() -> IncrementalMigrationService.this.insert(dto, eventData));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void insert(FullMigrationDTO dto, WriteRowsEventData eventData) {
        try {
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(migrationMapperService.getColumns());

            List<Map<String, Object>> data = mySQLBinLogService.mapDataToField(dto.getSource(), eventData);

            Pipeline<FullMigrationDTO, Long> pipeline = new Pipeline<>(dto);
            pipeline.append((context, input, next, errorHandler) -> data.forEach(next))
                    .append(migrationMapperService)
                    .append(mySQLWriteService)
                    .execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
