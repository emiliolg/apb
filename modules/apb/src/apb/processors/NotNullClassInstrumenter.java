

// Copyright 2008-2009 Emilio Lopez-Gabeiras
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License
//


package apb.processors;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
//
// User: emilio
// Date: Oct 27, 2009
// Time: 4:01:57 PM

class NotNullClassInstrumenter
    extends ClassAdapter
{
    //~ Instance fields ......................................................................................

    private boolean isModified;

    private String className;

    //~ Constructors .........................................................................................

    public NotNullClassInstrumenter(ClassVisitor classVisitor)
    {
        super(classVisitor);
        isModified = false;
    }

    //~ Methods ..............................................................................................

    public boolean isModified()
    {
        return isModified;
    }

    public void visit(int version, int access, String name, String signature, String superName,
                      String[] interfaces)
    {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access)
    {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    public MethodVisitor visitMethod(final int access, final String name, String desc, String signature,
                                     String[] exceptions)
    {
        final Type[] args = Type.getArgumentTypes(desc);
        final Type   returnType = Type.getReturnType(desc);

        MethodVisitor v = cv.visitMethod(access, name, desc, signature, exceptions);
        return new MethodAdapter(v) {
            @Override public AnnotationVisitor visitParameterAnnotation(int parameter, String anno,
                                                                        boolean visible)
            {
                final AnnotationVisitor result = mv.visitParameterAnnotation(parameter, anno, visible);

                if (NotNullClassInstrumenter.isReferenceType(args[parameter]) &&
                        anno.equals(NOT_NULL_ANNOATATION_SIGNATURE)) {
                    notNullParams.add(parameter);
                }

                return result;
            }

            @Override public AnnotationVisitor visitAnnotation(String anno, boolean isRuntime)
            {
                final AnnotationVisitor av = mv.visitAnnotation(anno, isRuntime);

                if (isReferenceType(returnType) && anno.equals(NOT_NULL_ANNOATATION_SIGNATURE)) {
                    isResultNotNull = true;
                }

                return av;
            }

            @Override public void visitCode()
            {
                if (isResultNotNull || !notNullParams.isEmpty()) {
                    startGeneratedCodeLabel = new Label();
                    mv.visitLabel(startGeneratedCodeLabel);
                }

                for (int nullParam : notNullParams) {
                    int var = (access & 8) != 0 ? 0 : 1;

                    for (int i = 0; i < nullParam; i++) {
                        var += args[i].getSize();
                    }

                    mv.visitVarInsn(Opcodes.ALOAD, var);
                    Label end = new Label();
                    mv.visitJumpInsn(Opcodes.IFNONNULL, end);
                    generateThrow(ILLEGAL_STATE_EXCEPTION_SIGNATURE,
                                  "Argument " + nullParam + " for @NotNull parameter of " + className + "." +
                                  name + " must not be null", end);
                }

                if (isResultNotNull) {
                    final Label codeStart = new Label();
                    mv.visitJumpInsn(Opcodes.GOTO, codeStart);
                    throwLabel = new Label();
                    mv.visitLabel(throwLabel);
                    generateThrow(ILLEGAL_STATE_EXCEPTION_SIGNATURE,
                                  "@NotNull method " + className + "." + name + " must not return null",
                                  codeStart);
                }
            }

            @Override public void visitLocalVariable(String name, String desc, String signature, Label start,
                                                     Label end, int index)
            {
                boolean isStatic = (access & 8) != 0;
                boolean isParameter = isStatic ? index < args.length : index <= args.length;
                mv.visitLocalVariable(name, desc, signature,
                                      !isParameter || startGeneratedCodeLabel == null
                                      ? start : startGeneratedCodeLabel, end, index);
            }

            public void visitInsn(int opcode)
            {
                if (opcode == Opcodes.ARETURN && isResultNotNull) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitJumpInsn(Opcodes.IFNULL, throwLabel);
                }

                mv.visitInsn(opcode);
            }

            private void generateThrow(String exceptionClass, String descr, Label end)
            {
                mv.visitTypeInsn(Opcodes.NEW, exceptionClass);
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(descr);

                final String exceptionParamClass = "(Ljava/lang/String;)V";
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, exceptionClass, "<init>", exceptionParamClass);
                mv.visitInsn(Opcodes.ATHROW);
                mv.visitLabel(end);
                isModified = true;
            }

            private final List<Integer> notNullParams = new ArrayList<Integer>();
            private boolean             isResultNotNull = false;
            public Label                throwLabel;
            private Label               startGeneratedCodeLabel;
        };
    }

    private static boolean isReferenceType(Type type)
    {
        return type.getSort() == 10 || type.getSort() == 9;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String NOT_NULL_ANNOATATION_SIGNATURE = "Lorg/jetbrains/annotations/NotNull;";
    private static final String ILLEGAL_STATE_EXCEPTION_SIGNATURE = "java/lang/IllegalArgumentException";
}
