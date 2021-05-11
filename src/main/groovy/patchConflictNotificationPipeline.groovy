#!groovy
import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)

pipeline {
    parameters {
        string(name: 'PARAMETERS', description: "JSON parameters")
    }

    agent any

    stages {
        stage("SendMail") {
            steps {
                script {
                    emailext to: "Julien.Helbling@apgsga.ch", subject: "Test patch conflict", body: "this is only a test with hardcoded values ...."
                }
            }
        }

    }
}