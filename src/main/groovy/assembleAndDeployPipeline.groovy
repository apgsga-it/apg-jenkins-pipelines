#!groovy

import groovy.json.JsonSlurperClassic
import groovy.json.JsonSlurper

pipeline {
    agent any

    parameters {
        //TODO JHE (09.12.2020) : couldn't we provide the target only as part of the PARAMETER params?
        string(name: 'TARGET')
        string(name: 'PARAMETER', description: 'JSON String containing all required info')

    }

    stages {
        // JHE (09.12.2020): Seems not easily possible to use variable in stage name for declarative pipeline : https://issues.jenkins.io/browse/JENKINS-43820
        stage("Assemble and Deploy") {
            steps {
                script {
                    commonPatchFunctions.log("assembleAndDeploy Job will be started for ${params.target} with following parameter ${params.PARAMETER}")
                    def paramsAsJson = new JsonSlurper().setType(groovy.json.JsonParserType.LAX).parseText(params.PARAMETER)
                    assembleAndDeployPatchFunctions.assembleAndDeploy(params.TARGET, paramsAsJson)
                }
            }
        }
    }
    post {
        success {
            script {
                println "TODO JHE: implement success post job"
                // commonPatchFunctions.notifyDb(patchConfig,params.STAGE,params.SUCCESS_NOTIFICATION,null)
            }
        }
        unsuccessful {
            script {
                println "TODO JHE: implement fail post job"
                // commonPatchFunctions.notifyDb(patchConfig,params.STAGE,null,params.ERROR_NOTIFICATION)
            }
        }

    }
}