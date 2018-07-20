import AbstractClient from "./abstract-client";

class MySQLApiClient extends AbstractClient {

    getDatabasesForServer(serverId) {
        return this.get('/api/mysql/databases', {serverId});
    }

    getFieldForServerDatabaseAndTable(serverId, database, table) {
        return this.get('/api/mysql/fields', {serverId, database, table});
    }

    getMappingFor2TableFlat(sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping) {
        return this.postJson('/api/mysql/fields-mapping', {sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping});
    }

    getMappingFor2Table(dto) {
        return this.postJson('/api/mysql/fields-mapping', dto);
    }

    getServers() {
        return this.get('/api/mysql/servers', {});
    }

    getTablesForServerAndDatabase(serverId, database) {
        return this.get('/api/mysql/tables', {serverId, database});
    }

    validateFilter(serverId, database, table, filter) {
        return this.post('/api/mysql/validate-filter', {serverId, database, table, filter});
    }
}

const mySQLApiClient = new MySQLApiClient();
export default mySQLApiClient;
