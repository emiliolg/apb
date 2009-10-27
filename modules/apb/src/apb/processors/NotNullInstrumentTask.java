

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import apb.tasks.Task;

import apb.utils.CollectionUtils;

import org.jetbrains.annotations.NotNull;

import org.objectweb.asm.ClassReader;

import static apb.utils.CollectionUtils.stringToList;

public class NotNullInstrumentTask
    extends Task
{
    //~ Methods ..............................................................................................

    @NotNull public static String getClassesProperty()
    {
        return CLASSES_PROPERTY + ":" + Thread.currentThread().getId();
    }

    public static void setClassesToProcess(Iterable<String> fileNames)
    {
        if (fileNames != null) {
            System.setProperty(getClassesProperty(), CollectionUtils.listToString(fileNames));
        }
    }

    public void execute()
    {
        final String classesProperty = getClassesProperty();
        final String classes = System.getProperty(classesProperty);
        System.setProperty(classesProperty, "");

        for (final String classPath : stringToList(classes)) {
            instrumentClass(classPath);
        }
    }

    private void instrumentClass(@NotNull final String classPath)
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
