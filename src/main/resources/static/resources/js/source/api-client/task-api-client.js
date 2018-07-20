import AbstractClient from "./abstract-client";

class TaskApiClient extends AbstractClient {

    createTaskActionFlat(taskId, taskName, mapping, source, target) {
        return this.postJson('/api/task/create', {taskId, taskName, mapping, source, target});
    }

    createTaskAction(dto) {
        return this.postJson('/api/task/create', dto);
    }

    getFullMigrationTaskProgress(taskId) {
        return this.get('/api/task/detail/' + taskId + '/full-migration-progress', {});
    }

    getTaskAction(taskId) {
        return this.get('/api/task/detail/' + taskId + '', {});
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
