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
import {TaskDTO} from "../../../dto/task-dto";
import {Table as TableDTO} from "../../../dto/table";
import Validator from "../../../util/validator";
import {SimpleFieldMappingDTO} from "../../../dto/simple-field-mapping-dto";
import {MySQLFieldWithMappingDTO} from "../../../dto/my-sqlfield-with-mapping-dto";

@observer
export default class TaskCreate extends React.Component {

    @observable task: TaskDTO = new TaskDTO(0, '', [], new TableDTO(), new TableDTO(), '', '', []);

    @observable fields: MySQLFieldWithMappingDTO[] = [];
    @observable taskTypes = [];
    @observable insertModes = [];
    autorunDisposers = [];

    constructor(props) {
        super(props);
        if (!Validator.isNull(props.match.params.taskId)) {
            taskApiClient.detail(props.match.params.taskId).done((data: TaskDTO) => this.task = data);
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
        return this.fields.length > 0 && this.task.taskName !== '' && this.task.insertMode !== '' && this.task.taskType !== '';
    }

    getMapping() {
        let source = this.task.source;
        let target = this.task.target;

        let emptySourceTable = Validator.isEmptyString(source.serverId) || Validator.isEmptyString(source.database) || Validator.isEmptyString(source.table);
        let emptyTargetTable = Validator.isEmptyString(target.serverId) || Validator.isEmptyString(target.database) || Validator.isEmptyString(target.table);

        let table = source;
        let sub = 'sourceField';
        if (!emptySourceTable && !emptyTargetTable) {
            mySQLApiClient.getMappingFor2TableFlat(source.serverId, source.database, source.table, target.serverId, target.database, target.table, this.task.mapping)
                .done(fields => this.fields = fields);

            return;
        } else if (emptySourceTable) {
            table = target;
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
        const mapping = this.fields.filter(field => field.mappable).map(field => new SimpleFieldMappingDTO(field.sourceField, field.targetField));

        const postTask = this.task;
        postTask.mapping = mapping;

        taskApiClient.create(postTask).done(data => {
            location.href = DOMAIN + '/task/detail/' + data.taskId;
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
                               defaultValue={this.task.taskName}
                               onChange={e => this.task.taskName = e.target.value}/>
                    </div>

                    <div className="row">
                        <div className="col">
                            <div className="form-group">
                                <label htmlFor="name">Task Type</label>
                                <Select className="fullWidth" btnTitle="Select Task Type"
                                        options={this.taskTypes.map((type, idx) => new SelectOption(idx, type))}
                                        onItemClick={o => this.task.taskType = o.value}
                                        value={this.taskTypes.indexOf(this.task.taskType)}/>
                            </div>
                        </div>
                        <div className="col">
                            <div className="form-group">
                                <label htmlFor="name">Insert Mode</label>
                                <Select className="fullWidth" btnTitle="Select Insert Mode"
                                        options={this.insertModes.map((mode, idx) => new SelectOption(idx, mode))}
                                        onItemClick={o => this.task.insertMode = o.value}
                                        value={this.insertModes.indexOf(this.task.insertMode)}/>
                            </div>
                        </div>
                    </div>

                    <div className="row">
                        <div className="col">
                            <TableSelector table={this.task.source} title='Source'/>
                        </div>
                        <div className="col">
                            <TableSelector table={this.task.target} title='Target'/>
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
                    <TagEditor items={this.task.filters}
                               validator={(value, cb) => mySQLApiClient.validateFilter(this.task.source.serverId, this.task.source.database, this.task.source.table, value)
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
        if (!Validator.isNull(this.props.isInEditMode) && this.props.isInEditMode.get())
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