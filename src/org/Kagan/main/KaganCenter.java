package org.Kagan.main;

import java.util.Scanner;

import org.Kagan.core.ConfigUtil;
import org.Kagan.core.Configure;
import org.Kagan.core.SpiderRobot;
import org.Kagan.util.Db;
import org.Kagan.util.T;

public class KaganCenter {
    
    private static Configure conf;
    private static Scanner scanf = new Scanner(System.in);
    
    public static final void KaganMessageHeader() {
        T.println("\n### Kagan Message ###");
        T.println("================================================================================");
    }
    
    public static final String PrintMenu() {
        T.println("--------------------------------------------------------------------------------");
        T.println("| Welcome to Kagan Spider Robot                                                |");
        T.println("| -----------------------------------------------------------------------------|");
        T.println("| 1. See Configure                                                             |");
        T.println("| 2. Count Records                                                             |");
        T.println("| 3. Check Repeat Data                                                         |");
        T.println("| 4. Spider Robot Start Working                                                |");
        T.println("| 5. Spider Robot Stop Working                                                 |");
        T.println("| 6. Quit                                                                      |");
        T.println("--------------------------------------------------------------------------------");
        T.print("~# ");
        return scanf.nextLine();
    }
    
    public static void main(String[] args) {
        try {
            String command;
            SpiderRobot spider = new SpiderRobot();
            conf = ConfigUtil.loadKaganXml("KaganConfig.xml");
            
            Db.Init(ConfigUtil.loadProperties("druid.properties"));
            SpiderRobot.Init(conf);
            
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
                        T.println(conf.toString());
                        break;
                    case 2:
                        T.println(String.format("Total Records : %d\n", spider.CountRecords()));
                        break;
                    case 3:
                        T.println(String.format("Total Repeat Data : \n%s\n", spider.FormatRepeatData(spider.CheckRepeatData())));
                        break;
                    case 4:
                        spider.Start();
                        break;
                    case 5:
                        SpiderRobot.Shutdown();
                        break;
                    default:
                        T.println("Invalid Command");
                        break;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Db.Shutdown();
            SpiderRobot.Shutdown();
            scanf.close();
        }
    }
}
