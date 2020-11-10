pipeline {
    agent any

    parameters {
        string(name: 'jsonParam', description: 'JSON String containing all required info')
    }

    stages {
        stage("test") {
            steps {
                println "This is a test where jsonParam = ${params.jsonParam}"
            }
        }
    }
}