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
                        installPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Started")
                    }
                }
            }

            // JHE (09.12.2020): Seems not easily possible to use variable in stage name for declarative pipeline : https://issues.jenkins.io/browse/JENKINS-43820
            stage("Install") {
                steps {
                    parallel(
                            "db-install": {
                                script {
                                    installPatchFunctions.installDb(paramsAsJson)
                                }
                            },
                            "java-install": {
                                script {
                                    installPatchFunctions.installJavaServices(paramsAsJson)
                                }
                            }
                    )
                }
            }
        }
        post {
            success {
                script {
                    installPatchFunctions.logPatchActivity(paramsAsJson.patchNumbers, paramsAsJson.target, "Done")
                    paramsAsJson.patchNumbers.each { patchNumber ->
                        commonPatchFunctions.notifyDb(patchNumber, "install", paramsAsJson.successNotification, null)
                    }
                }
            }
            unsuccessful {
                script {
                    paramsAsJson.patchNumbers.each { patchNumber ->
                        commonPatchFunctions.notifyDb(patchNumber, "install", null, paramsAsJson.errorNotification)
                    }
                }
            }

        }
}