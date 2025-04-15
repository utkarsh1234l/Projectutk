package com.capstone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerateChangeLogs {

    private static final String ROOT_FOLDER = "src/main/resources/Release 1.0.0";

    private static final String MASTER_CHANGELOG_FILE = "master-changelog.xml";
    private static final String TEAM_CHANGELOG_FILE = "team-changelog.xml";

    private static String curFolder = "";
    static int id = 1;
    static int fileid = 1;

    public static void checksql(File[] files) throws Exception {
        ArrayList<String> sqlfiles = new ArrayList<>();

        for (File filename : files) {
            if (filename.isDirectory()) {
                curFolder = filename.getAbsolutePath();
                findsqlFiles(filename, ROOT_FOLDER);
            } else if (filename.getName().endsWith(".sql")) {
                sqlfiles.add(filename.getAbsolutePath());
            } else if (filename.getName().endsWith(".xml")) {
                continue;
            }
        }
        generateChangeSets(sqlfiles, curFolder);
    }

    public static void generateChangeSets(ArrayList<String> sqlfiles, String curFolder) throws Exception {
        if (sqlfiles.size() == 0)
            return;

        int id=1;

        String temp=getRelativePath(curFolder,new File(ROOT_FOLDER));
        
        String new_id=temp.substring(0,5)+"_";
        new_id+=temp.substring(6, 9);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Create a new Document
        Document document = builder.newDocument();

        // Create root element
        Element root = document.createElement("databaseChangeLog");
        root.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:ext", "http://www.liquibase.org/xml/ns/dbchangelog-ext");
        root.setAttribute("xmlns:pro", "http://www.liquibase.org/xml/ns/pro");
        root.setAttribute("xsi:schemaLocation",
                "http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-pro-latest.xsd");

        document.appendChild(root);

        for (String apath : sqlfiles) {
            Element ch = document.createElement("changeSet");
            ch.setAttribute("id", new_id+Integer.valueOf(id).toString());
            id++;
            ch.setAttribute("author", "user");
            ch.setAttribute("runOnChange","true");

            root.appendChild(ch);

            Element sqlFile = document.createElement("sqlFile");
            sqlFile.setAttribute("path", getRelativePath(apath,new File(ROOT_FOLDER)));

            ch.appendChild(sqlFile);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(document);
        String path = curFolder + File.separator + fileid + "changelog.xml";
        StreamResult result = new StreamResult(new File(path));
        fileid++;
        transformer.transform(source, result);
    }

    public static void findsqlFiles(File curFolder, String rootFolder) throws Exception {
        File[] files = curFolder.listFiles();
        checksql(files);
    }

    

    public static void main(String[] args) throws Exception {
        // Uncomment the following line to delete all generated XML files
        deleteGeneratedFiles(new File(ROOT_FOLDER));

        findsqlFiles(new File(ROOT_FOLDER), ROOT_FOLDER);

        // Generate team changelogs
       generateTeamChangelogs(new File(ROOT_FOLDER));

        // Generate master changelog
       generateMasterChangelog(new File(ROOT_FOLDER));

        System.out.println("Done");
    }

    // Additional methods unchanged...

    private static void generateTeamChangelogs(File rootFolder) throws Exception {
        File[] files = rootFolder.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                // Generate changelog for each team directory
                generateTeamChangelog(file);
            }
        }
    }

    private static void generateTeamChangelog(File teamFolder) throws Exception {
        List<String> sqlFiles = new ArrayList<>();
        List<String> xmlFiles = new ArrayList<>();

        // Collect SQL and XML files
        collectFiles(teamFolder, sqlFiles, xmlFiles);

        // Generate team changelog if SQL files are found
        if (!sqlFiles.isEmpty()) {
            generateChangeSets(sqlFiles, new File(ROOT_FOLDER));
        }

        // Generate include tags in team changelog if XML files are found
        if (!xmlFiles.isEmpty()) {
            generateIncludeTag(xmlFiles, teamFolder);
        }
    }

    private static void collectFiles(File folder, List<String> sqlFiles, List<String> xmlFiles) {
        File[] files = folder.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectFiles(file, sqlFiles, xmlFiles);
            } else if (file.getName().endsWith(".sql")) {
                sqlFiles.add(file.getAbsolutePath());
            } else if (file.getName().endsWith(".xml")) {
                xmlFiles.add(file.getAbsolutePath());
            }
        }
    }

    private static void generateChangeSets(List<String> sqlFiles, File teamFolder) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element root = document.createElement("databaseChangeLog");
        root.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:ext", "http://www.liquibase.org/xml/ns/dbchangelog-ext");
        root.setAttribute("xmlns:pro", "http://www.liquibase.org/xml/ns/pro");
        root.setAttribute("xsi:schemaLocation",
                "http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-pro-latest.xsd");

        document.appendChild(root);

        int id = 1;
        for (String sqlFile : sqlFiles) {
            Element changeSet = document.createElement("changeSet");
            changeSet.setAttribute("id", Integer.toString(id++));
            changeSet.setAttribute("author", "user");

            root.appendChild(changeSet);

            Element sqlFileElement = document.createElement("sqlFile");
            String pp = getRelativePath(sqlFile, new File(ROOT_FOLDER));
            sqlFileElement.setAttribute("path", pp);
            sqlFileElement.setAttribute("relativeToChangelogFile", "true");
            changeSet.appendChild(sqlFileElement);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(document);
        String path = teamFolder + File.separator + TEAM_CHANGELOG_FILE;
        StreamResult result = new StreamResult(new File(path));
        transformer.transform(source, result);
    }

    private static void generateIncludeTag(List<String> xmlFiles, File teamFolder) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element root = document.createElement("databaseChangeLog");
        root.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:ext", "http://www.liquibase.org/xml/ns/dbchangelog-ext");
        root.setAttribute("xmlns:pro", "http://www.liquibase.org/xml/ns/pro");
        root.setAttribute("xsi:schemaLocation",
                "http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-pro-latest.xsd");

        document.appendChild(root);

        for (String xmlFile : xmlFiles) {
            Element include = document.createElement("include");
            include.setAttribute("file", getRelativePath(xmlFile, teamFolder));
            root.appendChild(include);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(document);
        String path = teamFolder + File.separator + TEAM_CHANGELOG_FILE;
        StreamResult result = new StreamResult(new File(path));
        transformer.transform(source, result);
    }

    private static void generateMasterChangelog(File rootFolder) throws Exception {
        List<String> teamChangelogs = new ArrayList<>();

        File[] files = rootFolder.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                File teamChangelogFile = new File(file, TEAM_CHANGELOG_FILE);
                if (teamChangelogFile.exists()) {
                    teamChangelogs.add(file.getName() + File.separator + TEAM_CHANGELOG_FILE);
                }
            }
        }

        if (!teamChangelogs.isEmpty()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element root = document.createElement("databaseChangeLog");
            root.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("xmlns:ext", "http://www.liquibase.org/xml/ns/dbchangelog-ext");
            root.setAttribute("xmlns:pro", "http://www.liquibase.org/xml/ns/pro");
            root.setAttribute("xsi:schemaLocation",
                    "http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-pro-latest.xsd");

            document.appendChild(root);

            for (String changelog : teamChangelogs) {
                Element include = document.createElement("include");
                include.setAttribute("file", changelog);
                root.appendChild(include);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(document);
            String path = rootFolder + File.separator + MASTER_CHANGELOG_FILE;
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);
        }
    }

    private static void deleteGeneratedFiles(File folder) {
        File[] files = folder.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                deleteGeneratedFiles(file);
            } else if (file.getName().endsWith(".xml")) {
                file.delete();
            }
        }
    }

    private static String getRelativePath(String filePath, File baseFolder) {
        return new File(ROOT_FOLDER).toURI().relativize(new File(filePath).toURI()).getPath();
    }
}