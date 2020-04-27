#!/bin/bash
# Command line parsing
# Usage info
show_help() {
  cat <<EOF
Usage: ${0##*/} [-i INSTALLDIR] [ -x ]

Tests the Jenkinsfilerunner and apscli

    -h          display this help and exit
    -i=INSTALLDIR Installation Dir for the Jenkinstests, defaults to ~/jenkinstests
    -x          no MAVENBASEDIR

EOF
}
preconditions() {
  # Preconditions
  if [ ! -d $RUNNER_DIR ]; then
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
  if [[ -z "$MAVENBASEDIR" ]]; then
    ./gradlew runTestLibHelloWorld -PinstallDir="$RUNNER_DIR" -PmavenSettings="$MAVENBASEDIR/settings.xml" --info --stacktrace
  else
    ./gradlew runTestLibHelloWorld -PinstallDir="$RUNNER_DIR" --info --stacktrace
  fi
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
INSTALL_DIR="$HOME/jenkinstests"
APSCLI_DIR="$INSTALL_DIR/apscli"
RUNNER_DIR="$INSTALL_DIR/runner"
MAVENBASEDIR="$HOME/jenkinstests/maven"

#Command line Options
OPTIONS=i:x
LONGOPTS=help,installdir:,nomaven

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
  -x | --nomaven)
    MAVENBASEDIR=
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
echo "Running with binstalldir=$INSTALL_DIR, maven=$MAVENBASEDIR"
testTestRunnerInstallation
testApsCli