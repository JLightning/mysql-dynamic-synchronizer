import React from 'react';
import {observer} from 'mobx-react';
import {observable} from "mobx/lib/mobx";
import PropTypes from "prop-types";

@observer
export default class TagEditor extends React.Component {

    @observable inputValue = '';

    render() {
        return (
            <div>
                <div className="filterWrapper">
                    {this.props.items.map((filter, idx) =>
                        <span key={idx} className="filter btn btn-secondary btn-sm mr-2"
                              onClick={() => this.props.items.splice(idx, 1)}>
                            <span>{filter}</span>
                            <i className="fa fa-close ml-2" aria-hidden="true"/>
                        </span>
                    )}
                </div>
                <input type="text" className="form-control mt-2" id="filter" name="filter" placeholder="Enter Task Name"
                       onKeyPress={(e) => {
                           if (e.key === 'Enter') {
                               const value = e.target.value;
                               if (value.trim() !== '') {
                                   this.props.validator(value, data => {
                                       this.props.items.push(data);
                                       this.inputValue = '';
                                   });
                               }
                               return;
                           }
                       }}
                       value={this.inputValue}
                       onChange={e => this.inputValue = e.target.value}/>
            </div>
        );
    }
}

TagEditor.propTypes = {
    items: PropTypes.array,
    validator: PropTypes.func
};