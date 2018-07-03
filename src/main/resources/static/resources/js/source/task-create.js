import React from 'react';
import ReactDOM from 'react-dom';
import Select, {SelectOption} from "./common/select";

class TaskCreate extends React.Component {

    constructor(props) {
        super(props);
        this.state = {taskName: '', fields: [], table: {}, readyForSubmit: false};
        if (typeof taskDTO !== 'undefined') {
            this.taskDTO = taskDTO;
            this.state.taskName = taskDTO.taskName;
            this.state.table.source = taskDTO.source;
            this.state.table.target = taskDTO.target;

            this.getMapping();
        }
    }

    recalculateReadyForSubmit() {
        const readyForSubmit = this.state.fields.length > 0 && this.state.taskName !== '';
        this.setState({readyForSubmit: readyForSubmit});
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
            this.getMapping();
        } else {
            $.get(DOMAIN + '/api/mysql/fields', params).done((data) => {
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

    getMapping() {
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
        if (typeof this.taskDTO !== 'undefined') {
            postParam.mapping = this.taskDTO.mapping;
        }
        $.ajax(DOMAIN + '/api/mysql/fields-mapping', {
            data: JSON.stringify(postParam),
            contentType: 'application/json',
            type: 'POST'
        }).done((data) => {
            if (data.success) {
                const fields = data.data;
                this.setState({fields: fields});
                this.recalculateReadyForSubmit();
            }
        });
    }

    handleMappableChange(e, idx) {
        const fields = this.state.fields;
        fields[idx].mappable = e.target.checked;
        this.setState({fields: fields});
    }

    submit() {
        const mapping = [];
        this.state.fields.filter(field => field.mappable).forEach(field => mapping.push({
            sourceField: field.sourceField.field,
            targetField: field.targetField.field
        }));
        const postParams = {
            taskName: this.state.taskName,
            mapping: mapping,
            source: this.state.table.source,
            target: this.state.table.target
        };

        $.ajax(DOMAIN + '/api/task/create', {
            data: JSON.stringify(postParams),
            contentType: 'application/json',
            type: 'POST'
        }).done(function (data) {
            if (!data.success) {
                showError(data.errorMessage);
            } else {
                location.href = DOMAIN + '/task/list';
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
                               defaultValue={this.state.taskName} onChange={e => {
                            this.setState({taskName: e.target.value});
                            this.recalculateReadyForSubmit();
                        }} placeholder="Enter Task Name"/>
                    </div>

                    <div className="row">
                        <div className="col">
                            <TableSelector table={this.state.table.source} title='Source' onSelected={o => this.tableSelected(o, true)}/>
                        </div>
                        <div className="col">
                            <TableSelector table={this.state.table.target} title='Target' onSelected={o => this.tableSelected(o, false)}/>
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

                    <button type="button" className="btn btn-primary float-right mt-3"
                            disabled={!this.state.readyForSubmit}
                            onClick={() => this.submit()}>
                        Submit
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
                        <td>{field.sourceField.field} [{field.sourceField.type}]</td>
                }
                <td>
                    <input type="checkbox" defaultChecked={field.mappable}
                           onChange={e => this.props.handleMappableChange(e)}/>
                </td>
                {
                    field.targetField == null ? <td></td> :
                        <td>{field.targetField.field} [{field.targetField.type}]</td>
                }
            </tr>
        );
    }
}

class TableSelector extends React.Component {

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
        $.get(DOMAIN + '/api/mysql/servers').done((data) => {
            if (data.success) {
                this.setState({servers: data.data});
            }
        });
    }

    serverSelected(serverId) {
        $.get(DOMAIN + '/api/mysql/databases', {serverId}).done((data) => {
            if (data.success) {
                this.setState({databases: data.data, serverId: serverId});
            }
        });
    }

    databaseSelected(databaseName) {
        $.get(DOMAIN + '/api/mysql/tables', {serverId: this.state.serverId, database: databaseName}).done((data) => {
            if (data.success) {
                this.setState({tables: data.data, databaseName: databaseName});
            }
        });
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

if (document.getElementById('taskCreateWrapper') !== null) {
    ReactDOM.render(<TaskCreate/>, document.getElementById('taskCreateWrapper'));
}