def imageRepo = 'manjushamenon/ui'
def servicePath = 'services/ui/angular'
node {
        stage('cleanup'){
            cleanWs()
        }
        checkout scm
        dir({servicePath})
        {
            stage('Dependencies'){
                docker.image('node:14.16').inside { //creates node in docker then go inside
                    //run these
                    sh 'npm ci --quiet --cache="./npm"'  //copies the user, mounts the directories, maps it to the exact same location as node agent, gives permissions\
                    //define location of cache files to be in the workspace.
                }
            }
            stage('Build') {
                docker.image('node:14.16').inside { //creates node in docker then go inside
                        //run these
                        sh 'npm run build.production --cache="./npm"'  //copies the user, mounts the directories, maps it to the exact same location as node agent, gives permissions\
                        //define location of cache files to be in the workspace.
                    }
            }
            /*
            stage('lint') {
                try{
                    echo 'Linting'
                }catch(Exception e) {
                    echo 'Failed linting ' + e.toString()
                }
            }
            stage('test') {
                    docker.image('buildkite/puppeteer:8.0.0').inside { //Configure the test stage to use image
                        //run these
                        sh 'npm run test --cache="./npm"'  //copies the user, mounts the directories, maps it to the exact same location as node agent, gives permissions\
                        //define location of cache files to be in the workspace.
                }
            }
            */
            stage('deliver') {
                if(env.BRANCH_NAME == 'Developer'){
                    docker.withRegistry('', 'docker1') {
                        def myImage=docker.build("$imageRepo}:${env.BUILD_ID}")
                        myImage.push()
                        myImage.push('dev')
                    }
                    build job: 'deploy', parameters: [string(name: 'env', value: 'dev'), string(name: 'tag', value: 'dev')]
                }
            }
            stage('deliver') {
                if(env.BRANCH_NAME == 'master'){
                    docker.withRegistry('', 'docker1') {
                        def myImage=docker.build("manjushamenon/ui:${env.BUILD_ID}")
                        myImage.push()
                        myImage.push('latest')
                    }
                    build job: 'deploy', parameters: [string(name: 'env', value: 'prod'), string(name: 'tag', value: 'latest')]
                }
            }
        }
    }
}