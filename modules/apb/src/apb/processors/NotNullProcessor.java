

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
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes({ "org.jetbrains.annotations.NotNull" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class NotNullProcessor
    extends AbstractProcessor
{
    //~ Methods ..............................................................................................

    public static Collection<String> getClassesToProcess()
    {
        Collection<String> result = annotatedFiles.get();
        return result != null ? result : Collections.<String>emptySet();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (!roundEnv.processingOver()) {
            process(roundEnv.getElementsAnnotatedWith(org.jetbrains.annotations.NotNull.class));
        }

        return true;
    }

    protected FileObject getClassFile(TypeElement t)
    {
        // Get the package & class file name
        final Name   pkg = getPackageOf(t).getQualifiedName();
        final String qname = t.getQualifiedName().toString();
        final String clsFileName = qname.substring(pkg.length() + 1).replace('.', '$') + ".class";

        return getResource(StandardLocation.CLASS_OUTPUT, pkg, clsFileName);
    }

    protected PackageElement getPackageOf(TypeElement t)
    {
        return getUtils().getPackageOf(t);
    }

    protected Elements getUtils()
    {
        return processingEnv.getElementUtils();
    }

    protected FileObject getResource(final StandardLocation location, CharSequence pkg,
                                     CharSequence clsFileName)
    {
        try {
            return processingEnv.getFiler().getResource(location, pkg, clsFileName);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void process(Set<? extends Element> elements)
    {
        final Collection<String> fileNames = new HashSet<String>();

        for (Element element : elements) {
            // Search the class
            while (element != null && !(element instanceof TypeElement)) {
                element = element.getEnclosingElement();
            }

            if (element != null) {
                FileObject f = getClassFile((TypeElement) element);
                URI        uri = f.toUri();
                boolean    absolute = uri.isAbsolute();

                if (absolute) {
                    fileNames.add(new File(uri).getPath());
                }
                else {
                    fileNames.add(uri.toString());
                }
            }

            annotatedFiles.set(fileNames);
        }
    }

    //~ Static fields/initializers ...........................................................................

    static final ThreadLocal<Collection<String>> annotatedFiles = new ThreadLocal<Collection<String>>();
}
