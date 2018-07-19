import AbstractClient from "./abstract-client";

class MySQLApiClient extends AbstractClient {

    getServers() {
        return this.get('/api/mysql/servers');
    }

    getDatabases(serverId) {
        return this.get('/api/mysql/databases', {serverId});
    }

    getTables(serverId, database) {
        return this.get('/api/mysql/tables', {serverId, database});
    }

    getFields(serverId, database, table) {
        return this.get('/api/mysql/fields', {serverId, database, table});
    }

    getMapping(sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping) {
        return this.postJson('/api/mysql/fields-mapping', {sourceServerId, sourceDatabase, sourceTable, targetServerId, targetDatabase, targetTable, mapping})
    }

    validateFilter(serverId, database, table, filter) {
        return this.post('/api/mysql/validate-filter', {serverId, database, table, filter});
    }
}

const mySQLApiClient = new MySQLApiClient();
export default mySQLApiClient;