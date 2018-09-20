import React from 'react';
import TableSelector from "./common/table-selector";
import TableSelectorEditable from "./common/table-selector-editable";
import mySQLApiClient from "./api-client/mysql-api-client";
import Table from "./common/table";
import EditableText from "./common/editable-text";
import toolApiClient from "./api-client/tool-api-client";
import {autorun, computed, observable} from "mobx";
import {observer} from 'mobx-react';
import DevTools from 'mobx-react-devtools';

@observer
export default class TableStructureSync extends React.Component {

    @observable sourceTable = {};
    @observable targetTable = {};
    @observable fields = [];
    @observable editing = [];
    targetTableFinished = observable.box(false);

    constructor(props) {
        super(props);

        autorun(() => this.getFields());
        autorun(() => this.fields.forEach((field, idx) => {
            if (this.editing.length <= idx) {
                this.editing.push(observable.box(false));
            } else {
                this.editing[idx].set(false);
            }
        }))
    }

    getFields() {
        if (typeof this.sourceTable.serverId === 'undefined' || typeof this.sourceTable.database === 'undefined' || typeof this.sourceTable.table === 'undefined') return;

        mySQLApiClient.getFieldForServerDatabaseAndTable(this.sourceTable.serverId, this.sourceTable.database, this.sourceTable.table).done(data => {
            let fields = [];

            data.forEach(field => fields.push({
                sourceField: field.field,
                mappable: true,
                targetField: field.field
            }));

            this.fields = fields;
        });
    }

    updateTargetField(value, idx) {
        this.fields[idx].targetField = value;
    }

    submit() {
        const mapping = this.fields.filter(field => field.mappable).map(field => {
            return {sourceField: field.sourceField, targetField: field.targetField}
        });
        const postParams = {
            mapping: mapping,
            source: this.sourceTable,
            target: this.targetTable
        };

        toolApiClient.syncStructure(postParams).done(data => {
            if (data) {
                showError("Success");
            }
        });
    }

    @computed get readyForSubmit() {
        return this.fields.length > 0 && this.editing.filter(f => f.get() === true).length === 0 && this.targetTableFinished.get();
    }

    render() {
        return (
            <div className="container mt-3">
                <div className="row">
                    <div className="col">
                        <TableSelector table={this.sourceTable} title='Source'/>
                    </div>
                    <div className="col">
                        <TableSelectorEditable table={this.targetTable} title='Target'
                                               finished={this.targetTableFinished}/>
                    </div>
                </div>
                {
                    this.fields.length > 0 ?
                        <Table th={['Source Fields', 'Sync?', 'Target Fields']} className="mt-3">
                            {this.fields.map((field, idx) => <FieldRow field={field} key={idx}
                                                                       updateTargetField={value => this.updateTargetField(value, idx)}
                                                                       editing={this.editing[idx]}/>)}
                        </Table> : ''
                }

                <button type="button" className="btn btn-primary float-right mt-3"
                        disabled={!this.readyForSubmit}
                        onClick={() => this.submit()}>
                    Submit
                </button>
                <DevTools/>
            </div>
        );
    }
}

@observer
class FieldRow extends React.Component {

    updateTargetField(value) {
        this.props.field.targetField = value;
    }

    render() {
        const field = this.props.field;
        return (
            <tr>
                {
                    <td>
                        <div style={{minHeight: '1.5rem'}}>{field.sourceField}</div>
                    </td>
                }
                <td>
                    <input type="checkbox" checked={field.mappable}
                           onChange={e => this.props.field.mappable = e.target.checked}/>
                </td>
                <td>
                    <EditableText updateValue={this.updateTargetField.bind(this)} value={field.targetField}
                                  editing={this.props.editing}/>
                </td>
            </tr>
        );
    }
}
