#!groovy
import groovy.json.JsonSlurperClassic

//node {
//	def file_in_workspace = unstashFileParameter "patchFile.json"
//	fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
//	fileOperations([fileRenameOperation(source: "${file_in_workspace}",  destination: 'PatchFile.json')])
//	sh "cat PatchFile.json"
//	stash name: "PatchFile" , includes:  'PatchFile.json'
//
//}

//def patchConfig = commonPatchFunctions.readPatchJsonFileFromStash("PatchFile")

def paramsAsJson = new JsonSlurperClassic().parseText(params.PARAMETERS)

pipeline {
//	options {
//		preserveStashes(buildCount: 2)
//		timestamps()
//	}

	parameters {
//		string(name: 'TARGET')
//		string(name: 'STAGE')
//		string(name: 'SUCCESS_NOTIFICATION')
//		string(name: 'ERROR_NOTIFICATION')
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