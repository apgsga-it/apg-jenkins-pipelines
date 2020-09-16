import hudson.model.*
def patchName = "Patch${patchnumber}"
def jobName = patchName
def downLoadJobName = jobName + "OnDemand"

pipelineJob (jobName) {
	authenticationToken(patchName)
	concurrentBuild(false)
	definition {
		cps {
			script(readFileFromWorkspace('src/main/groovy/patchProdPipeline.groovy'))
			sandbox(false)
		}
	}
	logRotator(3653,10,3653,-1) // ten years legal retention period
	description("Patch Pipeline for : ${patchName}")
	parameters {
		stringParam('PARAMETER', "", "Pfad zum Patch*.json File")
		stringParam('RESTART', "FALSE", "Indikator ob die Pipeline gerestart werden soll, bis zum letzten erfolgreichen Ststus default FALSE")
	}
	properties {
	}
}
pipelineJob (downLoadJobName) {
	authenticationToken(downLoadJobName)
	concurrentBuild(false)
	definition {
		cps {
			script(readFileFromWorkspace('src/main/groovy/patchOnDemandPipeline.groovy'))
			sandbox(false)
		}
	}
	logRotator(3653,10,3653,-1) // ten years legal retention period
	description("OnDemand Patch Pipeline for : ${patchName}")
	parameters {
		stringParam('PARAMETER', "", "String mit dem die PatchConfig Parameter als JSON transportiert werden")
	}
	properties {
	}
}
