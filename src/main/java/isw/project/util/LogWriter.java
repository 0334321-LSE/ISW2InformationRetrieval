package isw.project.util;


import isw.project.model.BugTicket;
import isw.project.model.ClassInfo;
import isw.project.model.VersionInfo;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LogWriter {

    private LogWriter() {}

    private static final String SEPARATOR = "----------------------------------------------------------------------------------" ;

    public static Path buildLogPath(String projectName) {
        return Path.of("./outputs", projectName.toUpperCase(), "Log") ;
    }

    public static void writeVersionLog(String projectName, List<VersionInfo> versionInfoList) throws IOException {
        Files.createDirectories(buildLogPath(projectName)) ;
        Writer writer = new BufferedWriter(new FileWriter(Path.of(buildLogPath(projectName).toString(), "Version").toString())) ;


        writer.write("Versioni Totali >> " + versionInfoList.size() + "\n\n");
        Integer commitNumber = 0 ;
        StringBuilder stringBuilder = new StringBuilder() ;
        for (VersionInfo versionInfo : versionInfoList) {

            stringBuilder.append("Number >> ").append(versionInfo.getVersion().getVersionInt()).append("\n") ;
            stringBuilder.append("VersionName >> ").append(versionInfo.getVersion().getVersionName()).append("\n") ;
            stringBuilder.append("VersionDate >> ").append(versionInfo.getVersion().getVersionDate()).append("\n") ;
            stringBuilder.append("ClassList Size >> ").append(versionInfo.getJavaClasses().size()).append("\n") ;
            stringBuilder.append("CommitList Size >> ").append(versionInfo.getCommitList().size()).append("\n\n") ;

            commitNumber += versionInfo.getCommitList().size() ;

            stringBuilder.append(SEPARATOR).append("\n\n") ;

        }
        stringBuilder.append("Commit Totali >> ").append(commitNumber).append("\n\n") ;
        stringBuilder.append(SEPARATOR).append("\n\n") ;
        writer.write(stringBuilder.toString());

        writer.close();
    }

    public static void writeTicketLog(String projectName, List<BugTicket> ticketInfoList) throws IOException {
        Files.createDirectories(buildLogPath(projectName)) ;
        Writer writer = new BufferedWriter(new FileWriter(Path.of(buildLogPath(projectName).toString(), "Ticket").toString())) ;

        writer.write("Ticket Totali >> " + ticketInfoList.size() + "\n\n");
        for (BugTicket ticketInfo : ticketInfoList) {
            StringBuilder stringBuilder = new StringBuilder() ;
            stringBuilder.append("Ticket ID >> ").append(ticketInfo.getIssueKey()).append("\n") ;
            stringBuilder.append("Opening Date >> ").append(ticketInfo.getCreationDate()).append("\n") ;
            stringBuilder.append("Resolution Date >> ").append(ticketInfo.getResolutionDate()).append("\n") ;
            stringBuilder.append("Injected Version >> ").append(ticketInfo.getInjectedVersion() == null ? "NULL" : ticketInfo.getInjectedVersion().getVersionName()).append("\n") ;
            stringBuilder.append("Fix Version >> ").append(ticketInfo.getFixedVersion().getVersionName()).append("\n") ;
            stringBuilder.append("Opening Version >> ").append(ticketInfo.getOpeningVersion().getVersionName()).append("\n") ;


            stringBuilder.append("Fix Commit List >> ").append("\n") ;
            for (RevCommit commit : ticketInfo.getAssociatedCommit()) {
                stringBuilder.append("\t").append(commit.getName()).append("\n") ;
            }

            stringBuilder.append(SEPARATOR).append("\n\n") ;
            writer.write(stringBuilder.toString());
        }

        writer.close();
    }


    public static void writeBuggyClassesLog(String projectName, List<VersionInfo> versionInfoList) throws IOException {
        Files.createDirectories(buildLogPath(projectName)) ;
        Writer writer = new BufferedWriter(new FileWriter(Path.of(buildLogPath(projectName).toString(), "BuggyClasses").toString())) ;

        StringBuilder stringBuilder = new StringBuilder() ;
        for (VersionInfo versionInfo : versionInfoList) {
            stringBuilder.append("Version Name >> ").append(versionInfo.getVersion().getVersionName()).append("\n") ;
            int buggyClassesNumber = 0 ;
            for (ClassInfo classInfo : versionInfo.getJavaClasses()) {
                if (classInfo.isBuggy().equals("true")) {
                    buggyClassesNumber ++ ;
                }
            }
            stringBuilder.append("Numero Buggy >> ").append(buggyClassesNumber).append("\n") ;
            stringBuilder.append(SEPARATOR).append("\n\n") ;
        }

        writer.write(stringBuilder.toString());
        writer.close();
    }
}
