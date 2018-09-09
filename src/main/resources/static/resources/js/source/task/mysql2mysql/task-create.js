import React from 'react';
import TableSelector from '../../common/table-selector';
import mySQLApiClient from '../../api-client/mysql-api-client';
import taskApiClient from "../../api-client/task-api-client";
import Table from "../../common/table";
import Select, {SelectOption} from "../../common/select";
import {autorun, computed, observable} from 'mobx';
import {observer} from 'mobx-react';
import TagEditor from "../../common/tag-editor";

@observer
export default class TaskCreate extends React.Component {

    @observable fields = [];
    @observable filters = [];
    @observable taskTypes = [];
    @observable insertModes = [];
    @observable taskType = '';
    @observable insertMode = '';
    @observable taskName = '';
    @observable sourceTable = {};
    @observable targetTable = {};

    constructor(props) {
        super(props);
        if (typeof taskDTO !== 'undefined') {
            this.taskDTO = taskDTO;
            this.taskName = taskDTO.taskName;
            this.sourceTable = taskDTO.source;
            this.targetTable = taskDTO.target;
            this.taskType = taskDTO.taskType;
            this.insertMode = taskDTO.insertMode;
            this.filters = taskDTO.filters;

            this.getMapping();
        }

        autorun(() => this.getMapping());
    }

    componentDidMount() {
        taskApiClient.getTaskTypes().done(taskTypes => this.taskTypes = taskTypes);
        taskApiClient.getInsertModes().done(insertModes => this.insertModes = insertModes);
    }

    @computed get readyToSubmit() {
        console.log('compute readyToSubmit');
        return this.fields.length > 0 && this.taskName !== '' && this.taskType !== '' && this.insertMode !== '';
    }

    getMapping() {
        let emptySourceTable = typeof this.sourceTable.serverId === 'undefined' || typeof this.sourceTable.database === 'undefined' || typeof this.sourceTable.table === 'undefined';
        let emptyTargetTable = typeof this.targetTable.serverId === 'undefined' || typeof this.targetTable.database === 'undefined' || typeof this.targetTable.table === 'undefined';
        let table = this.sourceTable;
        let sub = 'sourceField';
        if (!emptySourceTable && !emptyTargetTable) {
            const mapping = typeof this.taskDTO !== 'undefined' ? this.taskDTO.mapping : null;
            mySQLApiClient.getMappingFor2TableFlat(this.sourceTable.serverId, this.sourceTable.database, this.sourceTable.table,
                this.targetTable.serverId, this.targetTable.database, this.targetTable.table, mapping)
                .done(fields => this.fields = fields);

            return;
        } else if (emptySourceTable) {
            table = this.targetTable;
            sub = 'targetField';
        }
        if (emptySourceTable && emptyTargetTable) {
            return;
        }
        mySQLApiClient.getFieldForServerDatabaseAndTable(table.serverId, table.database, table.table).done(data => {
            const fields = this.fields;
            data.forEach((field, i) => {
                if (fields.length > i) {
                    fields[i][sub] = field.field;
                } else {
                    const _o = {};
                    _o[sub] = field.field;
                    fields.push(_o);
                }
            });

            this.fields = fields;
        });
    }

    handleMappableChange(e, idx) {
        const fields = this.fields;
        fields[idx].mappable = e.target.checked;
        this.fields = fields;
    }

    submit() {
        const mapping = this.fields.filter(field => field.mappable).map(field => {
            return {sourceField: field.sourceField, targetField: field.targetField}
        });
        const postParams = {
            taskName: this.taskName,
            mapping: mapping,
            source: this.sourceTable,
            target: this.targetTable,
            taskType: this.taskType,
            insertMode: this.insertMode,
            filters: this.filters
        };

        if (typeof taskDTO !== 'undefined') {
            postParams.taskId = taskDTO.taskId;
        }

        taskApiClient.createTaskAction(postParams).done(data => {
            location.href = DOMAIN + '/task/detail/?taskId=' + data.taskId;
        });
    }

    swapField(dragField, dropField) {
        let fields = this.fields;
        const dragFieldIdx = fields.indexOf(dragField);
        const dropFieldIdx = fields.indexOf(dropField);

        if (dragFieldIdx === dropFieldIdx) return;

        const tmp = fields[dragFieldIdx].sourceField;
        fields[dragFieldIdx].sourceField = fields[dropFieldIdx].sourceField;
        fields[dropFieldIdx].sourceField = tmp;

        fields[dropFieldIdx].mappable = true;
        fields[dragFieldIdx].mappable = false;

        this.fields = fields;
    }

    editField(idx, fieldName) {
        let fields = this.fields;
        fields[idx].sourceField = fieldName;
        this.fields = fields;
    }

    render() {
        return (
            <div className="container mt-3">
                <h1>Task Information</h1>
                <form>
                    <h4>Choose Source and Target table</h4>
                    <div className="form-group">
                        <label htmlFor="name">Name</label>
                        <input type="text" className="form-control" id="name" name="name" placeholder="Enter Task Name"
                               defaultValue={this.taskName}
                               onChange={e => this.taskName = e.target.value}/>
                    </div>

                    <div className="row">
                        <div className="col">
                            <div className="form-group">
                                <label htmlFor="name">Task Type</label>
                                <Select className="fullWidth" btnTitle="Select Task Type"
                                        options={this.taskTypes.map((type, idx) => new SelectOption(idx, type))}
                                        onItemClick={o => this.taskType = o.value}
                                        value={this.taskTypes.indexOf(this.taskType)}/>
                            </div>
                        </div>
                        <div className="col">
                            <div className="form-group">
                                <label htmlFor="name">Insert Mode</label>
                                <Select className="fullWidth" btnTitle="Select Insert Mode"
                                        options={this.insertModes.map((mode, idx) => new SelectOption(idx, mode))}
                                        onItemClick={o => this.insertMode = o.value}
                                        value={this.insertModes.indexOf(this.insertMode)}/>
                            </div>
                        </div>
                    </div>

                    <div className="row">
                        <div className="col">
                            <TableSelector table={this.sourceTable} title='Source'/>
                        </div>
                        <div className="col">
                            <TableSelector table={this.targetTable} title='Target'/>
                        </div>
                    </div>

                    <h4 className="mt-3">Mapping</h4>
                    {
                        this.fields.length > 0 ?
                            <Table th={['Source Fields', 'Sync?', 'Target Fields']} className="mt-3">
                                <FieldRowList
                                    fields={this.fields}
                                    handleMappableChange={(e, idx) => this.handleMappableChange(e, idx)}
                                    swapField={this.swapField.bind(this)}
                                    editField={this.editField.bind(this)}/>
                            </Table> : ''
                    }

                    <h4 className="mt-3">Filter</h4>
                    <TagEditor items={this.filters}
                               validator={(value, cb) => mySQLApiClient.validateFilter(this.sourceTable.serverId, this.sourceTable.database, this.sourceTable.table, value)
                                   .done(data => cb(data))}/>

                    <button type="button" className="btn btn-primary float-right mt-3"
                            disabled={!this.readyToSubmit}
                            onClick={() => this.submit()}>
                        Submit
                    </button>
                </form>
            </div>
        )
    }
}

@observer
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
            {...this.props}
            onDrop={this.onDrop.bind(this)}
            captureDrapStartField={this.captureDrapStartField.bind(this)}
        />);
    }
}

@observer
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
                            onChange={e => this.props.handleMappableChange(e, this.props.idx)}/>}
                </td>
                {
                    field.targetField == null ? <td></td> :
                        <td>{field.targetField}</td>
                }
            </tr>
        );
    }
}