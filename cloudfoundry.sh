#!/bin/bash
set -e

#
# the big CloudFoundry installer
#

CLOUD_DOMAIN=${DOMAIN:-run.pivotal.io}
CLOUD_TARGET=api.${DOMAIN}

function login(){
    cf api | grep ${CLOUD_TARGET} || cf api ${CLOUD_TARGET} --skip-ssl-validation
    cf a | grep OK || cf login
}

function app_domain(){
    D=`cf apps | grep $1 | tr -s ' ' | cut -d' ' -f 6 | cut -d, -f1`
    echo $D
}

### common
function deploy_cli_app(){

    APP_NAME=$1
    cd $APP_NAME
    mkdir -p target
    spring jar target/$APP_NAME.jar $APP_NAME.groovy
    cd target
    cd ..

    cf push $APP_NAME --no-start
    APPLICATION_DOMAIN=`app_domain $APP_NAME`
    echo determined that application_domain for $APP_NAME is $APPLICATION_DOMAIN.
    cf env $APP_NAME | grep APPLICATION_DOMAIN || cf set-env $APP_NAME APPLICATION_DOMAIN $APPLICATION_DOMAIN
    cf restart $APP_NAME
    cd ..
}

function deploy_app(){

    APP_NAME=$1
    cd $APP_NAME
    mvn clean install
    cf push $APP_NAME  --no-start
    APPLICATION_DOMAIN=`app_domain $APP_NAME`
    echo determined that application_domain for $APP_NAME is $APPLICATION_DOMAIN.
    cf env $APP_NAME | grep APPLICATION_DOMAIN || cf set-env $APP_NAME APPLICATION_DOMAIN $APPLICATION_DOMAIN
    cf restart $APP_NAME
    cd ..
}

function deploy_service(){
    N=$1
    D=`app_domain $N`
    JSON='{"uri":"http://'$D'"}'
    echo cf cups $N  -p $JSON
    cf cups $N -p $JSON
}

function deploy_config_service(){
    NAME=config-service
    deploy_cli_app $NAME
    deploy_service $NAME
}

function deploy_eureka_service(){
    NAME=eureka-service
    deploy_cli_app $NAME
    deploy_service $NAME
}

function deploy_hystrix_dashboard_service(){
    deploy_cli_app hystrix-dashboard-service
}

function deploy_reservation_service(){
    cf cs elephantsql turtle reservations-postgresql
    deploy_app reservation-service
}
function deploy_reservation_client(){
    deploy_app reservation-client
}

function reset(){


    apps="hystrix-dashboard-service reservation-service reservation-client eureka-service config-service"
    apps_arr=( $apps )
    for a in "${apps_arr[@]}";
    do
         echo $a
         cf d -f $a
    done

    services="reservations-postgresql eureka-service config-service "
    services_arr=( $services )
    for s in "${services_arr[@]}";
    do
        echo $s
        cf ds -f $s
    done
}

###
### INSTALLATION STEPS
###

#mvn -DskipTests=true clean install

login
reset
deploy_config_service
deploy_eureka_service
deploy_hystrix_dashboard_service
deploy_reservation_service
deploy_reservation_client
