function fn() {
    const classPath = 'classpath:acceptance/';

    const config = {
        // PATHS
        classPath: classPath,
        behavioursPath: classPath + 'features/',

        // JS FILES
        utilsJs: karate.call(classPath + 'resources/js/utils.js'),

        // URLS
        baseUrl: 'http://localhost:8080/safecube',

        // HELPERS
        registerHelper: classPath + '/resources/_helpers/registerHelper.feature',
        loginHelper: classPath + '/resources/_helpers/loginHelper.feature',
        createUserHelper: classPath + '/resources/_helpers/createUserHelper.feature',
    };

    karate.configure('connectTimeout', 500000);
    karate.configure('readTimeout', 500000);

    return config;
}
