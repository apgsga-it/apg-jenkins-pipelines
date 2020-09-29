#!groovy


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
			}
		}
		stage("Informatiktest") {
			steps {
				println "This is the entwicklung stage"
			}
		}
	}
}

