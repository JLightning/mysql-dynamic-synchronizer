import AbstractClient from "./abstract-client";

class TaskApiClient extends AbstractClient {

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
    createTaskActionFlat(taskId, taskName, mapping, source, target, taskType, insertMode, filters) {
        return super.postJson('/api/task/create', {taskId, taskName, mapping, source, target, taskType, insertMode, filters});
    }

    /**
     * @param dto {TaskDTO}
     * @returns {{done: (function(function(TaskDTO): *): *), error: (function(*): *)}}
     */
    createTaskAction(dto) {
        return super.postJson('/api/task/create', dto);
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(*): *): *), error: (function(*): *)}}
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

     * @returns {{done: (function(function(MySQLInsertMode[]): *): *), error: (function(*): *)}}
     */
    getInsertModes() {
        return super.get('/api/task/get-insert-modes', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(TaskDTO): *): *), error: (function(*): *)}}
     */
    getTaskAction(taskId) {
        return super.get('/api/task/detail/' + taskId + '', {});
    }

    /**

     * @returns {{done: (function(function(TaskType[]): *): *), error: (function(*): *)}}
     */
    getTaskTypes() {
        return super.get('/api/task/get-task-types', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(*): *): *), error: (function(*): *)}}
     */
    startFullMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/start-full-migration', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(*): *): *), error: (function(*): *)}}
     */
    startIncrementalMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/start-incremental-migration', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(*): *): *), error: (function(*): *)}}
     */
    stopIncrementalMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/stop-incremental-migration', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(*): *): *), error: (function(*): *)}}
     */
    truncateAndStartFullMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/truncate-and-start-full-migration', {});
    }
}

const taskApiClient = new TaskApiClient();
export default taskApiClient;
