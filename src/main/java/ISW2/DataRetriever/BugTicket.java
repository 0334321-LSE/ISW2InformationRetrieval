package ISW2.DataRetriever;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDate;
import java.util.*;

public class BugTicket {

    public BugTicket(){

    }

    public BugTicket(String issueKey, LocalDate ticketsCreationDate, LocalDate ticketsResolutionDate, VersionInfo injectedVersion){
        this.issueKeys = issueKey;
        this.ticketsCreationDate = ticketsCreationDate;
        this.ticketsResolutionDate = ticketsResolutionDate;
        this.injectedVersion = injectedVersion;
    }

    private  String issueKeys;
    private  LocalDate ticketsCreationDate;
    private  LocalDate ticketsResolutionDate;
    private VersionInfo injectedVersion;

    private  VersionInfo openingVersion;

    private  VersionInfo fixedVersion;

    private ArrayList<RevCommit> associatedCommit;

    void setInjectedVersion(VersionInfo injectedVersion){
        this.injectedVersion= injectedVersion;
    }
    private void setOpeningVersion(VersionInfo openingVersion){
        this.openingVersion= openingVersion;
    }
    private void setFixedVersion(VersionInfo fixedVersion){
        this.fixedVersion= fixedVersion;
    }

    public void setAssociatedCommit(ArrayList<RevCommit> associatedCommit) {
        this.associatedCommit = associatedCommit;
    }

    public void setVersionInfo(List<BugTicket> bugTickets, List<VersionInfo> versionInfoList){

        for (BugTicket bugTicket : bugTickets) {

            bugTicket.setOpeningVersion(bugTicket.getOvFromCreationDate(versionInfoList));
            bugTicket.setFixedVersion(bugTicket.getFvFromResolutionDate(versionInfoList));
            bugTicket.setInjectedVersion(bugTicket.getInjectedVersion());

        }

    }

    /** Return the ticket correct opening version from opening date*/
    private VersionInfo getOvFromCreationDate(List<VersionInfo> versionInfoList){
        int i =0, flag =0;
        VersionInfo openingVersion = new VersionInfo();

        for (i=0; i< versionInfoList.size() && flag==0; i++){
            if (this.getTicketsCreationDate().isBefore(versionInfoList.get(i).getVersionDate())) {
                flag = 1;
                openingVersion = versionInfoList.get(i);
            }
        }
        if(flag == 0){
            //if it comes here there isn't a valid OV, it may happen with not closed project
            //add the first version ( the NULL one )
            openingVersion = versionInfoList.get(0);
        }

        return openingVersion;
    }

    /** Return the ticket correct fixed version from resolution date*/
    private VersionInfo getFvFromResolutionDate(List<VersionInfo> versionInfoList){
        int i, flag =0;
        VersionInfo fixedVersion =  new VersionInfo();
        for (i=0; i< versionInfoList.size() && flag==0; i++){
            if (this.getTicketsResolutionDate().isBefore(versionInfoList.get(i).getVersionDate())){
                flag=1;
                fixedVersion = versionInfoList.get(i);
            }
        }
        if(flag == 0){
            //if it comes here there isn't a valid FV, it may happen with not closed project
            //add the first version ( the NULL one )
            fixedVersion = versionInfoList.get(0);
        }
        return fixedVersion;
    }

    public ArrayList<RevCommit> getAssociatedCommit() {
        return associatedCommit;
    }

    private void printVersionInformation(){
        System.out.println("\n---------------------------------------------------------------------------");
        System.out.println("TICKET: "+this.issueKeys);
        System.out.print("| Injected Version: "+this.injectedVersion);
        System.out.print(" | Opening Version: "+this.openingVersion);
        System.out.print(" | Fixed Version: "+this.fixedVersion+" |");
    }

    public void printVersionInformationList(List<BugTicket> ticketVersionInformationList){
        for ( BugTicket ticket: ticketVersionInformationList)
            ticket.printVersionInformation();

    }


    public String getIssueKey(){
        return this.issueKeys;
    }
    public LocalDate getTicketsCreationDate(){
        return this.ticketsCreationDate;
    }
    public LocalDate getTicketsResolutionDate(){
        return this.ticketsResolutionDate;
    }

    public VersionInfo getOpeningVersion(){ return this.openingVersion;}

    public VersionInfo getFixedVersion(){ return this.fixedVersion;}

    public VersionInfo getInjectedVersion(){ return this.injectedVersion;}



}
