#!/bin/bash

if [ "$DEBUG" == "true" ] ; then
    set -x
fi

# Set up locale (C is the default locale, compare with Locale.ROOT in Java)
export LANG=C.UTF-8

# Setup the UID the container runs as to be named 'jenkins' (purely for niceness)
if ! whoami &> /dev/null; then
  if [ -w /etc/passwd ]; then
    echo "jenkins:x:$(id -u):0:jenkins user:/opt:/sbin/nologin" >> /etc/passwd
  fi
fi
echo "Current user '$(whoami)' belongs to group(s): $(groups)"

reportFile="report-findbugs.xml"

antCommonParameters="-q -Dappserver.home=/tmp -Dappserver.type=jboss -Dappserver.subtype=jbosseap6 -Dejbca.productionmode=false"
antCommonParameters="$antCommonParameters"

echo "
### Building EJBCA components to scan ###
"
cd ejbca/
ant $antCommonParameters clean build clientToolBox

echo "
### Running findbugs ###
"
time java ${JAVA_OPTS} -jar /usr/share/java/findbugs.jar -textui -high -longBugCodes -maxRank 9 -xml:withMessages -output "${reportFile}" \
    -onlyAnalyze org.cesecore.-,org.ejbca.- \
    dist/ejbca.ear dist/clientToolBox/ dist/ejbca-ejb-cli/ ./

echo "
### Clean up EJBCA ###
"
ant $antCommonParameters clean
cd ../

echo "
### Done! ###
"
reportSize="$(du -h ejbca/${reportFile} | sed 's/\t.*//')"
echo "Report is available in $(realpath ejbca/${reportFile}) [${reportSize}]"
