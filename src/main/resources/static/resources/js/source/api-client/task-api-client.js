import AbstractClient from "./abstract-client";

class TaskApiClient extends AbstractClient {

    createTaskActionFlat(taskId, taskName, mapping, source, target, taskType, insertMode, filters) {
        return super.postJson('/api/task/create', {taskId, taskName, mapping, source, target, taskType, insertMode, filters});
    }

    createTaskAction(dto) {
        return super.postJson('/api/task/create', dto);
    }

    deleteTask(taskId) {
        return super.delete('/api/task/' + taskId + '', {});
    }

    getFullMigrationTaskProgressWs(taskId, callback) {
        return super.subscribe('/app/channel/task/full-migration-progress/' + taskId + '', callback);
    }

    getIncrementalMigrationProgressWs(taskId, callback) {
        return super.subscribe('/app/channel/task/incremental-migration-progress/' + taskId + '', callback);
    }

    getInsertModes() {
        return super.get('/api/task/get-insert-modes', {});
    }

    getTaskAction(taskId) {
        return super.get('/api/task/detail/' + taskId + '', {});
    }

    getTaskTypes() {
        return super.get('/api/task/get-task-types', {});
    }

    startFullMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/start-full-migration', {});
    }

    startIncrementalMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/start-incremental-migration', {});
    }

    stopIncrementalMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/stop-incremental-migration', {});
    }

    truncateAndStartFullMigrationTask(taskId) {
        return super.get('/api/task/detail/' + taskId + '/truncate-and-start-full-migration', {});
    }
}

const taskApiClient = new TaskApiClient();
export default taskApiClient;
