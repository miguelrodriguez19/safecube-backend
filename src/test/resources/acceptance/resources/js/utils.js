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

    /**
     * Generate Instant.now()
     */
    utils.instantNow = function() {
        return java.time.Instant.now(java.time.Clock.systemUTC()).toString();
    };

    /**
     * Add the amountDays to the date passed
     */
    utils.datePlusDays = function(date, amountDays){
        return java.time.Instant.parse(date.toString())
                    .plus(amountDays, java.time.temporal.ChronoUnit.DAYS).toString()
    }

    /**
     * CompareTo between params dates: date1 and date2
     */
    utils.compareDates = function(date1, date2){
        return java.time.Instant.parse(date1.toString())
                    .compareTo(java.time.Instant.parse(date2.toString()))
    }

    return utils;
}