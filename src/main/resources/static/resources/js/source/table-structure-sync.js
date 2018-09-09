import ReactDOM from "react-dom";
import React from 'react';
import TableSelector from "./common/table-selector";
import TableSelectorEditable from "./common/table-selector-editable";
import mySQLApiClient from "./api-client/mysql-api-client";
import Table from "./common/table";
import EditableText from "./common/editable-text";
import toolApiClient from "./api-client/tool-api-client";

class TableStructureSync extends React.Component {

    constructor(props) {
        super(props);
        this.state = {fields: [], table: {source:{}, target: {}}, readyForSubmit: true};
    }

    tableSelected(params, isSource) {
        const sub2 = isSource ? 'source' : 'target';

        const table = this.state.table;
        table[sub2] = params;
        this.setState({table: table});

        if (isSource) {
            mySQLApiClient.getFieldForServerDatabaseAndTable(params.serverId, params.database, params.table).done(data => {
                let fields = [];

                data.forEach(field => fields.push({
                    sourceField: field.field,
                    mappable: true,
                    targetField: field.field
                }));

                this.setState({fields: fields});
            });
        }
    }

    updateTargetField(value, idx) {
        let fields = this.state.fields;
        fields[idx].targetField = value;
        this.setState({fields});
    }

    handleMappableChange(e, idx) {
        const fields = this.state.fields;
        fields[idx].mappable = e.target.checked;
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

        toolApiClient.syncStructure(postParams).done(data => alert(data));
    }

    render() {
        return (
            <div className="container mt-3">
                <div className="row">
                    <div className="col">
                        <TableSelector table={this.state.table.source} title='Source'
                                       onSelected={o => this.tableSelected(o, true)}/>
                    </div>
                    <div className="col">
                        <TableSelectorEditable table={this.state.table.target} title='Target'
                                               onSelected={o => this.tableSelected(o, false)}/>
                    </div>
                </div>
                {
                    this.state.fields.length > 0 ?
                        <Table th={['Source Fields', 'Sync?', 'Target Fields']} className="mt-3">
                            {this.state.fields.map((field, idx) => <FieldRow field={field} key={idx}
                                                                             updateTargetField={value => this.updateTargetField(value, idx)}
                                                                             handleMappableChange={e => this.handleMappableChange(e, idx)}/>)}
                        </Table> : ''
                }

                <button type="button" className="btn btn-primary float-right mt-3"
                        disabled={!this.state.readyForSubmit}
                        onClick={() => this.submit()}>
                    Submit
                </button>
            </div>
        );
    }
}

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