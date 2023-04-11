package ISW2.DataRetriever;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BugTicket {

    public BugTicket(String issueKey, LocalDate ticketsCreationDate, LocalDate ticketsResolutionDate){
        this.issueKeys = issueKey;
        this.ticketsCreationDate = ticketsCreationDate;
        this.ticketsResolutionDate = ticketsResolutionDate;
    }

    private final String issueKeys;
    private final LocalDate ticketsCreationDate;
    private final LocalDate ticketsResolutionDate;


    public String getIssueKey(){
        return this.issueKeys;
    }
    public LocalDate getTicketsCreationDate(){
        return this.ticketsCreationDate;
    }
    public LocalDate getTicketsResolutionDate(){
        return this.ticketsResolutionDate;
    }



}
