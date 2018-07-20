import React from "react";
import TableSelector from "./table-selector";

export default class TableSelectorEditable extends TableSelector {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <p>{this.props.title}</p>
                {this.renderServerAndDb()}
                <div className="row mt-3">
                    <div className="col-10">
                        <input type="text" className="form-control" placeholder="New table name"/>
                    </div>
                    <div className="col-2">
                        <a href="#" className="btn btn-primary">Done</a>
                    </div>
                </div>
            </div>
        );
    }
}