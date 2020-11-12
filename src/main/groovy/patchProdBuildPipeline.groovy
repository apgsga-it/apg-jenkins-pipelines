#!groovy

node {
	def file_in_workspace = unstashFileParameter "patchFile.json"
	fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
	fileOperations([fileRenameOperation(source: "${file_in_workspace}",  destination: 'PatchFile.json')])
	sh "cat PatchFile.json"
	stash name: "PatchFile" , includes:  'PatchFile.json'

}

def InformatiktestBuild = "Informatiktest"
def patchConfig = commonPatchFunctions.readPatchJsonFileFromStash("PatchFile")

pipeline {
	options {
		preserveStashes(buildCount: 2)
		timestamps()
	}

	parameters {
		string(name: 'TARGET')
	}

	agent any

	stages {

		stage("BuildDbZip") {
			steps {
				script {
					patchfunctions.patchBuildDbZip(patchConfig)
				}
			}
		}

		stage("Build Java Artifact") {
			steps {
				script {
					patchConfig.currentTarget = params.TARGET
					//commonPatchFunctions.savePatchConfigState(patchConfig)
					println "patchConfig.currentTarget has been set with ${patchConfig.currentTarget}"
					patchfunctions.patchBuildsConcurrent(patchConfig)
					patchConfig.targetToState = commonPatchFunctions.getStatusCodeFor(patchConfig,InformatiktestBuild,"BuildFor")
					//commonPatchFunctions.savePatchConfigState(patchConfig)
					println "patchConfig.targetToState has been set with ${patchConfig.targetToState}"
				}
			}
		}

		stage("Notify DB Build is done") {
			steps {
				script {
					commonPatchFunctions.notifyDb(patchConfig)
				}
			}
		}
	}
}