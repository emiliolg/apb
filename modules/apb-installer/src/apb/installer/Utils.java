package apb.installer;

import java.io.File;
import java.util.List;
import java.util.LinkedList;


public class Utils {

public static File makeRelative(File baseDir, File file)
    {

        List base = getParts(baseDir.getAbsoluteFile());
        List f = getParts(file.getAbsoluteFile());
        int          i = 0;

        while (i < base.size() && i < f.size()) {
            if (!base.get(i).equals(f.get(i))) {
                break;
            }

            i++;
        }

        File result = null;

        if (i > 0) {
        for (int j = i; j < base.size(); j++) {
            result = new File(result, "..");
        }

        for (int j = i; j < f.size(); j++) {
            result = new File(result, (String) f.get(j));
        }
        }
        return result;
    }

    public static List getParts(final File file)
    {
        LinkedList result = new LinkedList();

        for (File f = file; f != null; f = f.getParentFile()) {
            final String name = f.getName();
            if ("..".equals(name))
                f = f.getParentFile();
            else
            result.addFirst(name.length() == 0 ? "/" : name);
        }
        return result;
    }

    public static void main(String[] args) {
        File f = new File(args[0]);
        File f2 = new File(args[1]);
        System.out.println("makeRelative(f,f2) = " + makeRelative(f, f2));
    }

}
