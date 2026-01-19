function fn() {
    const utils = {};

    /**
     * Authorization header helper.
     */
    utils.bearer = function(token) {
        return { Authorization: 'Bearer ' + token };
    };

    /**
     * Generate random email with prefix
     */
    utils.randomEmail = function(prefix) {
        var uuid = java.util.UUID.randomUUID().toString().substring(0, 8);
        return prefix + '_' + uuid + '@safecube.io';
    };

    /**
     * Convert string to Base64 (for byte[] JSON fields).
     */
    utils.base64 = function(str) {
        return java.util.Base64.getEncoder().encodeToString(str.getBytes());
    };

    return utils;
}