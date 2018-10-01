// @flow
import AbstractClient from "./abstract-client";

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
     */
    syncStructureFlat(taskId : number, taskName : string, mapping : *, source : Table, target : Table, taskType : TaskType, insertMode : MySQLInsertMode, filters : *) {
        return super.postJson('/api/tool/sync-structure', {taskId, taskName, mapping, source, target, taskType, insertMode, filters});
    }

    /**
     * @param dto {TaskDTO}
     * @returns {{done: (function(function(*): *): *), error: (function(*): *)}}
     */
    syncStructure(dto : TaskDTO) {
        return super.postJson('/api/tool/sync-structure', dto);
    }
}

const toolApiClient = new ToolApiClient();
export default toolApiClient;
