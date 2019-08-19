die () {
    echo
    echo "$*"
    echo
    exit 1
}

getValue () {
    local value=$(grep "$1" $file | cut -d '=' -f2)
    if [[ -z $value ]] ;
    then
        die "Unable to find $1 in $file"
    else
        echo $value
    fi
}

file="./gradle.properties"
if [ -f "$file" ] ; then
    export buildSystemVersion=$(getValue 'buildSystemVersion')
else
    die "$file not found."
fi

if [ -z "$BUILD_SYSTEM_ROOT" ] ; then
    export BUILD_SYSTEM_ROOT=$(getValue 'buildSystemRootDir')
fi

GRADLEW_PATH=$BUILD_SYSTEM_ROOT/$buildSystemVersion/gradlew
if [ -f "$GRADLEW_PATH" ] ; then
    exec "$GRADLEW_PATH" "$@"
else
    die "ERROR: Neither environment variable "BUILD_SYSTEM_ROOT" is set nor embedded build system is synced
Please check Build System guide:
https://github.com/vividus-framework/vividus-build-system
or
clone this repo recursively: git clone --recursive <git-repository-url>"
fi
