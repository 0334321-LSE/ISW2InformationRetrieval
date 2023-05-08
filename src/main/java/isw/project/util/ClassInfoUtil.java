package isw.project.util;

import isw.project.model.ClassInfo;
import isw.project.model.Version;
import org.eclipse.jgit.revwalk.RevCommit;

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

    /**  Check if the classes into the ClassInfo list have been modified, if iv <= ov < fv, then javaClass is buggy */
    public static void updateJavaClassBuggyness(List<ClassInfo> javaClasses, String className, Version injectedVersion, Version fixedVersion) {
        //fv is related to the single commit, not to the ticket

        for(ClassInfo javaClass : javaClasses) {
            /*if javaClass has been modified by commit (className contains modified class name) and
            is related to a version v such that iv <= ov < fv, then javaClass is buggy*/

            if(javaClass.getName().equals(className) && javaClass.getVersion().getVersionInt() >= injectedVersion.getVersionInt() && javaClass.getVersion().getVersionInt() < fixedVersion.getVersionInt()) {
                javaClass.setBuggy(true);

            }

        }

    }

    /** check if the classes into the list have been modified in the same release of the commit, in that case add it*/
    public static void updateJavaClassCommits(List<ClassInfo> javaClasses, String className, Version associatedVersion, RevCommit commit) {

        for(ClassInfo javaClass : javaClasses) {
            //if javaClass has been modified by commit (that is className) and is related to the same release of commit, then add commit to javaClass.commits
            if(javaClass.getName().equals(className) && javaClass.getVersion().getVersionInt() == associatedVersion.getVersionInt() && !javaClass.getCommits().contains(commit)) {
                javaClass.getCommits().add(commit);

            }

        }

    }


    public static void resetBuggyness(List<ClassInfo> javaClasses){
        for (ClassInfo javaClass: javaClasses){
            javaClass.setBuggy(false);
        }
    }

}
