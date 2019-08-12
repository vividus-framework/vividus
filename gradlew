die () {
    echo
    echo "$*"
    echo
    exit 1
}

file="./gradle.properties"
if [ -f "$file" ] ; then
    export $(cat $file | grep buildSystemVersion)
else
    die "$file not found."
fi

if [ -z "$BUILD_SYSTEM_ROOT" ] ; then
    export BUILD_SYSTEM_ROOT=vividus-build-system
fi

GRADLEW_PATH=$BUILD_SYSTEM_ROOT/$buildSystemVersion/gradlew
if [ -f "$GRADLEW_PATH" ] ; then
    exec "$GRADLEW_PATH" "$@"
else
    die "ERROR: Neither environment variable "BUILD_SYSTEM_ROOT" is set nor embedded build system is synced
Please check Build System guide:
https://Vividus-BuildSystem
or
clone this repo recursively: git clone --recursive <git-repository-url>"
fi
