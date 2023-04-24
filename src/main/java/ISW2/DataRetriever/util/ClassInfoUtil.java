package ISW2.DataRetriever.util;

import ISW2.DataRetriever.model.ClassInfo;
import ISW2.DataRetriever.model.VersionInfo;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class ClassInfoUtil {


    /** utility classes do not have to be instantiated. */
    private ClassInfoUtil() {
        throw new IllegalStateException("This class does not have to be instantiated.");
    }

    /** Return all the classes contained in a specific version */
    public static List<ClassInfo> filterJavaClassesByVersion(List<ClassInfo> javaClassesList, int versionID) {

        List<ClassInfo> remJavaClasses = new ArrayList<>();

        for(ClassInfo javaClass : javaClassesList) {
            if(javaClass.getVersionInfo().getVersionInt() == versionID) {
                remJavaClasses.add(javaClass);

            }

        }
        return remJavaClasses;

    }

    /**  Check if the classes into the ClassInfo list have been modified, if iv <= ov < fv, then javaClass is buggy */
    public static void updateJavaClassBuggyness(List<ClassInfo> javaClasses, String className, VersionInfo injectedVersion, VersionInfo fixedVersion) {
        //fv is related to the single commit, not to the ticket

        for(ClassInfo javaClass : javaClasses) {
            /*if javaClass has been modified by commit (className contains modified class name) and
            is related to a version v such that iv <= ov < fv, then javaClass is buggy*/
            if(javaClass.getName().equals(className) && javaClass.getVersionInfo().getVersionInt() >= injectedVersion.getVersionInt() && javaClass.getVersionInfo().getVersionInt() < fixedVersion.getVersionInt()) {
                javaClass.setBuggy(true);

            }

        }

    }

    /** check if the classes into the list have been modified in the same release of the commit, in that case add it*/
    public static void updateJavaClassCommits(List<ClassInfo> javaClasses, String className, VersionInfo associatedVersion, RevCommit commit) {

        for(ClassInfo javaClass : javaClasses) {
            //if javaClass has been modified by commit (that is className) and is related to the same release of commit, then add commit to javaClass.commits
            if(javaClass.getName().equals(className) && javaClass.getVersionInfo().getVersionInt() == associatedVersion.getVersionInt() && !javaClass.getCommits().contains(commit)) {
                javaClass.getCommits().add(commit);

            }

        }

    }
}
