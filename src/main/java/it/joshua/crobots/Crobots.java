package it.joshua.crobots;

import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.bean.RobotGameBean;
import it.joshua.crobots.data.TableName;
import it.joshua.crobots.http.obsolete.HTTPClient;
import it.joshua.crobots.http.obsolete.RunHTTP;
import it.joshua.crobots.http.obsolete.RunHTTPQueryManager;
import it.joshua.crobots.impl.DataSourceManager;
import it.joshua.crobots.impl.Manager;
import it.joshua.crobots.impl.RunnableCrobotsManager;
import it.joshua.crobots.impl.RunnableCrobotsThread;
import it.joshua.crobots.impl.SQLManagerFactory;
import it.joshua.crobots.xml.Match;
import it.joshua.crobots.xml.MatchList;
import it.joshua.crobots.xml.ObjectFactory;
import it.joshua.crobots.xml.Robot;
import it.joshua.crobots.xml.Robots;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/*
 * Created on 13-lug-2006
 *
 * @name     Crobots
 * @version  4.65
 * @buid     100
 * @revision 05-Feb-2013
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
/**
 * @author mcamangi
 *
 * @copyright Maurizio Camangi 2006-2013
 * Style - Code Templates
 */
public class Crobots {

    private final static int BUILD = 100;
    private final static String VERSION = "Crobots Java Tournament Manager v.4.65 (build " + BUILD + ") - 05/Feb/2013 - (C) Maurizio Camangi";
    private static final List<String> robots = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(Crobots.class.getName());
    private static SharedVariables sharedVariables;

    public static void main(String[] args) {

        // logger.setLevel(Level.DEBUG);
        sharedVariables = SharedVariables.getInstance();
        sharedVariables.setup(args);
        logger.info(VERSION);

        if (sharedVariables.isPrintUsage()) {
            printUsage();
            System.exit(0);
        }

        if (sharedVariables.isOnlyTest()) {
            if (sharedVariables.isHTTPMode()) {
                doHTTPTest();
            } else {
                doTest();
                System.exit(0);
            }
        }

        if (sharedVariables.isInitMode()) {
            InitializeRobots();
        }

        if (sharedVariables.isSetupMode()) {
            doSetup();
        }

        sharedVariables.setHttpURLs(new Vector<String>());

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader("Crobots.backup");
        } catch (FileNotFoundException e) {
            //botta silente
        }

        if (fileReader != null) {
            logger.warning("Crobots.backup file found!");
            logger.warning("Recovering lost URLs...");
            String url;
            try {
                bufferedReader = new BufferedReader(fileReader);
                while ((url = bufferedReader.readLine()) != null) {
                    sharedVariables.getHttpURLs().add(url);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Crobots {0}", e);
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Crobots {0}", ex);
                }
            }
            logger.log(Level.WARNING, "HTTP URLs buffer size not empty : {0}", sharedVariables.getHttpURLs().size());

            try {
                fileReader.close();
                logger.warning("Deleting Crobots.backup...");
                File f = new File("Crobots.backup");
                f.delete();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Crobots {0}", e);
            }
        }

        try {
            fileReader = new FileReader("Crobots.backup.xml");
        } catch (FileNotFoundException e) {
            //botta silente
        }

        if (fileReader != null) {
            logger.warning("Crobots.backup.xml file found!");
            logger.warning("Recovering lost matches...");
            MatchList ml = null;
            try {
                JAXBContext context = JAXBContext.newInstance("it.joshua.crobots.xml");
                Unmarshaller u = context.createUnmarshaller();
                ml = (MatchList) u.unmarshal(fileReader);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Crobots {0}", e);
            }
            GamesBean gb;
            if (ml != null && ml.getMatch().size() > 0) {
                for (Match match : ml.getMatch()) {
                    gb = new GamesBean.Builder(match).build();
                    for (Robot robot : match.getRobots().getRobot()) {
                        gb.getRobots().add(new RobotGameBean.Builder(robot.getName()).setWin(robot.getWin()).setTie(robot.getTie()).setPoints(robot.getPoints()).build());
                    }
                    sharedVariables.addToGames(gb);
                }
            }

            logger.log(Level.WARNING, "Game buffer size not empty : {0}", sharedVariables.getGamesSize());
            try {
                fileReader.close();
                logger.warning("Deleting Crobots.backup.xml ...");
                File f = new File("Crobots.backup.xml");
                f.delete();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Crobots {0}", e);
            }
        }

        if (!sharedVariables.isGamesEmpty()) {

            if (sharedVariables.isHTTPMode()) {
                String url, query;
                HTTPClient httpc = new HTTPClient(sharedVariables.getUserName(), sharedVariables.getPassWord());
                logger.info("Start flushing HTTP URLs buffer...");

                while (sharedVariables.getHttpURLs().size() > 0) {
                    if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                        logger.warning("Kill reached! Crobots.stop found!");
                        break;
                    }
                    url = sharedVariables.getHttpURLs().get(0);
                    sharedVariables.getHttpURLs().remove(0);
                    query = null;
                    try {
                        query = httpc.doQuery(url);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Crobots {0}", e);
                    }

                    if (!(query != null && query.equals("ok"))) {
                        logger.severe(query);
                        logger.severe("SKIP: " + url);
                        sharedVariables.getHttpURLs().add(url);
                    }

                }
                logger.info("End flushing HTTP URLs buffer...");
            } else {
                flushCalculatedGames();
            }
        } else {
            sharedVariables.setRunnable(true);
            sharedVariables.setGlobalStartTime(System.currentTimeMillis());
            if (sharedVariables.isTimeLimit()) {
                logger.log(Level.INFO, "Setting up time limit {0} minute(s)", sharedVariables.getTimeLimitMinutes());
            }

            if (sharedVariables.isAllRounds()) {

                runThreads(TableName.F2F);
                runThreads(TableName.VS3);
                runThreads(TableName.VS4);

            } else {

                if (sharedVariables.isOnlyF2F()) {
                    runThreads(TableName.F2F);
                }
                if (sharedVariables.isOnly3vs3()) {
                    runThreads(TableName.VS3);
                }
                if (sharedVariables.isOnly4vs4()) {
                    runThreads(TableName.VS4);
                }
            }
        }

        if (sharedVariables.getBufferSize() > 0) {
            for (GamesBean bean : sharedVariables.getBuffer()) {
                bean.setAction("recovery");
                if (!sharedVariables.getGames().contains(bean)) {
                    sharedVariables.addToGames(bean);
                }
            }
        }

        if (!sharedVariables.isGamesEmpty()) {
            logger.log(Level.WARNING, "Game buffer size not empty : {0}", sharedVariables.getGamesSize());
            if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                logger.warning("Crobots.backup.xml file creation forced");
                ObjectFactory obf = new ObjectFactory();
                MatchList ml = obf.createMatchList();
                Match match;
                Robots robots;
                Robot robot;

                for (GamesBean gb : sharedVariables.getGames()) {
                    match = obf.createMatch();
                    match.setAction(gb.getAction());
                    match.setId(gb.getId());
                    match.setTableName(gb.getTableName().getTableName());
                    match.setGames(gb.getGames());
                    robots = obf.createRobots();
                    for (RobotGameBean r : gb.getRobots()) {
                        robot = obf.createRobot();
                        robot.setName(r.getRobot());
                        if ("update".equals(gb.getAction())) {
                            robot.setWin(r.getWin());
                            robot.setTie(r.getTie());
                            robot.setPoints(r.getPoints());
                        }
                        robots.getRobot().add(robot);
                    }
                    match.setRobots(robots);
                    ml.getMatch().add(match);
                }

                try {
                    File f = new File("Crobots.backup.xml");
                    JAXBContext context = JAXBContext.newInstance(MatchList.class);
                    Marshaller m = context.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    m.marshal(ml, f);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Crobots {0}", e);
                }
            } else {
                flushCalculatedGames();
            }
        }

        if (sharedVariables.getHttpURLs().size() > 0) {
            logger.log(Level.WARNING, "HTTP URLs buffer size not empty : {0}", sharedVariables.getHttpURLs().size());
            logger.warning("Crobots.backup file creation forced");
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new FileWriter("Crobots.backup"));
                for (String url : sharedVariables.getHttpURLs()) {
                    bufferedWriter.write(url + "\n");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Crobots {0}", e);
            } finally {
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.flush();
                        bufferedWriter.close();
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Crobots {0}", ex);
                }
            }
        }
        logger.info("Execution of Crobots Client ... Completed!");
    }

    private static void runThreads(TableName tableName) {
        ArrayList<Runnable> processList = new ArrayList<>();
        sharedVariables.setActiveThreads((Integer) sharedVariables.getThreadNumber());
        logger.log(Level.INFO, "Setting sleep interval : {0} ms", sharedVariables.getSleepInterval(tableName));
        logger.log(Level.INFO, "Setting num of match   : {0}", sharedVariables.getNumOfMatch(tableName));
        ExecutorService threadExecutor = Executors
                .newFixedThreadPool(sharedVariables.getThreadNumber() + 1);

        //Main Thread
        if (sharedVariables.isHTTPMode()) {
            RunHTTPQueryManager httpQueryManager = new RunHTTPQueryManager();
            threadExecutor.execute(httpQueryManager);
        } else {
            RunnableCrobotsManager runSQLQueryManager = new RunnableCrobotsManager(tableName);
            threadExecutor.execute(runSQLQueryManager);
        }

        if (sharedVariables.isHTTPMode()) {

            for (int i = 0; i < sharedVariables.getThreadNumber(); i++) {
                RunHTTP runHTTP = new RunHTTP("Thread_" + i, tableName);
                processList.add(runHTTP);
            }
        } else {
            for (int i = 0; i < sharedVariables.getThreadNumber(); i++) {
                RunnableCrobotsThread runSQL = new RunnableCrobotsThread("Thread_" + i, tableName);
                processList.add(runSQL);
            }
        }

        // Crobots match calculator threads
        for (int i = 0; i < sharedVariables.getThreadNumber(); i++) {
            threadExecutor.execute(processList.get(i));
        }

        threadExecutor.shutdown();
        logger.info("Threads started");

        try {
            threadExecutor.awaitTermination(sharedVariables.getTimeout(), TimeUnit.HOURS);
            threadExecutor.shutdownNow();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Crobots {0}", e);
        } finally {
            sharedVariables.setRunnable(false);
            logger.info("Threads gonna be stopped");
        }

        if (!sharedVariables.isGamesEmpty()) {
            flushCalculatedGames();
        }
    }

    static private void inputFileReader() {

        logger.info("Loading robots...");
        try {
            try (BufferedReader input = new BufferedReader(new FileReader(
                            sharedVariables.getFileInput()))) {
                String robot = input.readLine();
                logger.fine(robot);
                robots.add(robot);
                while (input.ready()) {
                    robot = input.readLine();
                    robots.add(robot);
                    logger.fine(robot);
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Crobots {0}", e);
        }
    }

    static private void flushCalculatedGames() {
        logger.info("Start flushing game buffer...");
        DataSourceManager dataSourceManager = DataSourceManager.getDataSourceManager();

        SQLManagerInterface mySQLManagerF2F = SQLManagerFactory.getInstance(TableName.F2F);
        SQLManagerInterface mySQLManager3vs3 = SQLManagerFactory.getInstance(TableName.VS3);
        SQLManagerInterface mySQLManager4vs4 = SQLManagerFactory.getInstance(TableName.VS4);

        mySQLManagerF2F.setDataSourceManager(dataSourceManager);
        mySQLManager3vs3.setDataSourceManager(dataSourceManager);
        mySQLManager4vs4.setDataSourceManager(dataSourceManager);

        dataSourceManager.initialize();

        while (!sharedVariables.isGamesEmpty()) {
            if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
                logger.warning("Kill reached! Crobots.stop found!");
                break;
            }
            GamesBean gb = sharedVariables.getFromGames();
            switch (gb.getAction()) {
                case "update":
                    switch (gb.getTableName().getTableName()) {
                        case "f2f":
                            mySQLManagerF2F.initializeUpdates();
                            if (!mySQLManagerF2F.updateResults(gb)) {
                                logger.log(Level.SEVERE, "Can''t update results of {0}", gb.toString());
                                logger.log(Level.WARNING, "Recovery f2f id={0}", gb.getId());
                                mySQLManagerF2F.recoveryTable(gb);
                            }
                            mySQLManagerF2F.releaseUpdates();
                            break;
                        case "3vs3":
                            mySQLManager3vs3.initializeUpdates();
                            if (!mySQLManager3vs3.updateResults(gb)) {
                                logger.log(Level.SEVERE, "Can''t update results of {0}", gb.toString());
                                logger.log(Level.WARNING, "Recovery 3vs3 id={0}", gb.getId());
                                mySQLManager3vs3.recoveryTable(gb);
                            }
                            mySQLManager3vs3.releaseUpdates();
                            break;
                        case "4vs4":
                            mySQLManager4vs4.initializeUpdates();
                            if (!mySQLManager4vs4.updateResults(gb)) {
                                logger.log(Level.SEVERE, "Can''t update results of {0}", gb.toString());
                                logger.log(Level.WARNING, "Recovery 4vs4 id={0}", gb.getId());
                                mySQLManager4vs4.recoveryTable(gb);
                            }
                            mySQLManager4vs4.releaseUpdates();
                            break;
                    }
                    break;
                case "recovery":
                    logger.log(Level.WARNING, "Recovery {0}", gb.toString());
                    switch (gb.getTableName().getTableName()) {
                        case "f2f":
                            mySQLManagerF2F.recoveryTable(gb);
                            break;
                        case "3vs3":
                            mySQLManager3vs3.recoveryTable(gb);
                            break;
                        case "4vs4":
                            mySQLManager4vs4.recoveryTable(gb);
                            break;
                    }
                    break;
            }
        }

        dataSourceManager.closeAll();

        logger.info("End flushing game buffer...");
    }

    static private void printUsage() {

        logger.info("Usage: java -jar Crobots.jar -h hostname -u username -p password -f file -P path -c script -T timelimit -H -2 -4 -K -S -t -q -?");
        logger.info("Actual parameters");
        logger.log(Level.INFO, "OS Type           = {0}", sharedVariables.getOsType());
        logger.log(Level.INFO, "path delimitator  = {0}", sharedVariables.getDelimit());
        logger.log(Level.INFO, "hostname     (-h) = {0}", sharedVariables.getHostName());
        logger.log(Level.INFO, "fileinput    (-f) = {0}", sharedVariables.getFileInput());
        logger.log(Level.INFO, "cmd script   (-c) = {0}", sharedVariables.getCmdScript());
        logger.log(Level.INFO, "username     (-u) = {0}", sharedVariables.getUserName());
        logger.log(Level.INFO, "password     (-p) = {0}", sharedVariables.getPassWord());
        logger.log(Level.INFO, "path         (-P) = {0}", sharedVariables.getPath());
        logger.log(Level.INFO, "only test    (-t) = {0}", sharedVariables.isOnlyTest());
        logger.log(Level.INFO, "only f2f     (-2) = {0}", sharedVariables.isOnlyF2F());
        logger.log(Level.INFO, "only 3vs3    (-3) = {0}", sharedVariables.isOnly3vs3());
        logger.log(Level.INFO, "only 4vs4    (-4) = {0}", sharedVariables.isOnly4vs4());
        logger.log(Level.INFO, "quiet        (-q) = {0}", !sharedVariables.isVerbose());
        logger.log(Level.INFO, "kill         (-K) = {0}", sharedVariables.isKill());
        logger.log(Level.INFO, "threads      (-m) = {0}", sharedVariables.getThreadNumber());
        logger.log(Level.INFO, "http mode    (-H) = {0}", sharedVariables.isHTTPMode());
        logger.log(Level.INFO, "empty buffer (-E) = {0}", sharedVariables.isEmptyBuffer());
        logger.log(Level.INFO, "setup mode   (-S) = {0}", sharedVariables.isSetupMode());
        logger.log(Level.INFO, "init mode    (-I) = {0}", sharedVariables.isInitMode());
        logger.log(Level.INFO, "time limit   (-T) = {0}", sharedVariables.isTimeLimit());
    }

    static private void doTest() {
        logger.log(Level.INFO, "OS Type          = {0}", sharedVariables.getOsType());
        logger.log(Level.INFO, "path delimitator = {0}", sharedVariables.getDelimit());
        logger.log(Level.INFO, "user name        = {0}", sharedVariables.getUserName());
        logger.log(Level.INFO, "password         = {0}", sharedVariables.getPassWord());
        logger.log(Level.INFO, "host name        = {0}", sharedVariables.getHostName());


        Manager manager = new Manager.Builder(TableName.F2F.getNumOfOpponents()).build();
        robotTest(manager);

        if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
            logger.log(Level.WARNING, "Kill reached! {0} found!", sharedVariables.getKillFile());
        }

        SQLManagerInterface mySQLManager = SQLManagerFactory.getInstance(TableName.F2F);
        DataSourceManager dataSourceManager = DataSourceManager.getDataSourceManager();
        mySQLManager.setDataSourceManager(dataSourceManager);
        logger.log(Level.INFO, "Driver : {0}", sharedVariables.getRemoteDriver());
        logger.log(Level.INFO, "JDBC   : {0}", sharedVariables.getRemoteJdbc());
        dataSourceManager.initialize();
        if (mySQLManager.test(false)) {
            logger.info("Database store procedure test ok!");
        } else {
            logger.severe("Database store procedure test failed!");
        }

        if (sharedVariables.isLocalDb()) {
            logger.log(Level.INFO, "Driver : {0}", sharedVariables.getLocalDriver());
            logger.log(Level.INFO, "JDBC   : {0}", sharedVariables.getLocalJdbc());
            if (mySQLManager.test(true)) {
                logger.info("Database store procedure test ok!");
            } else {
                logger.severe("Database store procedure test failed!");
            }
        }
        dataSourceManager.closeAll();
        logger.info("Test completed");
    }

    private static void doSetup(TableName tableName) {
        SQLManagerInterface mySQLManager = SQLManagerFactory.getInstance(tableName);
        DataSourceManager dataSourceManager = DataSourceManager.getDataSourceManager();
        mySQLManager.setDataSourceManager(dataSourceManager);
        dataSourceManager.initialize();

        mySQLManager.setupTable();
        mySQLManager.setupResults();

        dataSourceManager.closeAll();
    }

    private static void InitializeRobots() {
        if (sharedVariables.isLocalDb()) {
            logger.info("Start locally initialize robots ... ");
            inputFileReader();
            SQLManagerInterface myLocalSQLManager = SQLManagerFactory.getInstance(TableName.F2F); //Any table
            DataSourceManager dataSourceManager = DataSourceManager.getDataSourceManager();
            myLocalSQLManager.setDataSourceManager(dataSourceManager);
            dataSourceManager.initialize();
            myLocalSQLManager.initializeRobots(robots);
            dataSourceManager.closeAll();
            logger.info("Init completed.");
        } else {
            logger.warning("Can't inizialize robots remotely!");
        }
        if (!sharedVariables.isSetupMode()) {
            System.exit(0);
        }
    }

    private static void doSetup() {
        logger.info("Setting Up Tournament...");
        inputFileReader();
        SQLManagerInterface mySQLManager = SQLManagerFactory.getInstance(TableName.F2F);
        DataSourceManager dataSourceManager = DataSourceManager.getDataSourceManager();
        mySQLManager.setDataSourceManager(dataSourceManager);
        dataSourceManager.initialize();

        mySQLManager.setupRobots(robots, false);
        if (sharedVariables.isLocalDb()) {
            mySQLManager.setupRobots(robots, true);
        }

        dataSourceManager.closeAll();

        if (sharedVariables.isAllRounds()) {
            doSetup(TableName.F2F);
            doSetup(TableName.VS3);
            doSetup(TableName.VS4);
        } else {
            if (sharedVariables.isOnlyF2F()) {
                doSetup(TableName.F2F);
            }
            if (sharedVariables.isOnly3vs3()) {
                doSetup(TableName.VS3);
            }
            if (sharedVariables.isOnly4vs4()) {
                doSetup(TableName.VS4);
            }
        }

        logger.info("Setup completed.");
        System.exit(0);
    }

    private static void robotTest(Manager manager) {
        inputFileReader();

        if (robots != null && robots.size() > 0) {
            int ok = 0, failure = 0;
            for (String tempRobot : robots) {
                String in = sharedVariables.getCmdScript() + " Thread_0 1 " + sharedVariables.getPath()
                        + sharedVariables.getDelimit() + tempRobot + " "
                        + sharedVariables.getPath() + sharedVariables.getDelimit() + tempRobot;
                String[] outCmd = manager.cmdExec(in.toString());

                if (outCmd != null) {

                    String robot, games, won, tie, lost, point, cmdString;

                    cmdString = outCmd[0];
                    if ((cmdString != null) && (cmdString.length() > 60)) {
                        robot = cmdString.substring(4, 17).trim();
                        games = cmdString.substring(18, 27).trim();
                        won = cmdString.substring(28, 37).trim();
                        tie = cmdString.substring(38, 47).trim();
                        lost = cmdString.substring(48, 57).trim();
                        point = cmdString.substring(58, 68).trim();

                        logger.log(Level.INFO, "Test ok {0} games={1} wins={2} tie={3} lost={4} point={5}", new Object[]{robot, games, won, tie, lost, point});
                        ok++;
                    } else {
                        logger.log(Level.SEVERE, "Test {0} failed!", tempRobot);
                        failure++;
                    }
                } else {
                    logger.log(Level.SEVERE, "Test {0} failed!", tempRobot);
                    failure++;
                }
            }
            logger.log(Level.INFO, "Test OK(s)={0}; Failure(s)={1}", new Object[]{ok, failure});
        }
    }

    @Deprecated
    static private void doHTTPTest() {

        logger.info("OS Type          = " + sharedVariables.getOsType());
        logger.info("path delimitator = " + sharedVariables.getDelimit());
        logger.info("user name        = " + sharedVariables.getUserName());
        logger.info("password         = " + sharedVariables.getPassWord());
        logger.info("host name        = " + sharedVariables.getHostName());

        Manager manager = new Manager.Builder(TableName.F2F.getNumOfOpponents()).build();

        robotTest(manager);

        if (sharedVariables.isKill() && sharedVariables.getKillfile().exists()) {
            logger.warning("Kill reached! " + sharedVariables.getKillFile() + " found!");
        }

        logger.fine("Starting HTTP connection test...");
        String temp = manager.encrypt("test");

        String url = sharedVariables.getHostName() + "/test.php?build=" + BUILD + "&checksum="
                + temp;

        HTTPClient httpc = new HTTPClient(sharedVariables.getUserName(), sharedVariables.getPassWord());
        try {
            logger.fine(httpc.doQuery(url));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Crobots {0}", e);
        }
        logger.fine("HTTP connection test completed...");
        System.exit(0);
    }
}
