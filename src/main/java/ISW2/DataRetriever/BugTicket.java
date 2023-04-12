package ISW2.DataRetriever;

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

    private void setInjectedVersion(String injectedVersion){
        this.injectedVersion= injectedVersion;
    }
    private void setOpeningVersion(String openingVersion){
        this.openingVersion= openingVersion;
    }
    private void setFixedVersion(String fixedVersion){
        this.fixedVersion= fixedVersion;
    }

    public void getVersionInformation(List<BugTicket> bugTickets, List<VersionInfo> versionInfoList){

        for (BugTicket bugTicket : bugTickets) {
            //TODO check if there is side effect with this kind of for
            bugTicket.setOpeningVersion(bugTicket.getTicketOpeningVersion(versionInfoList));
            bugTicket.setFixedVersion(bugTicket.getTicketFixedVersion(versionInfoList));
            bugTicket.setInjectedVersion(bugTicket.getInjectedVersion());

        }

    }

    private String getTicketOpeningVersion(List<VersionInfo> versionInfoList){
        int i =0, flag =0;
        String openingVersion = "";
        for (i=0; i< versionInfoList.size() && flag==0; i++){

            if (this.getTicketsCreationDate().isBefore(versionInfoList.get(i).getVersionDate())){
                flag=1;
                openingVersion = versionInfoList.get(i).getVersionName();
            }
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
            }
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

    public String getInjectedVersion(){ return this.injectedVersion;}

    public List<BugTicket> correctBugTicketListForProportioning(List<BugTicket> bugTicketsList, List<VersionInfo> versionInfoList){
        VersionInfo mapGenerator = new VersionInfo();
        Map<String,Integer> versionMap = mapGenerator.getVersionInteger(versionInfoList);
        List<BugTicket> correctBugTicketList =  new ArrayList<>(bugTicketsList);

        for ( BugTicket bugTicket: bugTicketsList){
            if ( bugTicket.injectedVersion.equals("NULL") )
                correctBugTicketList.remove(bugTicket);

            else if ( versionMap.get(bugTicket.injectedVersion) >= versionMap.get(bugTicket.openingVersion))
                //First control, cut off tickets which have IV > OV
                correctBugTicketList.remove(bugTicket);

            else if (versionMap.get( bugTicket.injectedVersion) >= versionMap.get(bugTicket.fixedVersion))
                //Second control, cut off tickets which have IV>= FV
                correctBugTicketList.remove(bugTicket);

            else if (versionMap.get(bugTicket.openingVersion) >= versionMap.get(bugTicket.fixedVersion))
                //Third control, cut off tickets which have OV> FV
                correctBugTicketList.remove(bugTicket);

            else if (Objects.equals(versionMap.get( bugTicket.injectedVersion), versionMap.get(bugTicket.openingVersion)) && Objects.equals(versionMap.get(bugTicket.fixedVersion), versionMap.get(bugTicket.openingVersion)))
                //Fourth control, cut off tickets which have IV = OV = FV
                correctBugTicketList.remove(bugTicket);

        }
        System.out.println("\n---------------------------------------------------------------------------");

        System.out.println("\nBug ticket list for proportioning: ");
        printVersionInformationList(correctBugTicketList);
        System.out.println("\n---------------------------------------------------------------------------");
        System.out.println("\nBug ticket consistency correctly checked, the remain number of ticket is: "+correctBugTicketList.size());
        return correctBugTicketList;
    }

    private double calculateProportioningCoefficient(List<BugTicket> bugTicketsListForProportion, List<VersionInfo> versionInfoList){
        VersionInfo mapGenerator = new VersionInfo();
        Map<String,Integer> versionMap = mapGenerator.getVersionInteger(versionInfoList);
        double proportionValue = 0;
        for( BugTicket bugTicket: bugTicketsListForProportion){
            // P = (FV-IV)/(FV-OV)
            proportionValue += ( (double) (versionMap.get(bugTicket.fixedVersion) - versionMap.get(bugTicket.injectedVersion)) / ((versionMap.get(bugTicket.fixedVersion)-versionMap.get(bugTicket.openingVersion))));
        }
        return   proportionValue / bugTicketsListForProportion.size();
    }

    public void proportionForInjectedVersion(List<BugTicket> bugTickets,List<BugTicket> bugTicketsListForProportion, List<VersionInfo> versionInfoList){
        VersionInfo mapGenerator = new VersionInfo();
        Map<String,Integer> versionMap = mapGenerator.getVersionInteger(versionInfoList);
        Object[] versionMapKey = versionMap.keySet().toArray();
        int iv;
        int flag;
        double proportionValue = calculateProportioningCoefficient(bugTicketsListForProportion,versionInfoList);
        for( BugTicket bugTicket: bugTickets){
            flag=0;
            if (bugTicket.injectedVersion.equals("NULL") ){
               //Apply proportion: IV= FV-(FV-OV)*P
                //TODO check BOOKKEEPER-24 has a problem with iv ( it becomes negative )
                iv = (int) ((int) versionMap.get(bugTicket.fixedVersion) - ((versionMap.get(bugTicket.fixedVersion) - versionMap.get(bugTicket.openingVersion)) * proportionValue));
                for ( int i = 0; i<versionMapKey.length && flag==0; i++ ){
                    if (versionMap.get(versionMapKey[i]) == iv){
                        bugTicket.injectedVersion = versionMapKey[i].toString();
                        flag = 1;
                    }
                }
            }
        }
    }


}
