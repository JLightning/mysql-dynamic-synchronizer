import AbstractClient from "./abstract-client";

class ToolApiClient extends AbstractClient {

    /**
     * @param taskId {int}
     * @param taskName {string}
     * @param mapping {SimpleFieldMappingDTO[]}
     * @param source {Table}
     * @param target {Table}
     * @param taskType {TaskType}
     * @param insertMode {MySQLInsertMode}
     * @param filters {string[]}
     */
    syncStructureFlat(taskId, taskName, mapping, source, target, taskType, insertMode, filters) {
        return super.postJson('/api/tool/sync-structure', {taskId, taskName, mapping, source, target, taskType, insertMode, filters});
    }

    /**
     * @param dto {TaskDTO}
     */
    syncStructure(dto) {
        return super.postJson('/api/tool/sync-structure', dto);
    }
}

const toolApiClient = new ToolApiClient();
export default toolApiClient;
