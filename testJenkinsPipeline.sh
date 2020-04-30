#!/bin/bash
# Command line parsing
# Usage info
show_help() {
  cat <<EOF
Usage: ${0##*/} [-i INSTALLDIR] [ -x ]

Tests the Jenkinsfilerunner and apscli

    -h          display this help and exit
    -i=INSTALLDIR Installation Dir for the Jenkinstests, defaults to /opt/jenkinstests
    -t=GRADLE_TASK Gradle Task to execute , defaults to runTestLibHelloWorld
    -a          Test apscli

EOF
}
preconditions() {
  # Preconditions
  if [ ! -d "$RUNNER_DIR" ]; then
    echo >&2 "Installation directtory $RUNNER_DIR for jenkinsfile-runner is missing.  Aborting."
    exit 1
  fi
  if [ ! -z "$MAVENBASEDIR" ]; then
    if [ ! -d "$MAVENBASEDIR" ]; then
      echo >&2 "Maven base  directory is missing for  $MAVENBASEDIR.  Aborting."
      exit 1
    fi
    if [ ! -d "$MAVENBASEDIR/repo" ]; then
      echo >&2 "Maven repo  directory is missing for  $MAVENBASEDIR.  Aborting."
      exit 1
    fi
    if [ ! -f "$MAVENBASEDIR/settings.xml" ]; then
      echo >&2 "Maven settings.xml file is missing for  $MAVENBASEDIR.  Aborting."
      exit 1
    fi
  fi
}

testTestRunnerInstallation() {
  ./gradlew tasks --group="Apg Gradle Jenkinsrunner"
  ./gradlew "$GRADLE_TASK" -PinstallDir="$RUNNER_DIR" -PmavenSettings="$MAVENBASEDIR/settings.xml" -Dgradle.user.home="$GRADLE_HOME" --info --stacktrace

}
testApsCli() {
  SAVEDWD=$(pwd)
  cd "$APSCLI_DIR"
  chmod u+x apscli.sh
  ./apscli.sh -h
  ./apscli.sh -d "/tmp"
}

# saner programming env: these switches turn some bugs into errors
set -o errexit -o pipefail -o noclobber -o nounset
# -allow a command to fail with !’s side effect on errexit
# -use return value from ${PIPESTATUS[0]}, because ! hosed $?
! getopt --test >/dev/null
if [[ ${PIPESTATUS[0]} -ne 4 ]]; then
  echo "I’m sorry, $(getopt --test) failed in this environment, see if you install gnu-getopt "
  exit 1
fi

#Defaults
INSTALL_DIR="/opt/jenkinstests"
APSCLI_DIR="$INSTALL_DIR/apscli"
RUNNER_DIR="$INSTALL_DIR/runner"
GRADLE_HOME="$INSTALL_DIR/gradle/home"
MAVENBASEDIR="/opt/jenkinstests/maven"$INSTALL_DIR''
GRADLE_TASK=runTestLibHelloWorld
TEST_APSCLI=n

#Command line Options
OPTIONS=i:t:a
LONGOPTS=help,installdir:,gradleTask:,testApsCli

# -regarding ! and PIPESTATUS see above
# -temporarily store output to be able to check for errors
# -activate quoting/enhanced mode (e.g. by writing out “--options”)
# -pass arguments only via   -- "$@"   to separate them correctly
! PARSED=$(getopt --options=$OPTIONS --longoptions=$LONGOPTS --name "$0" -- "$@")
if [[ ${PIPESTATUS[0]} -ne 0 ]]; then
  show_help
  exit 0
fi
# read getopt’s output this way to handle the quoting right:
eval set -- "$PARSED"
while true; do
  case "$1" in
  -h | --help)
    show_help
    exit 0
    ;;
  -i | --installdir)
    INSTALL_DIR=$2
    INSTALL_DIR="${INSTALL_DIR/#\~/$HOME}"
    shift 2
    ;;
   -t | --gradleTask)
    GRADLE_TASK=$2
    shift 2
    ;;
   -a | --testApsCli)
    TEST_APSCLI=y
    shift
    ;;
  --)
    shift
    break
    ;;
  *)
    show_help >&2
    exit 1
    ;;
  esac
done
echo "Running $0"
echo "Running with Installation Dir=$INSTALL_DIR, maven=$MAVENBASEDIR"
testTestRunnerInstallation
if [ "$TEST_APSCLI" == "y" ]; then
  testApsCli
fi
