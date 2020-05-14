#!groovy

node {
    shOutput = sh(returnStdout: true, script: "$SCRIPTS_DIR/input.rb").trim()
    echo "Input Script output: $shOutput"
}