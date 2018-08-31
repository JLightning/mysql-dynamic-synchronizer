import React from 'react';

export default class Modal extends React.Component {

    constructor(props) {
        super(props);
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
                            {this.props.content}
                        </div>
                        <div className="modal-footer">
                            {this.props.button}
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
                       data-dismiss="modal">{this.props.children}</button>;
    }
}