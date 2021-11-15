die () {
    echo
    echo "$*"
    echo
    exit 1
}

getValue () {
    local value=$(grep "$1" $file | cut -d '=' -f2)
    if [ -z $value ] ; then
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

#If VIVIDUS_BUILD_SYSTEM_HOME is not set -> try embedded
if [ -z "$VIVIDUS_BUILD_SYSTEM_HOME" ] ; then
    export VIVIDUS_BUILD_SYSTEM_HOME=$(getValue 'buildSystemRootDir')
fi

GRADLEW_PATH=$VIVIDUS_BUILD_SYSTEM_HOME/$buildSystemVersion/gradlew
if [ -f "$GRADLEW_PATH" ] ; then
    exec "$GRADLEW_PATH" "$@"
else
    die "ERROR: Neither environment variable "VIVIDUS_BUILD_SYSTEM_HOME" is set nor embedded build system is synced
Used path: $GRADLEW_PATH
Please check Build System guide:
https://github.com/vividus-framework/vividus-build-system
or
clone this repo recursively: git clone --recursive <git-repository-url>"
fi
