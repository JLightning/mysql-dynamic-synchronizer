import AbstractClient from "./abstract-client";

class MessageApiClient extends AbstractClient {

    getErrorMessages() {
        return super.get('/api/message/errors', {});
    }
}

const messageApiClient = new MessageApiClient();
export default messageApiClient;
