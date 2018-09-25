export default class Validator {

    static isNull(input) {
        return input === null || input === undefined || typeof input === 'undefined';
    }

    static isEmail(input) {
        var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return !this.isNull(input) && re.test(String(input).toLowerCase());
    }
}