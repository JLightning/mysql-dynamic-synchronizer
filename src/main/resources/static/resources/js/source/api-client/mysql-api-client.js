// @flow
import AbstractClient from "./abstract-client";
import {MySQLFieldDTO, SimpleFieldMappingDTO, TableFieldsMappingRequestDTO, MySQLFieldWithMappingDTO} from '../dto/common';
import {MySQLServerDTO} from '../dto/common';

class MySQLApiClient extends AbstractClient {

    /**
     * @param serverId {number}
     * @returns {{done: (function(function(string[]): *): *), error: (function(*): *)}}
     */
    getDatabasesForServer(serverId : number): {done: (string[] => void) => void, error: () => void} {
        return super.get('/api/mysql/databases', {serverId});
    }

    /**
     * @param serverId {number}
     * @param database {string}
     * @param table {string}
     * @returns {{done: (function(function(MySQLFieldDTO[]): *): *), error: (function(*): *)}}
     */
    getFieldForServerDatabaseAndTable(serverId : number, database : string, table : string): {done: (MySQLFieldDTO[] => void) => void, error: () => void} {
        return super.get('/api/mysql/fields', {serverId, database, table});
    }

    /**
     * @param sourceServerId {number}
     * @param sourceDatabase {string}
     * @param sourceTable {string}
     * @param targetServerId {number}
     * @param targetDatabase {string}
     * @param targetTable {string}
     * @param mapping {SimpleFieldMappingDTO[]}
     * @returns {{done: (function(function(MySQLFieldWithMappingDTO[]): *): *), error: (function(*): *)}}
     */
    getMappingFor2TableFlat(sourceServerId : number, sourceDatabase : string, sourceTable : string, targetServerId : number, targetDatabase : string, targetTable : string, mapping : SimpleFieldMappingDTO[]): {done: (MySQLFieldWithMappingDTO[] => void) => void, error: () => void} {
        return super.postJson('/api/mysql/fields-mapping', {sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping});
    }

    /**
     * @param dto {TableFieldsMappingRequestDTO}
     * @returns {{done: (function(function(MySQLFieldWithMappingDTO[]): *): *), error: (function(*): *)}}
     */
    getMappingFor2Table(dto : TableFieldsMappingRequestDTO): {done: (MySQLFieldWithMappingDTO[] => void) => void, error: () => void} {
        return super.postJson('/api/mysql/fields-mapping', dto);
    }

    /**

     * @returns {{done: (function(function(MySQLServerDTO[]): *): *), error: (function(*): *)}}
     */
    getServers(): {done: (MySQLServerDTO[] => void) => void, error: () => void} {
        return super.get('/api/mysql/servers', {});
    }

    /**
     * @param serverId {number}
     * @param database {string}
     * @returns {{done: (function(function(string[]): *): *), error: (function(*): *)}}
     */
    getTablesForServerAndDatabase(serverId : number, database : string): {done: (string[] => void) => void, error: () => void} {
        return super.get('/api/mysql/tables', {serverId, database});
    }

    /**
     * @param serverId {number}
     * @param database {string}
     * @param table {string}
     * @param filter {string}
     * @returns {{done: (function(function(string): *): *), error: (function(*): *)}}
     */
    validateFilter(serverId : number, database : string, table : string, filter : string): {done: (string => void) => void, error: () => void} {
        return super.post('/api/mysql/validate-filter', {serverId, database, table, filter});
    }
}

const mySQLApiClient = new MySQLApiClient();
export default mySQLApiClient;
