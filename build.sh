#! /bin/bash

#
# Boot build APB
#
#

linux=false
cygwin=false
darwin=false

case "$OSTYPE" in
  linux*)  linux=true ;;
  cygwin*) cygwin=true ;;
  darwin*) darwin=true ;;
esac

# Under Cygwin, JVM doesn't support unix paths. Must be translated to windows style paths.
if $cygwin; then
   ospath() { cygpath --windows "$1"; }
else
   ospath() { echo "$1"; }
fi

APB_DIR="$(type -p "$0")"
APB_DIR="$(ospath "${APB_DIR%/*}")"
SRC_DIR="$(ospath "$APB_DIR/modules/apb-base/src")"
SRC_API_DIR="$(ospath "$APB_DIR/modules/apb-base-api/src")"
LIB_DIR="$(ospath "$APB_DIR/lib")"
OUT_DIR="$(ospath "$APB_DIR/modules/output/apb-base/classes")"
OUT_API_DIR="$(ospath "$APB_DIR/modules/output/apb-base-api/classes")"

DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

export APB_PROJECT_PATH=$APB_DIR/modules/DEFS

rm -rf $LIB_DIR/apb.jar $LIB_DIR/apb-src.jar $LIB_DIR/ant-apb.jar $APB_DIR/modules/output

if [ "$1" != "clean" ]
then
    mkdir -p $OUT_DIR $OUT_API_DIR
    javac -Xlint -g -sourcepath $SRC_API_DIR -d $OUT_API_DIR $SRC_API_DIR/apb/*.java
    javac -Xlint -g -sourcepath $SRC_DIR -classpath $OUT_API_DIR -d $OUT_DIR $SRC_DIR/apb/Main.java $SRC_DIR/apb/ApbOptions.java $SRC_DIR/apb/metadata/*.java  $SRC_DIR/apb/idegen/IdeaInfo.java
    echo java -classpath $OUT_DIR:$OUT_API_DIR apb.Main -fs $APB_DIR/modules/DEFS/ApbAll.package
    java -classpath $OUT_DIR:$OUT_API_DIR apb.Main -fs $APB_DIR/modules/DEFS/ApbAll.package
    if [ "$1" != "package" ]
    then
	$APB_DIR/bin/apb ApbAll.run-tests
	$APB_DIR/bin/apb Apb.javadoc
    fi
fi
