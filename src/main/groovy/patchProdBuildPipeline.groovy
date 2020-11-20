#!groovy

node {
	def file_in_workspace = unstashFileParameter "patchFile.json"
	fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
	fileOperations([fileRenameOperation(source: "${file_in_workspace}",  destination: 'PatchFile.json')])
	sh "cat PatchFile.json"
	stash name: "PatchFile" , includes:  'PatchFile.json'

}

def patchConfig = commonPatchFunctions.readPatchJsonFileFromStash("PatchFile")

pipeline {
	options {
		preserveStashes(buildCount: 2)
		timestamps()
	}

	parameters {
		string(name: 'TARGET')
		string(name: 'STAGE')
		string(name: 'SUCCESS_NOTIFICATION')
	}

	agent any

	stages {
		stage("Build") {
			steps {
				parallel(
						"db-build": {
							script {
								patchfunctions.patchBuildDbZip(patchConfig, params.TARGET)
							}
						},
						"java-build": {
							script {
								patchfunctions.patchBuildsConcurrent(patchConfig, params.TARGET)
							}
						}
				)
			}
		}
		stage("Notify DB") {
			steps {
				script {
					commonPatchFunctions.notifyDb(patchConfig,params.STAGE,params.SUCCESS_NOTIFICATION)
				}
			}
		}
	}
}