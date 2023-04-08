package ISW2.DataRetriever;

public class TicketVersionInformation {

    private final String ticketKey;

    private  String injectedVersion;

    private  String openingVersion;

    private  String fixedVersion;

    public TicketVersionInformation(String ticketKey) {
        this.ticketKey = ticketKey;
    }

    public void setInjectedVersion(String injectedVersion){
        this.injectedVersion= injectedVersion;
    }
    public void setOpeningVersion(String injectedVersion){
        this.injectedVersion= injectedVersion;
    }
    public void setFixedVersion(String injectedVersion){
        this.injectedVersion= injectedVersion;
    }

    //TODO get opening version of the tickets by searching the first-next opened commit associated
}
