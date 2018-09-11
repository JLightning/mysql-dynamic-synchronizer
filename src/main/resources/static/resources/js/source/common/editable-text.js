import React from 'react';
import PropTypes from 'prop-types';
import {observable} from 'mobx';
import {observer} from 'mobx-react';

@observer
export default class EditableText extends React.Component {

    @observable editing = false;
    @observable tmpValue = '';

    constructor(props) {
        super(props);
        this.tmpValue = props.value || '';
    }

    componentDidUpdate(prevProps) {
        if (prevProps.value !== this.props.value) {
            this.tmpValue = this.props.value;
        }
    }

    cancelChange() {
        this.editing = false;
    }

    render() {
        const value = this.props.value || '';
        if (this.editing) {
            return (
                <div>
                    <input type="text" value={this.tmpValue}
                           onChange={e => this.tmpValue = e.target.value}/>
                    <i className="fa fa-check-square-o ml-2 pointer" aria-hidden="true"
                       onClick={() => {
                           this.props.updateValue(this.tmpValue);
                           this.editing = false;
                       }}/>
                    <i className="fa fa-close ml-2 pointer" aria-hidden="true"
                       onClick={this.cancelChange.bind(this)}/>
                </div>
            )
        }
        return <div className="pointer" onClick={() => this.editing = true}>{value}</div>
    }
}

EditableText.propTypes = {
    updateValue: PropTypes.func.isRequired,
    value: PropTypes.string
};