import React from 'react';
import ReactDOM from 'react-dom';
import Select, {SelectOption} from "./common/select";

class TaskCreate extends React.Component {

    constructor(props) {
        super(props);
        this.state = {fields: []};
    }

    tableSelected(o, isSource) {
        const sub = isSource ? 'source' : 'target';
        $.get('/api/mysql/fields', {
            serverId: o.serverId,
            database: o.databaseName,
            table: o.tableName
        }).done((data) => {
            if (data.success) {
                const fields = this.state.fields;
                data.data.forEach((field, i) => {
                    if (fields.length > i) {
                        fields[i][sub] = field;
                    } else {
                        const _o = {};
                        _o[sub] = field;
                        fields.push(_o);
                    }
                });

                this.setState({fields: fields});
            }
        });
    }

    render() {
        return (
            <div className="container mt-3">
                <form>
                    <div className="form-group">
                        <label htmlFor="name">Name</label>
                        <input type="text" className="form-control" id="name" name="name"
                               placeholder="Enter Task Name"/>
                    </div>

                    <div className="row">
                        <div className="col">
                            <TableSelector onSelected={o => this.tableSelected(o, true)}/>
                        </div>
                        <div className="col">
                            <TableSelector onSelected={o => this.tableSelected(o, true)}/>
                        </div>
                    </div>

                    <table className="table mt-3">
                        <thead>
                        <tr>
                            <th scope="col">Source Fields</th>
                            <th scope="col">Sync?</th>
                            <th scope="col">Target Fields</th>
                        </tr>
                        </thead>
                        <tbody>
                        {
                            this.state.fields.map(field => {
                                return (
                                    <tr>
                                        <td>{field.source.field} ({field.source.type})</td>
                                        <td><input type="checkbox"/></td>
                                        <td>please choose target table</td>
                                    </tr>
                                );
                            })
                        }
                        </tbody>
                    </table>
                </form>
            </div>
        )
    }
}

class TableSelector extends React.Component {

    constructor(props) {
        super(props);
        this.state = {servers: [], databases: [], tables: []};
    }

    componentDidMount() {
        this.getServers();
    }

    getServers() {
        $.get('/api/mysql/servers').done((data) => {
            if (data.success) {
                this.setState({servers: data.data});
            }
        });
    }

    serverSelected(serverId) {
        $.get('/api/mysql/databases', {serverId}).done((data) => {
            if (data.success) {
                this.setState({databases: data.data, serverId: serverId});
            }
        });
    }

    databaseSelected(databaseName) {
        $.get('/api/mysql/tables', {serverId: this.state.serverId, database: databaseName}).done((data) => {
            if (data.success) {
                this.setState({tables: data.data, databaseName: databaseName});
            }
        });
    }

    render() {
        return (
            <div>
                <p>Source Server</p>
                <Select
                    options={this.state.servers.map(server => new SelectOption(server.serverId, server.name + ' mysql://' + server.host + ':' + server.port))}
                    btnTitle={'Select Server'}
                    onItemClick={option => this.serverSelected(option.id)}/>

                <p className="mt-3">Source Database</p>
                <Select
                    options={this.state.databases.map((db, idx) => new SelectOption(idx, db))}
                    btnTitle={'Select Database'}
                    onItemClick={option => this.databaseSelected(option.value)}/>

                <p className="mt-3">Source Table</p>
                <Select
                    options={this.state.tables.map((db, idx) => new SelectOption(idx, db))}
                    btnTitle={'Select Database'}
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

if (document.getElementById('taskCreateWrapper') !== null) {
    ReactDOM.render(<TaskCreate/>, document.getElementById('taskCreateWrapper'));
}