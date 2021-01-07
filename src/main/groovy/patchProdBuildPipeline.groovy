#!groovy
import groovy.json.JsonSlurperClassic

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)

pipeline {
	parameters {
		string(name: 'PARAMETERS', description: "JSON parameters")
	}

	agent any

	stages {
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

	}
}