package ISW2.DataRetriever;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Proportion {

    private static final String[] PROJECT_NAMES = {"bookkeeper", "avro", "openjpa", "storm", "zookeeper", "syncope","tajo"};

    /** Use proportion to obtain injected version where there isn't */
    public static void proportion(List<BugTicket> bugTickets, List<VersionInfo> versionInfoList, String projectName){
        BugTicket ticketInfoRetriever = new BugTicket();
        ticketInfoRetriever.setVersionInfo(bugTickets,versionInfoList);
        //ticketInfoRetriever.printVersionInformationList(bugTickets);
        List<BugTicket> bugTicketsForProportion = correctBugTicketListForProportioning(bugTickets,versionInfoList);

        if(bugTicketsForProportion.size()>5){
            System.out.println("\nRemaining ticket for proportion: "+bugTicketsForProportion.size());
            proportionForInjectedVersion(bugTickets,projectName,bugTicketsForProportion,versionInfoList);
        }
        else{
            try {
                System.out.println("\n ----------------------------------------------");
                System.out.println("Tickets are not enough, using cold start to obtains proportion value");
                double proportionValue = calculateProportionValueWithColdStart(projectName);
                System.out.println("\n ----------------------------------------------");
                System.out.println("The proportion value from cold start is --> "+proportionValue);
                proportionForInjectVersion(bugTickets,proportionValue,versionInfoList);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        //ticketInfoRetriever.printVersionInformationList(bugTickets);
        //System.out.println("\n"+projectName.toUpperCase()+" ticket number: "+bugTickets.size());

        //TODO ask friends why isn't good to remove tickets with OV=IV
        bugTickets.removeIf(bugTicket-> bugTicket.getOpeningVersion().equals(bugTicket.getInjectedVersion()));
    }
    //TODO if u want add index attribute to version info to eliminate map

    /** Drop tickets not usefully for proportion  */
    private static List<BugTicket> correctBugTicketListForProportioning(List<BugTicket> bugTicketsList, List<VersionInfo> versionInfoList){

        //RemoveIF there are some tickets without OV or FV
        bugTicketsList.removeIf(bugTicket -> bugTicket.getOpeningVersion().getVersionName().equals("NULL") || bugTicket.getFixedVersion().getVersionName().equals("NULL"));
        //RemoveIF there are some tickets with IV == OV == First Version
        //bugTicketsList.removeIf(bugTicket-> bugTicket.getOpeningVersion().getVersionName().equals(versionInfoList.get(0).getVersionName()));
        List<BugTicket> correctBugTicketList =  new ArrayList<>(bugTicketsList);

        for ( BugTicket bugTicket: bugTicketsList){
            if ( bugTicket.getInjectedVersion().getVersionName().equals("NULL"))
                correctBugTicketList.remove(bugTicket);

            else if ( bugTicket.getInjectedVersion().getVersionInt() >= bugTicket.getOpeningVersion().getVersionInt())
                //First control, cut off tickets which have IV > OV
                correctBugTicketList.remove(bugTicket);

            else if ( bugTicket.getInjectedVersion().getVersionInt() >= bugTicket.getFixedVersion().getVersionInt())
                //Second control, cut off tickets which have IV>= FV
                correctBugTicketList.remove(bugTicket);

            else if (bugTicket.getOpeningVersion().getVersionInt() >= bugTicket.getFixedVersion().getVersionInt())
                //Third control, cut off tickets which have OV> FV
                correctBugTicketList.remove(bugTicket);

            else if (bugTicket.getInjectedVersion().getVersionName().equals(bugTicket.getOpeningVersion().getVersionName()) && bugTicket.getFixedVersion().getVersionName().equals(bugTicket.getOpeningVersion().getVersionName()))
                //Fourth control, cut off tickets which have IV = OV = FV
                correctBugTicketList.remove(bugTicket);

        }

       /* Print to check the but ticket list :
        System.out.println("\nBug ticket list for proportioning: ");
        printVersionInformationList(correctBugTicketList);
        System.out.println("\n---------------------------------------------------------------------------");
        System.out.println("\nBug ticket consistency correctly checked; the remain number of ticket is: "+correctBugTicketList.size());
        */return correctBugTicketList;
    }

    private static double calculateProportioningCoefficient(List<BugTicket> bugTicketsListForProportion, List<VersionInfo> versionInfoList){

        double proportionValue = 0;
        for( BugTicket bugTicket: bugTicketsListForProportion){
            // P = (FV-IV)/(FV-OV)
            proportionValue += ( (double) (bugTicket.getFixedVersion().getVersionInt() - bugTicket.getInjectedVersion().getVersionInt()) / (bugTicket.getFixedVersion().getVersionInt() - bugTicket.getOpeningVersion().getVersionInt()));
        }
        return   proportionValue / bugTicketsListForProportion.size();
    }

    private static void proportionForInjectedVersion(@NotNull List<BugTicket> bugTickets,String projectName ,List<BugTicket> bugTicketsListForProportion, List<VersionInfo> versionInfoList){
        int iv;

        double proportionValue = calculateProportioningCoefficient(bugTicketsListForProportion,versionInfoList);
        System.out.println("\nProportion value for "+ projectName.toUpperCase()+" is--> "+proportionValue);
        for(BugTicket bugTicket: bugTickets){

            if (bugTicket.getInjectedVersion().getVersionName().equals("NULL") ){
                //Apply proportion: IV= FV-(FV-OV)*P
                iv = (int) (bugTicket.getFixedVersion().getVersionInt() - ((bugTicket.getFixedVersion().getVersionInt() - (bugTicket.getOpeningVersion().getVersionInt())) * proportionValue));
                if (iv <= 1){
                    //If the calculated IV is <= 1, the injected is the first version.
                    bugTicket.setInjectedVersion(versionInfoList.get(1));
                    continue;
                }

                innerFor:for ( VersionInfo version: versionInfoList ){
                    if (version.getVersionInt() == iv){
                        bugTicket.setInjectedVersion(version);
                        break innerFor;
                    }
                }
            }
        }
    }

    private static void proportionForInjectVersion(List<BugTicket> bugTickets,double proportionValueFromColdStart, List<VersionInfo> versionInfoList){

        int iv;

        for( BugTicket bugTicket: bugTickets){

            if (bugTicket.getInjectedVersion().getVersionName().equals("NULL") ){
                //Apply proportion: IV= FV-(FV-OV)*P
                iv = (int) (bugTicket.getFixedVersion().getVersionInt() - ((bugTicket.getFixedVersion().getVersionInt() - (bugTicket.getOpeningVersion().getVersionInt())) * proportionValueFromColdStart));
                if (iv <= 1){
                    //If the calculated IV is <= 1, the injected is the first version.
                    bugTicket.setInjectedVersion(versionInfoList.get(1));
                    continue;
                }

                innerFor:for ( VersionInfo version: versionInfoList ){
                    if (version.getVersionInt() == iv){
                        bugTicket.setInjectedVersion(version);
                        break innerFor;
                    }
                }
            }
        }
    }

    private static double calculateProportionValueWithColdStart(String projectName) throws URISyntaxException, IOException {
        JiraRetriever retriever = new JiraRetriever() ;
        double proportionValue = 0;
        double localProportionValue;
        for (String project: PROJECT_NAMES){
            if(!project.equals(projectName)){

                List<VersionInfo> versionInfoList = retriever.retrieveVersions(project) ;

                List<BugTicket> bugTickets = retriever.retrieveBugTicket(project,versionInfoList) ;

                BugTicket ticketInfoRetriever = new BugTicket();
                ticketInfoRetriever.setVersionInfo(bugTickets,versionInfoList);
                List<BugTicket> bugTicketsForProportion = correctBugTicketListForProportioning(bugTickets,versionInfoList);
                localProportionValue = calculateProportioningCoefficient(bugTicketsForProportion, versionInfoList);
                proportionValue += localProportionValue;
                System.out.println("\nProportion value for "+project.toUpperCase()+" is--> "+localProportionValue);

            }
        }
        return proportionValue / (PROJECT_NAMES.length-1);
    }

}
