package isw2_data_retriever.util;

import isw2_data_retriever.model.ClassInfo;

import java.util.ArrayList;
import java.util.List;

public class ClassInfoUtil {


    /** This private constructor to hide the public one: utility classes do not have to be instantiated. */
    private ClassInfoUtil() {
        throw new IllegalStateException("This class does not have to be instantiated.");
    }

    /** Return all the classes that contained in a specific version */
    public static List<ClassInfo> filterJavaClassesByVersion(List<ClassInfo> javaClassesList, int versionID) {

        List<ClassInfo> remJavaClasses = new ArrayList<>();

        for(ClassInfo javaClass : javaClassesList) {
            if(javaClass.getVersion().getVersionInt() == versionID) {
                remJavaClasses.add(javaClass);

            }

        }
        return remJavaClasses;

    }
    public static void initializateBuggyness(List<ClassInfo> javaClassList){
        for(ClassInfo javaClass: javaClassList)
            javaClass.setBuggy(false);
    }
}
