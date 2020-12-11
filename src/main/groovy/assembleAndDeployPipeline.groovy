#!groovy

import groovy.json.JsonSlurperClassic

pipeline {
    agent any

    parameters {
        //TODO JHE (09.12.2020) : couldn't we provide the target only as part of the PARAMETER params?
        string(name: 'TARGET')
        string(name: 'PARAMETER', description: 'JSON String containing all required info')

    }

    stages {

        stage("Copy Revision file") {
            steps {
                //TODO JHE (11.12.2020) : get the lock name from a parameter, and coordonate it with operations done during build Pipeline
                lock("revisionFileOperation") {
                    fileOperations([
                            folderCreateOperation(folderPath: "./clonedInformation"),
                            fileCopyOperation(includes: "${env.GRADLE_USER_HOME_PATH}/Revisions.json", targetLocation: "./clonedInformation")
                    ])
                }
            }
        }

        // JHE (09.12.2020): Seems not easily possible to use variable in stage name for declarative pipeline : https://issues.jenkins.io/browse/JENKINS-43820
        stage("Assemble and Deploy") {
            steps {
                script {
                    commonPatchFunctions.log("assembleAndDeploy Job will be started for ${params.target} with following parameter ${params.PARAMETER}")
                    def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETER)
                    commonPatchFunctions.log("paramAsJson = ${paramsAsJson}")
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