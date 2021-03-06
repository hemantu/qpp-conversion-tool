#!/usr/bin/env bash

DIR=$(dirname "$0")
SONAR_HOST=https://sonarcloud.io
ORG_KEY=cmsgov

if [[ "$CIRCLE_BRANCH" == "master" || ( ! -z $SONAR_OTHER_BRANCH && "$CIRCLE_BRANCH" == "$SONAR_OTHER_BRANCH" ) ]]; then
	#Do a full SonarQube run
	echo "Doing full SonarQube run"
	./sonar-scanner-3.0.1.733/bin/sonar-scanner -Dsonar.host.url=${SONAR_HOST} \
    																					-Dsonar.organization=${ORG_KEY} \
    																					-Dsonar.login=${SONAR_KEY_NEW}
	sleep 3
	project_status=$(curl -sS ${SONAR_HOST}/api/qualitygates/project_status?projectKey=gov.cms.qpp.conversion:qpp-conversion)
	status=$(echo ${project_status} | jq '.projectStatus.status')
	if [[ ${status} != '"OK"' ]]; then
		echo "Sonar failed: " ${status}
		echo "Entire project status:"
		echo ${project_status} | jq .
		exit 1
	fi
elif [[ ! -z $CI_PULL_REQUESTS ]]; then
	#Do a PR preview SonarCube run
	#This build could be on multiple PRs
	IFS=","
	for PULL_REQUEST_URL in $CI_PULL_REQUESTS
	do
		PR_NUMBER=${PULL_REQUEST_URL##*/}
		echo "Doing preview SonarQube run on PR $PR_NUMBER"
		./sonar-scanner-3.0.1.733/bin/sonar-scanner -Dsonar.analysis.mode=preview \
		                                            -Dsonar.github.pullRequest=${PR_NUMBER} \
		                                            -Dsonar.github.repository=CMSgov/qpp-conversion-tool \
		                                            -Dsonar.github.oauth=${SONAR_PR_KEY} \
																								-Dsonar.organization=${ORG_KEY} \
		                                            -Dsonar.login=${SONAR_KEY_NEW}
	done
else
	echo "Not on master nor in a pull request."
fi
