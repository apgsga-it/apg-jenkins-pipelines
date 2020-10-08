#!groovy

node {
	def file_in_workspace = unstashFileParameter "patchFile.json"
	fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
	fileOperations([fileRenameOperation(source: "${file_in_workspace}",  destination: 'PatchFile.json')])
	sh "cat PatchFile.json"
	stash name: "PatchFile" , includes:  'PatchFile.json'

}

// JHE (08.10.2020) : Stage names in declarativ pipeline .... https://issues.jenkins-ci.org/browse/JENKINS-43820
//                  : So important to stick with variableName=variableValue
def Informatiktest_Build = "Informatiktest"
def Approve_InformatikTest_Build = "InformatiktestApprove"
def Anwendertest = "Anwendertest"
def Produktion = "Produktion"
def patchConfig = commonPatchFunctions.readPatchJsonFileFromStash("PatchFile")
def stageMappings = patchConfig.stageMappings

pipeline {
	options {
		preserveStashes(buildCount: 2)
		timestamps()
	}

	agent any

	stages {

		stage(Approve_InformatikTest_Build) {
			steps {
				println "TODO : approve"
			}
		}

		stage(Informatiktest_Build) {
			steps {
				println "starting ${Informatiktest} stage"
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

