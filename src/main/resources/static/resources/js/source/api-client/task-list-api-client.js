import AbstractClient from "./abstract-client";

class TaskListApiClient extends AbstractClient {

    /**

     * @returns {{done: (function(TaskDTO[]): *), error: (function(*): *)}}
     */
    getAllTasks() {
        return super.get('/api/task-list/all', {});
    }
}

const taskListApiClient = new TaskListApiClient();
export default taskListApiClient;
