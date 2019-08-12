#
# Copyright 2019 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

PREPARE_TEST=false
START_TEST=false
PATH_TO_ARTIFACT=
OVERRIDING_PROPERTIES=
PROFILE=
ENCODING=
MAIN_CLASS=org.vividus.runner.StoriesRunner

printParameter() {
    printf 'Parameter > %s=%s\n' "$1" "$2"
}

emptyOptionValueError() {
    printf 'ERROR: "%s" requires a non-empty option argument.\n' "$1"
    exit 1
}

while :; do
    case $1 in
        -h|-\?|--help)
            COMMAND_PATTERN=" %-30s | %s\n"
            printf 'Script is designed to prepare and run test \n'
            printf 'Usage:\n'
            printf "$COMMAND_PATTERN" '--path "path/to/artifact/folder"' 'Path to artifact folder. E.g.: --path "temp/my-tests-1.0.0"'
            printf "$COMMAND_PATTERN" '--prepareTest' 'If specified artifact content will be restructurized for test run.'
            printf "$COMMAND_PATTERN" '--overridingProperties "props"' 'Overriding properties list. E.g.: --overridingProperties "property1=value"'
            printf "$COMMAND_PATTERN" '--startTest' 'If specified test run will be started automaticly.'
            printf "$COMMAND_PATTERN" '--mainClass' 'Main class to start test'
            printf "$COMMAND_PATTERN" '--profile "profile"' 'Profile for test execution. E.g.: --profile "desktop/chrome"'
            printf "$COMMAND_PATTERN" '--encoding "encoding"' 'Encoding for test execution. E.g.: --encoding "UTF-8"'
            exit
            ;;
        --path)
            if [ -n "$2" ]
            then
                PATH_TO_ARTIFACT="$2"
                shift
            else
                emptyOptionValueError '--path'
            fi
            ;;
        --prepareTest)
            PREPARE_TEST=true
            ;;
        --startTest)
            START_TEST=true
            ;;
        --overridingProperties)
            if [ -n "$2" ]
            then
                OVERRIDING_PROPERTIES="$2"
                shift
            else
                emptyOptionValueError '--overridingProperties'
            fi
            ;;
        --profile)
            if [ -n "$2" ]
            then
                PROFILE="$2"
                shift
            else
                emptyOptionValueError '--profile'
            fi
            ;;
        --encoding)
            if [ -n "$2" ]
            then
                ENCODING="$2"
                shift
            else
                emptyOptionValueError '--encoding'
            fi
            ;;
        --mainClass)
            if [ -n "$2" ]
            then
                MAIN_CLASS="$2"
                shift
            else
                emptyOptionValueError '--mainClass'
            fi
            ;;
        -?*)
            printf 'WARN: Unknown option (ignored): %s\n' "$1"
            ;;
        *)
            break
    esac

    shift
done

printParameter 'PREPARE_TEST' "$PREPARE_TEST"
printParameter 'START_TEST' "$START_TEST"
printParameter 'PATH_TO_ARTIFACT' "$PATH_TO_ARTIFACT"
printParameter 'OVERRIDING_PROPERTIES' "$OVERRIDING_PROPERTIES"
printParameter 'PROFILE' "$PROFILE"
printParameter 'ENCODING' "$ENCODING"
printParameter 'MAIN_CLASS' "$MAIN_CLASS"

if [ -z "$PATH_TO_ARTIFACT" ]
then
    printf 'ERROR: Path to unpacked module is mandatory parameter. Please specify using "--path"\n'
    exit 1
fi

if [ ! -d "$PATH_TO_ARTIFACT" ]
then
    printf 'ERROR: Folder "%s" does not exist\n' "$PATH_TO_ARTIFACT"
    exit 1
fi

cd $PATH_TO_ARTIFACT
ARTIFACT_NAME=$(find . -maxdepth 1 -name "*.jar" | head -1 | sed 's|./||')

printParameter 'ARTIFACT_NAME' "$ARTIFACT_NAME"

if $PREPARE_TEST
then
    printf 'INFO: prepare test to run\n'
    mkdir classes
    cd classes
    jar -xfv ../$ARTIFACT_NAME
    cd ..
fi

if [ -n "$OVERRIDING_PROPERTIES" ] && [ -d "classes" ]
then
    cd classes
    echo "$OVERRIDING_PROPERTIES" > "overriding.properties"
    cd ..
fi

if $START_TEST
then
    printf 'INFO: start tests\n'
    cd ..
    rm -rf logs
    rm -rf output
    java -cp $PATH_TO_ARTIFACT/classes:$PATH_TO_ARTIFACT/lib/* ${PROFILE:+ -Dprofile='$PROFILE'} ${ENCODING:+ -Dfile.encoding='$ENCODING'} $MAIN_CLASS
fi
