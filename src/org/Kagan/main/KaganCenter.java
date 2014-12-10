package org.Kagan.main;

import java.util.Scanner;

import org.Kagan.config.Configure;
import org.Kagan.core.SpiderRobot;
import org.Kagan.util.ConfigKit;
import org.Kagan.util.Db;

public class KaganCenter {
    
    private static Configure conf;
    private static Scanner scanf = new Scanner(System.in);
    
    public static final void KaganMessageHeader() {
        System.out.println("\n### Kagan Message ###");
        System.out.println("================================================================================");
    }
    
    public static final String PrintMenu() {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("| Welcome to Kagan Spider Robot                                                |");
        System.out.println("| -----------------------------------------------------------------------------|");
        System.out.println("| 1. See Configure                                                             |");
        System.out.println("| 2. Count Records                                                             |");
        System.out.println("| 3. Check Repeat Data                                                         |");
        System.out.println("| 4. Spider Robot Start Working                                                |");
        System.out.println("| 5. Spider Robot Stop Working                                                 |");
        System.out.println("| 6. Check Running Status                                                      |");
        System.out.println("| 7. Quit                                                                      |");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.print("~# ");
        return scanf.nextLine();
    }
    
    public static void main(String[] args) {
        SpiderRobot spider = null;
        try {
            String command;
            conf = ConfigKit.loadKaganXml("KaganConfig.xml");
            spider = new SpiderRobot(conf);
            Db.Init(ConfigKit.loadProperties("druid.properties"));
            
            while (!(command = PrintMenu()).equals("7")) {
                int choice;
                try {
                    choice = Integer.valueOf(command);
                } catch (Exception e) {
                    choice = -1;
                }
                
                KaganMessageHeader();
                switch (choice) {
                    case 1:
                        System.out.println(conf.toString());
                        break;
                    case 2:
                        System.out.println(String.format("Total Records : %d\n", spider.countRecords()));
                        break;
                    case 3:
                        String str = spider.formatRepeatData(spider.checkRepeatData());
                        if (str != null) {
                            System.out.println(String.format("Total Repeat Data : \n%s\n", str));
                        } else {
                            System.out.println("Nothing\n");
                        }
                        break;
                    case 4:
                        System.out.println("Starting...\n");
                        spider.start();
                        break;
                    case 5:
                        System.out.println("Stopping...\n");
                        spider.shutdown();
                        break;
                    case 6:
                        System.out.println(spider);
                        break;
                    default:
                        System.out.println("Invalid Command");
                        break;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Db.shutdown();
            spider.shutdown();
            scanf.close();
        }
    }
}
