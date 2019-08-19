pipeline {
    agent any

    parameters {
        booleanParam(name: 'checkcode', defaultValue: false, description: '是否执行代码检查？')
    }

    environment {
        RD_ENV = 'dev'  // 标识开发测试环境，缺省为开发环境：dev
        GROUP_NAME = ''
        SERVICE_NAME = '-push'
        PVC_WORK = ''
        K8S_CLUSTER_NAME = 'kubernetes'
    }

    stages {
        stage('Clean') {
            steps {
                script {
                    GROUP_NAME = JOB_NAME.split("/")[0]
                    SERVICE_NAME = GROUP_NAME + SERVICE_NAME

                    // 如果组在Test视图下，则为测试环境：test
                    if (Jenkins.instance.getView('Test').contains(Jenkins.instance.getItem(GROUP_NAME))) {
                        RD_ENV = 'test'
                    }

                    sh "cp k8s/pvc.yml pvc.yml"
                    sh "sed -i s@__PROJECT__@${SERVICE_NAME}@g pvc.yml"

                    withKubeConfig(clusterName: "${K8S_CLUSTER_NAME}",
                            credentialsId: "k8s-${RD_ENV}",
                            serverUrl: "https://${KUBERNETES_SERVICE_HOST}:${KUBERNETES_SERVICE_PORT_HTTPS}") {
                        if (fileExists('k8s.yml')) {
                            sh "kubectl delete -f k8s.yml -n ${RD_ENV} --ignore-not-found"
                        }

                        try {
                            sh "kubectl create -f pvc.yml -n ${RD_ENV}"
                        } catch (ex) {
                        }
                    }
                }
            }
        }
        stage('Check Code') {
            when {
                environment name: 'checkcode', value: 'true'
                changeset "**/*"
            }
            steps {
                withMaven(jdk: 'oracle_jdk18', maven: 'maven', mavenSettingsConfig: 'e0af2237-7500-4e99-af21-60cc491267ec', options: [findbugsPublisher(disabled: true)]) {
                sh 'mvn clean compile checkstyle:checkstyle findbugs:findbugs pmd:pmd sonar:sonar'
                }
                recordIssues(tools: [checkStyle(), findBugs(useRankAsPriority: true), pmdParser()])
            }
        }
        stage('Package') {
            steps {
                withMaven(jdk: 'oracle_jdk18', maven: 'maven', mavenSettingsConfig: 'e0af2237-7500-4e99-af21-60cc491267ec', options: [findbugsPublisher(disabled: true)]) {
                    sh 'mvn clean package -DskipTests'
                }
                // stash includes: '**/target/*.jar', name: 'app'
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
        stage('Deploy') {
            when {
                expression {
                    currentBuild.result == null || currentBuild.result == 'SUCCESS'
                }
            }
            steps {
                sh "rm -rf tmp"
                sh "mkdir -p tmp/config"

                sh "cp target/*.jar tmp"

                sh "cp k8s/backend-k8s.yml k8s.yml"
                sh "cp k8s/backend-service.yml k8s-service.yml"
                sh "sed -i s@__PROJECT__@${SERVICE_NAME}@g k8s.yml"
                sh "sed -i s@__PROJECT__@${SERVICE_NAME}@g k8s-service.yml"
                sh "sed -i s@__ENV__@${RD_ENV}@g k8s.yml"
                sh "sed -i s@__GROUP_NAME__@${GROUP_NAME}@g k8s.yml"
                sh "sed -i s@__ARTIFACT_ID__@${readMavenPom().getArtifactId()}@g k8s.yml"

                script {
                    datas = readYaml file: 'src/main/resources/bootstrap.yml'
                    datas.eureka.client['service-url'].defaultZone = "http://wafer:wafer@${GROUP_NAME}-eureka:8080/eureka/"
                    datas.spring.cloud.config.uri = "http://wafer:wafer@${GROUP_NAME}-config:8080"
                    datas.server.port = 8080

                    writeYaml file: "tmp/config/bootstrap.yml", data: datas

                    withKubeConfig(clusterName: "${K8S_CLUSTER_NAME}",
                            credentialsId: "k8s-${RD_ENV}",
                            serverUrl: "https://${KUBERNETES_SERVICE_HOST}:${KUBERNETES_SERVICE_PORT_HTTPS}") {
                        RET = sh(
                                script: "kubectl get pvc ${SERVICE_NAME}-work --no-headers=true -o custom-columns=pv:.spec.volumeName -n ${RD_ENV}",
                                returnStdout: true
                        ).trim()
                        PVC_WORK = "${RD_ENV}-${SERVICE_NAME}-work-" + RET

                        ftpPublisher failOnError: true,
                                publishers: [
                                        [configName: 'ftp_ds1819_dev', transfers: [
                                                [cleanRemote    : true,
                                                 remoteDirectory: "${PVC_WORK}",
                                                 sourceFiles    : 'tmp/',
                                                 removePrefix   : 'tmp']
                                        ]]
                                ]

                        sh "kubectl apply -f k8s-service.yml -n ${RD_ENV}"
                        sh "kubectl apply -f k8s.yml -n ${RD_ENV}"
                    }
                }
            }
        }
    }
}
