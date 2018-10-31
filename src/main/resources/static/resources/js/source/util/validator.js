// @flow

export default class Validator {

    static isNull(input: any) {
        return typeof input === 'undefined' || input === null || input === undefined;
    }

    static isEmptyString(input: ?string) {
        return this.isNull(input) || input === '';
    }

    static isEmail(input: string) {
        var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return !this.isNull(input) && re.test(String(input).toLowerCase());
    }
}
