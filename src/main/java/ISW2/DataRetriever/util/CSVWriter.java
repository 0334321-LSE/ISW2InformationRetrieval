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
        for(int i=2; i<=lastVersionID; i++) {
            List<ClassInfo> iterJavaClassesList = ClassInfoUtil.filterJavaClassesByRelease(javaClassesList, i);
            ClassInfoFile labelingTesting = new ClassInfoFile(projName, CsvEnumaration.TESTING, i-1, iterJavaClassesList);
            labelingTesting.writeOnArff(false);	//"true" indicates that csv file will be deleted and only arff file will remain

        }

    }
}
