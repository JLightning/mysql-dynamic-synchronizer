import AbstractClient from "./abstract-client";

class MySQLApiClient extends AbstractClient {

    getDatabasesForServer(serverId) {
        return super.get('/api/mysql/databases', {serverId});
    }

    getFieldForServerDatabaseAndTable(serverId, database, table) {
        return super.get('/api/mysql/fields', {serverId, database, table});
    }

    getMappingFor2TableFlat(sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping) {
        return super.postJson('/api/mysql/fields-mapping', {sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping});
    }

    getMappingFor2Table(dto) {
        return super.postJson('/api/mysql/fields-mapping', dto);
    }

    getServers() {
        return super.get('/api/mysql/servers', {});
    }

    getTablesForServerAndDatabase(serverId, database) {
        return super.get('/api/mysql/tables', {serverId, database});
    }

    validateFilter(serverId, database, table, filter) {
        return super.post('/api/mysql/validate-filter', {serverId, database, table, filter});
    }
}

const mySQLApiClient = new MySQLApiClient();
export default mySQLApiClient;
