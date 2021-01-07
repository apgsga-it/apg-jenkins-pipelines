#!groovy

import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETER)

pipeline {
    agent any

    parameters {
        string(name: 'PARAMETER', description: 'JSON String containing all required info')
    }

    stages {
        // TODO JHE (14.12.2020): Depending on how IT-36715 will be implemented, we might remove this complete stage.
        stage("Copy Revision file") {
            steps {
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

        //JHE (14.12.2020): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
        stage("Starting logged") {
            steps {
                script {
                    commonPatchFunctions.log("Pipeline started with following parameter : ${paramsAsJson}")
                    assembleAndDeployPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Started")
                }
            }
        }

        // JHE (09.12.2020): Seems not easily possible to use variable in stage name for declarative pipeline : https://issues.jenkins.io/browse/JENKINS-43820
        stage("Assemble and Deploy") {
            steps {
                parallel(
                        "db-assemble": {
                            script {
                                assembleAndDeployPatchFunctions.assembleAndDeployDb(paramsAsJson)
                            }
                        },
                        "java-assemble": {
                            script {
                                assembleAndDeployPatchFunctions.assembleAndDeployJavaService(paramsAsJson)
                            }
                        }
                )
            }
        }
    }
    post {
        success {
            script {
                assembleAndDeployPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Done")
                paramsAsJson.patchNumbers.each{patchNumber ->
                    commonPatchFunctions.notifyDb(patchNumber,"assembleAndDeploy",paramsAsJson.successNotification,null)
                }
            }
        }
        unsuccessful {
            script {
                paramsAsJson.patchNumbers.each{patchNumber ->
                    commonPatchFunctions.notifyDb(patchNumber,"assembleAndDeploy",null,paramsAsJson.errorNotification)
                }
            }
        }

    }
}