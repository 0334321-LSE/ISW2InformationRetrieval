package isw.project.control;

import isw.project.model.Version;
import isw.project.retriever.JiraRetriever;
import isw.project.model.BugTicket;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Proportion {
    //This private constructor is meant to hide the public one: classes with only static methods do not have to be instantiated.
    private Proportion() {
        throw new IllegalStateException("This class does not have to be instantiated.");
    }

    private static final Logger LOGGER = Logger.getLogger(Proportion.class.getName());

    private static final String[] PROJECT_NAMES = {"bookkeeper", "avro", "openjpa", "zookeeper", "syncope","tajo"};

    /** Use proportion to obtain injected version where there isn't */
    public static void proportion(List<BugTicket> bugTickets, List<Version> versionList, String projectName) throws URISyntaxException, IOException {

        BugTicket.setVersionInfo(bugTickets, versionList);

        LOGGER.log(Level.INFO, ()->("%n ----------------------------------------------%nUsing cold start to obtains proportion value"));
        List<Double> proportionValues = calculateProportionValueWithColdStart(projectName);
        Collections.sort(proportionValues);
        LOGGER.log(Level.INFO, ()->String.format("%n ----------------------------------------------%nThe array of proportion values is --->%s%nThe proportion value from cold start is -->%s",proportionValues,obtainMedian(proportionValues)));
        proportionOnInjectVersion(bugTickets,obtainMedian(proportionValues), versionList);
        //Discard the invalid tickets after the end of proportion
        discardInvalidTicket(bugTickets, versionList);

    }

    /** Discard malformed ticket */
   private static void discardInvalidTicket(List<BugTicket> bugTicketsList, List<Version> versionList){
       //RemoveIF
       bugTicketsList.removeIf(bugTicket-> bugTicket.getCreationDate().isBefore(versionList.get(0).getVersionDate()));
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
    private static List<BugTicket> correctBugTicketListForProportioning(List<BugTicket> bugTicketsList){

        List<BugTicket> bugTicketListForProportion =  new ArrayList<>(bugTicketsList);
        //Zeroth control, remove if IV is null
        bugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getInjectedVersion().getVersionName().equals("NULL"));
        //First control, cut off tickets which have IV >= OV
        bugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getInjectedVersion().getVersionInt() >= bugTicketProp.getOpeningVersion().getVersionInt());
        //Second control, cut off tickets which have IV>= FV
        bugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getInjectedVersion().getVersionInt() >= bugTicketProp.getFixedVersion().getVersionInt());
        //Third control, cut off tickets which have OV>= FV
        bugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getOpeningVersion().getVersionInt() > bugTicketProp.getFixedVersion().getVersionInt());
        //Fourth control, cut off tickets which have IV = OV = FV
        bugTicketListForProportion.removeIf(bugTicketProp->bugTicketProp.getOpeningVersion().getVersionInt() == bugTicketProp.getFixedVersion().getVersionInt() && bugTicketProp.getInjectedVersion().getVersionInt() == bugTicketProp.getFixedVersion().getVersionInt());

        return bugTicketListForProportion;
    }

    /** Calculate proportion coefficient for each apache project and then return the average */
    private static double calculateProportioningCoefficient(List<BugTicket> bugTicketsListForProportion){

        ArrayList<Double> proportionValues = new ArrayList<>();
        for( BugTicket bugTicket: bugTicketsListForProportion){
           if((bugTicket.getFixedVersion().getVersionInt() == bugTicket.getOpeningVersion().getVersionInt()))
                // P = (FV-IV)
                proportionValues.add((double) (bugTicket.getFixedVersion().getVersionInt() - bugTicket.getInjectedVersion().getVersionInt()));
            else
                // P = (FV-IV)/(FV-OV)
                proportionValues.add((double) (bugTicket.getFixedVersion().getVersionInt() - bugTicket.getInjectedVersion().getVersionInt()) / (bugTicket.getFixedVersion().getVersionInt() - bugTicket.getOpeningVersion().getVersionInt()));
        }
        return  obtainAverage(proportionValues);
    }

    /** Return average of proportionValues */
    private static double obtainAverage(List<Double> proportionValues){
        double sum = 0;
        for ( Double value: proportionValues)
            sum += value;

        return sum/proportionValues.size();
    }
    /**Return the median of proportionValues */
    private static double obtainMedian(List<Double> proportionValues){
        int size =proportionValues.size();
        if(proportionValues.size()%2 != 0)
            //if is odd ( n/2 )
            return proportionValues.get(((size+1)/2)-1);
        else
            //if is even ( n/2 + n/2+1 ) /2
            return (proportionValues.get((size/2)) + proportionValues.get((size/2)+1)) /2;
    }

    /** For each bug ticket, use proportion coefficient to calculate IV */
    private static void proportionOnInjectVersion(List<BugTicket> bugTickets, double proportionValueFromColdStart, List<Version> versionList){

        for( BugTicket bugTicket: bugTickets){
            setInjected(bugTicket,proportionValueFromColdStart,versionList);
        }
    }

    /** Calculate the IV and then set it */
    private static  void setInjected(BugTicket bugTicket, double proportionValueFromColdStart, List<Version> versionList){
        int iv;
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
                return;
            }

            for (Version version: versionList){
                if (version.getVersionInt() == iv){
                    bugTicket.setInjectedVersion(version);
                    return;
                }
            }
        }
    }

    /** Evaluates the proportion value using cold start technique using all the other projects */
    private static List<Double> calculateProportionValueWithColdStart(String projectName) throws URISyntaxException, IOException {
        JiraRetriever retriever = new JiraRetriever() ;
        List<Double> proportionValue = new ArrayList<>();
        double localProportionValue;
        for (String project: PROJECT_NAMES){
            if(!project.equals(projectName)){

                List<Version> versionList = retriever.retrieveVersions(project) ;

                List<BugTicket> bugTickets = retriever.retrieveBugTicket(project, versionList) ;

                BugTicket.setVersionInfo(bugTickets, versionList);
                List<BugTicket> bugTicketsForProportion = correctBugTicketListForProportioning(bugTickets);
                localProportionValue = calculateProportioningCoefficient(bugTicketsForProportion);
                proportionValue.add(localProportionValue);
                double finalLocalProportionValue = localProportionValue;
                LOGGER.log(Level.INFO, ()->String.format("%nProportion value for %s is--> %s%n",project.toUpperCase(), finalLocalProportionValue));

            }
        }
        return proportionValue;
    }

}
