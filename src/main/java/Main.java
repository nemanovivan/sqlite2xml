import service.DbHandler;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        DbHandler.connect();
        Scanner scanner = new Scanner(System.in);
        boolean isOk = true;
        while (isOk) {
            System.out.println("Введите 1 для создания оборудования, 2 для вывода общей информации, 3 для создания XML, exit чтобы выйти");
            switch (scanner.next()) {
                case "1":
                    System.out.println("Введите кол-во оборудования и имя скважины");
                    Integer count = scanner.nextInt();
                    String name = scanner.next();
                    DbHandler.createEquipment(count, name);
                    break;
                case "2":
                    System.out.println("Введите названия скважин, разделив их ТОЛЬКО запятыми БЕЗ ПРОБЕЛОВ");
                    String string = scanner.next();
                    String[] strings = string.split(",");
                    DbHandler.countEquipment(strings);
                    break;
                case "3":
                    System.out.println("Введите имя файла");
                    String fileName = scanner.next();
                    DbHandler.exportToXML(fileName);
                    break;
                case "exit":
                    isOk = false;
            }
        }
    }
}
