package ISW2.DataRetriever.util;

import ISW2.DataRetriever.model.ClassInfo;

import java.util.ArrayList;
import java.util.List;

public class ClassInfoUtil {


    /** This private constructor to hide the public one: utility classes do not have to be instantiated. */
    private ClassInfoUtil() {
        throw new IllegalStateException("This class does not have to be instantiated.");
    }

    /** Return all the classes that contained in a specific version */
    public static List<ClassInfo> filterJavaClassesByRelease(List<ClassInfo> javaClassesList, int releaseID) {

        List<ClassInfo> remJavaClasses = new ArrayList<>();

        for(ClassInfo javaClass : javaClassesList) {
            if(javaClass.getVersionInfo().getVersionInt() == releaseID) {
                remJavaClasses.add(javaClass);

            }

        }
        return remJavaClasses;

    }
}
