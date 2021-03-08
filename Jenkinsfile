pipeline {
    agent any

    parameters {
        choice(name: 'replicasNum', choices: ['1', '2', '3', '4', '5'], description: '集群数量')
    }

    environment {
        RD_ENV = 'dev'  // 标识开发测试环境，缺省为开发环境：dev
        GROUP_NAME = ''
        SERVICE_NAME = '-push'
        PVC_WORK = ''
        K8S_CLUSTER_NAME = 'kubernetes'
        REPLICAS_NUM = "${params.replicasNum}"
    }

    stages {
        stage('Clean') {
            steps {
            echo "集群数量为 : ${REPLICAS_NUM}"
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

                        sh "kubectl apply -f pvc.yml -n ${RD_ENV}"
                    }
                }
            }
        }
        stage('Package') {
            steps {
                withMaven(jdk: 'oracle_jdk18', maven: 'maven', mavenSettingsConfig: 'e0af2237-7500-4e99-af21-60cc491267ec') {
                    sh 'mvn clean package -DskipTests'
                }
             }
        }
        stage('Deploy') {
            when {
                expression {
                    currentBuild.result == null || currentBuild.result == 'SUCCESS'
                }
            }
            steps {
                sh "rm -rf work"
                sh "mkdir -p work/config"

                sh "cp target/*.jar work"

                sh "cp k8s/backend-k8s.yml k8s.yml"
                sh "cat k8s/backend-service.yml k8s/ingress.yml > k8s-service.yml"
                sh "sed -i s@__PROJECT__@${SERVICE_NAME}@g k8s.yml"
                sh "sed -i s@__PROJECT__@${SERVICE_NAME}@g k8s-service.yml"
                sh "sed -i s@__ENV__@${RD_ENV}@g k8s.yml"
                sh "sed -i s@__GROUP_NAME__@${GROUP_NAME}@g k8s.yml"
                sh "sed -i s@__ARTIFACT_ID__@${readMavenPom().getArtifactId()}@g k8s.yml"
                sh "sed -i s@__REPLICAS_NUM__@${REPLICAS_NUM}@g k8s.yml"

                script {
                    if (SERVICE_NAME.length() > 24) {
                        APP_NAME = SERVICE_NAME.substring(0, 24)
                    } else {
                        APP_NAME = SERVICE_NAME
                    }

                    sh "sed -i s@__PINPOINT_APPNAME__@${APP_NAME}@g k8s.yml"

                    datas = readYaml file: 'src/main/resources/bootstrap.yml'
                    datas.eureka.client['service-url'].defaultZone = "http://wafer:wafer@${GROUP_NAME}-eureka:8080/eureka/"
                    datas.spring.cloud.config.uri = "http://wafer:wafer@${GROUP_NAME}-config:8080"
                    datas.server.port = 8080

                    writeYaml file: "work/config/bootstrap.yml", data: datas

                    withKubeConfig(clusterName: "${K8S_CLUSTER_NAME}",
                            credentialsId: "k8s-${RD_ENV}",
                            serverUrl: "https://${KUBERNETES_SERVICE_HOST}:${KUBERNETES_SERVICE_PORT_HTTPS}") {
                        // 创建一个初始化Pod，临时用户数据复制
                        INIT_POD_NAME = "${SERVICE_NAME}-init-${currentBuild.startTimeInMillis}"
                        sh "cp k8s/initpod.yml initpod.yml"
                        sh "sed -i s@__PROJECT__@${SERVICE_NAME}@g initpod.yml"
                        sh "sed -i s@__GROUP_NAME__@${GROUP_NAME}@g initpod.yml"
                        sh "sed -i s@__INIT_POD_NAME__@${INIT_POD_NAME}@g initpod.yml"
                        sh "kubectl apply -f initpod.yml -n ${RD_ENV}"

                        INIT_POD_STATUS = ''

                        while (INIT_POD_STATUS != 'Running') {
                            INIT_POD_STATUS = sh(
                                    script: "kubectl get pod ${INIT_POD_NAME} --no-headers=true -o custom-columns=status:.status.phase -n ${RD_ENV}",
                                    returnStdout: true
                            ).trim()
                            
                            echo 'Waiting for init...'
                            sleep 2
                        }

                        // 使用初始化Pod进行数据复制
                        sh "kubectl cp ./work ${INIT_POD_NAME}:/ -n ${RD_ENV}"
                        // 删除初始化Pod
                        sh "kubectl delete pod ${INIT_POD_NAME} --force -n ${RD_ENV}"

                        sh "kubectl apply -f k8s-service.yml -n ${RD_ENV}"
                        sh "kubectl apply -f k8s.yml -n ${RD_ENV}"
                    }
                }
            }
        }
    }
}
