#
DIRLIB=$(dirname $(type -p $0))/../lib
java -cp $DIRLIB/apb.jar apb.commands.idegen.idea.ModuleParser $1
