import React from 'react';
import TableSelector from '../../../common/table-selector';
import mySQLApiClient from '../../../api-client/mysql-api-client';
import taskApiClient from "../../../api-client/task-api-client";
import Table from "../../../common/table";
import Select, {SelectOption} from "../../../common/select";
import {autorun, computed, observable} from 'mobx';
import {observer} from 'mobx-react';
import TagEditor from "../../../common/tag-editor";
import PropTypes from 'prop-types';

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
    autorunDisposers = [];

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

        this.autorunDisposers.push(autorun(() => this.getMapping()));
    }

    componentDidMount() {
        taskApiClient.getTaskTypes().done(taskTypes => this.taskTypes = taskTypes);
        mySQLApiClient.getInsertModes().done(insertModes => this.insertModes = insertModes);
    }

    componentWillUnmount() {
        this.autorunDisposers.forEach(disposer => disposer());
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

        taskApiClient.create(postParams).done(data => {
            location.href = DOMAIN + '/task/detail/?taskId=' + data.taskId;
        });
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

    capturedField = null;
    editMode = {};
    autorunDisposer = [];

    constructor(props) {
        super(props);

        this.autorunDisposer.push(autorun(() => props.fields.forEach((field, idx) => this.editMode[idx] = observable.box(false))));
    }

    componentWillUnmount() {
        this.autorunDisposer.forEach(disposer => disposer());
    }

    captureDrapStartField(field) {
        this.capturedField = field;
    }

    onDrop(field) {
        if (this.capturedField !== null) {
            let fields = this.props.fields;
            const dragFieldIdx = fields.indexOf(this.capturedField);
            const dropFieldIdx = fields.indexOf(field);

            if (this.editMode[dropFieldIdx].get() === true) {
                showError('Cannot drop on an editing field');
                return;
            }

            if (dragFieldIdx === dropFieldIdx) return;

            const tmp = fields[dragFieldIdx].sourceField;
            fields[dragFieldIdx].sourceField = fields[dropFieldIdx].sourceField;
            fields[dropFieldIdx].sourceField = tmp;

            fields[dropFieldIdx].mappable = true;
            fields[dragFieldIdx].mappable = false;
        }
    }

    render() {
        return this.props.fields.map((field, idx) => <FieldRow
            key={idx}
            idx={idx}
            field={field}
            {...this.props}
            isInEditMode={this.editMode[idx]}
            onDrop={this.onDrop.bind(this)}
            captureDrapStartField={this.captureDrapStartField.bind(this)}
        />);
    }
}

@observer
class FieldRow extends React.Component {

    @observable dropTargetClass = '';

    onDragOver(e) {
        this.dropTargetClass = 'bg-primary text-white';
        e.preventDefault();
        e.stopPropagation();
    }

    onDragLeave(e) {
        this.dropTargetClass = '';
    }

    onDrop(e) {
        this.dropTargetClass = '';
    }

    getSourceText() {
        const field = this.props.field;
        if (this.props.isInEditMode.get())
            return (
                <div>
                    <input type="text" value={field.sourceField}
                           onChange={e => this.props.editField(this.props.idx, e.target.value)}/>
                    <i className="fa fa-check-square-o ml-2" aria-hidden="true"
                       onClick={() => this.props.isInEditMode.set(false)}/>
                </div>
            );
        return <div onClick={() => this.props.isInEditMode.set(true)}
                    style={{minHeight: '1.5rem'}}>{field.sourceField}</div>;
    }

    render() {
        const field = this.props.field;
        const sourceText = this.getSourceText();
        return (
            <tr>
                {
                    <td className={this.dropTargetClass + ' pointer'}
                        draggable="true"
                        onDragStart={e => {
                            if (this.props.isInEditMode.get()) {
                                e.preventDefault();
                                return;
                            }
                            this.dropTargetClass = 'bg-success text-white';
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

FieldRow.propTypes = {
    isInEditMode: PropTypes.object,
    captureDrapStartField: PropTypes.func,
    onDrop: PropTypes.func
};