import React from "react";
import TableSelector from "./table-selector";
import {observable} from 'mobx';
import {observer} from 'mobx-react';
import PropTypes from "prop-types";

@observer
export default class TableSelectorEditable extends TableSelector {

    finished = observable.box(false);
    @observable tmpTableName = '';

    constructor(props) {
        super(props);
        if (props.finished !== undefined) this.finished = props.finished;
    }

    render() {
        let doneBtn = '';
        let inputClass = 'col-12'
        if (!this.finished.get()) {
            doneBtn = (
                <div className="col-2">
                    <a className="btn btn-primary text-white" onClick={e => {
                        this.finished.set(true);
                        this.props.table.table = this.tmpTableName;
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
                               onChange={e => this.tmpTableName = e.target.value}/>
                    </div>
                    {doneBtn}
                </div>
            </div>
        );
    }
}

TableSelectorEditable.propTypes = {
    table: PropTypes.object,
    title: PropTypes.string.isRequired,
    finished: PropTypes.object
};