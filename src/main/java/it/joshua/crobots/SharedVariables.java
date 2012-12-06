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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jconfig.Configuration;
import org.jconfig.ConfigurationManager;
import org.jconfig.handler.XMLFileHandler;
/**
 * @author joshua
 * 
 * Shared variables across classes and threads
 * 
 */
public class SharedVariables {
    /**
     * Main singleton & utilities classes
     */
    private static SharedVariables instance = new SharedVariables();
    private static Configuration configuration = ConfigurationManager.getConfiguration("Crobots");
    private static Logger logger;

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
    /**
     * Singleton init
     */
    private SharedVariables() {
        logger = Logger.getLogger(SharedVariables.class.getName());
        if (configuration==null || configuration.isNew()) {
            try {
                File file = new File("Crobots_config.xml");
                
                if (!file.exists()) {
                    file = new File(this.getClass().getClassLoader().getResource("Crobots_config.xml").getFile());
                }
                
                XMLFileHandler handler = new XMLFileHandler();
                handler.setFile(file);
                ConfigurationManager configMgr = ConfigurationManager.getInstance();
                configMgr.load(handler, "Crobots");
                configuration = ConfigurationManager.getConfiguration("Crobots");
                if (configuration.isNew()) {
                    logger.severe("Configuration has not been correctly loaded!");
                    System.exit(-1);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE,"SharedVariables init {0}", e);
                System.exit(-1);
            }
        }

        hostName = configuration.getProperty("hostname", CONST.HTTP_SITE_URL, "HTTPClient");
        userName = configuration.getProperty("username", "crobots", "HTTPClient");
        passWord = configuration.getProperty("pwd", "test", "HTTPClient");
        fileInput = configuration.getProperty("fileinput", CONST.DATA_FILE, "Tournament");
        path = configuration.getProperty("path", CONST.CMD_PATH, "Tournament");
        cmdScript = configuration.getProperty("cmdscript", CONST.CMD_SCRIPT, "Application");
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

        remoteJdbc = configuration.getProperty("jdbc", CONST.JDBC, "RemoteDatabase");
        remoteDriver = configuration.getProperty("driver", CONST.DRIVER, "RemoteDatabase");
        remoteUser = configuration.getProperty("user", "crobots", "RemoteDatabase");
        remotePwd = configuration.getProperty("pwd", "test", "RemoteDatabase");
        remoteAutocommit = configuration.getBooleanProperty("autocommit", true, "RemoteDatabase");
        localJdbc = configuration.getProperty("jdbc", CONST.JDBC, "LocalDatabase");
        localDriver = configuration.getProperty("driver", CONST.DRIVER, "LocalDatabase");
        localUser = configuration.getProperty("user", "crobots", "LocalDatabase");
        localPwd = configuration.getProperty("pwd", "test", "LocalDatabase");
        localAutocommit = configuration.getBooleanProperty("autocommit", true, "LocalDatabase");
        bufferMinSize = configuration.getIntProperty("bufferminsize", 10, "Application");

        int threads = configuration.getIntProperty("maxthreads", 2, "Application");

        if (threads > CONST.MAX_THREADS) {
            logger
                    .warning("Max threads exceeded. Set to "
                    + CONST.MAX_THREADS);
        }

        if (threads < 1) {
            logger.warning("Min threads exceeded. Set to 1");
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
    /**
     * @param args 
     * 
     * Takes command line parameters and ends up the application configuration
     */
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
                            logger.warning("Max threads exeeded. Set to "
                                    + CONST.MAX_THREADS);
                        }

                        if (threads < 1) {
                            logger.warning("Min threads exeeded. Set to 1");
                        }

                        threadNumber = java.lang.Math.min(threads,
                                CONST.MAX_THREADS);
                        threadNumber = java.lang.Math.max(threads, 1);

                    } catch (NumberFormatException e) {
                        logger.severe("Bad input parameter");
                        printUsage = true;
                    }
                }

                if (paramStr == 'T') {
                    try {
                        timeLimitMinutes = Integer.parseInt(args[i + 1]);
                        timeLimit = true;
                    } catch (NumberFormatException e) {
                        logger.severe("Bad input parameter");
                        printUsage = true;
                    }
                }

                if (paramStr == 't') {
                    onlyTest = true;
                    logger.setLevel(Level.ALL);
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
    /**
     * Back-end business logic & threads configuration
     */
    // Thread sleep interval for each match modality
    private Map<TableName, Integer> sleepInterval = new HashMap<>();
    // Master thread sleep interval
    private Integer mainSleepInterval = 1000; // Default 1 s
    // Number of each Crobots match factor repetition
    private Map<TableName, Integer> numOfMatch = new HashMap<>();
    // Number of matches retreived from the database for each call
    private int bigBuffer = 10;
    // Number of match to cache into the buffer collection
    private int bufferSize = 4;
    // If set, the application switches to the HTTP modality
    @Deprecated
    private boolean HTTPMode = false;
    @Deprecated
    private Vector<String> httpURLs;
    @Deprecated
    private String userName = "crobots";
    @Deprecated
    private String passWord = "";
    // Calculated games, ready to be saved into the database
    private AbstractQueue<GamesBean> games = new ConcurrentLinkedQueue<>();
    // Uncalculated games, retreived from the database
    private AbstractQueue<GamesBean> buffer = new ConcurrentLinkedQueue<>();
    // Application running flag
    private volatile boolean runnable = true;
    // Timeout for threads
    private int timeout = 12;
    // Number of active and running slave threads
    private volatile Integer activeThreads;
    // If set, it forces the completion of previously suspended already-calculated matches
    private boolean emptyBuffer = false;
    // If set, any match modality (2, 3 and 4 simultaneously robots) will run
    private boolean allRounds = true;
    // If set, only the face2face (2 simultaneously robots) modality will run
    private boolean onlyF2F = false;
    // If set, only the 3vs3 (3 simultaneously robots) modality will run
    private boolean only3vs3 = false;
    // If set, only the 4vs4 (4 simultaneously robots) modality will run
    private boolean only4vs4 = false;
    // If set, it tests robot into binary compilation and database connections
    private boolean onlyTest = false;
    // If set, it switch on the logger debug level
    private boolean verbose = true;
    // If set, it forces the database-driven matches setup
    private boolean setupMode = false;
    // If set, it forces the database-driven robots setup
    private boolean initMode = false;
    // If set, it forces the print usage visualization
    private boolean printUsage = false;
    // If set, the application uses a secondary (local) database for any match combination
    private boolean localDb = false;
    // If set, the application stops its execution if the timeout has benn exceeded
    private boolean timeLimit = false;
    // Number or simultaneously running slave threads (match calculation)
    private int threadNumber = 2;
    private int bufferMinSize = 10;
    private long timeLimitMinutes = 0;
    private long globalStartTime = 0;
    private String hostName = "localhost";
    private String fileInput = "torneo.dat";
    private String path = "./torneo";
    private String cmdScript = "crobots.sh";
    // Operating system (Windows, UNIX)
    private String osType = "UNIX";
    // Directory separator
    private char delimit = File.separatorChar;
    // Stop file: if exists it stops the run
    private String killFile = "Crobots.stop";
    private boolean kill = false;
    private File killfile = new File(killFile);
    /**
     * Database configuration
     */
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

    public boolean isGamesEmpty() {
        return games.isEmpty();
    }

    public boolean isBufferEmpty() {
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

    public GamesBean getFromGames() {
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
