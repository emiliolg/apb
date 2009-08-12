package apb.processors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.util.*;
import java.net.URI;
import java.io.File;
import java.io.IOException;

@SupportedAnnotationTypes({ "org.jetbrains.annotations.NotNull" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class NotNullProcessor extends AbstractProcessor {
    static final ThreadLocal<Collection<String>> annotatedFiles = new ThreadLocal<Collection<String>>();

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(org.jetbrains.annotations.NotNull.class);
        final Collection<String> fileNames = new HashSet<String>();
        for (Element element : elements) {

            // Search the class
            while (element != null && !(element instanceof TypeElement)) {
                element = element.getEnclosingElement();
            }

            if (element != null) {
                FileObject f = getClassFile((TypeElement) element);
                URI uri = f.toUri();
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
        return true;
    }

    protected FileObject getClassFile(TypeElement t)
    {
        // Get the package & class file name
        final Name pkg = getPackageOf(t).getQualifiedName();
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

    public static Collection<String> getClassesToProcess()
    {
        Collection<String> result = annotatedFiles.get();
        return result!=null?result: Collections.<String>emptySet();
    }

}

