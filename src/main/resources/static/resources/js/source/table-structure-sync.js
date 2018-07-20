import ReactDOM from "react-dom";
import React from 'react';
import TableSelector from "./common/table-selector";
import TableSelectorEditable from "./common/table-selector-editable";
import mySQLApiClient from "./api-client/mysql-api-client";
import Table from "./common/table";

class TableStructureSync extends React.Component {

    constructor(props) {
        super(props);
        this.state = {fields: [], table: {}, readyForSubmit: false};
    }

    tableSelected(params, isSource) {
        const sub2 = isSource ? 'source' : 'target';

        const table = this.state.table;
        table[sub2] = params;
        this.setState({table: table});

        mySQLApiClient.getFieldForServerDatabaseAndTable(params.serverId, params.database, params.table).done(data => {
            let fields = [];

            data.forEach(field => fields.push({sourceField: field.field, mappable: true, targetField: field.field}));

            this.setState({fields: fields});
        });
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
                            {this.state.fields.map((field, idx) => <FieldRow field={field} key={idx}/>)}
                        </Table> : ''
                }
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
                {
                    field.targetField == null ? <td></td> :
                        <td>{field.targetField}</td>
                }
            </tr>
        );
    }
}

if (document.getElementById('tableStructureSyncWrapper') !== null) {
    ReactDOM.render(<TableStructureSync/>, document.getElementById('tableStructureSyncWrapper'));
}