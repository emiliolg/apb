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
SRC_DIR="$(ospath "$APB_DIR/modules/apb/src")"
LIB_DIR="$(ospath "$APB_DIR/lib")"
OUT_DIR="$(ospath "$APB_DIR/modules/output/apb/classes")"

DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
rm -rf $LIB_DIR/apb.jar $LIB_DIR/apb-src.jar $LIB_DIR/ant-apb.jar $APB_DIR/modules/output

if [ "$1" != "clean" ]
then
    mkdir -p $OUT_DIR

    # Find tools.jar
    if ! $darwin
    then
	tempdir="$OUT_DIR/test-$$"
	mkdir "$tempdir"

	cat > "$tempdir/ToolsJar.java" <<-"_EOF_"
		import javax.tools.JavaCompiler;
		import javax.tools.ToolProvider;
		import java.io.File;
		import java.net.URI;
		import java.net.URL;

		public class ToolsJar
		{
		    public static void main(String[] args)
		    {
			Class<?> clazz = ToolProvider.getSystemJavaCompiler().getClass();
			Class<?> topClazz = clazz.getDeclaringClass();
			if (topClazz != null) clazz = topClazz;

			URL url = clazz.getResource(clazz.getSimpleName() + ".class");

			if (url == null) System.exit(1);
			if (!"jar".equals(url.getProtocol())) System.exit(2);

			String path = url.getPath();
			int p = path.indexOf('!');
			if (p == -1) System.exit(3);

			System.out.println(new File(URI.create(path.substring(0, p))));
		    }
		}
_EOF_

	javac "$tempdir/ToolsJar.java"
	TOOLS_JAR="$(java -cp "$tempdir" ToolsJar)"
	rm -rf "$tempdir"
    fi

    javac -g -classpath "$LIB_DIR/annotations.jar${TOOLS_JAR:+:$TOOLS_JAR}" -sourcepath "$SRC_DIR" -d "$OUT_DIR" "$SRC_DIR"/apb/Main.java "$SRC_DIR"/apb/ApbOptions.java "$SRC_DIR"/apb/metadata/*.java  "$SRC_DIR"/apb/idegen/IdeaInfo.java
    export APB_PROJECT_PATH="$APB_DIR/modules/project-definitions"
    java -classpath $OUT_DIR apb.Main -fs ApbAll.package
    if [ "$1" != "package" ]
    then
	$APB_DIR/bin/apb ApbAll.run-tests
	$APB_DIR/bin/apb Apb.javadoc
    fi
fi
