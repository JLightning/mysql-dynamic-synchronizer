import React from "react";
import TableSelector from "./table-selector";

export default class TableSelectorEditable extends TableSelector {

    constructor(props) {
        super(props);
        this.state.showDone = true;
    }

    render() {
        let doneBtn = '';
        let inputClass = 'col-12'
        if (this.state.showDone) {
            doneBtn = (
                <div className="col-2">
                    <a className="btn btn-primary text-white" onClick={e => {
                        this.setState({showDone: false});
                        if (this.props.onSelected !== undefined) {
                            this.props.onSelected({
                                serverId: this.state.serverId,
                                database: this.state.database,
                                table: this.state.table
                            });
                        }
                    }}>Done</a>
                </div>
            );
            inputClass = 'col-10';
        }

        return (
            <div>
                <p>{this.props.title}</p>
                {this.renderServerAndDb()}
                <div className="row mt-3">
                    <div className={inputClass}>
                        <input type="text" className="form-control" placeholder="New table name"
                               onChange={e => this.setState({table: e.target.value})}/>
                    </div>
                    {doneBtn}
                </div>
            </div>
        );
    }
}