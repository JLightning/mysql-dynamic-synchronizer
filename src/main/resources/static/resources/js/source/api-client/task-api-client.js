import AbstractClient from "./abstract-client";

class TaskApiClient extends AbstractClient {

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
    createTaskActionFlat(taskId, taskName, mapping, source, target, taskType, insertMode, filters) {
        return super.postJson('/api/task/create', {taskId, taskName, mapping, source, target, taskType, insertMode, filters});
    }

    /**
     * @param dto {TaskDTO}
     */
    createTaskAction(dto) {
        return super.postJson('/api/task/create', dto);
    }

    /**
     * @param taskId {int}
     */
    deleteTask(taskId) {
        return super.delete('/api/task/' + taskId + '', {});
    }

    getFullMigrationTaskProgressWs(taskId, callback) {
        return super.subscribe('/app/channel/task/full-migration-progress/' + taskId + '', callback);
    }

    getIncrementalMigrationProgressWs(taskId, callback) {
        return super.subscribe('/app/channel/task/incremental-migration-progress/' + taskId + '', callback);
    }

    /**

     */
    getInsertModes() {
        return super.get('/api/task/get-insert-modes', {});
    }

    /**
     * @param taskId {int}
     */
    getTaskAction(taskId) {
        return super.get('/api/task/detail/' + taskId + '', {});
    }

    /**

     */
    getTaskTypes() {
        return super.get('/api/task/get-task-types', {});
    }

    /**
     * @param taskId {int}
     */
    startFullMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/start-full-migration', {});
    }

    /**
     * @param taskId {int}
     */
    startIncrementalMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/start-incremental-migration', {});
    }

    /**
     * @param taskId {int}
     */
    stopIncrementalMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/stop-incremental-migration', {});
    }

    /**
     * @param taskId {int}
     */
    truncateAndStartFullMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/truncate-and-start-full-migration', {});
    }
}

const taskApiClient = new TaskApiClient();
export default taskApiClient;
