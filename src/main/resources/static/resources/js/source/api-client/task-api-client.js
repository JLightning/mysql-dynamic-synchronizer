import AbstractClient from "./abstract-client";

class TaskApiClient extends AbstractClient {

    createTaskActionFlat(taskId, taskName, mapping, source, target, taskType, insertMode) {
        return this.postJson('/api/task/create', {taskId, taskName, mapping, source, target, taskType, insertMode});
    }

    createTaskAction(dto) {
        return this.postJson('/api/task/create', dto);
    }

    getFullMigrationTaskProgressWs(taskId, callback) {
        return this.subscribe('/app/channel/task/full-migration-progress/' + taskId + '', callback);
    }

    getIncrementalMigrationProgressWs(taskId, callback) {
        return this.subscribe('/app/channel/task/incremental-migration-progress/' + taskId + '', callback);
    }

    getInsertModes() {
        return this.get('/api/task/get-insert-modes', {});
    }

    getTaskAction(taskId) {
        return this.get('/api/task/detail/' + taskId + '', {});
    }

    getTaskTypes() {
        return this.get('/api/task/get-task-types', {});
    }

    startFullMigrationTask(taskId) {
        return this.get('/api/task/detail/' + taskId + '/start-full-migration', {});
    }

    startIncrementalMigrationTask(taskId) {
        return this.get('/api/task/detail/' + taskId + '/start-incremental-migration', {});
    }

    stopIncrementalMigrationTask(taskId) {
        return this.get('/api/task/detail/' + taskId + '/stop-incremental-migration', {});
    }
}

const taskApiClient = new TaskApiClient();
export default taskApiClient;
