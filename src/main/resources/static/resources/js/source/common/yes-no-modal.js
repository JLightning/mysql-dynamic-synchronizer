import React, {Fragment} from 'react';
import Modal, {ModalButton} from './modal';
import PropTypes from "prop-types";

export default class YesNoModal extends React.Component {

    modal = null;

    render() {
        return <Modal ref={elm => this.modal = elm} {...this.props} button={this.getButton()}/>
    }

    getButton() {
        return (
            <Fragment>
                <ModalButton onClick={e => this.onNo(e)}>No</ModalButton>
                <ModalButton className='btn-primary ml-1' onClick={e => this.onYes(e)}>Yes</ModalButton>
            </Fragment>
        )
    }

    onYes(e) {
        if (this.modal !== null) this.modal.hide();
        if (this.props.onYes) this.props.onYes(e)
    }

    onNo(e) {
        if (this.modal !== null) this.modal.hide();
        if (this.props.onNo) this.props.onNo(e)
    }
}

YesNoModal.propTypes = {
    onYes: PropTypes.func,
    onNo: PropTypes.func,
    title: PropTypes.string,
    onHide: PropTypes.func
}