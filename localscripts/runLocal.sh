#!/bin/sh

########################################################################
# Bash script for executing Chrome gradle test locally.
# To customize tags to run and other configs, edit test runner files in 'src/test/java/suites'.
# Passing in env vars CUCUMBER_TAGS and GRADLEW_RUN_PARAMS not currently supported for this.
# Note that setting any env vars below will be overwritten by .env file vars, if they are present.
########################################################################

export SELENIUM_DRIVER_TYPE=local

cd ..
./gradlew --stop
./gradlew runChromeTest --info &
wait