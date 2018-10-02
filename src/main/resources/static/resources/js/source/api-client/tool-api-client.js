// @flow
import AbstractClient from "./abstract-client";
import {RedisServerDTO} from '../dto/redis-server-dto';
import {MySQLFieldDTO, SimpleFieldMappingDTO, TableFieldsMappingRequestDTO, MySQLFieldWithMappingDTO, MySQLServerDTO, Table, TaskType, MySQLInsertMode, TaskDTO} from '../dto/common';

class ToolApiClient extends AbstractClient {

    /**
     * @param taskId {number}
     * @param taskName {string}
     * @param mapping {SimpleFieldMappingDTO[]}
     * @param source {Table}
     * @param target {Table}
     * @param taskType {TaskType}
     * @param insertMode {MySQLInsertMode}
     * @param filters {string[]}
     * @returns {{done: (function(function(boolean): *): *), error: (function(*): *)}}
     */
    syncStructureFlat(taskId : number, taskName : string, mapping : SimpleFieldMappingDTO[], source : Table, target : Table, taskType : TaskType, insertMode : MySQLInsertMode, filters : string[]): {done: (boolean => void) => void, error: () => void} {
        return super.postJson('/api/tool/sync-structure', {taskId, taskName, mapping, source, target, taskType, insertMode, filters});
    }

    /**
     * @param dto {TaskDTO}
     * @returns {{done: (function(function(boolean): *): *), error: (function(*): *)}}
     */
    syncStructure(dto : TaskDTO): {done: (boolean => void) => void, error: () => void} {
        return super.postJson('/api/tool/sync-structure', dto);
    }
}

const toolApiClient = new ToolApiClient();
export default toolApiClient;
