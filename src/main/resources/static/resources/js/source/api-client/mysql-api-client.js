// @flow
import AbstractClient from "./abstract-client";

class MySQLApiClient extends AbstractClient {

    /**
     * @param serverId {number}
     * @returns {{done: (function(function(string[]): *): *), error: (function(*): *)}}
     */
    getDatabasesForServer(serverId : number) {
        return super.get('/api/mysql/databases', {serverId});
    }

    /**
     * @param serverId {number}
     * @param database {string}
     * @param table {string}
     * @returns {{done: (function(function(MySQLFieldDTO[]): *): *), error: (function(*): *)}}
     */
    getFieldForServerDatabaseAndTable(serverId : number, database : string, table : string) {
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
     */
    getMappingFor2TableFlat(sourceServerId : number, sourceDatabase : string, sourceTable : string, targetServerId : number, targetDatabase : string, targetTable : string, mapping : *) {
        return super.postJson('/api/mysql/fields-mapping', {sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping});
    }

    /**
     * @param dto {TableFieldsMappingRequestDTO}
     * @returns {{done: (function(function(MySQLFieldWithMappingDTO[]): *): *), error: (function(*): *)}}
     */
    getMappingFor2Table(dto : TableFieldsMappingRequestDTO) {
        return super.postJson('/api/mysql/fields-mapping', dto);
    }

    /**

     * @returns {{done: (function(function(MySQLServerDTO[]): *): *), error: (function(*): *)}}
     */
    getServers() {
        return super.get('/api/mysql/servers', {});
    }

    /**
     * @param serverId {number}
     * @param database {string}
     * @returns {{done: (function(function(string[]): *): *), error: (function(*): *)}}
     */
    getTablesForServerAndDatabase(serverId : number, database : string) {
        return super.get('/api/mysql/tables', {serverId, database});
    }

    /**
     * @param serverId {number}
     * @param database {string}
     * @param table {string}
     * @param filter {string}
     * @returns {{done: (function(function(string): *): *), error: (function(*): *)}}
     */
    validateFilter(serverId : number, database : string, table : string, filter : string) {
        return super.post('/api/mysql/validate-filter', {serverId, database, table, filter});
    }
}

const mySQLApiClient = new MySQLApiClient();
export default mySQLApiClient;
