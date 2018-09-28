import AbstractClient from "./abstract-client";

class MySQLApiClient extends AbstractClient {

    /**
     * @param serverId {int}
     */
    getDatabasesForServer(serverId) {
        return super.get('/api/mysql/databases', {serverId});
    }

    /**
     * @param serverId {int}
     * @param database {string}
     * @param table {string}
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
     */
    getMappingFor2Table(dto) {
        return super.postJson('/api/mysql/fields-mapping', dto);
    }

    /**

     */
    getServers() {
        return super.get('/api/mysql/servers', {});
    }

    /**
     * @param serverId {int}
     * @param database {string}
     */
    getTablesForServerAndDatabase(serverId, database) {
        return super.get('/api/mysql/tables', {serverId, database});
    }

    /**
     * @param serverId {int}
     * @param database {string}
     * @param table {string}
     * @param filter {string}
     */
    validateFilter(serverId, database, table, filter) {
        return super.post('/api/mysql/validate-filter', {serverId, database, table, filter});
    }
}

const mySQLApiClient = new MySQLApiClient();
export default mySQLApiClient;
