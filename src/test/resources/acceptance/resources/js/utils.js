function fn() {
    const utils = {};

    /**
     * Authorization header helper.
     */
    utils.bearer = function(token) {
        return { Authorization: 'Bearer ' + token };
    };

    return utils;
}