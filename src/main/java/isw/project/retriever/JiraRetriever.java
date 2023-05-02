package isw.project.retriever;

import isw.project.model.Version;
import isw.project.util.URLBuilder;
import isw.project.model.BugTicket;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;

public class JiraRetriever {

    private static final Logger LOGGER = Logger.getLogger(JiraRetriever.class.getName());

    //todo commenta tutti i metodi con il loro scopo
    /** This method return a list that contains all bug tickets from jira*/
    public List<BugTicket> retrieveBugTicket(String projectName , List<Version> versionList) throws IOException, URISyntaxException {
        List<BugTicket> bugTickets = new ArrayList<>();
        URLBuilder urlBuilder = new URLBuilder() ;
        String urlFirstPart = urlBuilder.buildUrl(projectName) ;
        int startPoint = 0 ;
        int maxAmount = 500 ;
        int issuesNumber;

        //Create JSON file with Jira bug tickets
        FileWriter file = new FileWriter("./retrieved_data/projectsTickets/"+projectName+"JiraTicket.json");

        ArrayList<String> issuesKeys = new ArrayList<>();
        ArrayList<LocalDate> ticketsCreationDate = new ArrayList<>();
        ArrayList<LocalDate> ticketsResolutionDate = new ArrayList<>();
        ArrayList<String> affectedVersion = new ArrayList<>();

        do {
            String urlString = urlBuilder.completeUrl(startPoint, maxAmount, urlFirstPart) ;

            URI uri2 = new URI(urlString) ;
            URL url2 = uri2.toURL() ;

            String jsonString = getJsonString(url2) ;
            file.write(jsonString+"\n");

            JSONObject jsonObject =  new JSONObject(jsonString) ;
            issuesNumber = parseInt(jsonObject.get("total").toString());

            JSONArray jsonIssueArray = jsonObject.getJSONArray("issues") ;


            parseIssuesArray(issuesKeys, jsonIssueArray) ;
            parseCreationDate(ticketsCreationDate, jsonIssueArray);
            parseResolutionDate(ticketsResolutionDate,jsonIssueArray);
            parseAffectedVersion(affectedVersion, jsonIssueArray, versionList);


            startPoint = startPoint + maxAmount ;
        } while (startPoint < issuesNumber ) ;

        file.close();
        try {
            if(!issuesKeys.isEmpty() && !ticketsCreationDate.isEmpty() && !ticketsResolutionDate.isEmpty())
            {
                LOGGER.info("\n---------------------------------------------------------------------------"+
                        "\n"+projectName.toUpperCase()+" issue tickets acquired");
                Version affectedV;

                for(int i=0; i< issuesKeys.size();i++){
                    affectedV = Version.getVersionInfoFromName(affectedVersion.get(i), versionList);
                    BugTicket bugTicket = new BugTicket(issuesKeys.get(i), ticketsCreationDate.get(i), ticketsResolutionDate.get(i), affectedV);
                    bugTickets.add(bugTicket);
                }

            } else  {throw new Exception("Error during ticket acquisition");}
        }catch (Exception e){
            LOGGER.info("Somethings went wrong with issue tickets acquisition");
        }

        return bugTickets;
    }

/**
    Obtains all the existing version of the project from jira
*/
    public List<Version> retrieveVersions(String projectName) throws URISyntaxException, IOException {
        String urlString = "https://issues.apache.org/jira/rest/api/2/project/" + projectName.toUpperCase();
        URI uri = new URI(urlString);
        URL url = uri.toURL();

        String jsonString = getJsonString(url);
        JSONObject jsonObject = new JSONObject((jsonString));
        JSONArray jsonVersionArray = jsonObject.getJSONArray("versions");

        List<Version> versionList = new ArrayList<>();
        for (int i = 0; i < jsonVersionArray.length(); i++) {
            String versionName = "";
            String dateString = "";
            String versionId = "" ;
            if (jsonVersionArray.getJSONObject(i).has("releaseDate") && jsonVersionArray.getJSONObject(i).has("name") && jsonVersionArray.getJSONObject(i).has("id")) {
                versionName = jsonVersionArray.getJSONObject(i).get("name").toString();
                dateString = jsonVersionArray.getJSONObject(i).get("releaseDate").toString();
                versionId = jsonVersionArray.getJSONObject(i).get("id").toString() ;

                LocalDate versionDate = LocalDate.parse(dateString) ;
                Version version = new Version(versionName, versionDate, versionId, i+1) ;
                versionList.add(version) ;
            }
        }
        LocalDate nullVersionDate = LocalDate.parse("1900-01-01");
        versionList.add( new Version("NULL",nullVersionDate,"nullversion",0));
        versionList.sort(Comparator.comparing(Version::getVersionDate));

        return versionList;
    }

    /** Return issues key in a list*/
    public List<String> getIssueKeyList(@NotNull List<BugTicket> bugTicketsList){
        List<String> issueKeyList = new ArrayList<>();
        for (BugTicket bugTicket: bugTicketsList){
            issueKeyList.add(bugTicket.getIssueKey());
        }
        return issueKeyList;
    }

    /** Return String from the contacted url */
    private String getJsonString(URL url) throws IOException {
        try (InputStream urlInput = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlInput)) ;
            StringBuilder builder = new StringBuilder() ;

            int c ;
            while ( (c = reader.read()) != -1) {
                builder.append((char) c) ;
            }

            return builder.toString() ;
        }
    }

    /** Obtains issueKeys from json Array*/
    private void parseIssuesArray(ArrayList<String> issuesKeys, JSONArray jsonArray) {
        for (int i = 0 ; i < jsonArray.length() ; i++) {
            issuesKeys.add(jsonArray.getJSONObject(i).get("key").toString()) ;
        }
    }

    /** Obtains creationDate from json Array*/
    private void parseCreationDate(ArrayList<LocalDate> ticketsCreationDate, JSONArray jsonArray){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d");
        for (int i = 0 ; i < jsonArray.length() ; i++) {
            JSONObject fields = (JSONObject) jsonArray.getJSONObject(i).get("fields");
            String dateString = fields.get("created").toString();
            dateString = dateString.split("T")[0];
            LocalDate localDate = LocalDate.parse(dateString, formatter);
            ticketsCreationDate.add(localDate);
        }
    }

    /** Obtains resolutionDate from json Array*/
    private void parseResolutionDate(ArrayList<LocalDate> ticketsResolutionDate, JSONArray jsonArray){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d");
        for (int i = 0 ; i < jsonArray.length() ; i++) {
            JSONObject fields = (JSONObject) jsonArray.getJSONObject(i).get("fields");
            String dateString = fields.get("resolutiondate").toString();
            dateString = dateString.split("T")[0];
            LocalDate localDate = LocalDate.parse(dateString, formatter);
            ticketsResolutionDate.add(localDate);
        }
    }

   /** Obtains affectedVersion from json Array*/
    private void parseAffectedVersion(ArrayList<String> affectedVersion, JSONArray jsonArray, List<Version> versionList){
        Version mapGenerator = new Version();
        Map<String,Integer> versionMap = mapGenerator.getVersionInteger(versionList);

        for (int i =0; i < jsonArray.length(); i++){
            JSONObject fields = (JSONObject) jsonArray.getJSONObject(i).get("fields");
            JSONArray versionArray = (JSONArray) fields.get("versions");
            if( versionArray.length() != 0) {
                if (versionMap.containsKey(versionArray.getJSONObject(0).get("name").toString())){
                    affectedVersion.add(versionArray.getJSONObject(0).get("name").toString());
                }
                else
                    affectedVersion.add("NULL");

            }
            else{
                affectedVersion.add("NULL");
            }
        }
    }

    public void printVersionList(List<Version> versionList){
        for (Version info: versionList)
            info.printVersionInfo();
    }


}
