package isw.project.model;


import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDate;
import java.util.*;



public class BugTicket {

    public BugTicket(String issueKey, LocalDate creationDate, LocalDate resolutionDate, Version injectedVersion){
        this.issueKeys = issueKey;
        this.creationDate = creationDate;
        this.resolutionDate = resolutionDate;
        this.injectedVersion = injectedVersion;
    }

    private final String issueKeys;
    private final LocalDate creationDate;
    private final LocalDate resolutionDate;
    private Version injectedVersion;

    private Version openingVersion;

    private Version fixedVersion;
    private List<RevCommit> associatedCommit;

    private RevCommit lastCommit;

    public RevCommit getLastCommit() {
        return lastCommit;
    }



    public void setInjectedVersion(Version injectedVersion){
        this.injectedVersion= injectedVersion;
    }
    public void setOpeningVersion(Version openingVersion){
        this.openingVersion= openingVersion;
    }
    public void setFixedVersion(Version fixedVersion){
        this.fixedVersion= fixedVersion;
    }

    public void setAssociatedCommit(List<RevCommit> associatedCommit) {
        this.associatedCommit = associatedCommit;
        this.lastCommit = getLastCommit(associatedCommit);
    }

    /** Get last commit from one commit list */
    private static RevCommit getLastCommit(List<RevCommit> commitsList) {
        if(commitsList.isEmpty())
            return null;
        RevCommit lastCommit = commitsList.get(0);
        for(RevCommit commit : commitsList) {
            //if commitDate > lastCommitDate then refresh lastCommit
            if(commit.getCommitterIdent().getWhen().after(lastCommit.getCommitterIdent().getWhen())) {
                lastCommit = commit;

            }
        }
        return lastCommit;

    }
    public static void setVersionInfo(List<BugTicket> bugTickets, List<Version> versionList){

        for (BugTicket bugTicket : bugTickets) {

            bugTicket.setOpeningVersion(bugTicket.getOvFromCreationDate(versionList));
            bugTicket.setFixedVersion(bugTicket.getFvFromResolutionDate(versionList));
            bugTicket.setInjectedVersion(bugTicket.getInjectedVersion());

        }

    }

    /** Return the ticket correct opening version from opening date*/
    private Version getOvFromCreationDate(List<Version> versionList){
        int i;
        int flag =0;
        Version ov = new Version();

        for (i=0; i< versionList.size() && flag==0; i++){
            if (this.getCreationDate().isBefore(versionList.get(i).getVersionDate())) {
                flag = 1;
                ov = versionList.get(i);
            }
        }
        if(flag == 0){
            //if it comes here there isn't a valid OV, it may happen with not closed project
            //add the first version ( the NULL one )
            ov = versionList.get(0);
        }

        return ov;
    }

    /** Return the ticket correct fixed version from resolution date*/
    private Version getFvFromResolutionDate(List<Version> versionList){
        int i;
        int flag =0;
        Version fv =  new Version();
        for (i=0; i< versionList.size() && flag==0; i++){
            if (this.getResolutionDate().isBefore(versionList.get(i).getVersionDate())){
                flag=1;
                fv = versionList.get(i);
            }
        }
        if(flag == 0){
            //if it comes here there isn't a valid FV, it may happen with not closed project
            //add the first version ( the NULL one )
            fv = versionList.get(0);
        }
        return fv;
    }

    public List<RevCommit> getAssociatedCommit() {
        return associatedCommit;
    }

    public String getIssueKey(){
        return this.issueKeys;
    }
    public LocalDate getCreationDate(){
        return this.creationDate;
    }
    public LocalDate getResolutionDate(){
        return this.resolutionDate;
    }

    public Version getOpeningVersion(){ return this.openingVersion;}

    public Version getFixedVersion(){ return this.fixedVersion;}

    public Version getInjectedVersion(){ return this.injectedVersion;}

}
