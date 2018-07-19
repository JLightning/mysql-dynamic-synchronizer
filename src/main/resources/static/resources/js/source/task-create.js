import React from 'react';
import ReactDOM from 'react-dom';
import TableSelector from './common/table-selector';

class TaskCreate extends React.Component {

    constructor(props) {
        super(props);
        this.state = {taskName: '', fields: [], table: {}, readyForSubmit: false, filters: []};
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
        console.log(fields);
        this.setState({fields: fields});
    }

    submit() {
        const mapping = this.state.fields.filter(field => field.mappable).map(field => {
            return {sourceField: field.sourceField, targetField: field.targetField}
        });
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
                location.href = DOMAIN + '/task/detail/?taskId=' + data.data.taskId;
            }
        });
    }

    swapField(dragField, dropField) {
        let fields = this.state.fields;
        const dragFieldIdx = fields.indexOf(dragField);
        const dropFieldIdx = fields.indexOf(dropField);

        if (dragFieldIdx === dropFieldIdx) return;

        const tmp = fields[dragFieldIdx].sourceField;
        fields[dragFieldIdx].sourceField = fields[dropFieldIdx].sourceField;
        fields[dropFieldIdx].sourceField = tmp;

        fields[dropFieldIdx].mappable = true;
        fields[dragFieldIdx].mappable = false;

        this.setState({fields: fields});
    }

    editField(idx, fieldName) {
        let fields = this.state.fields;
        fields[idx].sourceField = fieldName;
        this.setState({fields: fields});
    }

    render() {
        return (
            <div className="container mt-3">
                <h1>Task Information</h1>
                <form>
                    <h4>Choose Source and Target table</h4>
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
                            <TableSelector table={this.state.table.source} title='Source'
                                           onSelected={o => this.tableSelected(o, true)}/>
                        </div>
                        <div className="col">
                            <TableSelector table={this.state.table.target} title='Target'
                                           onSelected={o => this.tableSelected(o, false)}/>
                        </div>
                    </div>

                    <h4 className="mt-3">Mapping</h4>
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
                                <FieldRowList
                                    fields={this.state.fields}
                                    handleMappableChange={(e, idx) => this.handleMappableChange(e, idx)}
                                    swapField={this.swapField.bind(this)}
                                    editField={this.editField.bind(this)}/>
                                </tbody>
                            </table> : ''
                    }

                    <h4 className="mt-3">Filter</h4>
                    <TaskFilter filters={this.state.filters} addFilter={(filter, cb) => {
                        let params = {
                            serverId: this.state.table.source.serverId,
                            database: this.state.table.source.database,
                            table: this.state.table.source.table,
                            filter: filter
                        };

                        $.post(DOMAIN + "/api/mysql/validate-filter", params).done(data => {
                            if (data.success) {
                                let filters = this.state.filters;
                                filters.push(data.data)
                                this.setState({filters: filters});
                                cb();
                            } else {
                                showError(data.errorMessage);
                            }
                        });
                    }}
                                removeFilter={idx => {
                                    let filters = this.state.filters;
                                    filters.splice(idx, 1);
                                    this.setState({filters: filters});
                                }}/>

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

class TaskFilter extends React.Component {

    constructor(props) {
        super(props);
        this.state = {filters: this.props.filters || [], inputValue: ''};
    }

    render() {
        return (
            <div>
                <div className="filterWrapper">
                    {this.state.filters.map((filter, idx) =>
                        <span key={idx} className="filter btn btn-secondary btn-sm mr-2"
                              onClick={idx => this.props.removeFilter(idx)}>
                            <span>{filter}</span>
                            <i className="fa fa-close ml-2" aria-hidden="true"/>
                        </span>
                    )}
                </div>
                <input type="text" className="form-control mt-2" id="filter" name="filter" placeholder="Enter Task Name"
                       onKeyPress={(e) => {
                           if (e.key === 'Enter') {
                               const value = e.target.value;
                               if (value.trim() !== '') {
                                   this.props.addFilter(value, () => this.setState({inputValue: ''}))
                               }
                               return;
                           }
                       }}
                       value={this.state.inputValue}
                       onChange={e => this.setState({inputValue: e.target.value})}/>
            </div>
        );
    }
}

class FieldRowList extends React.Component {

    captureDrapStartField(field) {
        this.setState({capturedField: field});
    }

    onDrop(field) {
        if (this.state.capturedField !== null) {
            this.props.swapField(this.state.capturedField, field);
        }
    }

    render() {
        return this.props.fields.map((field, idx) => <FieldRow
            key={idx}
            idx={idx}
            field={field}
            editField={this.props.editField}
            handleMappableChange={e => this.props.handleMappableChange(e, idx)}
            onDrop={this.onDrop.bind(this)}
            captureDrapStartField={this.captureDrapStartField.bind(this)}
        />);
    }
}

class FieldRow extends React.Component {

    constructor(props) {
        super(props);
        this.state = {dropTargetClass: '', custom: false};
    }

    onDragOver(e) {
        this.setState({dropTargetClass: 'bg-primary text-white'});
        e.preventDefault();
        e.stopPropagation();
    }

    onDragLeave(e) {
        this.setState({dropTargetClass: ''});
    }

    onDrop(e) {
        this.setState({dropTargetClass: ''});
    }

    getSourceText() {
        const field = this.props.field;
        if (this.state.custom)
            return (
                <div>
                    <input type="text" value={field.sourceField}
                           onChange={e => this.props.editField(this.props.idx, e.target.value)}/>
                    <i className="fa fa-check-square-o ml-2" aria-hidden="true"
                       onClick={() => this.setState({custom: false})}/>
                </div>
            );
        return <div onClick={() => this.setState({custom: true})}
                    style={{minHeight: '1.5rem'}}>{field.sourceField}</div>;
    }

    render() {
        const field = this.props.field;
        const sourceText = this.getSourceText();
        return (
            <tr>
                {
                    <td className={this.state.dropTargetClass + ' pointer'}
                        draggable="true"
                        onDragStart={() => {
                            this.setState({dropTargetClass: 'bg-success text-white'});
                            this.props.captureDrapStartField(field);
                        }}
                        onDrop={() => {
                            this.onDrop();
                            this.props.onDrop(field);
                        }}
                        onDragOver={e => this.onDragOver(e)}
                        onDragLeave={e => this.onDragLeave(e)}
                    >
                        {sourceText}
                    </td>
                }
                <td>
                    {<input type="checkbox" checked={field.mappable}
                            onChange={e => this.props.handleMappableChange(e)}/>}
                </td>
                {
                    field.targetField == null ? <td></td> :
                        <td>{field.targetField}</td>
                }
            </tr>
        );
    }
}

if (document.getElementById('taskCreateWrapper') !== null) {
    ReactDOM.render(<TaskCreate/>, document.getElementById('taskCreateWrapper'));
}