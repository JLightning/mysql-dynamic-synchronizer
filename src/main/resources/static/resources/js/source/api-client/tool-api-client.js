import AbstractClient from "./abstract-client";

class ToolApiClient extends AbstractClient {

    syncStructureFlat(taskId, taskName, mapping, source, target) {
        return this.postJson('/api/tool/sync-structure', {taskId, taskName, mapping, source, target});
    }

    syncStructure(dto) {
        return this.postJson('/api/tool/sync-structure', dto);
    }
}

const toolApiClient = new ToolApiClient();
export default toolApiClient;
