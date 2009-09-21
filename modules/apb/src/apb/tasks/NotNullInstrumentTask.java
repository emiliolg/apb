

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


package apb.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import apb.Environment;

import apb.processors.NotNullProcessor;

import org.jetbrains.annotations.NotNull;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class NotNullInstrumentTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull private final String classesProperty;

    //~ Constructors .........................................................................................

    public NotNullInstrumentTask(Environment env)
    {
        super(env);
        classesProperty = ":" + Thread.currentThread().getId();
    }

    //~ Methods ..............................................................................................

    public static void process(Environment env)
    {
        final NotNullInstrumentTask task = new NotNullInstrumentTask(env);
        task.execute();
    }

    public void execute()
    {
        final Collection<String> classesPaths = NotNullProcessor.getClassesToProcess();

        for (final String classPath : classesPaths) {
            instrumentClass(classPath);
        }
    }

    @NotNull public String getClassesProperty()
    {
        return classesProperty;
    }

    private void instrumentClass(final String classPath)
    {
        final File classFile = new File(classPath);

        if (classFile.getName().endsWith(".class")) {
            try {
                final InputStream fis = new FileInputStream(classFile);

                try {
                    final ClassReader reader = new ClassReader(fis);
                    final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                    final NotNullClassInstrumenter classInstrumenter = new NotNullClassInstrumenter(writer);
                    reader.accept(classInstrumenter, 0);

                    if (classInstrumenter.isModified()) {
                        logVerbose("Adding @NotNull assertions to " + classPath + "\n");
                        final OutputStream os = new FileOutputStream(classPath);

                        try {
                            os.write(writer.toByteArray());
                        }
                        finally {
                            os.close();
                        }
                    }
                }
                finally {
                    fis.close();
                }
            }
            catch (IOException e) {
                logVerbose("Failed to instrument @NotNull assertion for " + classPath + ": " +
                           e.getMessage() + "\n");
                e.printStackTrace();
            }
        }
    }

    //~ Static fields/initializers ...........................................................................

    public static final String CLASSES_PROPERTY = "notnull.classes.property";
}

class NotNullClassInstrumenter
    extends ClassAdapter
{
    //~ Instance fields ......................................................................................

    private String className;

    private boolean isModified;
    private boolean isNotStaticInner;
    private String  mySuperName;

    //~ Constructors .........................................................................................

    public NotNullClassInstrumenter(ClassVisitor classVisitor)
    {
        super(classVisitor);
        isModified = false;
        isNotStaticInner = false;
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
        mySuperName = superName;
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access)
    {
        super.visitInnerClass(name, outerName, innerName, access);

        if (className.equals(name)) {
            isNotStaticInner = (access & 8) == 0;
        }
    }

    public MethodVisitor visitMethod(final int access, final String name, String desc, String signature,
                                     String[] exceptions)
    {
        final Type[] args = Type.getArgumentTypes(desc);
        final Type   returnType = Type.getReturnType(desc);

        MethodVisitor v = cv.visitMethod(access, name, desc, signature, exceptions);
        return new MethodAdapter(v) {
            public AnnotationVisitor visitParameterAnnotation(int parameter, String anno, boolean visible)
            {
                final AnnotationVisitor result = mv.visitParameterAnnotation(parameter, anno, visible);

                if (NotNullClassInstrumenter.isReferenceType(args[parameter]) &&
                        anno.equals(NOT_NULL_ANNOATATION_SIGNATURE)) {
                    notNullParams.add(parameter);
                }

                return result;
            }

            public AnnotationVisitor visitAnnotation(String anno, boolean isRuntime)
            {
                AnnotationVisitor av = mv.visitAnnotation(anno, isRuntime);

                if (NotNullClassInstrumenter.isReferenceType(returnType) &&
                        anno.equals(NOT_NULL_ANNOATATION_SIGNATURE)) {
                    isNotNull = true;
                }

                return av;
            }

            public void visitCode()
            {
                if (isNotNull || !notNullParams.isEmpty()) {
                    startGeneratedCodeLabel = new Label();
                    mv.visitLabel(startGeneratedCodeLabel);
                }

                for (int nullParam : notNullParams) {
                    int var = (access & 8) != 0 ? 0 : 1;

                    for (int i = 0; i < nullParam; i++) {
                        var += args[i].getSize();
                    }

                    mv.visitVarInsn(25, var);
                    Label end = new Label();
                    mv.visitJumpInsn(199, end);
                    generateThrow(ILLEGAL_STATE_EXCEPTION_SIGNATURE,
                                  "Argument " + nullParam + " for @NotNull parameter of " + className + "." +
                                  name + " must not be null", end);
                }

                if (isNotNull) {
                    Label codeStart = new Label();
                    mv.visitJumpInsn(167, codeStart);
                    throwLabel = new Label();
                    mv.visitLabel(throwLabel);
                    generateThrow(ILLEGAL_STATE_EXCEPTION_SIGNATURE,
                                  "@NotNull method " + className + "." + name + " must not return null",
                                  codeStart);
                }
            }

            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end,
                                           int index)
            {
                boolean isStatic = (access & 8) != 0;
                boolean isParameter = isStatic ? index < args.length : index <= args.length;
                mv.visitLocalVariable(name, desc, signature,
                                      !isParameter || startGeneratedCodeLabel == null
                                      ? start : startGeneratedCodeLabel, end, index);
            }

            public void visitInsn(int opcode)
            {
                if (opcode == 176 && isNotNull) {
                    mv.visitInsn(89);
                    mv.visitJumpInsn(198, throwLabel);
                }

                mv.visitInsn(opcode);
            }

            private void generateThrow(String exceptionClass, String descr, Label end)
            {
                mv.visitTypeInsn(187, exceptionClass);
                mv.visitInsn(89);
                mv.visitLdcInsn(descr);
                String exceptionParamClass = "(Ljava/lang/String;)V";
                mv.visitMethodInsn(183, exceptionClass, "<init>", exceptionParamClass);
                mv.visitInsn(191);
                mv.visitLabel(end);
                isModified = true;
            }

            private final List<Integer> notNullParams = new ArrayList<Integer>();
            private boolean             isNotNull = true;
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

class ClassWriter
    extends org.objectweb.asm.ClassWriter
{
    //~ Constructors .........................................................................................

    public ClassWriter(int flags)
    {
        super(flags);
    }

    //~ Methods ..............................................................................................

    protected String getCommonSuperClass(String type1, String type2)
    {
        Class<?> c;
        Class<?> d;

        try {
            c = Thread.currentThread().getContextClassLoader().loadClass(type1.replace('/', '.'));
            d = Thread.currentThread().getContextClassLoader().loadClass(type2.replace('/', '.'));
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (c.isAssignableFrom(d)) {
            return type1;
        }

        if (d.isAssignableFrom(c)) {
            return type2;
        }

        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        }

        do {
            c = c.getSuperclass();
        }
        while (!c.isAssignableFrom(d));

        return c.getName().replace('.', '/');
    }
}
