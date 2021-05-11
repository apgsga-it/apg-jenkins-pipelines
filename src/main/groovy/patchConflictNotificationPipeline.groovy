#!groovy
import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)

pipeline {
    agent any

    stages {
        stage('SendNotification') {
            steps {
                script {
                    paramsAsJson.each{conflict ->
                        def p1Number = conflict.patchConflict.p1.patchNumber
                        def p2Number = conflict.patchConflict.p2.patchNumber
                        def dockerServices = conflict.patchConflict.dockerServices
                        def dbObjects = conflict.patchConflict.dbObjects
                        def javaArtifacts = conflict.patchConflict.serviceWithMavenArtifacts
                        def body = "Conflict(s) between Patch ${p1Number} and ${p2Number}. Following objects are present in both patches:"
                        body += System.getProperty('line.separator')
                        body += System.getProperty('line.separator')
                        if(!dockerServices.isEmpty()) {
                            body += "Docker Service(s)"
                            body += System.getProperty('line.separator')
                            body += "================="
                            dockerServices.each{docker ->
                                body += System.getProperty('line.separator')
                                body += " - ${docker}"
                            }
                        }
                        if(!dbObjects.isEmpty()) {
                            body += System.getProperty('line.separator')
                            body += System.getProperty('line.separator')
                            body += "DB Object(s)"
                            body += System.getProperty('line.separator')
                            body += "============"
                            for(def dboModuleName : dbObjects.keySet()) {
                                body += System.getProperty('line.separator')
                                body += "DB Object(s) for Module '${dboModuleName}'"
                                dbObjects.get(dboModuleName).each { module ->
                                    body += System.getProperty('line.separator')
                                    body += " - Path: ${module.filePath} ; Object name: ${module.fileName}"
                                }
                            }
                        }
                        if(!javaArtifacts.isEmpty()) {
                            body += System.getProperty('line.separator')
                            body += System.getProperty('line.separator')
                            body += "Java Artifact(s)"
                            body += System.getProperty('line.separator')
                            body += "================"
                            for(def service : javaArtifacts.keySet()) {
                                body += System.getProperty('line.separator')
                                body += "Artifact(s) for service '${service}' :"
                                javaArtifacts.get(service).each{art ->
                                    body += System.getProperty('line.separator')
                                    body += " - Artifact Id: ${art.artifactId} ; Group Id: ${art.groupId} ; Version: ${art.version}"
                                }
                            }
                        }

                        body += System.getProperty('line.separator')
                        body += System.getProperty('line.separator')
                        body += "For any question, please contact UGE, JHE or STB."

                        emailext to: conflict.emailAdress.join(";"), from: "it.architektur@apgsga.ch" , subject: "${env.PIPELINE_MAIL_ENV} - WARNING / Patch Conflict(s)", body: "${body}"
                    }
                }
            }
        }
    }
}
