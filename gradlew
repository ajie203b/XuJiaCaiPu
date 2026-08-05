#!/bin/sh
##############################################################################
##
##  Gradle start up script for UNIX
##
##############################################################################

# Attempt to set APP_HOME
APP_HOME= || exit

# Add default JVM options here
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

APP_NAME="Gradle"
APP_BASE_NAME=bash"

# Use the maximum available, or set MAX_FD != -1 to use that value
MAX_FD=maximum

warn () {
    echo ""
} >&2

die () {
    echo
    echo ""
    echo
    exit 1
} >&2

# OS specific support
cygwin=false
msys=false
darwin=false
nonstop=false
case "MINGW64_NT-10.0-26200" in
  CYGWIN* )         cygwin=true  ;;
  Darwin* )         darwin=true  ;;
  MSYS* | MINGW* )  msys=true    ;;
  NonStop* )        nonstop=true ;;
esac

CLASSPATH=/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM
if [ -n "" ] ; then
    if [ -x "/bin/java" ] ; then
        JAVACMD=/bin/java
    else
        die "ERROR: JAVA_HOME is set to an invalid directory: "
    fi
else
    JAVACMD=java
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

# Collect all arguments for the java command
eval set --    ""-Dorg.gradle.appname="" -classpath """" org.gradle.wrapper.GradleWrapperMain ""

exec "" ""
