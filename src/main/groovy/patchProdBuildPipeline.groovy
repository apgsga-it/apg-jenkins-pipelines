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

/*
			steps {
				script {
					patchfunctions.patchBuildDbZip(patchConfig,params.TARGET)
				}
			}

 */
		}


		/*
		stage("Build Java Artifact") {
			steps {
				script {
					//patchConfig.currentTarget = params.TARGET
					//commonPatchFunctions.savePatchConfigState(patchConfig)
					//println "patchConfig.currentTarget has been set with ${patchConfig.currentTarget}"
					patchfunctions.patchBuildsConcurrent(patchConfig,params.TARGET)
					//patchConfig.targetToState = commonPatchFunctions.getStatusCodeFor(patchConfig,params.STAGE,"BuildFor")
					//commonPatchFunctions.savePatchConfigState(patchConfig)
					//println "patchConfig.targetToState has been set with ${patchConfig.targetToState}"
				}
			}
		}

		 */

		stage("Notify DB") {
			steps {
				script {
					def targetToState = commonPatchFunctions.getStatusCodeFor(patchConfig,params.STAGE,"BuildFor")
					commonPatchFunctions.notifyDb(patchConfig,targetToState)
				}
			}
		}
	}
}