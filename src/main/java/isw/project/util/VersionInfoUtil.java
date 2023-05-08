package isw.project.util;


import isw.project.model.Version;
import isw.project.model.VersionInfo;
import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class VersionInfoUtil {
    private VersionInfoUtil(){throw new IllegalStateException("This class does not have to be instantiated.");}

    /** Return VersionInfo: list of matchingCommits for a version*/
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


}
