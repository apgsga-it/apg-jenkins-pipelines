#!/bin/bash
# Usage info
show_help() {
  cat <<EOF
Usage: ${0##*/} -u USER [ -i INSTALL_DIR ]
Runs all installation and initialization scripts for Jenkinspipeline Tests:
- initLocalRepos.sh : Initialization of a Gradle User Home and Mavenlocal with USER
- installJenkinsFilerunner.sh : Installs the Jenkins Filerunner
- installApscli.sh : Installs the serverless apscli needed for apg Patch Pipelines
- testJenkinsPipeline.sh : Tests the Installations

    -h          display this help and exit
    -u=USER     userid for the gitrepo, from which apg-gradle-properties will be clone. Mandatory option
    -i=INSTALL_DIR Installation Directory , defaults to "/opt/jenkinstests"
EOF
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

INSTALL_DIR="/opt/jenkinstests"
USER=

#Command line Options
OPTIONS=hu:i:
LONGOPTS=help,user:,installDir:

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
  -u | --user)
    USER=$2
    shift 2
    ;;
  -i | --installDir)
    TARGET_DIR=$2
    TARGET_DIR="${TARGET_DIR/#\~/$HOME}"
    shift 2
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
echo "Running with Target directory=$INSTALL_DIR, user:$USER"
echo "Running initLocalRepos.sh, installJenkinsFulerunner.sh, installApscli.sh and testJenkinsPipeline.sh with defaults"
if [  -d "$INSTALL_DIR" ]; then
  echo "Removing current Installation: $INSTALL_DIR"
  sudo rm -Rf  "$INSTALL_DIR"
fi
if [ ! -d "$INSTALL_DIR" ]; then
  echo "Creating INSTALL_DIR: $INSTALL_DIR"
  sudo mkdir "$INSTALL_DIR"
  echo "Changing Owner to current User"
  sudo chown -R $(id -u):$(id -g) "$INSTALL_DIR"
fi
./initLocalRepos.sh -u "$USER" -i "$INSTALL_DIR"
./installJenkinsFilerunner.sh -i "$INSTALL_DIR"
./installApscli.sh -i "$INSTALL_DIR"
./testJenkinsPipeline.sh -i "$INSTALL_DIR" -a
echo "Done"
