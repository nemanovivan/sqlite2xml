package service;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import domain.Equipment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DbHandler {

    public static Connection connection;
    public static Statement statement;
    public static ResultSet resultSet;
    private static final String filePath =
            "C:\\Users\\tcmch\\OneDrive\\Desktop\\CROC\\wells\\src\\main\\resources\\files\\";

    public static void connect() throws ClassNotFoundException, SQLException
    {
        connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:wells.db");
        statement = connection.createStatement();

        System.out.println("База Подключена!");
    }

    public static void createEquipment(Integer count, String name) throws SQLException {
        for (int i = 0; i < count; i++) {
            String equipmentName = java.util.UUID.randomUUID().toString();
            Integer wellId;
            resultSet = statement.executeQuery("SELECT id FROM well WHERE name = '" + name + "';");
            if (resultSet.next()) {
                wellId = resultSet.getInt("id");
            } else {
                statement.execute("INSERT INTO 'well' ('name') VALUES ('" + name + "'); ");
                wellId = statement.executeQuery("SELECT id FROM well WHERE name = '" + name + "';")
                        .getInt("id");
            }
            statement.execute("INSERT INTO 'equipment' ('name', 'wellId') VALUES ('"
                    + equipmentName + "', '" + wellId + "'); ");
        }
    }

    public static void countEquipment (String[] strings) throws SQLException {
        for (String name: strings) {
            resultSet = statement.executeQuery("SELECT id FROM well WHERE name = '" + name + "';");
            int wellId = resultSet.getInt("id");
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM equipment WHERE wellId = '" + wellId + "';");
            int count = resultSet.getInt("COUNT(*)");
            System.out.println(name + " " + count);
        }
    }

    public static void exportToXML(String fileName) throws Exception {
        Map<Integer, String> wellMap = new HashMap();
        resultSet = statement.executeQuery("SELECT id, name FROM well");
        while (resultSet.next()) {
            wellMap.put(resultSet.getInt("id"), resultSet.getString("name"));
        }
        Map<Integer, Equipment> equipmentMap = new HashMap<>();
        resultSet = statement.executeQuery("SELECT id, name, wellId FROM equipment");
        while (resultSet.next()) {
            Equipment equipment = new Equipment(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("wellId"));
            equipmentMap.put(resultSet.getInt("id"), equipment);
        }
        Document xmlDoc = buildXML(equipmentMap, wellMap);
        File outputFile = new File(filePath + fileName);
        printDOM(xmlDoc, outputFile);

    }

    private static Document buildXML(Map<Integer, Equipment> equipmentMap, Map<Integer, String> wellMap) throws SQLException {
        Document xmlDoc = new DocumentImpl();
        Element rootElement = xmlDoc.createElement("wells");
        xmlDoc.appendChild(rootElement);

        for (Integer wellId: wellMap.keySet()) {
            Element well = xmlDoc.createElement("well");
            well.setAttribute("name", wellMap.get(wellId));
            well.setAttribute("id", String.valueOf(wellId));
            for (Integer equipmentId: equipmentMap.keySet()) {
                if (equipmentMap.get(equipmentId).getWellId() == wellId) {
                    Element equipment = xmlDoc.createElement("equipment");
                    equipment.setAttribute("name", equipmentMap.get(equipmentId).getName());
                    equipment.setAttribute("id", String.valueOf(equipmentId));
                    well.appendChild(equipment);
                }
            }
            rootElement.appendChild(well);
        }
        while (resultSet.next()) {

        }
        return xmlDoc;
    }

    private static void printDOM(Document xmlDoc, File outputFile) throws Exception
    {
        OutputFormat outputFormat = new OutputFormat("XML","UTF-8",true);
        FileWriter fileWriter = new FileWriter(outputFile);

        XMLSerializer xmlSerializer = new XMLSerializer(fileWriter, outputFormat);

        xmlSerializer.asDOMSerializer();

        xmlSerializer.serialize(xmlDoc.getDocumentElement());
    }
}
