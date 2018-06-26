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
            result.push(<a key={option.id} className="dropdown-item" href="#"
                           onClick={() => this.onItemClick(option)}>{option.value}</a>)
        });
        return result;
    }

    componentDidUpdate(prevProps) {
        if (this.props.options !== prevProps.options || this.props.value !== prevProps.value) {
            this.props.options.forEach((o) => {
                if (o.id === this.props.value) {
                    this.setState({btnTitle: o.value});
                }
            });
        }
    }

    onItemClick(option) {
        this.setState({show: false, btnTitle: option.value});
        if (this.props.onItemClick !== undefined) {
            this.props.onItemClick(option);
        }
    }

    render() {
        const className = 'dropdown-menu' + (this.state.show ? ' show' : '');
        return (
            <div className={'dropdown ' + (this.props.className || '')}>
                <button className="btn btn-secondary dropdown-toggle" type="button"
                        onClick={() => this.setState({show: !this.state.show})}>
                    {this.state.btnTitle || this.props.btnTitle || 'Select'}
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