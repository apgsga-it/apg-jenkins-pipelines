pipeline {
    agent any

    parameters {
        string(name: 'TARGET')
        string(name: 'PARAMETER', description: 'JSON String containing all required info')
    }

    stages {
        stage("test") {
            steps {
                println "This is a test where TARGET = ${params.TARGET} and PARAMETER = ${params.PARAMETER}"
            }
        }
    }
}