import ReactDOM from "react-dom";
import React from 'react';
import TableSelector from "./common/table-selector";
import TableSelectorEditable from "./common/table-selector-editable";
import mySQLApiClient from "./api-client/mysql-api-client";
import Table from "./common/table";
import EditableText from "./common/editable-text";
import toolApiClient from "./api-client/tool-api-client";
import {computed, observable, autorun} from "mobx";
import {observer} from 'mobx-react';

@observer
class TableStructureSync extends React.Component {

    @observable sourceTable = {};
    @observable targetTable = {};
    @observable fields = [];

    constructor(props) {
        super(props);

        autorun(() => this.getFields());
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

    handleMappableChange(e, idx) {
        this.fields[idx].mappable = e.target.checked;
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
        return this.fields.length > 0;
    }

    render() {
        return (
            <div className="container mt-3">
                <div className="row">
                    <div className="col">
                        <TableSelector table={this.sourceTable} title='Source'/>
                    </div>
                    <div className="col">
                        <TableSelectorEditable table={this.targetTable} title='Target'/>
                    </div>
                </div>
                {
                    this.fields.length > 0 ?
                        <Table th={['Source Fields', 'Sync?', 'Target Fields']} className="mt-3">
                            {this.fields.map((field, idx) => <FieldRow field={field} key={idx}
                                                                             updateTargetField={value => this.updateTargetField(value, idx)}
                                                                             handleMappableChange={e => this.handleMappableChange(e, idx)}/>)}
                        </Table> : ''
                }

                <button type="button" className="btn btn-primary float-right mt-3"
                        disabled={!this.readyForSubmit}
                        onClick={() => this.submit()}>
                    Submit
                </button>
            </div>
        );
    }
}

@observer
class FieldRow extends React.Component {

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
                    {<input type="checkbox" checked={field.mappable}
                            onChange={e => this.props.handleMappableChange(e)}/>}
                </td>
                <td>
                    <EditableText updateValue={this.props.updateTargetField} value={field.targetField}/>
                </td>
            </tr>
        );
    }
}

if (document.getElementById('tableStructureSyncWrapper') !== null) {
    ReactDOM.render(<TableStructureSync/>, document.getElementById('tableStructureSyncWrapper'));
}