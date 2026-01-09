function fn() {
    const classPath = 'classpath:acceptance/';

    let config = {
        // PATHS
        classPath: classPath,
        behavioursPath: classPath + 'features/',
        // URLS
        baseUrl: 'http://localhost:8080/safecube'
    };

    karate.configure('connectTimeout', 500);
    karate.configure('readTimeout', 500);

    return config;
}
