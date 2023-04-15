package ISW2.DataRetriever;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class Proportion {
    //TODO put here all the methods for proportion and create a specific method "proportion" that use cold start if the usable tickets are <5
    private static final String[] PROJECT_NAMES = {"bookkeeper", "avro", "openjpa", "storm", "zookeeper", "syncope","tajo"};
    private static double calculateProportionValueWithColdStart(String projectName) throws URISyntaxException, IOException {
        JiraRetriever retriever = new JiraRetriever() ;
        double proportionValue = 0;
        for (String project: PROJECT_NAMES){
            if(!project.equals(projectName)){

                List<VersionInfo> versionInfoList = retriever.retrieveVersions(project) ;

                List<BugTicket> bugTickets = retriever.retrieveBugTicket(project,versionInfoList) ;

                BugTicket ticketInfoRetriever = new BugTicket();
                ticketInfoRetriever.setVersionInfo(bugTickets,versionInfoList);
                List<BugTicket> bugTicketsForProportion = ticketInfoRetriever.correctBugTicketListForProportioning(bugTickets,versionInfoList);
                proportionValue += ticketInfoRetriever.calculateProportioningCoefficient(bugTicketsForProportion, versionInfoList);
            }
        }
        return proportionValue / (PROJECT_NAMES.length-1);
    }

    public static void proportion(List<BugTicket> bugTickets, List<VersionInfo> versionInfoList, String projectName) throws URISyntaxException, IOException {
        BugTicket ticketInfoRetriever = new BugTicket();
        ticketInfoRetriever.setVersionInfo(bugTickets,versionInfoList);
        //ticketInfoRetriever.printVersionInformationList(bugTickets);
        List<BugTicket> bugTicketsForProportion = ticketInfoRetriever.correctBugTicketListForProportioning(bugTickets,versionInfoList);

        if(bugTicketsForProportion.size()>5)
            ticketInfoRetriever.proportionForInjectedVersion(bugTickets,bugTicketsForProportion,versionInfoList);
        else{
            try {
                System.out.println("\n ----------------------------------------------");
                System.out.println("Tickets are not enough, use cold start to obtains proportion value");
                double proportionValue = calculateProportionValueWithColdStart(projectName);
                ticketInfoRetriever.proportionForInjectVersion(bugTickets,proportionValue,versionInfoList);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        ticketInfoRetriever.printVersionInformationList(bugTickets);
    }



}
