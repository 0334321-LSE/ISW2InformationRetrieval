package ISW2.DataRetriever;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TicketVersionInformation {

    private  String ticketKey;

    private  String injectedVersion;

    private  String openingVersion;

    private  String fixedVersion;

    public TicketVersionInformation() {

    }

    public TicketVersionInformation(String ticketKey) {
        this.ticketKey = ticketKey;
    }

    private void setInjectedVersion(String injectedVersion){
        this.injectedVersion= injectedVersion;
    }
    private void setOpeningVersion(String openingVersion){
        this.openingVersion= openingVersion;
    }
    private void setFixedVersion(String fixedVersion){
        this.fixedVersion= fixedVersion;
    }

    public List<TicketVersionInformation> getVersionInformation(List<BugTicket> bugTickets, List<VersionInfo> versionInfoList){
        List<TicketVersionInformation> ticketList = new ArrayList<>();
        for (BugTicket bugTicket : bugTickets) {
            TicketVersionInformation ticketInformation = new TicketVersionInformation(bugTicket.getIssueKey());
            ticketInformation.setOpeningVersion(ticketInformation.getOpeningVersion(bugTicket, versionInfoList));
            ticketInformation.setFixedVersion(ticketInformation.getFixedVersion(bugTicket, versionInfoList));
            ticketInformation.setInjectedVersion(bugTicket.getInjectedVersion());

            ticketList.add(ticketInformation);
        }
            
        return ticketList;
    }

    public String getOpeningVersion(){
        return this.openingVersion;
    }
    public String getFixedVersion(){
        return this.fixedVersion;
    }

    private String getOpeningVersion(BugTicket ticket, List<VersionInfo> versionInfoList){
        int i =0, flag =0;
        String openingVersion = "";
        for (i=0; i< versionInfoList.size() && flag==0; i++){

            if (ticket.getTicketsCreationDate().isBefore(versionInfoList.get(i).getVersionDate())){
                flag=1;
                openingVersion = versionInfoList.get(i).getVersionName();
            }
        }

        return openingVersion;
    }

    private String getFixedVersion(BugTicket ticket, List<VersionInfo> versionInfoList){
        int i =0, flag =0;
        String fixedVersion = "";
        for (i=0; i< versionInfoList.size() && flag==0; i++){
            if (ticket.getTicketsResolutionDate().isBefore(versionInfoList.get(i).getVersionDate())){
                flag=1;
                fixedVersion = versionInfoList.get(i).getVersionName();
            }
        }

        return fixedVersion;
    }


    private void printVersionInformation(){
        System.out.println("\n-------------");
        System.out.println("TICKET: "+this.ticketKey);
        System.out.println("Injected Version: "+this.injectedVersion);
        System.out.println("Opening Version: "+this.openingVersion);
        System.out.println("Fixed Version: "+this.fixedVersion);
    }

    public void printVersionInformationList(List<TicketVersionInformation> ticketVersionInformationList){
        for ( TicketVersionInformation ticket: ticketVersionInformationList)
            ticket.printVersionInformation();

    }

    public void checkConsistencyValidity(List<TicketVersionInformation> ticketVersionInformationList, List<VersionInfo> versionInfoList){
       //TODO: discard ticket with IV > OV and calculate proportion factor
        VersionInfo mapCreator = new VersionInfo();
        Map<String, Integer> versionMap = mapCreator.getVersionInteger(versionInfoList);
        int validityCounter=0;
        for( TicketVersionInformation ticket: ticketVersionInformationList){
            if(ticket.getOpeningVersion().compareTo(ticket.getFixedVersion()) != 0)
                validityCounter++;

        }
        System.out.println(validityCounter);
    }


}
