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
        System.out.println("| 6. Quit                                                                      |");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.print("~# ");
        return scanf.nextLine();
    }
    
    public static void main(String[] args) {
        try {
            String command;
            SpiderRobot spider = new SpiderRobot();
            conf = ConfigKit.loadKaganXml("KaganConfig.xml");
            
            Db.Init(ConfigKit.loadProperties("druid.properties"));
            SpiderRobot.init(conf);
            
            while (!(command = PrintMenu()).equals("6")) {
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
                        System.out.println(String.format("Total Repeat Data : \n%s\n", spider.formatRepeatData(spider.checkRepeatData())));
                        break;
                    case 4:
                        spider.start();
                        break;
                    case 5:
                        SpiderRobot.shutdown();
                        break;
                    default:
                        System.out.println("Invalid Command");
                        break;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Db.Shutdown();
            SpiderRobot.shutdown();
            scanf.close();
        }
    }
}
