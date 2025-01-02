#!/bin/bash
# Check on couchdb
USR=admin
PWD=password
USER=$USR:$PWD
PORT=5984
SRV=http://couchdb:$PORT/

sleep 5

#SYSTEM databases

curl -u ${USER} -X PUT ${SRV}/_users
curl -u ${USER} -X PUT ${SRV}/_replicator
curl -u ${USER} -X PUT ${SRV}/_global_changes

# for configuring quickjs
curl -u ${USER} -X PUT ${SRV}/_node/_local/_config/couchdb/js_engine -d '"quickjs"'

#DEMO database
curl -u ${USER} -X PUT ${SRV}/demo
curl -u ${USER} -X PUT ${SRV}/demo/firstdoc -d '{"name" : "Peter Pan", "location" : "Neverland"}' | jq

# Status
curl -u ${USER} ${SRV} | jq

# Session
curl -u ${USER} ${SRV}/_session | jq

#DEMO back
curl -u ${USER} ${SRV}/demo/firstdoc | jq

# Development mode
export DEVELOPMENT=true
#Maven
mvn clean
