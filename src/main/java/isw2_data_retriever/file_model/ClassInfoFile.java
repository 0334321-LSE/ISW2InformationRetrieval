package isw2_data_retriever.file_model;

import isw2_data_retriever.model.ClassInfo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


public class ClassInfoFile {

    private String projName;
    private CsvEnumaration csvName;
    private int csvIndex;
    private List<ClassInfo> javaClassesList;

    public ClassInfoFile(String projName, CsvEnumaration csvName, int csvIndex, List<ClassInfo> javaClassesList) {
        this.projName = projName;
        this.csvName = csvName;
        this.csvIndex = csvIndex;
        this.javaClassesList = javaClassesList;

    }

    private String enumToString() {

        switch(csvName) {

            case TRAINING:
                return "_TR" + csvIndex;
            case TESTING:
                return "_TE" + csvIndex;
            case BUGGY:
                return "_buggy_classes";
            case CURRENT:
                return "_current_classes";
            default:
                return null;

        }

    }

    /** write all the ClassInfo data on a CSV file*/
    public Sheet writeOnCsv() throws IOException {

        Sheet sheet;
        String csvNameStr = enumToString();
        Workbook wb = new HSSFWorkbook();
        //TODO CREATE A DIRECTORY FOR EACH WALK FORWARD ITERATION
       /* if(this.csvIndex==2){
            String pathname = "./projectsARFF/" +this.projName.toLowerCase() + csvNameStr + ".csv";
        }*/
        try(OutputStream os = new FileOutputStream("./projectsCSV/"+this.projName.toLowerCase()+"/"+ this.projName + csvNameStr + ".csv")) {
            sheet = wb.createSheet(this.projName);

            for(int i=-1; i<this.javaClassesList.size(); i++) {
                Row row = sheet.createRow(i+1);		//i = row index - 1

                Cell cell0 = row.createCell(0);
                Cell cell1 = row.createCell(1);
                Cell cell2 = row.createCell(2);
                Cell cell3 = row.createCell(3);
                Cell cell4 = row.createCell(4);
                Cell cell5 = row.createCell(5);
                Cell cell6 = row.createCell(6);
                Cell cell7 = row.createCell(7);
                Cell cell8 = row.createCell(8);
                Cell cell9 = row.createCell(9);
                Cell cell10 = row.createCell(10);
                Cell cell11 = row.createCell(11);
                Cell cell12 = row.createCell(12);

                if(i==-1) {
                    cell0.setCellValue("CLASS");
                    cell1.setCellValue("VERSION");
                    cell2.setCellValue("SIZE");
                    cell3.setCellValue("NR");
                    cell4.setCellValue("NFix");
                    cell5.setCellValue("N_AUTH");
                    cell6.setCellValue("LOC_ADDED");
                    cell7.setCellValue("MAX_LOC_ADDED");
                    cell8.setCellValue("AVG_LOC_ADDED");
                    cell9.setCellValue("CHURN");
                    cell10.setCellValue("MAX_CHURN");
                    cell11.setCellValue("AVG_CHURN");
                    cell12.setCellValue("IS_BUGGY");

                }
                else {
                    cell0.setCellValue(this.javaClassesList.get(i).getName());
                    cell1.setCellValue(this.javaClassesList.get(i).getVersion().getVersionInt());
                    cell2.setCellValue(this.javaClassesList.get(i).getSize());
                    cell3.setCellValue(this.javaClassesList.get(i).getNr());
                    cell4.setCellValue(this.javaClassesList.get(i).getnFix());
                    cell5.setCellValue(this.javaClassesList.get(i).getnAuth());
                    cell6.setCellValue(this.javaClassesList.get(i).getLocAdded());
                    cell7.setCellValue(this.javaClassesList.get(i).getMaxLocAdded());
                    cell8.setCellValue(this.javaClassesList.get(i).getAvgLocAdded());
                    cell9.setCellValue(this.javaClassesList.get(i).getChurn());
                    cell10.setCellValue(this.javaClassesList.get(i).getMaxChurn());
                    cell11.setCellValue(this.javaClassesList.get(i).getAvgChurn());
                    cell12.setCellValue(this.javaClassesList.get(i).isBuggy());

                }

            }
            wb.write(os);	//Write on file Excel

        }
        return sheet;

    }

    /**Write for all the ClassInfo elements the data into a ARFF file */
    public void writeOnArff(boolean deleteCsv) throws IOException {

        String csvNameStr = enumToString();
        Sheet sheet = writeOnCsv();
        try(FileWriter wr = new FileWriter("./projectsARFF/"+this.projName.toLowerCase()+"/"+ this.projName + csvNameStr + ".arff")) {

            wr.write("@relation " + this.projName + csvNameStr + "\n");
            wr.write("@attribute SIZE numeric\n");
            wr.write("@attribute NR numeric\n");
            wr.write("@attribute NFix numeric\n");
            wr.write("@attribute N_AUTH numeric\n");
            wr.write("@attribute LOC_ADDED numeric\n");
            wr.write("@attribute MAX_LOC_ADDED numeric\n");
            wr.write("@attribute AVG_LOC_ADDED numeric\n");
            wr.write("@attribute CHURN numeric\n");
            wr.write("@attribute MAX_CHURN numeric\n");
            wr.write("@attribute AVG_CHURN numeric\n");
            wr.write("@attribute IS_BUGGY {'true', 'false'}\n");
            wr.write("@data\n");

            for (int r=1; r<=sheet.getLastRowNum(); r++){
                Row row = sheet.getRow(r);

                Double val2 = row.getCell(2).getNumericCellValue();
                Double val3 = row.getCell(3).getNumericCellValue();
                Double val4 = row.getCell(4).getNumericCellValue();
                Double val5 = row.getCell(5).getNumericCellValue();
                Double val6 = row.getCell(6).getNumericCellValue();
                Double val7 = row.getCell(7).getNumericCellValue();
                Double val8 = row.getCell(8).getNumericCellValue();
                Double val9 = row.getCell(9).getNumericCellValue();
                Double val10 = row.getCell(10).getNumericCellValue();
                Double val11 = row.getCell(11).getNumericCellValue();
                Boolean val12 = row.getCell(12).getBooleanCellValue();

                wr.write(val2.toString() + "," + val3.toString() + "," + val4.toString() + "," + val5.toString() + "," + val6.toString() + "," +
                        val7.toString() + "," + val8.toString() + "," + val9.toString() + "," + val10.toString() + "," + val11.toString() + "," + val12.toString() + "\n");

            }

        }

        if(deleteCsv) {
            Files.delete(Paths.get("./projectsCSV/"+this.projName.toLowerCase()+"/"+ this.projName + csvNameStr +".csv"));
        }

    }

}
