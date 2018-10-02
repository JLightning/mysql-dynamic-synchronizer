// @flow
import AbstractClient from "./abstract-client";
import {MySQLFieldDTO, SimpleFieldMappingDTO, TableFieldsMappingRequestDTO, MySQLFieldWithMappingDTO, MySQLServerDTO, RedisServerDTO, Table, TaskType, MySQLInsertMode, TaskDTO} from '../dto/common';

class TaskListApiClient extends AbstractClient {

    /**

     * @returns {{done: (function(function(TaskDTO[]): *): *), error: (function(*): *)}}
     */
    getAllTasks(): {done: (TaskDTO[] => void) => void, error: () => void} {
        return super.get('/api/task-list/all', {});
    }
}

const taskListApiClient = new TaskListApiClient();
export default taskListApiClient;
