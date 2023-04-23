package ISW2.DataRetriever.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VersionInfo {

    private String versionName ;
    private LocalDate versionDate ;
    private String versionId ;
    private int versionInt;

    public VersionInfo(){
    }

    public VersionInfo(String versionName, LocalDate versionDate, String versionId, int versionInt) {
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

    public static VersionInfo getVersionInfoFromName (String name, List<VersionInfo> list){
            for (VersionInfo version: list){
                if( version.getVersionName().equals(name))
                    return version;
            }
            //if it doesn't find anything return null version
            return list.get(0);
    }

    public Map<String, Integer> getVersionInteger(List<VersionInfo> versionInfoList){
        Map<String,Integer> versionMap = new LinkedHashMap<>();
        int i=0;
        versionMap.put("NULL",i);
        for ( VersionInfo info: versionInfoList){
            i++;
            versionMap.put(info.versionName,i);
        }
        return versionMap;
    }

    public static VersionInfo getLastVersion (List<VersionInfo> versionInfoList){
        return versionInfoList.get(versionInfoList.size()-1);
    }
    //TODO create util classes for all model classes, put into those static method etc...
    public static VersionInfo getVersionOfCommit(RevCommit commit, List<CommitInfo> CommitsAssociatedWithVersion){
        for(CommitInfo commitInfo : CommitsAssociatedWithVersion) {
            for(RevCommit c : commitInfo.getCommitList()) {
                if(c.equals(commit)) {
                    return commitInfo.getVersionInfo();
                }

            }

        }
        return null;
    }
}
