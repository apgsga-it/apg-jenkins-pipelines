#!groovy
import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)
def revisionClonedPath = "/var/jenkins/gradle/home/patch${paramsAsJson.patchNumber}_${paramsAsJson.target}"

pipeline {
	parameters {
		string(name: 'PARAMETERS', description: "JSON parameters")
	}

	agent any

	stages {
		stage("Create local Revision folder") {
			steps {
				script {
					commonPatchFunctions.createFolder(revisionClonedPath)
				}
			}
		}
		stage("Build") {
			steps {
				parallel(
						"db-build": {
							script {
								patchfunctions.patchBuildDbZip(paramsAsJson)
							}
						},
						"java-build": {
							script {
								patchfunctions.patchBuildsConcurrent(paramsAsJson)
							}
						}
				)
			}
		}
	}
	post {
		success {
				script {
					commonPatchFunctions.notifyDb(paramsAsJson.patchNumber,paramsAsJson.stageName,paramsAsJson.successNotification,null)
				}
		}
		unsuccessful {
				script {
					commonPatchFunctions.notifyDb(paramsAsJson.patchNumber,paramsAsJson.stageName,null,paramsAsJson.errorNotification)
				}
		}
		always {
			script {
				commonPatchFunctions.deleteFolder(revisionClonedPath)
			}
		}

	}
}