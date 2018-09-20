import AbstractClient from "./abstract-client";

class TaskListApiClient extends AbstractClient {

    getAllTasks() {
        return this.get('/api/task-list/all', {});
    }
}

const taskListApiClient = new TaskListApiClient();
export default taskListApiClient;
