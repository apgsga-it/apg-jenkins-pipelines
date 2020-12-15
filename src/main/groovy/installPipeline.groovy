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
        //JHE (14.12.2020): This is not really a stage ... but Jenkins won't accept to have step done before parallel tasks (below)
        stage("Starting logged") {
            steps {
                script {
                    assembleAndDeployPatchFunctions.logPatchActivity(paramsAsJson.patches, params.TARGET, "Installation started")
                }
            }
        }

        // JHE (09.12.2020): Seems not easily possible to use variable in stage name for declarative pipeline : https://issues.jenkins.io/browse/JENKINS-43820
        stage("Install") {
            steps {
                parallel(
                        "db-install": {
                            script {
                                installPatchFunctions.installDb(params.TARGET,paramsAsJson)
                            }
                        },
                        "java-install": {
                            script {
                                installPatchFunctions.installJavaServices(params.TARGET,paramsAsJson)
                            }
                        }
                )
            }
        }
    }
    post {
        success {
            script {
                assembleAndDeployPatchFunctions.logPatchActivity(paramsAsJson.patches, params.TARGET, "Installation done")
                paramsAsJson.patches.each{patchNumber ->
                    commonPatchFunctions.notifyDb(patchNumber,"install",paramsAsJson.successNotification,null)
                }
            }
        }
        unsuccessful {
            script {
                paramsAsJson.patches.each{patchNumber ->
                    commonPatchFunctions.notifyDb(patchNumber,"install",null,paramsAsJson.errorNotification)
                }
            }
        }

    }
}