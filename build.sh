#
# Boot build APB
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
OUT_DIR=$(ospath "$APB_DIR/modules/apb/output/classes")
rm -rf $LIB_DIR/apb.jar $LIB_DIR/apb-src.jar $LIB_DIR/ant-apb.jar $APB_DIR/modules/*/output

if [ "$1" != "clean" ]
then
    mkdir -p $OUT_DIR
    javac -classpath $LIB_DIR/annotations.jar -sourcepath $SRC_DIR -d $OUT_DIR $SRC_DIR/apb/Main.java
    java -classpath $OUT_DIR apb.Main -ft $APB_DIR/modules/project-definitions/ApbAll.package
fi
