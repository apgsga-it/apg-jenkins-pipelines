#!groovy

library 'common-patch-functions'

node {
	def file_in_workspace = unstashFileParameter "patchFile.json"
	fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
	fileOperations([fileRenameOperation(source: "${file_in_workspace}",  destination: 'PatchFile.json')])
	sh "cat PatchFile.json"
	stash name: "PatchFile" , includes:  'PatchFile.json'
}



pipeline {
	options {
		preserveStashes(buildCount: 2)
		timestamps()
	}

	agent any

	stages {
		// JHE (05.10.2020): We need a kind of wrapper stage in order to dynamically look within stageMappings
		stage('Starting') {
			steps {
				script {
					//TODO JHE (01.10.2020): Unstash file first
					def patchConfig = commonPatchFunctions.readPatchJsonFile(new File("${WORKSPACE}/PatchFile.json"))
					def stageMappings = patchConfig.stageMappings

					stage('CO from SVC') {
						patchfunctions.coFromBranchCvs(patchConfig)
					}


					stageMappings.each { s ->
						stage(s) {
							println "TODO JHE"
							println "This is the ${s} stage"
							commonPatchFunctions.printTestMessage("from ${s}")
						}
					}
				}
			}
		}
	}
}

