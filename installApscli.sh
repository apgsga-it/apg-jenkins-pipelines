#!/bin/bash
# Command line parsing
# Usage info
show_help() {
  cat <<EOF
Usage: ${0##*/} [-v VERSION] [-r GITREPO] [-b BRANCH] [-i INSTALLDIR] [ -m  MAVENBASEDIR ] -ns
Installs die server apscli.sh variant into the INSTALL_DIR

Builds and Installs the jenkinsfile-runner to a installation Dir to be used with the Jenkinspipeline Tests
It clones the jenkinsfile-runner from GITREPO into a BUILDDIR and builds. After the build the respective artifacts are
copied to INSTALLDIR

    -h          display this help and exit
    -i=INSTALLDIR Installation Dir for the Jenkinstests, defaults to ~/jenkinstests
    -v=VERSION  Maven Version of the apscli artefact. The artefactId & groupId are coded als variables, defaults to 2.0.0-SNAPSHOT
    -x          no MAVENBASEDIR

EOF
}
preconditions() {
  # Preconditions
  mvn --version >/dev/null 2>&1 || {
    echo >&2 "mvn is either not in Path or Installed.  Aborting."
    exit 1
  }
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

installServlessApsCli() {
  SAVEDWD=$(pwd)
  cd "$INSTALL_DIR"
  rm -f "/tmp/$ARTIFACTID*"
  export MAVEN_OPTS=-Dmaven.repo.local="$MAVEN_BASE_DIR/repo"
  mvn dependency:copy -Dartifact=$GROUPID:$ARTIFACTID:$VERSION:zip -DoutputDirectory=/tmp
  if [ -d "$APSCLI_DIR" ]; then
    echo "Deleting Target pkg directory $APSCLI_DIR"
    rm -Rf "$APSCLI_DIR"
    echo "Done"
  fi
  unzip "/tmp/$ARTIFACTID*.zip" 
  # Move Data Directory
  if [ -d "data" ]; then
    echo "Deleting data  directory"
    rm -Rf data
    echo "Done"
  fi
  mv "$APSCLI_DIR/data" .
  # Update Property file
  sed -i -e "s&@dataDir@&/$INSTALL_DIR/data&g" "$APSCLI_DIR/conf/application.properties"
  cd "$SAVEDWD" || {
    echo >&2 "Could'nt cd to $SAVEDWD.  Aborting."
    exit 1
  }
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
MAVENBASEDIR="$INSTALL_DIR/maven"
GROUPID=com.apgsga.patchframework
ARTIFACTID=apg-patch-service-cmdclient
VERSION=2.0.0-SNAPSHOT

#Command line Options
OPTIONS=h:i:v:x
LONGOPTS=help,installdir:,version:,nomaven

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
  -v | --version)
    VERSION=$2
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
echo "Running $0"
echo "Running with installdir=$APSCLI_DIR, version:$VERSION and $MAVENBASEDIR "
preconditions
installServlessApsCli
