#!groovy

import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETER)

pipeline {
    agent any

    options {
        lock resource: "assembleDeployInstall_${paramsAsJson.target}"
    }

    parameters {
        string(name: 'PARAMETER', description: 'JSON String containing all required info')
    }


    stages {
        //JHE (14.12.2020): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
        stage("Starting logged") {
            steps {
                script {
                    commonPatchFunctions.log("Pipeline started with following parameter : ${paramsAsJson}")
                    assembleAndDeployPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Started", BUILD_URL)
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
                assembleAndDeployPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Done", "todo")
                commonPatchFunctions.notifyDb(paramsAsJson.patchNumbers.join(","),paramsAsJson.target,paramsAsJson.successNotification)
            }
        }
        unsuccessful {
            script {
                commonPatchFunctions.notifyDb(paramsAsJson.patchNumbers.join(","),paramsAsJson.target,paramsAsJson.errorNotification)
            }
        }
    }
}