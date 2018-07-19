import AbstractClient from "./abstract-client";

class MySQLApiClient extends AbstractClient {

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