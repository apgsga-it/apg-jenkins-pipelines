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

								println "DB-Build with following parameters : ${paramsAsJson}"

//								patchfunctions.patchBuildDbZip(patchConfig, params.TARGET)
							}
						},
						"java-build": {
							script {

								println "Java-Build with following parameters : ${paramsAsJson}"

//								patchfunctions.patchBuildsConcurrent(patchConfig, params.TARGET)
							}
						}
				)
			}
		}
	}
	post {
		success {
				script {
					commonPatchFunctions.notifyDb(patchConfig.patchNummer,params.STAGE,params.SUCCESS_NOTIFICATION,null)
				}
		}
		unsuccessful {
				script {
					commonPatchFunctions.notifyDb(patchConfig.patchNummer,params.STAGE,null,params.ERROR_NOTIFICATION)
				}
		}

	}
}