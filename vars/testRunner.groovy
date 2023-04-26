def call(args = [:]) {
   
    def defaultArgs = [
			userInputTimeout       : 60
    ]
    args = defaultArgs + args
    def defaultAemEnvs = [
            'dev1',
            'dev2',
            'dev3'
    ]
    def aemEnvs = defaultAemEnvs
    pipeline {
        agent any
        stages {
            stage('test') {
                options {
                    timeout(time: args.userInputTimeout, unit: 'MINUTES')
                }
                input {
                    message "Select a branch and AEM environment."
                    ok "Confirm"
                    parameters {
                      string(name: 'BRANCH_INPUT', defaultValue : 'develop', description: 'Which branch do you want to test?')
                      string(name: 'test3', defaultValue: '', description: 'Provide path .')
                      }
                }

                steps {
                    script {
                        echo "Selected branch: ${env.BRANCH_INPUT}"
                     
						}
					}
			}
		}
	}
}
