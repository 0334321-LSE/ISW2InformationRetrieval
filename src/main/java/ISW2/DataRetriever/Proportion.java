package ISW2.DataRetriever;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Proportion {

    private static final String[] PROJECT_NAMES = {"bookkeeper", "avro", "openjpa", "storm", "zookeeper", "syncope","tajo"};

    public static void proportion(List<BugTicket> bugTickets, List<VersionInfo> versionInfoList, String projectName) throws URISyntaxException, IOException {
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
    }

    private static List<BugTicket> correctBugTicketListForProportioning(List<BugTicket> bugTicketsList, List<VersionInfo> versionInfoList){
        VersionInfo mapGenerator = new VersionInfo();
        Map<String,Integer> versionMap = mapGenerator.getVersionInteger(versionInfoList);

        //Check if there are some tickets without OV or FV
        bugTicketsList.removeIf(bugTicket -> bugTicket.getOpeningVersion().equals("NULL") || bugTicket.getFixedVersion().equals("NULL"));

        List<BugTicket> correctBugTicketList =  new ArrayList<>(bugTicketsList);

        for ( BugTicket bugTicket: bugTicketsList){
            if ( bugTicket.getInjectedVersion().equals("NULL"))
                correctBugTicketList.remove(bugTicket);

            else if ( versionMap.get(bugTicket.getInjectedVersion()) >= versionMap.get(bugTicket.getOpeningVersion()))
                //First control, cut off tickets which have IV > OV
                correctBugTicketList.remove(bugTicket);

            else if (versionMap.get( bugTicket.getInjectedVersion()) >= versionMap.get(bugTicket.getFixedVersion()))
                //Second control, cut off tickets which have IV>= FV
                correctBugTicketList.remove(bugTicket);

            else if (versionMap.get(bugTicket.getOpeningVersion()) >= versionMap.get(bugTicket.getFixedVersion()))
                //Third control, cut off tickets which have OV> FV
                correctBugTicketList.remove(bugTicket);

            else if (Objects.equals(versionMap.get( bugTicket.getInjectedVersion()), versionMap.get(bugTicket.getOpeningVersion())) && Objects.equals(versionMap.get(bugTicket.getFixedVersion()), versionMap.get(bugTicket.getOpeningVersion())))
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
        VersionInfo mapGenerator = new VersionInfo();
        Map<String,Integer> versionMap = mapGenerator.getVersionInteger(versionInfoList);
        double proportionValue = 0;
        for( BugTicket bugTicket: bugTicketsListForProportion){
            // P = (FV-IV)/(FV-OV)
            proportionValue += ( (double) (versionMap.get(bugTicket.getFixedVersion()) - versionMap.get(bugTicket.getInjectedVersion())) / ((versionMap.get(bugTicket.getFixedVersion())-versionMap.get(bugTicket.getOpeningVersion()))));
        }
        return   proportionValue / bugTicketsListForProportion.size();
    }

    private static void proportionForInjectedVersion(@NotNull List<BugTicket> bugTickets,String projectName ,List<BugTicket> bugTicketsListForProportion, List<VersionInfo> versionInfoList){
        VersionInfo mapGenerator = new VersionInfo();
        Map<String,Integer> versionMap = mapGenerator.getVersionInteger(versionInfoList);
        Object[] versionMapKey = versionMap.keySet().toArray();
        int iv;
        int flag;
        double proportionValue = calculateProportioningCoefficient(bugTicketsListForProportion,versionInfoList);
        System.out.println("\nProportion value for "+ projectName.toUpperCase()+" is--> "+proportionValue);
        for( BugTicket bugTicket: bugTickets){
            flag=0;
            if (bugTicket.getInjectedVersion().equals("NULL") ){
                //Apply proportion: IV= FV-(FV-OV)*P
                iv = (int) ( (int) versionMap.get(bugTicket.getFixedVersion()) - ((versionMap.get(bugTicket.getFixedVersion()) - versionMap.get(bugTicket.getOpeningVersion())) * proportionValue));
                if (iv <= 1){
                    //If the calculated IV is <= 1, the injected is the first version.
                    bugTicket.setInjectedVersion(versionMapKey[1].toString());
                    flag = 1;
                }

                for ( int i = 0; i<versionMapKey.length && flag==0; i++ ){
                    if (versionMap.get(versionMapKey[i]) == iv){
                        bugTicket.setInjectedVersion(versionMapKey[i].toString());
                        flag = 1;
                    }
                }
            }
        }
    }

    private static void proportionForInjectVersion(List<BugTicket> bugTickets,double proportionValueFromColdStart, List<VersionInfo> versionInfoList){
        VersionInfo mapGenerator = new VersionInfo();
        Map<String,Integer> versionMap = mapGenerator.getVersionInteger(versionInfoList);
        Object[] versionMapKey = versionMap.keySet().toArray();
        int iv;
        int flag;
        for( BugTicket bugTicket: bugTickets){
            flag=0;
            if (bugTicket.getInjectedVersion().equals("NULL") ){
                //Apply proportion: IV= FV-(FV-OV)*P
                iv = (int) ( (int) versionMap.get(bugTicket.getFixedVersion()) - ((versionMap.get(bugTicket.getFixedVersion()) - versionMap.get(bugTicket.getOpeningVersion())) * proportionValueFromColdStart));
                if (iv <= 1){
                    //If the calculated IV is <= 1, the injected is the first version.
                    bugTicket.setInjectedVersion(versionMapKey[1].toString());
                    flag = 1;
                }

                for ( int i = 0; i<versionMapKey.length && flag==0; i++ ){
                    if (versionMap.get(versionMapKey[i]) == iv){
                        bugTicket.setInjectedVersion(versionMapKey[1].toString());
                        flag = 1;
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
