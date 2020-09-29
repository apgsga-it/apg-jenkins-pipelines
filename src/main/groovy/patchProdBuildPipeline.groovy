#!groovy

library 'common-patch-functions'

node {

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
				commonPatchFunctions.printTestMessage("from Entwicklung")
			}
		}
		stage("Informatiktest") {
			steps {
				println "This is the Informatiktest stage"
				commonPatchFunctions.printTestMessage("from Informatiktest")
			}
		}
	}
}

