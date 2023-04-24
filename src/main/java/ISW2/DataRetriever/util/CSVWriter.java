package ISW2.DataRetriever.util;

import ISW2.DataRetriever.fileModel.ClassInfoFile;
import ISW2.DataRetriever.fileModel.CsvEnumaration;
import ISW2.DataRetriever.model.ClassInfo;

import java.io.IOException;
import java.util.List;


public class CSVWriter {
    private CSVWriter(){}
    public static void writeCsvPerRelease(String projName, List<ClassInfo> javaClassesList, int lastVersionID) throws IOException {

        //In the first iteration of walk forward, testing set is composed of second release classes
        for(int i=1; i<=lastVersionID; i++) {
            List<ClassInfo> remainingJavaClassesList = ClassInfoUtil.filterJavaClassesByVersion(javaClassesList, i);
            if ( remainingJavaClassesList.size() == 0 )
                //TODO skip if there isn't class in that version
                continue;;
            ClassInfoFile labelingTesting = new ClassInfoFile(projName, CsvEnumaration.TESTING, i-1, remainingJavaClassesList);
            labelingTesting.writeOnArff(false);	//"true" indicates that csv file will be deleted and only arff file will remain

        }

    }
}
