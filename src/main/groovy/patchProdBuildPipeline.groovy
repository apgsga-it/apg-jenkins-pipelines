#!groovy

library 'common-patch-functions'

node {
	def file_in_workspace = unstashFileParameter "patchFile.json"
	fileOperations([fileDeleteOperation(includes: 'PatchFile.json')])
	fileOperations([fileRenameOperation(source: "${file_in_workspace}",  destination: 'PatchFile.json')])
	sh "cat PatchFile.json"


	def patchConfig = commonPatchFunctions.readPatchJsonFile(new File("./PatchFile.json"))
	def stageMappings = patchConfig.stageMappings

	println "stageMappings = ${stageMappings}"

	stash name: "PatchFile" , includes:  'PatchFile.json'
}



pipeline {
	options {
		preserveStashes(buildCount: 2)
		timestamps()
	}

	agent any

	stages {
		stage("Entwicklung") {
			steps {
				println "This is the entwicklung stage"
				script {
					commonPatchFunctions.printTestMessage("from Entwicklung")
				}
			}
		}
		stage("Informatiktest") {
			steps {
				println "This is the Informatiktest stage"
				script {
					commonPatchFunctions.printTestMessage("from Informatiktest")
				}
			}
		}
	}
}

