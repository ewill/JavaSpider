package org.kagan.main;

import java.util.Scanner;

import org.kagan.config.Configure;
import org.kagan.core.SpiderRobot;
import org.kagan.util.ConfigKit;
import org.kagan.util.Db;

public class KaganCenter {
    
    private static Configure conf;
    private static Scanner scanf = new Scanner(System.in);
    
    public static final void helpMenu() {
        System.out.println();
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("| Command List                                                                 |");
        System.out.println("| -----------------------------------------------------------------------------|");
        System.out.println("| conf[ig] - See Configure                                                     |");
        System.out.println("| count - Count Records                                                        |");
        System.out.println("| chkrep - Check Repeat Data                                                   |");
        System.out.println("| start - Spider Robot Start Working                                           |");
        System.out.println("| stop - Spider Robot Stop Working                                             |");
        System.out.println("| status - Check Running Status                                                |");
        System.out.println("| quit - Exit Program                                                          |");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println();
    }
    
    public static void main(String[] args) {
        SpiderRobot spider = null;
        try {
            String command;
            conf = ConfigKit.loadKaganXml("KaganConfig.xml");
            spider = new SpiderRobot(conf);
            Db.Init(ConfigKit.loadProperties("druid.properties"));
            
            System.out.println("Welcome to Kagan Spider Robot");
            System.out.print(">>> ");
            while (!(command = scanf.nextLine()).equals("quit")) {
                switch (command) {
                    case "conf":
                    case "config":
                        System.out.println(conf.toString());
                        break;
                    case "count":
                        System.out.println(String.format("\nTotal Records : %d\n", spider.countRecords()));
                        break;
                    case "chkrep":
                        String str = spider.formatRepeatData(spider.checkRepeatData());
                        if (str != null) {
                            System.out.println(String.format("\nRepeat Data : \n%s\n", str));
                        } else {
                            System.out.println("\nNothing.\n");
                        }
                        break;
                    case "start":
                        System.out.println("\nStarting...");
                        spider.start();
                        System.out.println("Working Thread Started.\n");
                        break;
                    case "stop":
                        spider.shutdown();
                        System.out.println("\nWorking Thread Stopped.\n");
                        break;
                    case "status":
                        System.out.println(spider);
                        break;
                    case "help":
                        helpMenu();
                        break;
                    default:
                        System.out.println("\nInvalid Command.\n");
                        break;
                }
                System.out.print(">>> ");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanf.close();
            spider.shutdown();
            Db.shutdown();
            System.out.println("\nExit Kagan System\n");
        }
    }
}
