import React from 'react';
import ReactDOM from "react-dom";
import PropTypes from 'prop-types';

export default class Modal extends React.Component {

    modal = null;

    setModalElm(modalElm) {
        this.modal = modalElm;
        $(modalElm).modal();
        $(modalElm).on('hidden.bs.modal', e => this.props.onHide(e));
    }

    hide() {
        $(this.modal).modal('hide');
    }

    render() {
        if (document.getElementById('modalWrapper') !== null) {
            return ReactDOM.createPortal(
                <div className="modal" tabIndex="-1" role="dialog" ref={modalElm => this.setModalElm(modalElm)}>
                    <div className="modal-dialog" role="document">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">{this.props.title || 'Modal Title'}</h5>
                                <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                                    <span aria-hidden="true">&times;</span>
                                </button>
                            </div>
                            <div className="modal-body">
                                {this.props.children}
                            </div>
                            <div className="modal-footer">
                                {this.props.button}
                            </div>
                        </div>
                    </div>
                </div>,
                document.getElementById('modalWrapper')
            );
        }
        return null;
    }
}

Modal.propTypes = {
    button: PropTypes.element.isRequired,
    title: PropTypes.string,
    onHide: PropTypes.func
};

export class ModalButton extends React.Component {

    render() {
        return <button type="button" className={"btn " + (this.props.className || '')} onClick={e => this.onClick(e)}>{this.props.children}</button>;
    }

    onClick(e) {
        if (this.props.onClick) this.props.onClick(e);
    }
}