import React from "react";
import Select, {SelectOption} from "./select";
import mySQLApiClient from "../api-client/mysql-api-client";

export default class TableSelector extends React.Component {

    constructor(props) {
        super(props);
        this.state = {servers: [], databases: [], tables: [], serverId: 0, databaseName: '', tableName: ''};
        if (this.props.table != null) {
            this.state.serverId = this.props.table.serverId;
            this.state.databaseName = this.props.table.database;
            this.state.tableName = this.props.table.table;

            this.serverSelected(this.state.serverId);
            this.databaseSelected(this.state.databaseName);
        }
    }

    componentDidMount() {
        this.getServers();
    }

    getServers() {
        mySQLApiClient.getServers().done(data => this.setState({servers: data}));
    }

    serverSelected(serverId) {
        mySQLApiClient.getDatabasesForServer(serverId).done(data => this.setState({databases: data, serverId: serverId}));
    }

    databaseSelected(databaseName) {
        mySQLApiClient.getTablesForServerAndDatabase(this.state.serverId, databaseName).done(data => this.setState({
            tables: data,
            databaseName: databaseName
        }));
    }

    render() {
        return (
            <div>
                <p>{this.props.title}</p>
                <Select className='mt-3 fullWidth'
                        options={this.state.servers.map(server => new SelectOption(server.serverId, server.name + ' mysql://' + server.host + ':' + server.port))}
                        btnTitle={'Select Server'}
                        value={this.state.serverId}
                        onItemClick={option => this.serverSelected(option.id)}/>

                <Select className='mt-3 fullWidth'
                        options={this.state.databases.map(db => new SelectOption(db, db))}
                        btnTitle={'Select Database'}
                        value={this.state.databaseName}
                        onItemClick={option => this.databaseSelected(option.value)}/>

                <Select className='mt-3 fullWidth'
                        options={this.state.tables.map(table => new SelectOption(table, table))}
                        btnTitle={'Select Table'}
                        value={this.state.tableName}
                        onItemClick={option => {
                            this.setState({tableName: option.value});
                            if (this.props.onSelected !== undefined) {
                                this.props.onSelected({
                                    serverId: this.state.serverId,
                                    databaseName: this.state.databaseName,
                                    tableName: option.value
                                })
                            }
                        }}/>
            </div>
        );
    }
}