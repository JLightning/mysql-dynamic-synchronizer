const Stomp = require('@stomp/stompjs');

class WsClient {

    constructor() {
        this.successCallback = [];
        this.errorCallback = [];
        this.connected = false;
        this.stompClient = Stomp.client('ws://localhost:8080/ws');

        this.stompClient.connect('guest', 'guest', this.onWsConnected.bind(this), this.onWsError.bind(this));
        this.stompClient.reconnect_delay = 5000;
    }

    onWsConnected() {
        console.log("WS connected");
        this.connected = true;
        this.successCallback.forEach(cb => cb());
    }

    onWsError() {
        this.connected = false;
        this.errorCallback.forEach(cb => cb());
    }

    connect(successCb, errorCb) {
        if (this.connected) {
            successCb();
        }
        this.successCallback.push(successCb);
        this.errorCallback.push(errorCb);
    }

    subscribe(channel, callback) {
        const r = () => this.stompClient.subscribe(channel, payload => {
            const data = JSON.parse(payload.body);
            callback(data);
        });
        if (this.connected) {
            r();
        }

        this.successCallback.push(r);
    }

    send(channel, body1, body2) {
        this.stompClient.send(channel, body1, body2);
    }
}

const wsClient = new WsClient();
export default wsClient;