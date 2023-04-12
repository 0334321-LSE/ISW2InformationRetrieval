package ISW2.DataRetriever;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    private void calculateProportioning(){

    }


}
