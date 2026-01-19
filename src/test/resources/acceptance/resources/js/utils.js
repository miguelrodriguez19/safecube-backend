function fn() {
    const utils = {};

    /**
     * Authorization header helper.
     */
    utils.bearer = function(token) {
        return { Authorization: 'Bearer ' + token };
    };

    utils.randomEmail = function(prefix) {
        var uuid = java.util.UUID.randomUUID().toString().substring(0, 8);
        return prefix + '_' + uuid + '@safecube.io';
    };



    return utils;
}