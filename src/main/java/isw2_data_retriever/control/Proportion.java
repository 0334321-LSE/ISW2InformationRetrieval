package isw2_data_retriever.control;

import isw2_data_retriever.model.BugTicket;
import isw2_data_retriever.model.Version;
import isw2_data_retriever.retriever.JiraRetriever;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class Proportion {

    private static final String[] PROJECT_NAMES = {"bookkeeper", "avro", "openjpa", "storm", "zookeeper", "syncope","tajo"};

    /** Use proportion to obtain injected version where there isn't */
    public static void proportion(List<BugTicket> bugTickets, List<Version> versionList, String projectName) throws URISyntaxException, IOException {
        BugTicket ticketInfoRetriever = new BugTicket();
        ticketInfoRetriever.setVersionInfo(bugTickets, versionList);
        //ticketInfoRetriever.printVersionInformationList(bugTickets);

        System.out.println("\n ----------------------------------------------");
        System.out.println("Using cold start to obtains proportion value");
        double proportionValue = calculateProportionValueWithColdStart(projectName);
        System.out.println("\n ----------------------------------------------");
        System.out.println("The proportion value from cold start is --> "+proportionValue);
        proportionForInjectVersion(bugTickets,proportionValue, versionList);

        //ticketInfoRetriever.printVersionInformationList(bugTickets);
        //System.out.println("\n"+projectName.toUpperCase()+" ticket number: "+bugTickets.size());

        // I discard the invalid tickets after the end of proportion
        discardInvalidTicket(bugTickets, versionList);
    }

    /** Discard malformed ticket */
   private static void discardInvalidTicket(List<BugTicket> bugTicketsList, List<Version> versionList){
       //RemoveIF
       bugTicketsList.removeIf(bugTicket-> bugTicket.getTicketsCreationDate().isBefore(versionList.get(0).getVersionDate()));
       //RemoveIF there are some tickets without OV or FV
       bugTicketsList.removeIf(bugTicket -> bugTicket.getOpeningVersion().getVersionName().equals("NULL") || bugTicket.getFixedVersion().getVersionName().equals("NULL"));
       //RemoveIF there are some tickets with FV == OV == First Version
       bugTicketsList.removeIf(bugTicket-> bugTicket.getOpeningVersion().getVersionName().equals(versionList.get(0).getVersionName()) && bugTicket.getFixedVersion().getVersionName().equals(versionList.get(0).getVersionName()));
       //RemoveIF IV >= OV
       bugTicketsList.removeIf(bugTicket->bugTicket.getInjectedVersion().getVersionInt() >= bugTicket.getOpeningVersion().getVersionInt());
       //RemoveIF IV > FV
       bugTicketsList.removeIf(bugTicket->bugTicket.getInjectedVersion().getVersionInt() > bugTicket.getFixedVersion().getVersionInt());
       //RemoveIF OV > FV
       bugTicketsList.removeIf(bugTicket->bugTicket.getOpeningVersion().getVersionInt() > bugTicket.getFixedVersion().getVersionInt());

   }

    /** Drop tickets not usefully for proportion  */
    private static List<BugTicket> correctBugTicketListForProportioning(List<BugTicket> bugTicketsList, List<Version> versionList){

        List<BugTicket> BugTicketListForProportion =  new ArrayList<>(bugTicketsList);
        //Zeroth control, remove if IV is null
        BugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getInjectedVersion().getVersionName().equals("NULL"));
        //First control, cut off tickets which have IV >= OV
        BugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getInjectedVersion().getVersionInt() >= bugTicketProp.getOpeningVersion().getVersionInt());
        //Second control, cut off tickets which have IV>= FV
        BugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getInjectedVersion().getVersionInt() >= bugTicketProp.getFixedVersion().getVersionInt());
        //Third control, cut off tickets which have OV>= FV
        BugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getOpeningVersion().getVersionInt() >= bugTicketProp.getFixedVersion().getVersionInt());
        //Fourth control, cut off tickets which have IV = OV = FV
        BugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getOpeningVersion().getVersionInt() == bugTicketProp.getFixedVersion().getVersionInt() && bugTicketProp.getInjectedVersion().getVersionInt() == bugTicketProp.getFixedVersion().getVersionInt());

       /* Print to check the bug ticket list :
        System.out.println("\nBug ticket list for proportioning: ");
        printVersionInformationList(correctBugTicketList);
        System.out.println("\n---------------------------------------------------------------------------");
        System.out.println("\nBug ticket consistency correctly checked; the remain number of ticket is: "+correctBugTicketList.size());
        */
        return BugTicketListForProportion;
    }

    private static double calculateProportioningCoefficient(List<BugTicket> bugTicketsListForProportion, List<Version> versionList){

        ArrayList<Double> proportionValues = new ArrayList<>();
        for( BugTicket bugTicket: bugTicketsListForProportion){
            // P = (FV-IV)/(FV-OV)
            proportionValues.add((double) (bugTicket.getFixedVersion().getVersionInt() - bugTicket.getInjectedVersion().getVersionInt()) / (bugTicket.getFixedVersion().getVersionInt() - bugTicket.getOpeningVersion().getVersionInt()));
        }
        return  obtainMedian(proportionValues);
    }

    private static double obtainMedian(List<Double> proportionValues){
        if(proportionValues.size()%2 != 0)
            //if is odd ( n/2 )
            return proportionValues.get((proportionValues.size()+1)/2);
        else
            //if is even ( n/2 + n/2+1 ) /2
            return (proportionValues.get(proportionValues.size()/2)+ proportionValues.get((proportionValues.size()/2)+1) )/2 ;
    }


    private static void proportionForInjectVersion(List<BugTicket> bugTickets,double proportionValueFromColdStart, List<Version> versionList){

        int iv;

        for( BugTicket bugTicket: bugTickets){

            if (bugTicket.getInjectedVersion().getVersionName().equals("NULL") ){
                //Apply proportion: IV= FV-(FV-OV)*P
                if( bugTicket.getFixedVersion().getVersionInt() != (bugTicket.getOpeningVersion().getVersionInt()))
                    iv = (int) (bugTicket.getFixedVersion().getVersionInt() - ((bugTicket.getFixedVersion().getVersionInt() - (bugTicket.getOpeningVersion().getVersionInt())) * proportionValueFromColdStart));
                // in this case IV = FV-P
                else
                    iv = (int) ( bugTicket.getFixedVersion().getVersionInt() - proportionValueFromColdStart);

                if (iv <= 1){
                    //If the calculated IV is <= 1, the injected is the first version.
                    bugTicket.setInjectedVersion(versionList.get(1));
                    continue;
                }

                innerFor:for ( Version version: versionList){
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

                List<Version> versionList = retriever.retrieveVersions(project) ;

                List<BugTicket> bugTickets = retriever.retrieveBugTicket(project, versionList) ;

                BugTicket ticketInfoRetriever = new BugTicket();
                ticketInfoRetriever.setVersionInfo(bugTickets, versionList);
                List<BugTicket> bugTicketsForProportion = correctBugTicketListForProportioning(bugTickets, versionList);
                localProportionValue = calculateProportioningCoefficient(bugTicketsForProportion, versionList);
                proportionValue += localProportionValue;
                System.out.println("\nProportion value for "+project.toUpperCase()+" is--> "+localProportionValue);

            }
        }
        return proportionValue / (PROJECT_NAMES.length-1);
    }

}
