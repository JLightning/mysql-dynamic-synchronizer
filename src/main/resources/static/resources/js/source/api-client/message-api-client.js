import AbstractClient from "./abstract-client";

class MessageApiClient extends AbstractClient {

    /**
     * @param response {*}
     */
    getErrorMessages() {
        return super.get('/api/message/errors', {});
    }
}

const messageApiClient = new MessageApiClient();
export default messageApiClient;
