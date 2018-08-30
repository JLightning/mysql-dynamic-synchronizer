import React from 'react';

export default class Modal extends React.Component {

    constructor(props) {
        super(props);
        this.state = {contents: [], buttons: []};
    }

    componentDidMount() {
        this.checkChildProps();
    }

    componentDidUpdate(prevProps) {
        if (this.props.children !== prevProps.children) {
            this.checkChildProps();
        }
    }

    checkChildProps() {
        const contents = [];
        const buttons = [];
        let children = this.props.children;
        if (!Array.isArray(children)) {
            children = [children];
        }
        children.forEach(item => {
            const type = item.type;
            if (type.name === 'ModalButton') {
                buttons.push(item);
            } else {
                contents.push(item);
            }
        });
        this.setState({contents, buttons})
    }

    show() {
        $(this.modal).modal('show');
    }

    render() {
        return (
            <div className="modal" tabIndex="-1" role="dialog" ref={o => this.modal = o}>
                <div className="modal-dialog" role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">{this.props.title || 'Modal Title'}</h5>
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                        <div className="modal-body">
                            {this.state.contents}
                        </div>
                        <div className="modal-footer">
                            {this.state.buttons}
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export class ModalButton extends React.Component {

    render() {
        return <button type="button" className={"btn " + this.props.className} onClick={this.props.onClick}
                       data-dismiss="modal">{this.props.title || 'Button'}</button>;
    }
}