package isw2_data_retriever.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VersionInfo {
    private Version version;
    private List<RevCommit> commitList;
    private RevCommit lastCommit;
    private Map<String,String> javaClasses;

    public VersionInfo(Version version, List<RevCommit> commitList, RevCommit lastCommit){
        this.version = version;
        this.commitList=commitList;
        this.lastCommit=lastCommit;
        this.javaClasses=null;
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

    private void setLastCommit(RevCommit lastCommit) {
        this.lastCommit = lastCommit;
    }

    public Map<String, String> getJavaClasses() {
        return javaClasses;
    }

    public void setJavaClasses(Map<String, String> javaClasses) {
        this.javaClasses = javaClasses;
    }

    /** Return versionInfo from a version*/
    public static VersionInfo getCommitsOfVersion(List<RevCommit> commitsList, Version version, LocalDate firstDate) {

        List<RevCommit> matchingCommits = new ArrayList<>();
        LocalDate lastDate = version.getVersionDate();

        for(RevCommit commit : commitsList) {
            //Cast date to local date then compare
            LocalDate commitDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            //if firstDate < commitDate <= lastDate then add the commit in matchingCommits list
            if(commitDate.isAfter(firstDate) && (commitDate.isBefore(lastDate) || commitDate.equals(lastDate))) {
                matchingCommits.add(commit);
            }

        }
        RevCommit lastCommit = null;

        if(!matchingCommits.isEmpty())
            lastCommit = getLastCommit(matchingCommits);

        return new VersionInfo(version, matchingCommits, lastCommit);
    }

    /** Get last commit from one commit list */
    private static RevCommit getLastCommit(List<RevCommit> commitsList) {

        RevCommit lastCommit = commitsList.get(0);
        for(RevCommit commit : commitsList) {
            //if commitDate > lastCommitDate then refresh lastCommit
            if(commit.getCommitterIdent().getWhen().after(lastCommit.getCommitterIdent().getWhen())) {
                lastCommit = commit;

            }
        }
        return lastCommit;

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


}
