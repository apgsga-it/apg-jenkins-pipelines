#!groovy

import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETER)

pipeline {
    agent any

    parameters {
        //TODO JHE (09.12.2020) : couldn't we provide the target only as part of the PARAMETER params? Waiting on IT-36505
        string(name: 'TARGET')
        string(name: 'PARAMETER', description: 'JSON String containing all required info')
    }

    stages {
        // TODO JHE (14.12.2020): This will probably be done differently, or maybe not necessary as soon as IT-36715 will be done
        stage("Copy Revision file") {
            steps {
                //TODO JHE (11.12.2020) : get the lock name from a parameter, and coordonate it with operations done during build Pipeline
                lock("revisionFileOperation") {
                    fileOperations([
                            folderCreateOperation(folderPath: "${env.WORKSPACE}/clonedInformation"),
                    ])
                    dir(env.GRADLE_USER_HOME_PATH) {
                        fileOperations([
                                fileCopyOperation(includes: "Revisions.json", targetLocation: "${env.WORKSPACE}/clonedInformation")
                        ])
                    }

                }
            }
        }

        // JHE (09.12.2020): Seems not easily possible to use variable in stage name for declarative pipeline : https://issues.jenkins.io/browse/JENKINS-43820
        stage("Assemble and Deploy") {
            steps {
                script {
                    commonPatchFunctions.log("assembleAndDeploy Job will be started for ${params.target} with following parameter ${params.PARAMETER}")
                    commonPatchFunctions.log("paramAsJson = ${paramsAsJson}")
                    assembleAndDeployPatchFunctions.assembleAndDeploy(params.TARGET, paramsAsJson)
                }
            }
        }
    }
    post {
        success {
            script {
                paramsAsJson.patches.each{patchNumber ->
                    commonPatchFunctions.notifyDb(patchNumber,"assembleAndDeploy",params.PARAMETER.successNotification,null)
                }
            }
        }
        unsuccessful {
            script {
                paramsAsJson.patches.each{patchNumber ->
                    commonPatchFunctions.notifyDb(patchNumber,"assembleAndDeploy",null,params.PARAMETER.errorNotification)
                }
            }
        }

    }
}