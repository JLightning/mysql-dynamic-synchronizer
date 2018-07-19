import ReactDOM from "react-dom";
import React from 'react';
import TableSelector from "./common/table-selector";

class TableStructureSync extends React.Component {

    constructor(props) {
        super(props);
        this.state = {table: {}};
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
                        <TableSelector table={this.state.table.target} title='Target'
                                       onSelected={o => this.tableSelected(o, false)}/>
                    </div>
                </div>
            </div>
        );
    }
}

if (document.getElementById('tableStructureSyncWrapper') !== null) {
    ReactDOM.render(<TableStructureSync/>, document.getElementById('tableStructureSyncWrapper'));
}