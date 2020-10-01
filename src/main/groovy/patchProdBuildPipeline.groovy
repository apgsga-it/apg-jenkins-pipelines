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
		stage('Init') {
			steps {
				script {
					//TODO JHE (01.10.2020): Unstash file first
					def patchConfig = commonPatchFunctions.readPatchJsonFile(new File("${WORKSPACE}/PatchFile.json"))
					def stageMappings = patchConfig.stageMappings

					stageMappings.each { s ->
						stage(s) {
							println "This is the ${s} stage"
							commonPatchFunctions.printTestMessage("from ${s}")
						}
					}
				}
			}
		}
	}
}

