// @flow
import AbstractClient from "./abstract-client";
import {FullMigrationProgressDTO, IncrementalMigrationProgressDTO} from '../dto/common';
import {SimpleFieldMappingDTO} from '../dto/simple-field-mapping-dto';
import {TaskDTO, Table, TaskType, MySQLInsertMode} from '../dto/task-dto';

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
     * @returns {{done: (function(function(TaskDTO): *): *), error: (function(*): *)}}
     */
    createFlat(taskId : number, taskName : string, mapping : SimpleFieldMappingDTO[], source : Table, target : Table, taskType : TaskType, insertMode : MySQLInsertMode, filters : string[]): {done: (TaskDTO => void) => void, error: () => void} {
        return super.putJson('/api/task/', {taskId, taskName, mapping, source, target, taskType, insertMode, filters});
    }

    /**
     * @param dto {TaskDTO}
     * @returns {{done: (function(function(TaskDTO): *): *), error: (function(*): *)}}
     */
    create(dto : TaskDTO): {done: (TaskDTO => void) => void, error: () => void} {
        return super.putJson('/api/task/', dto);
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(boolean): *): *), error: (function(*): *)}}
     */
    delete(taskId : number): {done: (boolean => void) => void, error: () => void} {
        return super.delete('/api/task/' + taskId + '', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(TaskDTO): *): *), error: (function(*): *)}}
     */
    detail(taskId : number): {done: (TaskDTO => void) => void, error: () => void} {
        return super.get('/api/task/' + taskId + '', {});
    }

    getFullMigrationTaskProgressWs(taskId : number, callback: (FullMigrationProgressDTO) => any): void {
        return super.subscribe('/app/channel/task/full-migration-progress/' + taskId + '', callback);
    }

    getIncrementalMigrationProgressWs(taskId : number, callback: (IncrementalMigrationProgressDTO) => any): void {
        return super.subscribe('/app/channel/task/incremental-migration-progress/' + taskId + '', callback);
    }

    /**

     * @returns {{done: (function(function(TaskType[]): *): *), error: (function(*): *)}}
     */
    getTaskTypes(): {done: (TaskType[] => void) => void, error: () => void} {
        return super.get('/api/task/get-task-types', {});
    }

    /**

     * @returns {{done: (function(function(TaskDTO[]): *): *), error: (function(*): *)}}
     */
    list(): {done: (TaskDTO[] => void) => void, error: () => void} {
        return super.get('/api/task/', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(boolean): *): *), error: (function(*): *)}}
     */
    startFullMigrationTask(taskId : number): {done: (boolean => void) => void, error: () => void} {
        return super.get('/api/task/detail/' + taskId + '/start-full-migration', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(boolean): *): *), error: (function(*): *)}}
     */
    startIncrementalMigrationTask(taskId : number): {done: (boolean => void) => void, error: () => void} {
        return super.get('/api/task/detail/' + taskId + '/start-incremental-migration', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(boolean): *): *), error: (function(*): *)}}
     */
    stopIncrementalMigrationTask(taskId : number): {done: (boolean => void) => void, error: () => void} {
        return super.get('/api/task/detail/' + taskId + '/stop-incremental-migration', {});
    }

    /**
     * @param taskId {number}
     * @returns {{done: (function(function(boolean): *): *), error: (function(*): *)}}
     */
    truncateAndStartFullMigrationTask(taskId : number): {done: (boolean => void) => void, error: () => void} {
        return super.get('/api/task/detail/' + taskId + '/truncate-and-start-full-migration', {});
    }
}

const taskApiClient = new TaskApiClient();
export default taskApiClient;
