package isw2_data_retriever.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Version {

    private String versionName ;
    private LocalDate versionDate ;
    private String versionId ;
    private int versionInt;

    public Version(){
    }

    public Version(String versionName, LocalDate versionDate, String versionId, int versionInt) {
        this.versionName = versionName ;
        this.versionDate = versionDate ;
        this.versionId = versionId ;
        this.versionInt = versionInt;
    }

    public int getVersionInt() {
        return versionInt;
    }

    public void setVersionInt(int versionInt) {
        this.versionInt = versionInt;
    }

    public LocalDate getVersionDate() {
        return versionDate;
    }

    public void setVersionDate(LocalDate versionDate) {
        this.versionDate = versionDate;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void printVersionInfo(){
        System.out.println("\n ----------------------------------------------");
        System.out.print("| Name: "+this.versionName);
        System.out.print(" | Date: "+this.versionDate);
        System.out.print(" | ID: "+this.versionId+" |");
    }

    public static Version getVersionInfoFromName (String name, List<Version> list){
            for (Version version: list){
                if( version.getVersionName().equals(name))
                    return version;
            }
            //if it doesn't find anything return null version
            return list.get(0);
    }

    public Map<String, Integer> getVersionInteger(List<Version> versionList){
        Map<String,Integer> versionMap = new LinkedHashMap<>();
        int i=0;
        versionMap.put("NULL",i);
        for ( Version info: versionList){
            i++;
            versionMap.put(info.versionName,i);
        }
        return versionMap;
    }

    public static Version getLastVersion (List<Version> versionList){
        return versionList.get(versionList.size()-1);
    }
    public static Version getVersionOfCommit(RevCommit commit, List<VersionInfo> CommitsAssociatedWithVersion){
        for(VersionInfo versionInfo : CommitsAssociatedWithVersion) {
            for(RevCommit c : versionInfo.getCommitList()) {
                if(c.equals(commit)) {
                    return versionInfo.getVersion();
                }

            }

        }
        return null;
    }
}
