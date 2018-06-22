import React from 'react';

export default class Select extends React.Component {

    constructor(props) {
        super(props);
        this.state = {show: false}
    }

    renderDropdownItems() {
        const result = [];
        let options = this.props.options || [];
        options.forEach(option => {
            result.push(<a key={option.id} className="dropdown-item" href="#">{option.value}</a>)
        });
        return result;
    }

    render() {
        const className = 'dropdown-menu' + (this.state.show ? ' show' : '');
        return (
            <div className="dropdown">
                <button className="btn btn-secondary dropdown-toggle" type="button" onClick={() => this.setState({show: !this.state.show})}>
                    {this.props.btnTitle || 'Select'}
                </button>
                <div className={className} aria-labelledby="dropdownMenuButton">
                    {this.renderDropdownItems()}
                </div>
            </div>
        );
    }
}

export class SelectOption {

    constructor(id, value) {
        this.id = id;
        this.value = value;
    }
}