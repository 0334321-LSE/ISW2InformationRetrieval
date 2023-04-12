package ISW2.DataRetriever;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JiraRetriever {

    public List<BugTicket> retrieveBugTicket(String projectName) throws IOException, URISyntaxException {
        List<BugTicket> bugTickets = new ArrayList<>();
        URLBuilder urlBuilder = new URLBuilder() ;
        String urlFirstPart = urlBuilder.buildUrl(projectName) ;
        int startPoint = 0 ;
        int maxAmount = 500 ;
        int issuesNumber;

        //Create JSON file with Jira bug tickets
        FileWriter file = new FileWriter("./"+projectName+"JiraTicket.json");
        String urlString = urlBuilder.completeUrl(startPoint, maxAmount, urlFirstPart) ;
        URI uri = new URI(urlString) ;
        URL url = uri.toURL() ;
        String jsonString = getJsonString(url) ;
        if(!jsonString.isEmpty()){
            System.out.println(projectName.toUpperCase()+" Jira bug tickets acquired");
        }
        file.write(jsonString+"\n");
        file.close();

        ArrayList<String> issuesKeys = new ArrayList<>();
        ArrayList<LocalDate> ticketsCreationDate = new ArrayList<>();
        ArrayList<LocalDate> ticketsResolutionDate = new ArrayList<>();
        ArrayList<String> affectedVersion = new ArrayList<>();

         do {
            urlString = urlBuilder.completeUrl(startPoint, maxAmount, urlFirstPart) ;
            Logger.getGlobal().log(Level.INFO, urlString);
            URI uri2 = new URI(urlString) ;
            URL url2 = uri2.toURL() ;

            jsonString = getJsonString(url2) ;
            JSONObject jsonObject = new JSONObject(jsonString) ;
            JSONArray jsonIssueArray = jsonObject.getJSONArray("issues") ;

            parseIssuesArray(issuesKeys, jsonIssueArray) ;
            parseCreationDate(ticketsCreationDate, jsonIssueArray);
            parseResolutionDate(ticketsResolutionDate,jsonIssueArray);
            parseAffectedVersion(affectedVersion, jsonIssueArray);

            issuesNumber = jsonIssueArray.length() ;
            startPoint = startPoint + maxAmount ;
        } while (issuesNumber == 0) ;
        try {
            if(!issuesKeys.isEmpty() && !ticketsCreationDate.isEmpty() && !ticketsResolutionDate.isEmpty())
            {
                System.out.println(projectName.toUpperCase()+" issue tickets acquired");
                for(int i=0; i< issuesKeys.size();i++){
                    BugTicket bugTicket = new BugTicket(issuesKeys.get(i), ticketsCreationDate.get(i), ticketsResolutionDate.get(i), affectedVersion.get(i));
                    bugTickets.add(bugTicket);
                }
            }
        }catch (Exception e){
            System.out.println("Somethings went wrong issue tickets acquisition");
        }
        return bugTickets;
    }

    public List<VersionInfo> retrieveVersions(String projectName) throws URISyntaxException, IOException {
        String urlString = "https://issues.apache.org/jira/rest/api/2/project/" + projectName.toUpperCase();
        URI uri = new URI(urlString);
        URL url = uri.toURL();

        String jsonString = getJsonString(url);
        JSONObject jsonObject = new JSONObject((jsonString));
        JSONArray jsonVersionArray = jsonObject.getJSONArray("versions");

        List<VersionInfo> versionInfoList = new ArrayList<>();
        for (int i = 0; i < jsonVersionArray.length(); i++) {
            String versionName = "";
            String dateString = "";
            String versionId = "" ;
            if (jsonVersionArray.getJSONObject(i).has("releaseDate") && jsonVersionArray.getJSONObject(i).has("name") && jsonVersionArray.getJSONObject(i).has("id")) {
                versionName = jsonVersionArray.getJSONObject(i).get("name").toString();
                dateString = jsonVersionArray.getJSONObject(i).get("releaseDate").toString();
                versionId = jsonVersionArray.getJSONObject(i).get("id").toString() ;

                LocalDate versionDate = LocalDate.parse(dateString) ;
                VersionInfo versionInfo = new VersionInfo(versionName, versionDate, versionId) ;
                versionInfoList.add(versionInfo) ;
            }
        }

        versionInfoList.sort(Comparator.comparing(VersionInfo::getVersionDate));

        for (VersionInfo info : versionInfoList) {
            Logger.getGlobal().log(Level.INFO, "Version >> " + info.getVersionName()+ "Date >> " + info.getVersionDate());
        }

        return versionInfoList ;
    }

    public List<String> getIssueKeyList(List<BugTicket> bugTicketsList){
        List<String> issueKeyList = new ArrayList<>();
        for (BugTicket bugTicket: bugTicketsList){
            issueKeyList.add(bugTicket.getIssueKey());
        }
        return issueKeyList;
    }


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

    private void parseIssuesArray(ArrayList<String> issuesKeys, JSONArray jsonArray) {
        for (int i = 0 ; i < jsonArray.length() ; i++) {
            issuesKeys.add(jsonArray.getJSONObject(i).get("key").toString()) ;
        }
    }

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

    private void parseAffectedVersion(ArrayList<String> affectedVersion, JSONArray jsonArray){

        for (int i =0; i < jsonArray.length(); i++){
            JSONObject fields = (JSONObject) jsonArray.getJSONObject(i).get("fields");
            JSONArray versionArray = (JSONArray) fields.get("versions");
            if( versionArray.length() != 0){
                affectedVersion.add(versionArray.getJSONObject(0).get("name").toString());
            }else{
                affectedVersion.add("NULL");
            }
        }
    }

    public void printVersionList(List<VersionInfo> versionInfoList){
        for (VersionInfo info: versionInfoList)
            info.printVersionInfo();
    }


}
