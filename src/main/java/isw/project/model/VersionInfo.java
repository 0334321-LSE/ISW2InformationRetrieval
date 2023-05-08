package isw.project.model;

import org.eclipse.jgit.revwalk.RevCommit;


import java.util.ArrayList;
import java.util.List;


public class VersionInfo implements Comparable<VersionInfo>{
    private final Version version;
    private final List<RevCommit> commitList;
    private final RevCommit lastCommit;
    private List<ClassInfo> javaClasses;

    public VersionInfo(Version version, List<RevCommit> commitList, RevCommit lastCommit){
        this.version = version;
        this.commitList=commitList;
        this.lastCommit=lastCommit;
        this.javaClasses= new ArrayList<>();
    }

    public Version getVersion() {
        return version;
    }

    public List<RevCommit> getCommitList() {
        return commitList;
    }


    public RevCommit getLastCommit() {
        return lastCommit;
    }

    public List<ClassInfo> getJavaClasses() {
        return javaClasses;
    }

    public void setJavaClasses(List<ClassInfo> javaClasses) {
        this.javaClasses = javaClasses;
    }


    /** From commit gets version*/
    public static Version getVersionOfCommit(RevCommit commit, List<VersionInfo> versionInfoList) {

        for(VersionInfo versionAssociatedCommit : versionInfoList) {
            for(RevCommit c : versionAssociatedCommit.getCommitList()) {
                if(c.equals(commit)) {
                    return versionAssociatedCommit.getVersion();
                }

            }

        }
        return null;

    }


    @Override
    public int compareTo(VersionInfo u) {
        if (getVersion() == null || u.getVersion() == null) {
            return 0;
        }
        return Integer.compare(getVersion().getVersionInt(), getVersion().getVersionInt());
    }

}
