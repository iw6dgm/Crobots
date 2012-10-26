package it.joshua.crobots;

import it.joshua.crobots.bean.GamesBean;
import it.joshua.crobots.data.CONST;
import it.joshua.crobots.data.TableName;
import java.io.File;
import java.util.AbstractQueue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.SynchronousQueue;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jconfig.Configuration;
import org.jconfig.ConfigurationManager;
import org.jconfig.handler.XMLFileHandler;

public class SharedVariables {

    private static SharedVariables instance = new SharedVariables();
    private static Configuration configuration = ConfigurationManager.getConfiguration("Crobots");
    private static Logger logger = Logger.getLogger(SharedVariables.class.getName());

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static void setConfiguration(Configuration aConfiguration) {
        configuration = aConfiguration;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger aLogger) {
        logger = aLogger;
    }

    public int getBigBuffer() {
        return bigBuffer;
    }

    public void setBigBuffer(int aBigBuffer) {
        bigBuffer = aBigBuffer;
    }

    public Vector<String> getHttpURLs() {
        return httpURLs;
    }

    public void setHttpURLs(Vector<String> aHttpURLs) {
        httpURLs = aHttpURLs;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int aTimeout) {
        timeout = aTimeout;
    }

    public Integer getMainSleepInterval() {
        return mainSleepInterval;
    }

    public void setMainSleepInterval(Integer aMainSleepInterval) {
        mainSleepInterval = aMainSleepInterval;
    }

    public Integer getActiveThreads() {
        return activeThreads;
    }

    public void setActiveThreads(Integer aActiveThreads) {
        activeThreads = aActiveThreads;
    }

    public boolean isEmptyBuffer() {
        return emptyBuffer;
    }

    public void setEmptyBuffer(boolean aEmptyBuffer) {
        emptyBuffer = aEmptyBuffer;
    }

    public boolean isAllRounds() {
        return allRounds;
    }

    public void setAllRounds(boolean aAllRounds) {
        allRounds = aAllRounds;
    }

    public boolean isOnlyF2F() {
        return onlyF2F;
    }

    public void setOnlyF2F(boolean aOnlyF2F) {
        onlyF2F = aOnlyF2F;
    }

    public boolean isOnly3vs3() {
        return only3vs3;
    }

    public void setOnly3vs3(boolean aOnly3vs3) {
        only3vs3 = aOnly3vs3;
    }

    public boolean isOnly4vs4() {
        return only4vs4;
    }

    public void setOnly4vs4(boolean aOnly4vs4) {
        only4vs4 = aOnly4vs4;
    }

    public boolean isOnlyTest() {
        return onlyTest;
    }

    public void setOnlyTest(boolean aOnlyTest) {
        onlyTest = aOnlyTest;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean aVerbose) {
        verbose = aVerbose;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean aTest) {
        test = aTest;
    }

    public boolean isSetupMode() {
        return setupMode;
    }

    public void setSetupMode(boolean aSetupMode) {
        setupMode = aSetupMode;
    }

    public boolean isInitMode() {
        return initMode;
    }

    public void setInitMode(boolean aInitMode) {
        initMode = aInitMode;
    }

    public boolean isPrintUsage() {
        return printUsage;
    }

    public void setPrintUsage(boolean aPrintUsage) {
        printUsage = aPrintUsage;
    }

    public boolean isHTTPMode() {
        return HTTPMode;
    }

    public void setHTTPMode(boolean aHTTPMode) {
        HTTPMode = aHTTPMode;
    }

    public boolean isLocalDb() {
        return localDb;
    }

    public void setLocalDb(boolean aLocalDb) {
        localDb = aLocalDb;
    }

    public boolean isTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(boolean aTimeLimit) {
        timeLimit = aTimeLimit;
    }

    public int getThreadNumber() {
        return threadNumber;
    }

    public void setThreadNumber(int aThreadNumber) {
        threadNumber = aThreadNumber;
    }

    public int getBufferMinSize() {
        return bufferMinSize;
    }

    public void setBufferMinSize(int aBufferMinSize) {
        bufferMinSize = aBufferMinSize;
    }

    public long getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(long aTimeLimitMinutes) {
        timeLimitMinutes = aTimeLimitMinutes;
    }

    public long getGlobalStartTime() {
        return globalStartTime;
    }

    public void setGlobalStartTime(long aGlobalStartTime) {
        globalStartTime = aGlobalStartTime;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String aHostName) {
        hostName = aHostName;
    }

    public String getFileInput() {
        return fileInput;
    }

    public void setFileInput(String aFileInput) {
        fileInput = aFileInput;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String aPath) {
        path = aPath;
    }

    public String getCmdScript() {
        return cmdScript;
    }

    public void setCmdScript(String aCmdScript) {
        cmdScript = aCmdScript;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String aUserName) {
        userName = aUserName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String aPassWord) {
        passWord = aPassWord;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String aOsType) {
        osType = aOsType;
    }

    public char getDelimit() {
        return delimit;
    }

    public void setDelimit(char aDelimit) {
        delimit = aDelimit;
    }

    public String getKillFile() {
        return killFile;
    }

    public void setKillFile(String aKillFile) {
        killFile = aKillFile;
    }

    public boolean isKill() {
        return kill;
    }

    public void setKill(boolean aKill) {
        kill = aKill;
    }

    public File getKillfile() {
        return killfile;
    }

    public void setKillfile(File aKillfile) {
        killfile = aKillfile;
    }

    private SharedVariables() {
        BasicConfigurator.configure();
        PropertyConfigurator.configure("log4j.properties");

        if (configuration==null || configuration.isNew()) {
            try {
                File file = new File("Crobots_config.xml");
                XMLFileHandler handler = new XMLFileHandler();
                handler.setFile(file);
                ConfigurationManager configMgr = ConfigurationManager.getInstance();
                configMgr.load(handler, "Crobots");
                configuration = ConfigurationManager.getConfiguration("Crobots");
                if (configuration.isNew()) {
                    logger.error("Configuration has not been correctly loaded!");
                    System.exit(-1);
                }
            } catch (Exception e) {
                logger.error("", e);
                System.exit(-1);
            }
        }

        hostName = configuration.getProperty("hostname", "http://localhost/crobots/sql", "HTTPClient");
        userName = configuration.getProperty("username", "crobots", "HTTPClient");
        passWord = configuration.getProperty("pwd", "test", "HTTPClient");
        fileInput = configuration.getProperty("fileinput", "torneo.dat", "Tournament");
        path = configuration.getProperty("path", "C:\\crobots", "Tournament");
        cmdScript = configuration.getProperty("cmdscript", "crobots.bat", "Application");
        onlyTest = configuration.getBooleanProperty("onlytest", false, "Application");
        localDb = configuration.getBooleanProperty("localdb", false, "Application");
        mainSleepInterval = configuration.getIntProperty("mainSleepInterval", 1000, "Application");

        if (configuration.getBooleanProperty("onlyf2f", false, "Application")) {
            allRounds = false;
            onlyF2F = true;
        }

        if (configuration.getBooleanProperty("only3vs3", false, "Application")) {
            allRounds = false;
            only3vs3 = true;
        }

        if (configuration.getBooleanProperty("only4vs4", false, "Application")) {
            allRounds = false;
            only4vs4 = true;
        }

        HTTPMode = configuration.getBooleanProperty("httpmode", false, "Application");
        timeout = configuration.getIntProperty("timeout", 12, "Application");
        kill = configuration.getBooleanProperty("killfile", true, "Application");

        remoteJdbc = configuration.getProperty("jdbc", "jdbc:mysql://127.0.01:3306/crobots?user=crobots&password=", "RemoteDatabase");
        remoteDriver = configuration.getProperty("driver", "com.mysql.jdbc.Driver", "RemoteDatabase");
        remoteUser = configuration.getProperty("user", "crobots", "RemoteDatabase");
        remotePwd = configuration.getProperty("pwd", "test", "RemoteDatabase");
        remoteAutocommit = configuration.getBooleanProperty("autocommit", true, "RemoteDatabase");
        localJdbc = configuration.getProperty("jdbc", "jdbc:mysql://127.0.01:3306/crobots?user=crobots&password=", "LocalDatabase");
        localDriver = configuration.getProperty("driver", "com.mysql.jdbc.Driver", "LocalDatabase");
        localUser = configuration.getProperty("user", "crobots", "LocalDatabase");
        localPwd = configuration.getProperty("pwd", "test", "LocalDatabase");
        localAutocommit = configuration.getBooleanProperty("autocommit", true, "LocalDatabase");
        bufferMinSize = configuration.getIntProperty("bufferminsize", 10, "Application");

        int threads = configuration.getIntProperty("maxthreads", 2, "Application");

        if (threads > CONST.MAX_THREADS) {
            logger
                    .warn("Max threads exceeded. Set to "
                    + CONST.MAX_THREADS);
        }

        if (threads < 1) {
            logger.warn("Min threads exceeded. Set to 1");
        }

        threadNumber = java.lang.Math.min(threads, CONST.MAX_THREADS);
        threadNumber = java.lang.Math.max(threads, 1);

        for (TableName name : TableName.values()) {
            sleepInterval.put(name, configuration.getIntProperty(name.getTableName() + "sleepinterval", 1000, "Application"));
            numOfMatch.put(name, configuration.getIntProperty("numofmatch" + name.getTableName(), 0, "Tournament"));
        }

        String os = System.getenv("OS");
        if (os != null && os.length() > 0) {
            logger.info("OS               = " + os);
            if (os.contains("Windows")) {
                osType = "Windoze";
                // delimit = "\\";
            }
        }
    }

    public int getSleepInterval(TableName tableName) {
        return sleepInterval.get(tableName);
    }

    public int getNumOfMatch(TableName tableName) {
        return numOfMatch.get(tableName);
    }

    public void setup(String args[]) {
        int l = args.length;

        // if (l == 0) printUsage();

        for (int i = 0; i < l; i++) {
            String tmpStr = args[i];

            if (tmpStr.charAt(0) == '-') {
                char paramStr = tmpStr.charAt(1);

                if (paramStr == '?') {
                    printUsage = true;
                }

                if (paramStr == 'h') {
                    hostName = args[i + 1];
                }
                if (paramStr == 'f') {
                    fileInput = args[i + 1];
                }
                if (paramStr == 'u') {
                    userName = args[i + 1];
                }
                if (paramStr == 'p') {
                    passWord = args[i + 1];
                }
                if (paramStr == 'P') {
                    path = args[i + 1];
                }
                if (paramStr == 'c') {
                    cmdScript = args[i + 1];
                }

                if (paramStr == 'm') {
                    try {
                        int threads = Integer.parseInt(args[i + 1]);

                        if (threads > CONST.MAX_THREADS) {
                            logger.warn("Max threads exeeded. Set to "
                                    + CONST.MAX_THREADS);
                        }

                        if (threads < 1) {
                            logger.warn("Min threads exeeded. Set to 1");
                        }

                        threadNumber = java.lang.Math.min(threads,
                                CONST.MAX_THREADS);
                        threadNumber = java.lang.Math.max(threads, 1);

                    } catch (NumberFormatException e) {
                        logger.error("Bad input parameter");
                        printUsage = true;
                    }
                }

                if (paramStr == 'T') {
                    try {
                        timeLimitMinutes = Integer.parseInt(args[i + 1]);
                        timeLimit = true;
                    } catch (NumberFormatException e) {
                        logger.error("Bad input parameter");
                        printUsage = true;
                    }
                }

                if (paramStr == 't') {
                    onlyTest = true;
                    logger.setLevel(Level.DEBUG);
                }
                if (paramStr == 'q') {
                    logger.setLevel(Level.INFO);
                }

                if (paramStr == '2') {
                    allRounds = false;
                    onlyF2F = true;
                }
                if (paramStr == '3') {
                    allRounds = false;
                    only3vs3 = true;
                }
                if (paramStr == '4') {
                    allRounds = false;
                    only4vs4 = true;
                }
                if (paramStr == 'H') {
                    HTTPMode = true;
                }

                if (paramStr == 'K') {
                    kill = true;
                }

                if (paramStr == 'E') {
                    emptyBuffer = true;
                }

                if (paramStr == 'S') {
                    setupMode = true;
                }

                if (paramStr == 'I') {
                    initMode = true;
                }
            }
        }
    }

    public static SharedVariables getInstance() {
        return instance;
    }
    private Map<TableName, Integer> sleepInterval = new HashMap<>();
    private Map<TableName, Integer> numOfMatch = new HashMap<>();
    private int bigBuffer = 10;
    private int bufferSize = 4;
    //static Connection connection 		= null;
    private Vector<String> httpURLs;
    private AbstractQueue<GamesBean> games = new SynchronousQueue<>();
    private AbstractQueue<GamesBean> buffer = new SynchronousQueue<>();
    private volatile boolean runnable = true;
    private int timeout = 12;
    private Integer mainSleepInterval = 1000; // Default 1 s
    private volatile Integer activeThreads;
    private boolean emptyBuffer = false;
    private boolean allRounds = true;
    private boolean onlyF2F = false;
    private boolean only3vs3 = false;
    private boolean only4vs4 = false;
    private boolean onlyTest = false;
    private boolean verbose = true;
    private boolean test = false;
    private boolean setupMode = false;
    private boolean initMode = false;
    private boolean printUsage = false;
    private boolean HTTPMode = false;
    private boolean localDb = false;
    private boolean timeLimit = false;
    //public static int mGroup        	= 10;
    private int threadNumber = 2;
    private int bufferMinSize = 10;
    private long timeLimitMinutes = 0;
    private long globalStartTime = 0;
    private String hostName = "localhost";
    private String fileInput = "torneo.dat";
    private String path = "./torneo";
    private String cmdScript = "crobots.sh";
    private String userName = "crobots";
    private String passWord = "";
    private String osType = "UNIX";
    private char delimit = File.separatorChar;
    private String killFile = "Crobots.stop";
    //Database
    private final String remoteDriver;
    private final String localDriver;
    private final String remoteJdbc;
    private final String localJdbc;
    private final String localUser;
    private final String localPwd;
    private final String remoteUser;
    private final String remotePwd;
    private final boolean remoteAutocommit;
    private final boolean localAutocommit;
    private boolean kill = false;
    private File killfile = new File(killFile);

    public synchronized boolean isGameBufferEmpty() {
        return games.isEmpty();
    }

    public synchronized boolean isInputBufferEmpty() {
        return buffer.isEmpty();
    }

    public boolean isRunnable() {
        return runnable;
    }

    public void setRunnable(boolean runnable) {
        this.runnable = runnable;
    }

    public int getBufferSize() {
        return buffer.size();
    }

    public int getGamesSize() {
        return games.size();
    }

    public GamesBean getAndRemoveBean() {
        return games.remove();
    }

    public GamesBean getFromBuffer() {
        return buffer.remove();
    }

    public void addToBuffer(GamesBean bean) {
        buffer.add(bean);
    }

    public void addToGames(GamesBean bean) {
        games.add(bean);
    }
    
    public void addAllToBuffer(List<GamesBean> list) {
        buffer.addAll(list);
    }
    
    public void addAllToGames(List<GamesBean> list) {
        games.addAll(list);
    }

    public AbstractQueue<GamesBean> getGames() {
        return games;
    }

    public AbstractQueue<GamesBean> getBuffer() {
        return buffer;
    }

    public Map<TableName, Integer> getSleepInterval() {
        return sleepInterval;
    }

    public void setSleepInterval(Map<TableName, Integer> sleepInterval) {
        this.sleepInterval = sleepInterval;
    }

    public Map<TableName, Integer> getNumOfMatch() {
        return numOfMatch;
    }

    public void setNumOfMatch(Map<TableName, Integer> numOfMatch) {
        this.numOfMatch = numOfMatch;
    }

    public String getRemoteDriver() {
        return remoteDriver;
    }

    public String getLocalDriver() {
        return localDriver;
    }

    public String getRemoteJdbc() {
        return remoteJdbc;
    }

    public String getLocalJdbc() {
        return localJdbc;
    }

    public String getLocalUser() {
        return localUser;
    }

    public String getLocalPwd() {
        return localPwd;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public String getRemotePwd() {
        return remotePwd;
    }

    public boolean isRemoteAutocommit() {
        return remoteAutocommit;
    }

    public boolean isLocalAutocommit() {
        return localAutocommit;
    }
}
