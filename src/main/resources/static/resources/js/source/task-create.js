import React from 'react';
import ReactDOM from 'react-dom';
import Select, {SelectOption} from "./common/select";

class TaskCreate extends React.Component {

    constructor(props) {
        super(props);
        this.state = {fields: [], table: {}, readyForSubmit: false};
    }

    tableSelected(o, isSource) {
        const sub = isSource ? 'sourceField' : 'targetField';
        const sub2 = isSource ? 'source' : 'target';

        let params = {
            serverId: o.serverId,
            database: o.databaseName,
            table: o.tableName
        };
        const table = this.state.table;
        table[sub2] = params;
        this.setState({table: table});

        if (this.state.table.source != null && this.state.table.target != null) {
            const sourceParam = this.state.table.source;
            const targetParam = this.state.table.target;
            const postParam = {
                sourceServerId: sourceParam.serverId,
                sourceDatabase: sourceParam.database,
                sourceTable: sourceParam.table,
                targetServerId: targetParam.serverId,
                targetDatabase: targetParam.database,
                targetTable: targetParam.table
            };
            $.ajax('/api/mysql/fields-mapping', {
                data: JSON.stringify(postParam),
                contentType: 'application/json',
                type: 'POST'
            }).done((data) => {
                if (data.success) {
                    const fields = data.data;
                    this.setState({fields: fields, readyForSubmit: true});
                }
            });
        } else {
            $.get('/api/mysql/fields', params).done((data) => {
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
    }

    handleMappableChange(e, idx) {
        const fields = this.state.fields;
        fields[idx].mappable = e.target.checked;
        this.setState({fields: fields});
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
                            <TableSelector onSelected={o => this.tableSelected(o, false)}/>
                        </div>
                    </div>

                    {
                        this.state.fields.length > 0 ?
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
                                    this.state.fields.map((field, idx) => <FieldRow key={idx} field={field}
                                                                                    handleMappableChange={e => this.handleMappableChange(e, idx)}/>)
                                }
                                </tbody>
                            </table> : ''
                    }

                    <button type="button" className="btn btn-primary float-right"
                            disabled={!this.state.readyForSubmit}>Submit
                    </button>
                </form>
            </div>
        )
    }
}

class FieldRow extends React.Component {

    render() {
        const field = this.props.field;
        return (
            <tr>
                {
                    field.sourceField == null ? <td></td> :
                        <td>{field.sourceField.field} ({field.sourceField.type})</td>
                }
                <td>
                    <input type="checkbox" defaultChecked={field.mappable} onChange={e => this.props.handleMappableChange(e)}/>
                </td>
                {
                    field.targetField == null ? <td></td> :
                        <td>{field.targetField.field} ({field.targetField.type})</td>
                }
            </tr>
        );
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