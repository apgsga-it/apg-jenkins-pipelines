#!groovy

node {
	def file_in_workspace = unstashFileParameter "patchFile.json"
	fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
	fileOperations([fileRenameOperation(source: "${file_in_workspace}",  destination: 'PatchFile.json')])
	sh "cat PatchFile.json"
	stash name: "PatchFile" , includes:  'PatchFile.json'

}

def informatiktestStage = "Informatiktest"
def anwendertestStage = "Anwendertest"
def produktionStage = "Produktion"
def patchConfig = commonPatchFunctions.readPatchJsonFileFromStash("PatchFile")
def stageMappings = patchConfig.stageMappings

pipeline {
	options {
		preserveStashes(buildCount: 2)
		timestamps()
	}

	agent any

	stages {
		stage("${informatiktestStage}") {
			steps {
				println "starting "
				println "Stage mapping are : ${stageMappings}"
				/*
				stage("Approve ${s} Build") {
					//TODO JHE (05.10.2020) : Here we need to call approveBuild function
					println "TODO : approveBuild"
				}

				stage("Build for ${s}") {
					patchfunctions.patchBuildsConcurrent(patchConfig)
				}

				 */
			}
		}
	}
}

