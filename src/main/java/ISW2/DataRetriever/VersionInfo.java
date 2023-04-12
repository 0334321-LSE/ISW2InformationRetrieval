package ISW2.DataRetriever;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VersionInfo {

    private String versionName ;
    private LocalDate versionDate ;
    private String versionId ;

    public VersionInfo(){
    }

    public VersionInfo(String versionName, LocalDate versionDate, String versionId) {
        this.versionName = versionName ;
        this.versionDate = versionDate ;
        this.versionId = versionId ;
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
        System.out.println("\n-------------");
        System.out.println("Name: "+this.versionName);
        System.out.println("Date: "+this.versionDate);
        System.out.println("ID: "+this.versionId);
    }

    public Map<String, Integer> getVersionInteger(List<VersionInfo> versionInfoList){
        Map<String,Integer> versionMap = new LinkedHashMap<>();
        int i=0;
        for ( VersionInfo info: versionInfoList){
            versionMap.put(info.versionName,i);
            i++;
        }
        return versionMap;
    }
}
