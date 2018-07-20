import React from 'react';

export default class EditableText extends React.Component {

    constructor(props) {
        super(props);
        this.state = {editing: false, tmpValue: props.value || ''};
    }

    componentDidUpdate(prevProps) {
        if (prevProps.value !== this.props.value) {
            this.setState({tmpValue: this.props.value});
        }
    }

    cancelChange() {
        this.setState({editing: false});
    }

    render() {
        const value = this.props.value || '';
        if (this.state.editing) {
            return (
                <div>
                    <input type="text" value={this.state.tmpValue}
                           onChange={e => this.setState({tmpValue: e.target.value})}/>
                    <i className="fa fa-check-square-o ml-2 pointer" aria-hidden="true"
                       onClick={() => {
                           this.props.updateValue(this.state.tmpValue);
                           this.setState({editing: false});
                       }}/>
                    <i className="fa fa-close ml-2 pointer" aria-hidden="true"
                       onClick={this.cancelChange.bind(this)}/>
                </div>
            )
        }
        return <div className="pointer" onClick={() => this.setState({editing: true})}>{value}</div>
    }
}