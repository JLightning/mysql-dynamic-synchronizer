// @flow
import AbstractClient from "./abstract-client";
import {Table} from '../dto/table';
import {MigrationType} from '../dto/migration-type';
import {TaskType} from '../dto/task-type';
import {SimpleFieldMappingDTO} from '../dto/simple-field-mapping-dto';
import {MySQLInsertMode} from '../dto/my-sqlinsert-mode';
import {TaskDTO} from '../dto/task-dto';

class ToolApiClient extends AbstractClient {

    /**
     * @param taskId {number}
     * @param taskName {string}
     * @param mapping {SimpleFieldMappingDTO[]}
     * @param source {Table}
     * @param target {Table}
     * @param migrationType {MigrationType}
     * @param taskType {TaskType}
     * @param insertMode {MySQLInsertMode}
     * @param filters {string[]}
     * @returns {{done: (function(function(boolean): *): *), error: (function(*): *)}}
     */
    syncStructureFlat(taskId : number, taskName : string, mapping : SimpleFieldMappingDTO[], source : Table, target : Table, migrationType : MigrationType, taskType : TaskType, insertMode : MySQLInsertMode, filters : string[]): {done: (boolean => void) => void, error: () => void} {
        return super.postJson('/api/tool/sync-structure', {taskId, taskName, mapping, source, target, migrationType, taskType, insertMode, filters});
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
