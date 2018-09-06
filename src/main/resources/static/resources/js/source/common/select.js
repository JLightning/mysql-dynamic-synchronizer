import React from 'react';
import PropTypes from 'prop-types';
import {observable} from "mobx";
import {observer} from 'mobx-react';

@observer
export default class Select extends React.Component {

    @observable showDropdown = false;
    @observable btnTitle = null;

    renderDropdownItems() {
        let options = this.props.options || [];
        return options.map(option => <a key={option.id} className="dropdown-item" href="#"
                                         onClick={() => this.onItemClick(option)}>{option.value}</a>);
    }

    componentDidUpdate(prevProps) {
        if (this.props.options !== prevProps.options || this.props.value !== prevProps.value) {
            this.props.options.forEach((o) => {
                if (o.id === this.props.value) {
                    this.btnTitle = o.value;
                }
            });
        }
    }

    onItemClick(option) {
        this.showDropdown = false;
        this.btnTitle = option.value;
        if (this.props.onItemClick !== undefined) {
            this.props.onItemClick(option);
        }
    }

    render() {
        const className = 'dropdown-menu' + (this.showDropdown ? ' show' : '');
        return (
            <div className={'dropdown ' + (this.props.className || '')}>
                <button className="btn btn-secondary dropdown-toggle" type="button"
                        onClick={() => this.showDropdown = !this.showDropdown}>
                    {this.btnTitle || this.props.btnTitle || 'Select'}
                </button>
                <div className="col">
                    <div className={className} aria-labelledby="dropdownMenuButton">
                        {this.renderDropdownItems()}
                    </div>
                </div>
            </div>
        );
    }
}

Select.propTypes = {
    options: PropTypes.array,
    btnTitle: PropTypes.string,
    onItemClick: PropTypes.func
};

export class SelectOption {

    constructor(id, value) {
        this.id = id;
        this.value = value;
    }
}