def call(args = [:]) {
   
    def defaultArgs = [
			userInputTimeout       : 60
    ]
    args = defaultArgs + args
    def defaultAemEnvs = [
            AemEnv.DEV1,
            AemEnv.DEV2,
            AemEnv.DEV3
    ]
    def aemEnvs = defaultAemEnvs
    pipeline {
        agent  master
        stages {
            stage('test') {
                options {
                    timeout(time: args.userInputTimeout, unit: 'MINUTES')
                }
                input {
                    message "Select a branch and AEM environment."
                    ok "Confirm"
                    parameters {
                      string(name: 'TEST_BRANCH_INPUT', defaultValue : 'develop', description: 'Which branch do you want to test?')
                      choice(name: 'AEM_SERVER_INPUT', choices: aemEnvs*.resourceName, description: 'Which server to test against?')
                      string(name: 'CYPRESS_SPEC_NAME_INPUT', defaultValue: '', description: 'Provide path after integration folder in Cypress Directory.')
                      }
                }

                steps {
                    script {
                        echo "Selected branch: ${env.TEST_BRANCH_INPUT}"
                        echo "Selected server: ${env.AEM_SERVER_INPUT}"
						}
					}
			}
		}
	}
}
