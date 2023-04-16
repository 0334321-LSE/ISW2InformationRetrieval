package ISW2.DataRetriever;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.*;

public class BugTicket {

    public BugTicket(){

    }

    public BugTicket(String issueKey, LocalDate ticketsCreationDate, LocalDate ticketsResolutionDate, String injectedVersion){
        this.issueKeys = issueKey;
        this.ticketsCreationDate = ticketsCreationDate;
        this.ticketsResolutionDate = ticketsResolutionDate;
        this.injectedVersion = injectedVersion;
    }

    private  String issueKeys;
    private  LocalDate ticketsCreationDate;
    private  LocalDate ticketsResolutionDate;
    private String injectedVersion;

    private  String openingVersion;

    private  String fixedVersion;

    void setInjectedVersion(String injectedVersion){
        this.injectedVersion= injectedVersion;
    }
    private void setOpeningVersion(String openingVersion){
        this.openingVersion= openingVersion;
    }
    private void setFixedVersion(String fixedVersion){
        this.fixedVersion= fixedVersion;
    }

    public void setVersionInfo(List<BugTicket> bugTickets, List<VersionInfo> versionInfoList){

        for (BugTicket bugTicket : bugTickets) {

            bugTicket.setOpeningVersion(bugTicket.getTicketOpeningVersion(versionInfoList));
            bugTicket.setFixedVersion(bugTicket.getTicketFixedVersion(versionInfoList));
            bugTicket.setInjectedVersion(bugTicket.getInjectedVersion());

        }

    }

    private String getTicketOpeningVersion(List<VersionInfo> versionInfoList){
        int i =0, flag =0;
        String openingVersion = "";
        for (i=0; i< versionInfoList.size() && flag==0; i++){

            if (this.getTicketsCreationDate().isBefore(versionInfoList.get(i).getVersionDate())) {
                flag = 1;
                openingVersion = versionInfoList.get(i).getVersionName();
            }else openingVersion ="NULL";
        }

        return openingVersion;
    }

    private String getTicketFixedVersion(List<VersionInfo> versionInfoList){
        int i =0, flag =0;
        String fixedVersion = "";
        for (i=0; i< versionInfoList.size() && flag==0; i++){
            if (this.getTicketsResolutionDate().isBefore(versionInfoList.get(i).getVersionDate())){
                flag=1;
                fixedVersion = versionInfoList.get(i).getVersionName();
            }else fixedVersion ="NULL";
        }

        return fixedVersion;
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

    public String getOpeningVersion(){ return this.openingVersion;}

    public String getFixedVersion(){ return this.fixedVersion;}

    public String getInjectedVersion(){ return this.injectedVersion;}



}
