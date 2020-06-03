#!groovy
// Invoking external Ruby Script , which waits blocking for subscription
node {
    recievedMsg = sh(returnStdout: true, script: "$SCRIPTS_DIR/input.rb").trim()
        echo "Input Script output: $recievedMsg"
}