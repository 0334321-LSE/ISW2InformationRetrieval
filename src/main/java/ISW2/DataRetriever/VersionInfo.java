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
        System.out.println("\n ----------------------------------------------");
        System.out.print("| Name: "+this.versionName);
        System.out.print(" | Date: "+this.versionDate);
        System.out.print(" | ID: "+this.versionId+" |");
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
}
