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

APB_DIR=$(ospath "$(dirname $(type -p $0))")
SRC_DIR=$(ospath "$APB_DIR/modules/apb/src")
LIB_DIR=$(ospath "$APB_DIR/lib")
OUT_DIR=$(ospath "$APB_DIR/modules/output/apb/classes")
DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
rm -rf $LIB_DIR/apb.jar $LIB_DIR/apb-src.jar $LIB_DIR/ant-apb.jar $APB_DIR/modules/output

if [ "$1" != "clean" ]
then
    mkdir -p $OUT_DIR
    javac -g -classpath $LIB_DIR/annotations.jar:$LIB_DIR/asm-3.1.jar -sourcepath $SRC_DIR -d $OUT_DIR $SRC_DIR/apb/Main.java $SRC_DIR/apb/ApbOptions.java $SRC_DIR/apb/metadata/*.java  $SRC_DIR/apb/idegen/IdeaInfo.java
    java -classpath $OUT_DIR apb.Main -fs $APB_DIR/modules/project-definitions/ApbAll.package
    if [ "$1" != "package" ]
    then
	export APB_PROJECT_PATH="$APB_DIR/modules/project-definitions"
	$APB_DIR/bin/apb ApbAll.run-tests
	$APB_DIR/bin/apb Apb.javadoc
    fi
fi
