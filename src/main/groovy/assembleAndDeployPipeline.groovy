pipeline {
    agent any
    stages {
        stage("test") {
            steps {
                println "This is a test"
            }
        }
    }
}