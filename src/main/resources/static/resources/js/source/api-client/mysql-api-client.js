import AbstractClient from "./abstract-client";

class MySQLApiClient extends AbstractClient {

    /**
     * @param serverId {int}
     * @returns {{done: (function(function(string[]): *): *), error: (function(*): *)}}
     */
    getDatabasesForServer(serverId) {
        return super.get('/api/mysql/databases', {serverId});
    }

    /**
     * @param serverId {int}
     * @param database {string}
     * @param table {string}
     * @returns {{done: (function(function(MySQLFieldDTO[]): *): *), error: (function(*): *)}}
     */
    getFieldForServerDatabaseAndTable(serverId, database, table) {
        return super.get('/api/mysql/fields', {serverId, database, table});
    }

    /**
     * @param sourceServerId {int}
     * @param sourceDatabase {string}
     * @param sourceTable {string}
     * @param targetServerId {int}
     * @param targetDatabase {string}
     * @param targetTable {string}
     * @param mapping {SimpleFieldMappingDTO[]}
     */
    getMappingFor2TableFlat(sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping) {
        return super.postJson('/api/mysql/fields-mapping', {sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping});
    }

    /**
     * @param dto {TableFieldsMappingRequestDTO}
     * @returns {{done: (function(function(MySQLFieldWithMappingDTO[]): *): *), error: (function(*): *)}}
     */
    getMappingFor2Table(dto) {
        return super.postJson('/api/mysql/fields-mapping', dto);
    }

    /**

     * @returns {{done: (function(function(MySQLServerDTO[]): *): *), error: (function(*): *)}}
     */
    getServers() {
        return super.get('/api/mysql/servers', {});
    }

    /**
     * @param serverId {int}
     * @param database {string}
     * @returns {{done: (function(function(string[]): *): *), error: (function(*): *)}}
     */
    getTablesForServerAndDatabase(serverId, database) {
        return super.get('/api/mysql/tables', {serverId, database});
    }

    /**
     * @param serverId {int}
     * @param database {string}
     * @param table {string}
     * @param filter {string}
     * @returns {{done: (function(function(string): *): *), error: (function(*): *)}}
     */
    validateFilter(serverId, database, table, filter) {
        return super.post('/api/mysql/validate-filter', {serverId, database, table, filter});
    }
}

const mySQLApiClient = new MySQLApiClient();
export default mySQLApiClient;
