/**
 * Static pairings generator
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Maurizio Camangi
 *
 */
public class Pairings {
    private final static int GROUP_SIZE = 64; //Desired group size
    private static final int matchF2F = 1000;
    private static final int match3vs3 = 15;
    private static final int match4vs4 = 1;
    private static final String label = "group";
    private static final List<String> torneo1990 = new ArrayList<>(8);
    private static final List<String> torneo1991 = new ArrayList<>(23);
    private static final List<String> torneo1992 = new ArrayList<>(56);
    private static final List<String> torneo1993 = new ArrayList<>(68);
    private static final List<String> torneo1994 = new ArrayList<>(47);
    private static final List<String> torneo1995 = new ArrayList<>(42);
    private static final List<String> torneo1996 = new ArrayList<>(35);
    private static final List<String> torneo1997 = new ArrayList<>(29);
    private static final List<String> torneo1998 = new ArrayList<>(32);
    private static final List<String> torneo1999 = new ArrayList<>(53);
    private static final List<String> torneo2000 = new ArrayList<>(29);
    private static final List<String> torneo2001 = new ArrayList<>(53);
    private static final List<String> torneo2002 = new ArrayList<>(54);
    private static final List<String> torneo2003 = new ArrayList<>(64);
    private static final List<String> torneo2004 = new ArrayList<>(46);
    private static final List<String> torneo2007 = new ArrayList<>(28);
    private static final List<String> torneo2010 = new ArrayList<>(22);
    private static final List<String> torneo2011 = new ArrayList<>(26);
    private static final List<String> torneo2012 = new ArrayList<>(34);
    private static final List<String> torneo2013 = new ArrayList<>(26);
    private static final List<String> torneo2015 = new ArrayList<>(41);
    private static final List<String> torneo2020 = new ArrayList<>(24);
    private static final List<String> micro = new ArrayList<>(23);
    private static final List<String> crobs = new ArrayList<>(97);
    private static final List<String> aminet = new ArrayList<>(8);
    private static final List<String> cplusplus = new ArrayList<>(2);
    private static final List<List<String>> tournaments = new ArrayList<>(26);
    private static final String[] TYPES = {"f2f", "3vs3", "4vs4"};
    private static Set<String> round;
    private static List<Set<String>> rounds;
    private static int robots;

    /**
     * @param args
     */
    public static void main(String[] args) {
        setup();
        countRobots();
        int attempts = 0;
        boolean withConflicts;
        do {
            System.out.println("ATTEMPT : " + ++attempts);
            shuffle();
            collect();
            withConflicts = alternativePairing();
        } while (withConflicts);
        show();
        buildConfigFileYAML();
        buildSQLInserts();
    }
    /**
     * 
     * @param filename
     * @return file basename (no path)
     */
    private static String getBasename(final String filename) {
        File f = new File(filename);
        return f.getName();
    }

    /** 
     * Show pairings (plain text)
     */
    private static void show() {
        int n = 1;
        for (final Set<String> round : rounds) {
            if (round != null && round.size() > 0) {
                System.out.println("------- Group " + n++ + " (size " + round.size() + ") ------");
                for (final String s : round) {
                    System.out.println(s);
                }
            }
        }
    }
    
    /**
     * Show pairings Python format
     * @deprecated Use YAML format instead for GoRobots utility
     */
    @Deprecated
    private static void buildConfigFile() {
        int n = 1;
        for (Set<String> round : rounds) {
            int count = 0;
            if (round.size() > 0) {
                System.out.println("------- CFG group" + n + " ------");
                System.out.println("class Configuration(object):");
                System.out.printf("\tmatchF2F = %d\n\tmatch3VS3 = %d\n\tmatch4VS4 = %d\n\tsourcePath = '.'\n", matchF2F, match3vs3, match4vs4);
                System.out.printf("\tlabel = '%s%d'\n", label, n++);
                final StringBuilder sb = new StringBuilder("\tlistRobots = [");
                for (String s : round) {
                    if (count++ != 0) {
                        sb.append(", ");
                    }
                    sb.append("'").append(s).append("'");
                }
                sb.append("]");
                System.out.println(sb.toString());
            }
        }
    }

    /**
     * Show pairings YAML format
     */
    private static void buildConfigFileYAML() {
        int n = 1;
        for (Set<String> round : rounds) {
            int count = 0;
            if (round.size() > 0) {
                System.out.println("------- CFG group" + n + " ------");
                System.out.printf("matchF2F: %d\nmatch3VS3: %d\nmatch4VS4: %d\nsourcePath: '.'\n", matchF2F, match3vs3, match4vs4);
                System.out.printf("label: '%s%d'\n", label, n++);
                final StringBuilder sb = new StringBuilder("listRobots: [");
                for (String s : round) {
                    if (count++ != 0) {
                        sb.append(", ");
                    }
                    sb.append("'").append(s).append("'");
                }
                sb.append("]");
                System.out.println(sb.toString());
            }
        }
    }

    /**
     * Prints SQL Insert statements to initialise reports tables
     */
    private static void buildSQLInserts() {
        int n = 1;
        for (Set<String> round : rounds) {
            int count = 0;
            int size = round.size()-1;
            if (round.size() > 0) {
                System.out.println("------- SQL group" + n++ + " ------");
                final StringBuilder values = new StringBuilder();
                for (String s : round) {
                    values.append(String.format("('%s')", getBasename(s)));
                    if (count++ != size) {
                        values.append(",\n");
                    }
                }
                values.append(";");
                final String sql = values.toString();
                for (String table : TYPES) {
                    System.out.printf("------- %s -------\n", table);
                    System.out.printf("DELETE FROM `results_%s`;\n", table);
                    System.out.printf("INSERT INTO `results_%s` (robot) VALUES\n", table);
                    System.out.println(sql);
                }
            }
        }

    }

    private static void collect() {
        tournaments.clear();
        tournaments.add(torneo1990);
        tournaments.add(torneo1991);
        tournaments.add(torneo1992);
        tournaments.add(torneo1993);
        tournaments.add(torneo1994);
        tournaments.add(torneo1995);
        tournaments.add(torneo1996);
        tournaments.add(torneo1997);
        tournaments.add(torneo1998);
        tournaments.add(torneo1999);
        tournaments.add(torneo2000);
        tournaments.add(torneo2001);
        tournaments.add(torneo2002);
        tournaments.add(torneo2003);
        tournaments.add(torneo2004);
        tournaments.add(torneo2007);
        tournaments.add(torneo2010);
        tournaments.add(torneo2011);
        tournaments.add(torneo2012);
        tournaments.add(torneo2013);
        tournaments.add(torneo2015);
        tournaments.add(torneo2020);
        tournaments.add(crobs);
        tournaments.add(micro);
        tournaments.add(aminet);
        tournaments.add(cplusplus);

        Collections.shuffle(tournaments);
    }

    private static void countRobots() {
        robots += micro.size();
        robots += crobs.size();
        robots += aminet.size();
        robots += cplusplus.size();
        robots += torneo1990.size();
        robots += torneo1991.size();
        robots += torneo1992.size();
        robots += torneo1993.size();
        robots += torneo1994.size();
        robots += torneo1995.size();
        robots += torneo1996.size();
        robots += torneo1997.size();
        robots += torneo1998.size();
        robots += torneo1999.size();
        robots += torneo2000.size();
        robots += torneo2001.size();
        robots += torneo2002.size();
        robots += torneo2003.size();
        robots += torneo2004.size();
        robots += torneo2007.size();
        robots += torneo2010.size();
        robots += torneo2011.size();
        robots += torneo2012.size();
        robots += torneo2013.size();
        robots += torneo2015.size();
        robots += torneo2020.size();
        System.out.println("TOTAL Robots :" + robots);
    }

    private static boolean alternativePairing() {
        int groupCount = (robots / GROUP_SIZE) + ((robots % GROUP_SIZE) > 0 ? 1 : 0);

        int groupIndex = 0;

        rounds = new ArrayList<>();

        for (int i = 0; i < groupCount; i++) {
            round = new TreeSet<>(RobotComparator.getInstance());
            rounds.add(round);
        }

        for (final List<String> tournament : tournaments) {
            for (final String r : tournament) {
                if (!rounds.get(groupIndex++).add(r)) {
                    return true; // has conflicts
                }
                if (groupIndex == groupCount) {
                    groupIndex = 0;
                }
            }
        }

        return false; // no conflicts
    }

    private static void shuffle() {
        Collections.shuffle(torneo1990);
        Collections.shuffle(torneo1991);
        Collections.shuffle(torneo1992);
        Collections.shuffle(torneo1993);
        Collections.shuffle(torneo1994);
        Collections.shuffle(torneo1995);
        Collections.shuffle(torneo1996);
        Collections.shuffle(torneo1997);
        Collections.shuffle(torneo1998);
        Collections.shuffle(torneo1999);
        Collections.shuffle(torneo2000);
        Collections.shuffle(torneo2001);
        Collections.shuffle(torneo2002);
        Collections.shuffle(torneo2003);
        Collections.shuffle(torneo2004);
        Collections.shuffle(torneo2007);
        Collections.shuffle(torneo2010);
        Collections.shuffle(torneo2011);
        Collections.shuffle(torneo2012);
        Collections.shuffle(torneo2013);
        Collections.shuffle(torneo2015);
        Collections.shuffle(torneo2020);
        Collections.shuffle(crobs);
        Collections.shuffle(micro);
        Collections.shuffle(aminet);
        Collections.shuffle(cplusplus);
    }
    
    private static void setupMidi() {
        
        System.out.print("Loading cplusplus... ");

        cplusplus.add("cplusplus/selvaggio");
        cplusplus.add("cplusplus/vikingo");

        System.out.println(cplusplus.size() + " robot(s)");
        System.out.print("Loading aminet... ");

        aminet.add("aminet/anticlock");
        aminet.add("aminet/beaver");
        aminet.add("aminet/blindschl");
        aminet.add("aminet/blindschl2");
        aminet.add("aminet/mirobot");
        aminet.add("aminet/opfer");
        aminet.add("aminet/schwan");
        aminet.add("aminet/tron");

        System.out.println(aminet.size() + " robot(s)");
        System.out.print("Loading torneo1990... ");

        torneo1990.add("1990/et_1");
        torneo1990.add("1990/et_2");
        torneo1990.add("1990/hunter");
        torneo1990.add("1990/killer");
        torneo1990.add("1990/nexus_1");
        torneo1990.add("1990/rob1");
        torneo1990.add("1990/scanner");
        torneo1990.add("1990/york");

        System.out.println(torneo1990.size() + " robot(s)");
        System.out.print("Loading torneo1991... ");

        torneo1991.add("1991/blade3");
        torneo1991.add("1991/casimiro");
        torneo1991.add("1991/ccyber");
        torneo1991.add("1991/clover");
        torneo1991.add("1991/diagonal");
        torneo1991.add("1991/et_3");
        torneo1991.add("1991/f1");
        torneo1991.add("1991/fdig");
        torneo1991.add("1991/geltrude");
        torneo1991.add("1991/genius_j");
        torneo1991.add("1991/gira");
        torneo1991.add("1991/gunner");
        torneo1991.add("1991/jazz");
        torneo1991.add("1991/nexus_2");
        torneo1991.add("1991/paolo101");
        torneo1991.add("1991/paolo77");
        torneo1991.add("1991/poor");
        torneo1991.add("1991/qibo");
        torneo1991.add("1991/robocop");
        torneo1991.add("1991/runner");
        torneo1991.add("1991/sara_6");
        torneo1991.add("1991/seeker");
        torneo1991.add("1991/warrior2");

        System.out.println(torneo1991.size() + " robot(s)");
        System.out.print("Loading torneo1992... ");

        torneo1992.add("1992/666");
        torneo1992.add("1992/ap_1");
        torneo1992.add("1992/assassin");
        torneo1992.add("1992/baeos");
        torneo1992.add("1992/banzel");
        torneo1992.add("1992/bronx-00");
        torneo1992.add("1992/bry_bry");
        torneo1992.add("1992/crazy");
        torneo1992.add("1992/cube");
        torneo1992.add("1992/cw");
        torneo1992.add("1992/d47");
        torneo1992.add("1992/daitan3");
        torneo1992.add("1992/dancer");
        torneo1992.add("1992/deluxe");
        torneo1992.add("1992/dorsai");
        torneo1992.add("1992/et_4");
        torneo1992.add("1992/et_5");
        torneo1992.add("1992/flash");
        torneo1992.add("1992/genesis");
        torneo1992.add("1992/hunter");
        torneo1992.add("1992/ice");
        torneo1992.add("1992/jack");
        torneo1992.add("1992/jager");
        torneo1992.add("1992/johnny");
        torneo1992.add("1992/lead1");
        torneo1992.add("1992/marika");
        torneo1992.add("1992/mimo6new");
        torneo1992.add("1992/mrcc");
        torneo1992.add("1992/mut");
        torneo1992.add("1992/ninus6");
        torneo1992.add("1992/nl_1a");
        torneo1992.add("1992/nl_1b");
        torneo1992.add("1992/ola");
        torneo1992.add("1992/paolo");
        torneo1992.add("1992/pavido");
        torneo1992.add("1992/phobos_1");
        torneo1992.add("1992/pippo92");
        torneo1992.add("1992/pippo");
        torneo1992.add("1992/raid");
        torneo1992.add("1992/random");
        torneo1992.add("1992/revenge3");
        torneo1992.add("1992/robbie");
        torneo1992.add("1992/robocop2");
        torneo1992.add("1992/robocop");
        torneo1992.add("1992/sassy");
        torneo1992.add("1992/spider");
        torneo1992.add("1992/sp");
        torneo1992.add("1992/superv");
        torneo1992.add("1992/t1000");
        torneo1992.add("1992/thunder");
        torneo1992.add("1992/triangol");
        torneo1992.add("1992/trio");
        torneo1992.add("1992/uanino");
        torneo1992.add("1992/warrior3");
        torneo1992.add("1992/xdraw2");
        torneo1992.add("1992/zorro");

        System.out.println(torneo1992.size() + " robot(s)");
        System.out.print("Loading torneo1993... ");

        torneo1993.add("1993/am_174");
        torneo1993.add("1993/ap_2");
        torneo1993.add("1993/ares");
        torneo1993.add("1993/argon");
        torneo1993.add("1993/aspide");
        torneo1993.add("1993/beast");
        torneo1993.add("1993/biro");
        torneo1993.add("1993/blade8");
        torneo1993.add("1993/boom");
        torneo1993.add("1993/brain");
        torneo1993.add("1993/cantor");
        torneo1993.add("1993/castore");
        torneo1993.add("1993/casual");
        torneo1993.add("1993/corner1d");
        torneo1993.add("1993/corner3");
        torneo1993.add("1993/courage");
        torneo1993.add("1993/(c)");
        torneo1993.add("1993/crob1");
        torneo1993.add("1993/deluxe_2");
        torneo1993.add("1993/deluxe_3");
        torneo1993.add("1993/didimo");
        torneo1993.add("1993/duke");
        torneo1993.add("1993/elija");
        torneo1993.add("1993/fermo");
        torneo1993.add("1993/flash2");
        torneo1993.add("1993/food5");
        torneo1993.add("1993/godel");
        torneo1993.add("1993/gunnyboy");
        torneo1993.add("1993/hamp1");
        torneo1993.add("1993/hamp2");
        torneo1993.add("1993/hell");
        torneo1993.add("1993/horse");
        torneo1993.add("1993/isaac");
        torneo1993.add("1993/kami");
        torneo1993.add("1993/lazy");
        torneo1993.add("1993/mimo13");
        torneo1993.add("1993/mister2");
        torneo1993.add("1993/mister3");
        torneo1993.add("1993/mohawk");
        torneo1993.add("1993/mutation");
        torneo1993.add("1993/ninus17");
        torneo1993.add("1993/nl_2a");
        torneo1993.add("1993/nl_2b");
        torneo1993.add("1993/p68");
        torneo1993.add("1993/p69");
        torneo1993.add("1993/penta");
        torneo1993.add("1993/phobos_2");
        torneo1993.add("1993/pippo93");
        torneo1993.add("1993/pognant");
        torneo1993.add("1993/poirot");
        torneo1993.add("1993/polluce");
        torneo1993.add("1993/premana");
        torneo1993.add("1993/puyopuyo");
        torneo1993.add("1993/raid2");
        torneo1993.add("1993/rapper");
        torneo1993.add("1993/r_cyborg");
        torneo1993.add("1993/r_daneel");
        torneo1993.add("1993/robocop3");
        torneo1993.add("1993/spartaco");
        torneo1993.add("1993/target");
        torneo1993.add("1993/tm");
        torneo1993.add("1993/torneo");
        torneo1993.add("1993/vannina");
        torneo1993.add("1993/vocus");
        torneo1993.add("1993/warrior4");
        torneo1993.add("1993/wassilij");
        torneo1993.add("1993/wolfgang");
        torneo1993.add("1993/zulu");

        System.out.println(torneo1993.size() + " robot(s)");
        System.out.print("Loading torneo1994... ");

        torneo1994.add("1994/8bismark");
        torneo1994.add("1994/anglek2");
        torneo1994.add("1994/apache");
        torneo1994.add("1994/bachopin");
        torneo1994.add("1994/baubau");
        torneo1994.add("1994/biro");
        torneo1994.add("1994/blob");
        torneo1994.add("1994/circlek1");
        torneo1994.add("1994/corner3b");
        torneo1994.add("1994/corner4");
        torneo1994.add("1994/deluxe_4");
        torneo1994.add("1994/deluxe_5");
        torneo1994.add("1994/didimo");
        torneo1994.add("1994/dima10");
        torneo1994.add("1994/dima9");
        torneo1994.add("1994/emanuela");
        torneo1994.add("1994/ematico");
        torneo1994.add("1994/fastfood");
        torneo1994.add("1994/flash3");
        torneo1994.add("1994/funky");
        torneo1994.add("1994/giali1");
        torneo1994.add("1994/hal9000");
        torneo1994.add("1994/heavens");
        torneo1994.add("1994/horse2");
        torneo1994.add("1994/iching");
        torneo1994.add("1994/jet");
        torneo1994.add("1994/ken");
        torneo1994.add("1994/lazyii");
        torneo1994.add("1994/matrox");
        torneo1994.add("1994/maverick");
        torneo1994.add("1994/miaomiao");
        torneo1994.add("1994/nemesi");
        torneo1994.add("1994/ninus75");
        torneo1994.add("1994/patcioca");
        torneo1994.add("1994/pioppo");
        torneo1994.add("1994/pippo94a");
        torneo1994.add("1994/pippo94b");
        torneo1994.add("1994/polipo");
        torneo1994.add("1994/randwall");
        torneo1994.add("1994/robot1");
        torneo1994.add("1994/robot2");
        torneo1994.add("1994/sdix3");
        torneo1994.add("1994/sgnaus");
        torneo1994.add("1994/shadow");
        torneo1994.add("1994/superfly");
        torneo1994.add("1994/the_dam");
        torneo1994.add("1994/t-rex");

        System.out.println(torneo1994.size() + " robot(s)");
        System.out.print("Loading torneo1995... ");

        torneo1995.add("1995/andrea");
        torneo1995.add("1995/animal");
        torneo1995.add("1995/apache95");
        torneo1995.add("1995/archer");
        torneo1995.add("1995/b115e2");
        torneo1995.add("1995/b52");
        torneo1995.add("1995/biro");
        torneo1995.add("1995/boss");
        torneo1995.add("1995/camillo");
        torneo1995.add("1995/carlo");
        torneo1995.add("1995/circle");
        torneo1995.add("1995/cri95");
        torneo1995.add("1995/diablo");
        torneo1995.add("1995/flash4");
        torneo1995.add("1995/hal9000");
        torneo1995.add("1995/heavens");
        torneo1995.add("1995/horse3");
        torneo1995.add("1995/kenii");
        torneo1995.add("1995/losendos");
        torneo1995.add("1995/mikezhar");
        torneo1995.add("1995/ninus99");
        torneo1995.add("1995/paccu");
        torneo1995.add("1995/passion");
        torneo1995.add("1995/peribolo");
        torneo1995.add("1995/pippo95");
        torneo1995.add("1995/rambo");
        torneo1995.add("1995/rocco");
        torneo1995.add("1995/saxy");
        torneo1995.add("1995/sel");
        torneo1995.add("1995/skizzo");
        torneo1995.add("1995/star");
        torneo1995.add("1995/stinger");
        torneo1995.add("1995/tabori-1");
        torneo1995.add("1995/tabori-2");
        torneo1995.add("1995/tequila");
        torneo1995.add("1995/tmii");
        torneo1995.add("1995/tox");
        torneo1995.add("1995/t-rex");
        torneo1995.add("1995/tricky");
        torneo1995.add("1995/twins");
        torneo1995.add("1995/upv-9596");
        torneo1995.add("1995/xenon");

        System.out.println(torneo1995.size() + " robot(s)");
        System.out.print("Loading torneo1996... ");

        torneo1996.add("1996/aleph");
        torneo1996.add("1996/andrea96");
        torneo1996.add("1996/ap_4");
        torneo1996.add("1996/carlo96");
        torneo1996.add("1996/diablo2");
        torneo1996.add("1996/drago5");
        torneo1996.add("1996/d_ray");
        torneo1996.add("1996/fb3");
        torneo1996.add("1996/gevbass");
        torneo1996.add("1996/golem");
        torneo1996.add("1996/gpo2");
        torneo1996.add("1996/hal9000");
        torneo1996.add("1996/heavnew");
        torneo1996.add("1996/hider2");
        torneo1996.add("1996/infinity");
        torneo1996.add("1996/jaja");
        torneo1996.add("1996/memories");
        torneo1996.add("1996/murdoc");
        torneo1996.add("1996/natas");
        torneo1996.add("1996/newb52");
        torneo1996.add("1996/pacio");
        torneo1996.add("1996/pippo96a");
        torneo1996.add("1996/pippo96b");
        torneo1996.add("1996/!");
        torneo1996.add("1996/risk");
        torneo1996.add("1996/robot1");
        torneo1996.add("1996/robot2");
        torneo1996.add("1996/rudolf");
        torneo1996.add("1996/second3");
        torneo1996.add("1996/s-seven");
        torneo1996.add("1996/tatank_3");
        torneo1996.add("1996/tronco");
        torneo1996.add("1996/uht");
        torneo1996.add("1996/xabaras");
        torneo1996.add("1996/yuri");

        System.out.println(torneo1996.size() + " robot(s)");
        System.out.print("Loading torneo1997... ");

        torneo1997.add("1997/1&1");
        torneo1997.add("1997/abyss");
        torneo1997.add("1997/ai1");
        torneo1997.add("1997/andrea97");
        torneo1997.add("1997/arale");
        torneo1997.add("1997/belva");
        torneo1997.add("1997/carlo97");
        torneo1997.add("1997/ciccio");
        torneo1997.add("1997/colossus");
        torneo1997.add("1997/diablo3");
        torneo1997.add("1997/diabolik");
        torneo1997.add("1997/drago6");
        torneo1997.add("1997/erica");
        torneo1997.add("1997/fable");
        torneo1997.add("1997/flash5");
        torneo1997.add("1997/fya");
        torneo1997.add("1997/gevbass2");
        torneo1997.add("1997/golem2");
        torneo1997.add("1997/gundam");
        torneo1997.add("1997/hal9000");
        torneo1997.add("1997/jedi");
        torneo1997.add("1997/kill!");
        torneo1997.add("1997/me-110c");
        torneo1997.add("1997/ncmplt");
        torneo1997.add("1997/paperone");
        torneo1997.add("1997/pippo97");
        torneo1997.add("1997/raid3");
        torneo1997.add("1997/robivinf");
        torneo1997.add("1997/rudolf_2");

        System.out.println(torneo1997.size() + " robot(s)");
        System.out.print("Loading torneo1998... ");

        torneo1998.add("1998/ai2");
        torneo1998.add("1998/bartali");
        torneo1998.add("1998/carla");
        torneo1998.add("1998/coppi");
        torneo1998.add("1998/dia");
        torneo1998.add("1998/dicin");
        torneo1998.add("1998/eva00");
        torneo1998.add("1998/eva01");
        torneo1998.add("1998/freedom");
        torneo1998.add("1998/fscan");
        torneo1998.add("1998/goblin");
        torneo1998.add("1998/goldrake");
        torneo1998.add("1998/hal9000");
        torneo1998.add("1998/heavnew");
        torneo1998.add("1998/maxheav");
        torneo1998.add("1998/ninja");
        torneo1998.add("1998/paranoid");
        torneo1998.add("1998/pippo98");
        torneo1998.add("1998/plump");
        torneo1998.add("1998/quarto");
        torneo1998.add("1998/rattolo");
        torneo1998.add("1998/rudolf_3");
        torneo1998.add("1998/son-goku");
        torneo1998.add("1998/sottolin");
        torneo1998.add("1998/stay");
        torneo1998.add("1998/stighy98");
        torneo1998.add("1998/themicro");
        torneo1998.add("1998/titania");
        torneo1998.add("1998/tornado");
        torneo1998.add("1998/traker1");
        torneo1998.add("1998/traker2");
        torneo1998.add("1998/vision");

        System.out.println(torneo1998.size() + " robot(s)");
        System.out.print("Loading torneo1999... ");

        torneo1999.add("1999/11");
        torneo1999.add("1999/aeris");
        torneo1999.add("1999/akira");
        torneo1999.add("1999/alezai17");
        torneo1999.add("1999/alfa99");
        torneo1999.add("1999/alien");
        torneo1999.add("1999/ap_5");
        torneo1999.add("1999/bastrd!!");
        torneo1999.add("1999/cancer");
        torneo1999.add("1999/carlo99");
        torneo1999.add("1999/#cimice#");
        torneo1999.add("1999/cortez");
        torneo1999.add("1999/cyborg");
        torneo1999.add("1999/dario");
        torneo1999.add("1999/dav46");
        torneo1999.add("1999/defender");
        torneo1999.add("1999/elisir");
        torneo1999.add("1999/flash6");
        torneo1999.add("1999/hal9000");
        torneo1999.add("1999/ilbestio");
        torneo1999.add("1999/jedi2");
        torneo1999.add("1999/ka_aroth");
        torneo1999.add("1999/kakakatz");
        torneo1999.add("1999/lukather");
        torneo1999.add("1999/mancino");
        torneo1999.add("1999/marko");
        torneo1999.add("1999/mcenrobo");
        torneo1999.add("1999/m_hingis");
        torneo1999.add("1999/minatela");
        torneo1999.add("1999/new");
        torneo1999.add("1999/nexus_2");
        torneo1999.add("1999/nl_3a");
        torneo1999.add("1999/nl_3b");
        torneo1999.add("1999/obiwan");
        torneo1999.add("1999/omega99");
        torneo1999.add("1999/panduro");
        torneo1999.add("1999/panic");
        torneo1999.add("1999/pippo99");
        torneo1999.add("1999/pizarro");
        torneo1999.add("1999/quarto");
        torneo1999.add("1999/quingon");
        torneo1999.add("1999/rudolf_4");
        torneo1999.add("1999/satana");
        torneo1999.add("1999/shock");
        torneo1999.add("1999/songohan");
        torneo1999.add("1999/stealth");
        torneo1999.add("1999/storm");
        torneo1999.add("1999/surrende");
        torneo1999.add("1999/t1001");
        torneo1999.add("1999/themicro");
        torneo1999.add("1999/titania2");
        torneo1999.add("1999/vibrsper");
        torneo1999.add("1999/zero");

        System.out.println(torneo1999.size() + " robot(s)");
        System.out.print("Loading torneo2000... ");

        torneo2000.add("2000/bach_2k");
        torneo2000.add("2000/defender");
        torneo2000.add("2000/doppia_g");
        torneo2000.add("2000/flash7");
        torneo2000.add("2000/jedi3");
        torneo2000.add("2000/mancino");
        torneo2000.add("2000/marine");
        torneo2000.add("2000/m_hingis");
        torneo2000.add("2000/navaho");
        
        System.out.println(torneo2000.size() + " robot(s)");
        System.out.print("Loading torneo2001... ");

        torneo2001.add("2001/burrfoot");
        torneo2001.add("2001/charles");
        torneo2001.add("2001/cisc");
        torneo2001.add("2001/cobra");
        torneo2001.add("2001/copter");
        torneo2001.add("2001/defender");
        torneo2001.add("2001/fizban");
        torneo2001.add("2001/gers");
        torneo2001.add("2001/grezbot");
        torneo2001.add("2001/hammer");
        torneo2001.add("2001/heavnew");
        torneo2001.add("2001/homer");
        torneo2001.add("2001/klr2");
        torneo2001.add("2001/kyashan");
        torneo2001.add("2001/max10");
        torneo2001.add("2001/mflash2");
        torneo2001.add("2001/microdna");
        torneo2001.add("2001/midi_zai");
        torneo2001.add("2001/mnl_1a");
        torneo2001.add("2001/mnl_1b");
        torneo2001.add("2001/murray");
        torneo2001.add("2001/neo0");
        torneo2001.add("2001/nl_5b");
        torneo2001.add("2001/pippo1a");
        torneo2001.add("2001/pippo1b");
        torneo2001.add("2001/raistlin");
        torneo2001.add("2001/ridicol");
        torneo2001.add("2001/risc");
        torneo2001.add("2001/rudy_xp");
        torneo2001.add("2001/sdc2");
        torneo2001.add("2001/staticii");
        torneo2001.add("2001/thunder");
        torneo2001.add("2001/vampire");
        
        System.out.println(torneo2001.size() + " robot(s)");
        System.out.print("Loading torneo2002... ");

        torneo2002.add("2002/01");
        torneo2002.add("2002/adsl");
        torneo2002.add("2002/anakin");
        torneo2002.add("2002/asterix");
        torneo2002.add("2002/bruenor");
        torneo2002.add("2002/colera");
        torneo2002.add("2002/colosseum");
        torneo2002.add("2002/copter_2");
        torneo2002.add("2002/corner5");
        torneo2002.add("2002/doom2099");
        torneo2002.add("2002/dynamite");
        torneo2002.add("2002/enigma");
        torneo2002.add("2002/groucho");
        torneo2002.add("2002/halman");
        torneo2002.add("2002/harpo");
        torneo2002.add("2002/idefix");
        torneo2002.add("2002/kyash_2");
        torneo2002.add("2002/marco");
        torneo2002.add("2002/mazinga");
        torneo2002.add("2002/medioman");
        torneo2002.add("2002/mg_one");
        torneo2002.add("2002/mind");
        torneo2002.add("2002/neo_sifr");
        torneo2002.add("2002/ollio");
        torneo2002.add("2002/padawan");
        torneo2002.add("2002/peste");
        torneo2002.add("2002/pippo2a");
        torneo2002.add("2002/pippo2b");
        torneo2002.add("2002/regis");
        torneo2002.add("2002/scsi");
        torneo2002.add("2002/serse");
        torneo2002.add("2002/ska");
        torneo2002.add("2002/stanlio");
        torneo2002.add("2002/staticxp");
        torneo2002.add("2002/supernov");
        torneo2002.add("2002/tifo");
        torneo2002.add("2002/tigre");
        torneo2002.add("2002/todos");
        torneo2002.add("2002/tomahawk");
        torneo2002.add("2002/vaiolo");
        torneo2002.add("2002/vauban");
        torneo2002.add("2002/yoyo");
        
        System.out.println(torneo2002.size() + " robot(s)");
        System.out.print("Loading torneo2003... ");

        torneo2003.add("2003/730");
        torneo2003.add("2003/adrian");
        torneo2003.add("2003/ares");
        torneo2003.add("2003/barbarian");
        torneo2003.add("2003/blitz");
        torneo2003.add("2003/briscolo");
        torneo2003.add("2003/bruce");
        torneo2003.add("2003/cadderly");
        torneo2003.add("2003/cariddi");
        torneo2003.add("2003/cvirus2");
        torneo2003.add("2003/cvirus");
        torneo2003.add("2003/danica");
        torneo2003.add("2003/dynacond");
        torneo2003.add("2003/falco");
        torneo2003.add("2003/foursquare");
        torneo2003.add("2003/frame");
        torneo2003.add("2003/herpes");
        torneo2003.add("2003/ici");
        torneo2003.add("2003/instict");
        torneo2003.add("2003/irpef");
        torneo2003.add("2003/janu");
        torneo2003.add("2003/kyash_3c");
        torneo2003.add("2003/kyash_3m");
        torneo2003.add("2003/lbr1");
        torneo2003.add("2003/lbr");
        torneo2003.add("2003/lebbra");
        torneo2003.add("2003/mg_two");
        torneo2003.add("2003/minicond");
        torneo2003.add("2003/morituro");
        torneo2003.add("2003/nautilus");
        torneo2003.add("2003/nemo");
        torneo2003.add("2003/neo_sel");
        torneo2003.add("2003/piiico");
        torneo2003.add("2003/pippo3b");
        torneo2003.add("2003/pippo3");
        torneo2003.add("2003/red_wolf");
        torneo2003.add("2003/scanner");
        torneo2003.add("2003/scilla");
        torneo2003.add("2003/sirio");
        torneo2003.add("2003/sith");
        torneo2003.add("2003/sky");
        torneo2003.add("2003/spaceman");
        torneo2003.add("2003/tartaruga");
        torneo2003.add("2003/valevan");
        torneo2003.add("2003/virus2");
        torneo2003.add("2003/virus");
        torneo2003.add("2003/yoda");
        
        System.out.println(torneo2003.size() + " robot(s)");
        System.out.print("Loading torneo2004... ");

        torneo2004.add("2004/adam");
        torneo2004.add("2004/b_selim");
        torneo2004.add("2004/!caos");
        torneo2004.add("2004/ciclope");
        torneo2004.add("2004/coyote");
        torneo2004.add("2004/diodo");
        torneo2004.add("2004/fisco");
        torneo2004.add("2004/gostar");
        torneo2004.add("2004/gotar2");
        torneo2004.add("2004/gotar");
        torneo2004.add("2004/irap");
        torneo2004.add("2004/ires");
        torneo2004.add("2004/magneto");
        torneo2004.add("2004/mg_three");
        torneo2004.add("2004/mystica");
        torneo2004.add("2004/n3g4_jr");
        torneo2004.add("2004/n3g4tivo");
        torneo2004.add("2004/new_mini");
        torneo2004.add("2004/pippo04a");
        torneo2004.add("2004/pippo04b");
        torneo2004.add("2004/poldo");
        torneo2004.add("2004/puma");
        torneo2004.add("2004/rat-man");
        torneo2004.add("2004/ravatto");
        torneo2004.add("2004/rotar");
        torneo2004.add("2004/selim_b");
        torneo2004.add("2004/unlimited");
        torneo2004.add("2004/wgdi");
        torneo2004.add("2004/zener");
        torneo2004.add("2004/!zeus");
        
        System.out.println(torneo2004.size() + " robot(s)");
        System.out.print("Loading torneo2007... ");

        torneo2007.add("2007/angel");
        torneo2007.add("2007/back");
        torneo2007.add("2007/brontolo");
        torneo2007.add("2007/electron");
        torneo2007.add("2007/e");
        torneo2007.add("2007/gongolo");
        torneo2007.add("2007/iceman");
        torneo2007.add("2007/mammolo");
        torneo2007.add("2007/microbo1");
        torneo2007.add("2007/microbo2");
        torneo2007.add("2007/midi1");
        torneo2007.add("2007/neutron");
        torneo2007.add("2007/pippo07a");
        torneo2007.add("2007/pippo07b");
        torneo2007.add("2007/pisolo");
        torneo2007.add("2007/pyro");
        torneo2007.add("2007/rythm");
        torneo2007.add("2007/tobey");
        torneo2007.add("2007/t");
        torneo2007.add("2007/zigozago");
        
        System.out.println(torneo2007.size() + " robot(s)");
        System.out.print("Loading torneo2010... ");

        torneo2010.add("2010/buffy");
        torneo2010.add("2010/cancella");
        torneo2010.add("2010/copia");
        torneo2010.add("2010/enkidu");
        torneo2010.add("2010/eurialo");
        torneo2010.add("2010/hal9010");
        torneo2010.add("2010/macchia");
        torneo2010.add("2010/niso");
        torneo2010.add("2010/party");
        torneo2010.add("2010/pippo10a");
        torneo2010.add("2010/reuben");
        torneo2010.add("2010/stitch");
        torneo2010.add("2010/sweat");
        torneo2010.add("2010/taglia");
        torneo2010.add("2010/toppa");
        torneo2010.add("2010/wall-e");
        
        System.out.println(torneo2010.size() + " robot(s)");
        System.out.print("Loading torneo2011... ");

        torneo2011.add("2011/ataman");
        torneo2011.add("2011/coeurl");
        torneo2011.add("2011/gerty");
        torneo2011.add("2011/hal9011");
        torneo2011.add("2011/jeeg");
        torneo2011.add("2011/minion");
        torneo2011.add("2011/nikita");
        torneo2011.add("2011/origano");
        torneo2011.add("2011/ortica");
        torneo2011.add("2011/pain");
        torneo2011.add("2011/piperita");
        torneo2011.add("2011/pippo11a");
        torneo2011.add("2011/pippo11b");
        torneo2011.add("2011/tannhause");
        torneo2011.add("2011/unmaldestr");
        torneo2011.add("2011/vector");
        torneo2011.add("2011/wall-e_ii");
        
        System.out.println(torneo2011.size() + " robot(s)");
        System.out.print("Loading torneo2012... ");

        torneo2012.add("2012/avoider");
        torneo2012.add("2012/beat");
        torneo2012.add("2012/british");
        torneo2012.add("2012/camille");
        torneo2012.add("2012/china");
        torneo2012.add("2012/dampyr");
        torneo2012.add("2012/easyjet");
        torneo2012.add("2012/flash8c");
        torneo2012.add("2012/flash8e");
        torneo2012.add("2012/gerty2");
        torneo2012.add("2012/grezbot2");
        torneo2012.add("2012/gunnyb29");
        torneo2012.add("2012/hal9012");
        torneo2012.add("2012/lycan");
        torneo2012.add("2012/mister2b");
        torneo2012.add("2012/mister3b");
        torneo2012.add("2012/pippo12a");
        torneo2012.add("2012/pippo12b");
        torneo2012.add("2012/power");
        torneo2012.add("2012/puffomic");
        torneo2012.add("2012/puffomid");
        torneo2012.add("2012/q");
        torneo2012.add("2012/ryanair");
        torneo2012.add("2012/silversurf");
        torneo2012.add("2012/torchio");
        torneo2012.add("2012/wall-e_iii");
        torneo2012.add("2012/yeti");
        
        System.out.println(torneo2012.size() + " robot(s)");
        System.out.print("Loading torneo2013... ");

        torneo2013.add("2013/axolotl");
        torneo2013.add("2013/destro");
        torneo2013.add("2013/eternity");
        torneo2013.add("2013/frisa_13");
        torneo2013.add("2013/gerty3");
        torneo2013.add("2013/ghostrider");
        torneo2013.add("2013/guanaco");
        torneo2013.add("2013/gunnyb13");
        torneo2013.add("2013/hal9013");
        torneo2013.add("2013/jarvis");
        torneo2013.add("2013/lamela");
        torneo2013.add("2013/leopon");
        torneo2013.add("2013/ncc-1701");
        torneo2013.add("2013/osvaldo");
        torneo2013.add("2013/pippo13a");
        torneo2013.add("2013/pippo13b");
        torneo2013.add("2013/pray");
        torneo2013.add("2013/ug2k");
        torneo2013.add("2013/wall-e_iv");
        
        System.out.println(torneo2013.size() + " robot(s)");
        System.out.print("Loading torneo2015... ");

        torneo2015.add("2015/antman");
        torneo2015.add("2015/aswhup");
//        torneo2015.add("2015/avoider");
        torneo2015.add("2015/babadook");
        torneo2015.add("2015/circles15");
        torneo2015.add("2015/colour");
        torneo2015.add("2015/coppi15mc1");
        torneo2015.add("2015/coppi15md1");
        torneo2015.add("2015/flash9");
        torneo2015.add("2015/frank15");
        torneo2015.add("2015/gerty4");
        torneo2015.add("2015/hal9015");
        torneo2015.add("2015/hulk");
        torneo2015.add("2015/linabo15");
        torneo2015.add("2015/lluke");
        torneo2015.add("2015/mcfly");
        torneo2015.add("2015/mike3");
        torneo2015.add("2015/pippo15a");
        torneo2015.add("2015/pippo15b");
        torneo2015.add("2015/puppet");
        torneo2015.add("2015/randguard");
        torneo2015.add("2015/salippo");
        torneo2015.add("2015/sidewalk");
        torneo2015.add("2015/thor");
        torneo2015.add("2015/tux");
        torneo2015.add("2015/tyrion");
        torneo2015.add("2015/wall-e_v");


        System.out.println(torneo2015.size() + " robot(s)");
        System.out.print("Loading torneo2020... ");

        torneo2020.add("2020/antman_20");
        torneo2020.add("2020/b4b");
        torneo2020.add("2020/brexit");
        torneo2020.add("2020/coppi20mc1");
        torneo2020.add("2020/coppi20md1");
        torneo2020.add("2020/discotek");
        torneo2020.add("2020/flash10");
        torneo2020.add("2020/gerty5");
        torneo2020.add("2020/hal9020");
        torneo2020.add("2020/hulk_20");
        torneo2020.add("2020/jarvis2");
        torneo2020.add("2020/leavy2");
        torneo2020.add("2020/loneliness");
        torneo2020.add("2020/pippo20a");
        torneo2020.add("2020/pippo20b");
        torneo2020.add("2020/wall-e_vi");
        torneo2020.add("2020/wizard2");

        System.out.println(torneo2020.size() + " robot(s)");
        System.out.print("Loading crobs... ");

        crobs.add("crobs/adversar");
        crobs.add("crobs/agressor");
        crobs.add("crobs/antru");
        crobs.add("crobs/assassin");
        crobs.add("crobs/b4");
        crobs.add("crobs/bishop");
        crobs.add("crobs/bouncer");
        crobs.add("crobs/boxer");
        crobs.add("crobs/cassius");
        crobs.add("crobs/catfish3");
        crobs.add("crobs/chase");
        crobs.add("crobs/chaser");
        crobs.add("crobs/cooper1");
        crobs.add("crobs/cooper2");
        crobs.add("crobs/cornerkl");
        crobs.add("crobs/counter");
        crobs.add("crobs/counter2");
        crobs.add("crobs/cruiser");
        crobs.add("crobs/cspotrun");
        crobs.add("crobs/danimal");
        crobs.add("crobs/dave");
        crobs.add("crobs/di");
        crobs.add("crobs/dirtyh");
        crobs.add("crobs/duck");
        crobs.add("crobs/dumbname");
        crobs.add("crobs/etf_kid");
        crobs.add("crobs/flyby");
        crobs.add("crobs/fred");
        crobs.add("crobs/friendly");
        crobs.add("crobs/grunt");
        crobs.add("crobs/gsmr2");
        crobs.add("crobs/h-k");
        crobs.add("crobs/hac_atak");
        crobs.add("crobs/hak3");
        crobs.add("crobs/hitnrun");
        crobs.add("crobs/hunter");
        crobs.add("crobs/huntlead");
        crobs.add("crobs/intrcptr");
        crobs.add("crobs/jagger");
        crobs.add("crobs/jason100");
        crobs.add("crobs/kamikaze");
        crobs.add("crobs/killer");
        crobs.add("crobs/leader");
        crobs.add("crobs/leavy");
        crobs.add("crobs/lethal");
        crobs.add("crobs/maniac");
        crobs.add("crobs/marvin");
        crobs.add("crobs/mini");
        crobs.add("crobs/ninja");
        crobs.add("crobs/nord");
        crobs.add("crobs/nord2");
        crobs.add("crobs/ogre");
        crobs.add("crobs/ogre2");
        crobs.add("crobs/ogre3");
        crobs.add("crobs/perizoom");
        crobs.add("crobs/pest");
        crobs.add("crobs/phantom");
        crobs.add("crobs/pingpong");
        crobs.add("crobs/politik");
        crobs.add("crobs/pzk");
        crobs.add("crobs/pzkmin");
        crobs.add("crobs/quack");
        crobs.add("crobs/quikshot");
        crobs.add("crobs/rabbit10");
        crobs.add("crobs/rambo3");
        crobs.add("crobs/rapest");
        crobs.add("crobs/reflex");
        crobs.add("crobs/robbie");
        crobs.add("crobs/rook");
        crobs.add("crobs/rungun");
        crobs.add("crobs/samurai");
        crobs.add("crobs/scan");
        crobs.add("crobs/scanlock");
        crobs.add("crobs/scanner");
        crobs.add("crobs/secro");
        crobs.add("crobs/sentry");
        crobs.add("crobs/shark3");
        crobs.add("crobs/shark4");
        crobs.add("crobs/silly");
        crobs.add("crobs/slead");
        crobs.add("crobs/sniper");
        crobs.add("crobs/spinner");
        crobs.add("crobs/spot");
        crobs.add("crobs/squirrel");
        crobs.add("crobs/stalker");
        crobs.add("crobs/stush-1");
        crobs.add("crobs/topgun");
        crobs.add("crobs/tracker");
        crobs.add("crobs/trial4");
        crobs.add("crobs/twedlede");
        crobs.add("crobs/twedledm");
        crobs.add("crobs/venom");
        crobs.add("crobs/watchdog");
        crobs.add("crobs/wizard");
        crobs.add("crobs/xecutner");
        crobs.add("crobs/xhatch");
        crobs.add("crobs/yal");

        System.out.println(crobs.size() + " robot(s)");
        System.out.print("Loading micro... ");

        micro.add("micro/caccola");
        micro.add("micro/carletto");
        micro.add("micro/chobin");
        micro.add("micro/dream");
        micro.add("micro/ld");
        micro.add("micro/lucifer");
        micro.add("micro/marlene");
        micro.add("micro/md8");
        micro.add("micro/md9");
        micro.add("micro/mflash");
        micro.add("micro/minizai");
        micro.add("micro/pacoon");
        micro.add("micro/pikachu");
        micro.add("micro/pippo00a");
        micro.add("micro/pippo00");
        micro.add("micro/pirla");
        micro.add("micro/p");
        micro.add("micro/rudy");
        micro.add("micro/static");
        micro.add("micro/tanzen");
        micro.add("micro/uhm");
        micro.add("micro/zioalfa");
        micro.add("micro/zzz");

        System.out.println(micro.size() + " robot(s)");
        
    }
    
    private static void setupMicro() {
        System.out.print("Loading cplusplus... ");

        cplusplus.add("cplusplus/selvaggio");
        cplusplus.add("cplusplus/vikingo");

        System.out.println(cplusplus.size() + " robot(s)");
        System.out.print("Loading aminet... ");
        
        aminet.add("aminet/anticlock");
        aminet.add("aminet/mirobot");
        aminet.add("aminet/schwan");
        aminet.add("aminet/tron");
        
        System.out.println(aminet.size() + " robot(s)");
        System.out.print("Loading torneo1990... ");

        torneo1990.add("1990/et_1");
        torneo1990.add("1990/et_2");
        torneo1990.add("1990/hunter");
        torneo1990.add("1990/nexus_1");
        torneo1990.add("1990/scanner");
        
        System.out.println(torneo1990.size() + " robot(s)");
        System.out.print("Loading torneo1991... ");

        torneo1991.add("1991/blade3");
        torneo1991.add("1991/ccyber");
        torneo1991.add("1991/diagonal");
        torneo1991.add("1991/et_3");
        torneo1991.add("1991/fdig");
        torneo1991.add("1991/genius_j");
        torneo1991.add("1991/gira");
        torneo1991.add("1991/gunner");
        torneo1991.add("1991/jazz");
        torneo1991.add("1991/nexus_2");
        torneo1991.add("1991/paolo101");
        torneo1991.add("1991/paolo77");
        torneo1991.add("1991/poor");
        torneo1991.add("1991/robocop");
        torneo1991.add("1991/runner");
        torneo1991.add("1991/seeker");
        torneo1991.add("1991/warrior2");
        
        System.out.println(torneo1991.size() + " robot(s)");
        System.out.print("Loading torneo1992... ");

        torneo1992.add("1992/ap_1");
        torneo1992.add("1992/assassin");
        torneo1992.add("1992/baeos");
        torneo1992.add("1992/banzel");
        torneo1992.add("1992/bronx-00");
        torneo1992.add("1992/bry_bry");
        torneo1992.add("1992/crazy");
        torneo1992.add("1992/d47");
        torneo1992.add("1992/daitan3");
        torneo1992.add("1992/dancer");
        torneo1992.add("1992/deluxe");
        torneo1992.add("1992/et_4");
        torneo1992.add("1992/et_5");
        torneo1992.add("1992/flash");
        torneo1992.add("1992/genesis");
        torneo1992.add("1992/hunter");
        torneo1992.add("1992/ice");
        torneo1992.add("1992/johnny");
        torneo1992.add("1992/mimo6new");
        torneo1992.add("1992/mut");
        torneo1992.add("1992/ninus6");
        torneo1992.add("1992/nl_1a");
        torneo1992.add("1992/nl_1b");
        torneo1992.add("1992/ola");
        torneo1992.add("1992/paolo");
        torneo1992.add("1992/pavido");
        torneo1992.add("1992/phobos_1");
        torneo1992.add("1992/pippo");
        torneo1992.add("1992/raid");
        torneo1992.add("1992/random");
        torneo1992.add("1992/revenge3");
        torneo1992.add("1992/robbie");
        torneo1992.add("1992/robocop2");
        torneo1992.add("1992/robocop");
        torneo1992.add("1992/superv");
        torneo1992.add("1992/t1000");
        torneo1992.add("1992/thunder");
        torneo1992.add("1992/trio");
        torneo1992.add("1992/uanino");
        torneo1992.add("1992/warrior3");

        System.out.println(torneo1992.size() + " robot(s)");
        System.out.print("Loading torneo1993... ");

        torneo1993.add("1993/ares");
        torneo1993.add("1993/argon");
        torneo1993.add("1993/aspide");
        torneo1993.add("1993/beast");
        torneo1993.add("1993/biro");
        torneo1993.add("1993/boom");
        torneo1993.add("1993/casual");
        torneo1993.add("1993/corner1d");
        torneo1993.add("1993/corner3");
        torneo1993.add("1993/courage");
        torneo1993.add("1993/(c)");
        torneo1993.add("1993/crob1");
        torneo1993.add("1993/deluxe_2");
        torneo1993.add("1993/didimo");
        torneo1993.add("1993/elija");
        torneo1993.add("1993/fermo");
        torneo1993.add("1993/flash2");
        torneo1993.add("1993/gunnyboy");
        torneo1993.add("1993/hell");
        torneo1993.add("1993/horse");
        torneo1993.add("1993/isaac");
        torneo1993.add("1993/kami");
        torneo1993.add("1993/lazy");
        torneo1993.add("1993/mimo13");
        torneo1993.add("1993/mohawk");
        torneo1993.add("1993/ninus17");
        torneo1993.add("1993/nl_2a");
        torneo1993.add("1993/nl_2b");
        torneo1993.add("1993/phobos_2");
        torneo1993.add("1993/pippo93");
        torneo1993.add("1993/pognant");
        torneo1993.add("1993/premana");
        torneo1993.add("1993/raid2");
        torneo1993.add("1993/rapper");
        torneo1993.add("1993/r_cyborg");
        torneo1993.add("1993/r_daneel");
        torneo1993.add("1993/robocop3");
        torneo1993.add("1993/spartaco");
        torneo1993.add("1993/target");
        torneo1993.add("1993/torneo");
        torneo1993.add("1993/vannina");
        torneo1993.add("1993/wassilij");
        torneo1993.add("1993/wolfgang");
        torneo1993.add("1993/zulu");
        
        System.out.println(torneo1993.size() + " robot(s)");
        System.out.print("Loading torneo1994... ");

        torneo1994.add("1994/anglek2");
        torneo1994.add("1994/baubau");
        torneo1994.add("1994/biro");
        torneo1994.add("1994/circlek1");
        torneo1994.add("1994/corner3b");
        torneo1994.add("1994/didimo");
        torneo1994.add("1994/dima10");
        torneo1994.add("1994/dima9");
        torneo1994.add("1994/emanuela");
        torneo1994.add("1994/ematico");
        torneo1994.add("1994/heavens");
        torneo1994.add("1994/iching");
        torneo1994.add("1994/jet");
        torneo1994.add("1994/nemesi");
        torneo1994.add("1994/ninus75");
        torneo1994.add("1994/pioppo");
        torneo1994.add("1994/pippo94b");
        torneo1994.add("1994/robot1");
        torneo1994.add("1994/robot2");
        torneo1994.add("1994/superfly");
        torneo1994.add("1994/the_dam");
        torneo1994.add("1994/t-rex");
        
        System.out.println(torneo1994.size() + " robot(s)");
        System.out.print("Loading torneo1995... ");

        torneo1995.add("1995/andrea");
        torneo1995.add("1995/b115e2");
        torneo1995.add("1995/carlo");
        torneo1995.add("1995/circle");
        torneo1995.add("1995/diablo");
        torneo1995.add("1995/flash4");
        torneo1995.add("1995/heavens");
        torneo1995.add("1995/mikezhar");
        torneo1995.add("1995/ninus99");
        torneo1995.add("1995/rocco");
        torneo1995.add("1995/sel");
        torneo1995.add("1995/skizzo");
        torneo1995.add("1995/tmii");
        
        System.out.println(torneo1995.size() + " robot(s)");
        System.out.print("Loading torneo1996... ");

        torneo1996.add("1996/andrea96");
        torneo1996.add("1996/carlo96");
        torneo1996.add("1996/drago5");
        torneo1996.add("1996/d_ray");
        torneo1996.add("1996/gpo2");
        torneo1996.add("1996/murdoc");
        torneo1996.add("1996/natas");
        torneo1996.add("1996/risk");
        torneo1996.add("1996/tronco");
        torneo1996.add("1996/yuri");

        System.out.println(torneo1996.size() + " robot(s)");
        System.out.print("Loading torneo1997... ");

        torneo1997.add("1997/ciccio");
        torneo1997.add("1997/drago6");
        torneo1997.add("1997/erica");
        torneo1997.add("1997/fya");
        torneo1997.add("1997/pippo97");
        torneo1997.add("1997/raid3");
        
        System.out.println(torneo1997.size() + " robot(s)");
        System.out.print("Loading torneo1998... ");

        torneo1998.add("1998/carla");
        torneo1998.add("1998/fscan");
        torneo1998.add("1998/maxheav");
        torneo1998.add("1998/pippo98");
        torneo1998.add("1998/plump");
        torneo1998.add("1998/themicro");
        torneo1998.add("1998/traker1");
        
        System.out.println(torneo1998.size() + " robot(s)");
        System.out.print("Loading torneo1999... ");

        torneo1999.add("1999/ap_5");
        torneo1999.add("1999/flash6");
        torneo1999.add("1999/mcenrobo");
        torneo1999.add("1999/nexus_2");
        torneo1999.add("1999/surrende");
        torneo1999.add("1999/themicro");
        
        System.out.println(torneo1999.size() + " robot(s)");
        System.out.print("Loading torneo2000... ");

        System.out.println(torneo2000.size() + " robot(s)");
        System.out.print("Loading torneo2001... ");

        torneo2001.add("2001/burrfoot");
        torneo2001.add("2001/charles");
        torneo2001.add("2001/cisc");
        torneo2001.add("2001/cobra");
        torneo2001.add("2001/copter");
        torneo2001.add("2001/gers");
        torneo2001.add("2001/grezbot");
        torneo2001.add("2001/hammer");
        torneo2001.add("2001/homer");
        torneo2001.add("2001/klr2");
        torneo2001.add("2001/kyashan");
        torneo2001.add("2001/max10");
        torneo2001.add("2001/mflash2");
        torneo2001.add("2001/microdna");
        torneo2001.add("2001/midi_zai");
        torneo2001.add("2001/mnl_1a");
        torneo2001.add("2001/mnl_1b");
        torneo2001.add("2001/murray");
        torneo2001.add("2001/neo0");
        torneo2001.add("2001/pippo1a");
        torneo2001.add("2001/pippo1b");
        torneo2001.add("2001/raistlin");
        torneo2001.add("2001/ridicol");
        torneo2001.add("2001/risc");
        torneo2001.add("2001/rudy_xp");
        torneo2001.add("2001/sdc2");
        torneo2001.add("2001/staticii");
        torneo2001.add("2001/thunder");
        torneo2001.add("2001/vampire");
        
        System.out.println(torneo2001.size() + " robot(s)");
        System.out.print("Loading torneo2002... ");

        torneo2002.add("2002/01");
        torneo2002.add("2002/adsl");
        torneo2002.add("2002/anakin");
        torneo2002.add("2002/copter_2");
        torneo2002.add("2002/corner5");
        torneo2002.add("2002/doom2099");
        torneo2002.add("2002/groucho");
        torneo2002.add("2002/idefix");
        torneo2002.add("2002/kyash_2");
        torneo2002.add("2002/marco");
        torneo2002.add("2002/mazinga");
        torneo2002.add("2002/mind");
        torneo2002.add("2002/neo_sifr");
        torneo2002.add("2002/pippo2a");
        torneo2002.add("2002/pippo2b");
        torneo2002.add("2002/regis");
        torneo2002.add("2002/scsi");
        torneo2002.add("2002/ska");
        torneo2002.add("2002/stanlio");
        torneo2002.add("2002/staticxp");
        torneo2002.add("2002/supernov");
        torneo2002.add("2002/tigre");
        torneo2002.add("2002/vaiolo");
        torneo2002.add("2002/vauban");
        
        System.out.println(torneo2002.size() + " robot(s)");
        System.out.print("Loading torneo2003... ");

        torneo2003.add("2003/730");
        torneo2003.add("2003/barbarian");
        torneo2003.add("2003/blitz");
        torneo2003.add("2003/briscolo");
        torneo2003.add("2003/bruce");
        torneo2003.add("2003/cvirus");
        torneo2003.add("2003/danica");
        torneo2003.add("2003/falco");
        torneo2003.add("2003/foursquare");
        torneo2003.add("2003/frame");
        torneo2003.add("2003/herpes");
        torneo2003.add("2003/ici");
        torneo2003.add("2003/instict");
        torneo2003.add("2003/janu");
        torneo2003.add("2003/kyash_3m");
        torneo2003.add("2003/lbr1");
        torneo2003.add("2003/lbr");
        torneo2003.add("2003/lebbra");
        torneo2003.add("2003/minicond");
        torneo2003.add("2003/morituro");
        torneo2003.add("2003/nemo");
        torneo2003.add("2003/neo_sel");
        torneo2003.add("2003/piiico");
        torneo2003.add("2003/pippo3b");
        torneo2003.add("2003/pippo3");
        torneo2003.add("2003/red_wolf");
        torneo2003.add("2003/scilla");
        torneo2003.add("2003/sirio");
        torneo2003.add("2003/tartaruga");
        torneo2003.add("2003/valevan");
        torneo2003.add("2003/virus");
        torneo2003.add("2003/yoda");
        
        System.out.println(torneo2003.size() + " robot(s)");
        System.out.print("Loading torneo2004... ");

        torneo2004.add("2004/adam");
        torneo2004.add("2004/!caos");
        torneo2004.add("2004/ciclope");
        torneo2004.add("2004/coyote");
        torneo2004.add("2004/diodo");
        torneo2004.add("2004/gostar");
        torneo2004.add("2004/gotar2");
        torneo2004.add("2004/gotar");
        torneo2004.add("2004/irap");
        torneo2004.add("2004/magneto");
        torneo2004.add("2004/n3g4_jr");
        torneo2004.add("2004/new_mini");
        torneo2004.add("2004/pippo04a");
        torneo2004.add("2004/pippo04b");
        torneo2004.add("2004/poldo");
        torneo2004.add("2004/puma");
        torneo2004.add("2004/rat-man");
        torneo2004.add("2004/ravatto");
        torneo2004.add("2004/rotar");
        torneo2004.add("2004/selim_b");
        torneo2004.add("2004/unlimited");
        
        System.out.println(torneo2004.size() + " robot(s)");
        System.out.print("Loading torneo2007... ");

        torneo2007.add("2007/angel");
        torneo2007.add("2007/back");
        torneo2007.add("2007/brontolo");
        torneo2007.add("2007/electron");
        torneo2007.add("2007/gongolo");
        torneo2007.add("2007/microbo1");
        torneo2007.add("2007/microbo2");
        torneo2007.add("2007/pippo07a");
        torneo2007.add("2007/pippo07b");
        torneo2007.add("2007/pisolo");
        torneo2007.add("2007/pyro");
        torneo2007.add("2007/tobey");
        torneo2007.add("2007/t");
        torneo2007.add("2007/zigozago");
        
        System.out.println(torneo2007.size() + " robot(s)");
        System.out.print("Loading torneo2010... ");

        torneo2010.add("2010/copia");
        torneo2010.add("2010/eurialo");
        torneo2010.add("2010/macchia");
        torneo2010.add("2010/niso");
        torneo2010.add("2010/pippo10a");
        torneo2010.add("2010/stitch");
        torneo2010.add("2010/sweat");
        torneo2010.add("2010/taglia");
        torneo2010.add("2010/wall-e");
        
        System.out.println(torneo2010.size() + " robot(s)");
        System.out.print("Loading torneo2011... ");

        torneo2011.add("2011/ataman");
        torneo2011.add("2011/coeurl");
        torneo2011.add("2011/minion");
        torneo2011.add("2011/pain");
        torneo2011.add("2011/piperita");
        torneo2011.add("2011/pippo11a");
        torneo2011.add("2011/pippo11b");
        torneo2011.add("2011/tannhause");
        torneo2011.add("2011/unmaldestr");
        torneo2011.add("2011/wall-e_ii");
        
        System.out.println(torneo2011.size() + " robot(s)");
        System.out.print("Loading torneo2012... ");

        torneo2012.add("2012/avoider");
        torneo2012.add("2012/beat");
        torneo2012.add("2012/china");
        torneo2012.add("2012/easyjet");
        torneo2012.add("2012/flash8c");
        torneo2012.add("2012/flash8e");
        torneo2012.add("2012/grezbot2");
        torneo2012.add("2012/lycan");
        torneo2012.add("2012/pippo12a");
        torneo2012.add("2012/pippo12b");
        torneo2012.add("2012/puffomic");
        torneo2012.add("2012/ryanair");
        torneo2012.add("2012/silversurf");
        torneo2012.add("2012/wall-e_iii");
        
        System.out.println(torneo2012.size() + " robot(s)");
        System.out.print("Loading torneo2013... ");

        torneo2013.add("2013/axolotl");
        torneo2013.add("2013/destro");
        torneo2013.add("2013/osvaldo");
        torneo2013.add("2013/pippo13a");
        torneo2013.add("2013/pippo13b");
        torneo2013.add("2013/pray");
        torneo2013.add("2013/wall-e_iv");

        System.out.println(torneo2013.size() + " robot(s)");
        System.out.print("Loading torneo2015... ");

        torneo2015.add("2015/antman");
        torneo2015.add("2015/aswhup");
//        torneo2015.add("2015/avoider");
        torneo2015.add("2015/babadook");
        torneo2015.add("2015/colour");
        torneo2015.add("2015/coppi15mc1");
        torneo2015.add("2015/flash9");
        torneo2015.add("2015/linabo15");
        torneo2015.add("2015/mike3");
        torneo2015.add("2015/pippo15a");
        torneo2015.add("2015/pippo15b");
        torneo2015.add("2015/puppet");
        torneo2015.add("2015/randguard");
        torneo2015.add("2015/salippo");
        torneo2015.add("2015/sidewalk");
        torneo2015.add("2015/tux");
        torneo2015.add("2015/tyrion");
        torneo2015.add("2015/wall-e_v");

        System.out.println(torneo2015.size() + " robot(s)");
        System.out.print("Loading torneo2020... ");

        torneo2020.add("2020/antman_20");
        torneo2020.add("2020/b4b");
        torneo2020.add("2020/brexit");
        torneo2020.add("2020/coppi20mc1");
        torneo2020.add("2020/discotek");
        torneo2020.add("2020/flash10");
        torneo2020.add("2020/pippo20a");
        torneo2020.add("2020/pippo20b");
        torneo2020.add("2020/wall-e_vi");
        System.out.println(torneo2020.size() + " robot(s)");
        System.out.print("Loading crobs... ");

        crobs.add("crobs/adversar");
        crobs.add("crobs/agressor");
        crobs.add("crobs/assassin");
        crobs.add("crobs/b4");
        crobs.add("crobs/bishop");
        crobs.add("crobs/bouncer");
        crobs.add("crobs/cassius");
        crobs.add("crobs/catfish3");
        crobs.add("crobs/chase");
        crobs.add("crobs/chaser");
        crobs.add("crobs/cornerkl");
        crobs.add("crobs/counter2");
        crobs.add("crobs/cruiser");
        crobs.add("crobs/cspotrun");
        crobs.add("crobs/danimal");
        crobs.add("crobs/dave");
        crobs.add("crobs/di");
        crobs.add("crobs/dirtyh");
        crobs.add("crobs/duck");
        crobs.add("crobs/etf_kid");
        crobs.add("crobs/flyby");
        crobs.add("crobs/fred");
        crobs.add("crobs/grunt");
        crobs.add("crobs/gsmr2");
        crobs.add("crobs/hac_atak");
        crobs.add("crobs/hak3");
        crobs.add("crobs/hitman");
        crobs.add("crobs/h-k");
        crobs.add("crobs/hunter");
        crobs.add("crobs/huntlead");
        crobs.add("crobs/intrcptr");
        crobs.add("crobs/kamikaze");
        crobs.add("crobs/killer");
        crobs.add("crobs/leader");
        crobs.add("crobs/marvin");
        crobs.add("crobs/micro");
        crobs.add("crobs/mini");
        crobs.add("crobs/ninja");
        crobs.add("crobs/nord2");
        crobs.add("crobs/nord");
        crobs.add("crobs/ogre2");
        crobs.add("crobs/ogre");
        crobs.add("crobs/pest");
        crobs.add("crobs/phantom");
        crobs.add("crobs/pingpong");
        crobs.add("crobs/pzkmin");
        crobs.add("crobs/pzk");
        crobs.add("crobs/quack");
        crobs.add("crobs/quikshot");
        crobs.add("crobs/rabbit10");
        crobs.add("crobs/rabbit");
        crobs.add("crobs/rambo3");
        crobs.add("crobs/rapest");
        crobs.add("crobs/reflex");
        crobs.add("crobs/rungun");
        crobs.add("crobs/scanlock");
        crobs.add("crobs/scanner");
        crobs.add("crobs/scan");
        crobs.add("crobs/sentry");
        crobs.add("crobs/silly");
        crobs.add("crobs/slead");
        crobs.add("crobs/spinner");
        crobs.add("crobs/spot");
        crobs.add("crobs/squirrel");
        crobs.add("crobs/stush-1");
        crobs.add("crobs/topgun");
        crobs.add("crobs/tracker");
        crobs.add("crobs/twedlede");
        crobs.add("crobs/twedledm");
        crobs.add("crobs/venom");
        crobs.add("crobs/watchdog");
        crobs.add("crobs/xecutner");
        crobs.add("crobs/xhatch");
        crobs.add("crobs/yal");
        
        System.out.println(crobs.size() + " robot(s)");
        System.out.print("Loading micro... ");

        micro.add("micro/caccola");
        micro.add("micro/carletto");
        micro.add("micro/chobin");
        micro.add("micro/dream");
        micro.add("micro/ld");
        micro.add("micro/lucifer");
        micro.add("micro/marlene");
        micro.add("micro/md8");
        micro.add("micro/md9");
        micro.add("micro/mflash");
        micro.add("micro/minizai");
        micro.add("micro/pacoon");
        micro.add("micro/pikachu");
        micro.add("micro/pippo00a");
        micro.add("micro/pippo00");
        micro.add("micro/pirla");
        micro.add("micro/p");
        micro.add("micro/rudy");
        micro.add("micro/static");
        micro.add("micro/tanzen");
        micro.add("micro/uhm");
        micro.add("micro/zioalfa");
        micro.add("micro/zzz");
        
        System.out.println(micro.size() + " robot(s)");        
    }

    private static void setup() {
        System.out.print("Loading cplusplus... ");

        cplusplus.add("cplusplus/selvaggio");
        cplusplus.add("cplusplus/vikingo");

        System.out.println(cplusplus.size() + " robot(s)");
        System.out.print("Loading aminet... ");

        aminet.add("aminet/anticlock");
        aminet.add("aminet/beaver");
        aminet.add("aminet/blindschl");
        aminet.add("aminet/blindschl2");
        aminet.add("aminet/mirobot");
        aminet.add("aminet/opfer");
        aminet.add("aminet/schwan");
        aminet.add("aminet/tron");

        System.out.println(aminet.size() + " robot(s)");
        System.out.print("Loading torneo1990... ");

        torneo1990.add("1990/et_1");
        torneo1990.add("1990/et_2");
        torneo1990.add("1990/hunter");
        torneo1990.add("1990/killer");
        torneo1990.add("1990/nexus_1");
        torneo1990.add("1990/rob1");
        torneo1990.add("1990/scanner");
        torneo1990.add("1990/york");

        System.out.println(torneo1990.size() + " robot(s)");
        System.out.print("Loading torneo1991... ");

        torneo1991.add("1991/blade3");
        torneo1991.add("1991/casimiro");
        torneo1991.add("1991/ccyber");
        torneo1991.add("1991/clover");
        torneo1991.add("1991/diagonal");
        torneo1991.add("1991/et_3");
        torneo1991.add("1991/f1");
        torneo1991.add("1991/fdig");
        torneo1991.add("1991/geltrude");
        torneo1991.add("1991/genius_j");
        torneo1991.add("1991/gira");
        torneo1991.add("1991/gunner");
        torneo1991.add("1991/jazz");
        torneo1991.add("1991/nexus_2");
        torneo1991.add("1991/paolo101");
        torneo1991.add("1991/paolo77");
        torneo1991.add("1991/poor");
        torneo1991.add("1991/qibo");
        torneo1991.add("1991/robocop");
        torneo1991.add("1991/runner");
        torneo1991.add("1991/sara_6");
        torneo1991.add("1991/seeker");
        torneo1991.add("1991/warrior2");

        System.out.println(torneo1991.size() + " robot(s)");
        System.out.print("Loading torneo1992... ");

        torneo1992.add("1992/666");
        torneo1992.add("1992/ap_1");
        torneo1992.add("1992/assassin");
        torneo1992.add("1992/baeos");
        torneo1992.add("1992/banzel");
        torneo1992.add("1992/bronx-00");
        torneo1992.add("1992/bry_bry");
        torneo1992.add("1992/crazy");
        torneo1992.add("1992/cube");
        torneo1992.add("1992/cw");
        torneo1992.add("1992/d47");
        torneo1992.add("1992/daitan3");
        torneo1992.add("1992/dancer");
        torneo1992.add("1992/deluxe");
        torneo1992.add("1992/dorsai");
        torneo1992.add("1992/et_4");
        torneo1992.add("1992/et_5");
        torneo1992.add("1992/flash");
        torneo1992.add("1992/genesis");
        torneo1992.add("1992/hunter");
        torneo1992.add("1992/ice");
        torneo1992.add("1992/jack");
        torneo1992.add("1992/jager");
        torneo1992.add("1992/johnny");
        torneo1992.add("1992/lead1");
        torneo1992.add("1992/marika");
        torneo1992.add("1992/mimo6new");
        torneo1992.add("1992/mrcc");
        torneo1992.add("1992/mut");
        torneo1992.add("1992/ninus6");
        torneo1992.add("1992/nl_1a");
        torneo1992.add("1992/nl_1b");
        torneo1992.add("1992/ola");
        torneo1992.add("1992/paolo");
        torneo1992.add("1992/pavido");
        torneo1992.add("1992/phobos_1");
        torneo1992.add("1992/pippo92");
        torneo1992.add("1992/pippo");
        torneo1992.add("1992/raid");
        torneo1992.add("1992/random");
        torneo1992.add("1992/revenge3");
        torneo1992.add("1992/robbie");
        torneo1992.add("1992/robocop2");
        torneo1992.add("1992/robocop");
        torneo1992.add("1992/sassy");
        torneo1992.add("1992/spider");
        torneo1992.add("1992/sp");
        torneo1992.add("1992/superv");
        torneo1992.add("1992/t1000");
        torneo1992.add("1992/thunder");
        torneo1992.add("1992/triangol");
        torneo1992.add("1992/trio");
        torneo1992.add("1992/uanino");
        torneo1992.add("1992/warrior3");
        torneo1992.add("1992/xdraw2");
        torneo1992.add("1992/zorro");

        System.out.println(torneo1992.size() + " robot(s)");
        System.out.print("Loading torneo1993... ");

        torneo1993.add("1993/am_174");
        torneo1993.add("1993/ap_2");
        torneo1993.add("1993/ares");
        torneo1993.add("1993/argon");
        torneo1993.add("1993/aspide");
        torneo1993.add("1993/beast");
        torneo1993.add("1993/biro");
        torneo1993.add("1993/blade8");
        torneo1993.add("1993/boom");
        torneo1993.add("1993/brain");
        torneo1993.add("1993/cantor");
        torneo1993.add("1993/castore");
        torneo1993.add("1993/casual");
        torneo1993.add("1993/corner1d");
        torneo1993.add("1993/corner3");
        torneo1993.add("1993/courage");
        torneo1993.add("1993/(c)");
        torneo1993.add("1993/crob1");
        torneo1993.add("1993/deluxe_2");
        torneo1993.add("1993/deluxe_3");
        torneo1993.add("1993/didimo");
        torneo1993.add("1993/duke");
        torneo1993.add("1993/elija");
        torneo1993.add("1993/fermo");
        torneo1993.add("1993/flash2");
        torneo1993.add("1993/food5");
        torneo1993.add("1993/godel");
        torneo1993.add("1993/gunnyboy");
        torneo1993.add("1993/hamp1");
        torneo1993.add("1993/hamp2");
        torneo1993.add("1993/hell");
        torneo1993.add("1993/horse");
        torneo1993.add("1993/isaac");
        torneo1993.add("1993/kami");
        torneo1993.add("1993/lazy");
        torneo1993.add("1993/mimo13");
        torneo1993.add("1993/mister2");
        torneo1993.add("1993/mister3");
        torneo1993.add("1993/mohawk");
        torneo1993.add("1993/mutation");
        torneo1993.add("1993/ninus17");
        torneo1993.add("1993/nl_2a");
        torneo1993.add("1993/nl_2b");
        torneo1993.add("1993/p68");
        torneo1993.add("1993/p69");
        torneo1993.add("1993/penta");
        torneo1993.add("1993/phobos_2");
        torneo1993.add("1993/pippo93");
        torneo1993.add("1993/pognant");
        torneo1993.add("1993/poirot");
        torneo1993.add("1993/polluce");
        torneo1993.add("1993/premana");
        torneo1993.add("1993/puyopuyo");
        torneo1993.add("1993/raid2");
        torneo1993.add("1993/rapper");
        torneo1993.add("1993/r_cyborg");
        torneo1993.add("1993/r_daneel");
        torneo1993.add("1993/robocop3");
        torneo1993.add("1993/spartaco");
        torneo1993.add("1993/target");
        torneo1993.add("1993/tm");
        torneo1993.add("1993/torneo");
        torneo1993.add("1993/vannina");
        torneo1993.add("1993/vocus");
        torneo1993.add("1993/warrior4");
        torneo1993.add("1993/wassilij");
        torneo1993.add("1993/wolfgang");
        torneo1993.add("1993/zulu");

        System.out.println(torneo1993.size() + " robot(s)");
        System.out.print("Loading torneo1994... ");

        torneo1994.add("1994/8bismark");
        torneo1994.add("1994/anglek2");
        torneo1994.add("1994/apache");
        torneo1994.add("1994/bachopin");
        torneo1994.add("1994/baubau");
        torneo1994.add("1994/biro");
        torneo1994.add("1994/blob");
        torneo1994.add("1994/circlek1");
        torneo1994.add("1994/corner3b");
        torneo1994.add("1994/corner4");
        torneo1994.add("1994/deluxe_4");
        torneo1994.add("1994/deluxe_5");
        torneo1994.add("1994/didimo");
        torneo1994.add("1994/dima10");
        torneo1994.add("1994/dima9");
        torneo1994.add("1994/emanuela");
        torneo1994.add("1994/ematico");
        torneo1994.add("1994/fastfood");
        torneo1994.add("1994/flash3");
        torneo1994.add("1994/funky");
        torneo1994.add("1994/giali1");
        torneo1994.add("1994/hal9000");
        torneo1994.add("1994/heavens");
        torneo1994.add("1994/horse2");
        torneo1994.add("1994/iching");
        torneo1994.add("1994/jet");
        torneo1994.add("1994/ken");
        torneo1994.add("1994/lazyii");
        torneo1994.add("1994/matrox");
        torneo1994.add("1994/maverick");
        torneo1994.add("1994/miaomiao");
        torneo1994.add("1994/nemesi");
        torneo1994.add("1994/ninus75");
        torneo1994.add("1994/patcioca");
        torneo1994.add("1994/pioppo");
        torneo1994.add("1994/pippo94a");
        torneo1994.add("1994/pippo94b");
        torneo1994.add("1994/polipo");
        torneo1994.add("1994/randwall");
        torneo1994.add("1994/robot1");
        torneo1994.add("1994/robot2");
        torneo1994.add("1994/sdix3");
        torneo1994.add("1994/sgnaus");
        torneo1994.add("1994/shadow");
        torneo1994.add("1994/superfly");
        torneo1994.add("1994/the_dam");
        torneo1994.add("1994/t-rex");

        System.out.println(torneo1994.size() + " robot(s)");
        System.out.print("Loading torneo1995... ");

        torneo1995.add("1995/andrea");
        torneo1995.add("1995/animal");
        torneo1995.add("1995/apache95");
        torneo1995.add("1995/archer");
        torneo1995.add("1995/b115e2");
        torneo1995.add("1995/b52");
        torneo1995.add("1995/biro");
        torneo1995.add("1995/boss");
        torneo1995.add("1995/camillo");
        torneo1995.add("1995/carlo");
        torneo1995.add("1995/circle");
        torneo1995.add("1995/cri95");
        torneo1995.add("1995/diablo");
        torneo1995.add("1995/flash4");
        torneo1995.add("1995/hal9000");
        torneo1995.add("1995/heavens");
        torneo1995.add("1995/horse3");
        torneo1995.add("1995/kenii");
        torneo1995.add("1995/losendos");
        torneo1995.add("1995/mikezhar");
        torneo1995.add("1995/ninus99");
        torneo1995.add("1995/paccu");
        torneo1995.add("1995/passion");
        torneo1995.add("1995/peribolo");
        torneo1995.add("1995/pippo95");
        torneo1995.add("1995/rambo");
        torneo1995.add("1995/rocco");
        torneo1995.add("1995/saxy");
        torneo1995.add("1995/sel");
        torneo1995.add("1995/skizzo");
        torneo1995.add("1995/star");
        torneo1995.add("1995/stinger");
        torneo1995.add("1995/tabori-1");
        torneo1995.add("1995/tabori-2");
        torneo1995.add("1995/tequila");
        torneo1995.add("1995/tmii");
        torneo1995.add("1995/tox");
        torneo1995.add("1995/t-rex");
        torneo1995.add("1995/tricky");
        torneo1995.add("1995/twins");
        torneo1995.add("1995/upv-9596");
        torneo1995.add("1995/xenon");

        System.out.println(torneo1995.size() + " robot(s)");
        System.out.print("Loading torneo1996... ");

        torneo1996.add("1996/aleph");
        torneo1996.add("1996/andrea96");
        torneo1996.add("1996/ap_4");
        torneo1996.add("1996/carlo96");
        torneo1996.add("1996/diablo2");
        torneo1996.add("1996/drago5");
        torneo1996.add("1996/d_ray");
        torneo1996.add("1996/fb3");
        torneo1996.add("1996/gevbass");
        torneo1996.add("1996/golem");
        torneo1996.add("1996/gpo2");
        torneo1996.add("1996/hal9000");
        torneo1996.add("1996/heavnew");
        torneo1996.add("1996/hider2");
        torneo1996.add("1996/infinity");
        torneo1996.add("1996/jaja");
        torneo1996.add("1996/memories");
        torneo1996.add("1996/murdoc");
        torneo1996.add("1996/natas");
        torneo1996.add("1996/newb52");
        torneo1996.add("1996/pacio");
        torneo1996.add("1996/pippo96a");
        torneo1996.add("1996/pippo96b");
        torneo1996.add("1996/!");
        torneo1996.add("1996/risk");
        torneo1996.add("1996/robot1");
        torneo1996.add("1996/robot2");
        torneo1996.add("1996/rudolf");
        torneo1996.add("1996/second3");
        torneo1996.add("1996/s-seven");
        torneo1996.add("1996/tatank_3");
        torneo1996.add("1996/tronco");
        torneo1996.add("1996/uht");
        torneo1996.add("1996/xabaras");
        torneo1996.add("1996/yuri");

        System.out.println(torneo1996.size() + " robot(s)");
        System.out.print("Loading torneo1997... ");

        torneo1997.add("1997/1&1");
        torneo1997.add("1997/abyss");
        torneo1997.add("1997/ai1");
        torneo1997.add("1997/andrea97");
        torneo1997.add("1997/arale");
        torneo1997.add("1997/belva");
        torneo1997.add("1997/carlo97");
        torneo1997.add("1997/ciccio");
        torneo1997.add("1997/colossus");
        torneo1997.add("1997/diablo3");
        torneo1997.add("1997/diabolik");
        torneo1997.add("1997/drago6");
        torneo1997.add("1997/erica");
        torneo1997.add("1997/fable");
        torneo1997.add("1997/flash5");
        torneo1997.add("1997/fya");
        torneo1997.add("1997/gevbass2");
        torneo1997.add("1997/golem2");
        torneo1997.add("1997/gundam");
        torneo1997.add("1997/hal9000");
        torneo1997.add("1997/jedi");
        torneo1997.add("1997/kill!");
        torneo1997.add("1997/me-110c");
        torneo1997.add("1997/ncmplt");
        torneo1997.add("1997/paperone");
        torneo1997.add("1997/pippo97");
        torneo1997.add("1997/raid3");
        torneo1997.add("1997/robivinf");
        torneo1997.add("1997/rudolf_2");

        System.out.println(torneo1997.size() + " robot(s)");
        System.out.print("Loading torneo1998... ");

        torneo1998.add("1998/ai2");
        torneo1998.add("1998/bartali");
        torneo1998.add("1998/carla");
        torneo1998.add("1998/coppi");
        torneo1998.add("1998/dia");
        torneo1998.add("1998/dicin");
        torneo1998.add("1998/eva00");
        torneo1998.add("1998/eva01");
        torneo1998.add("1998/freedom");
        torneo1998.add("1998/fscan");
        torneo1998.add("1998/goblin");
        torneo1998.add("1998/goldrake");
        torneo1998.add("1998/hal9000");
        torneo1998.add("1998/heavnew");
        torneo1998.add("1998/maxheav");
        torneo1998.add("1998/ninja");
        torneo1998.add("1998/paranoid");
        torneo1998.add("1998/pippo98");
        torneo1998.add("1998/plump");
        torneo1998.add("1998/quarto");
        torneo1998.add("1998/rattolo");
        torneo1998.add("1998/rudolf_3");
        torneo1998.add("1998/son-goku");
        torneo1998.add("1998/sottolin");
        torneo1998.add("1998/stay");
        torneo1998.add("1998/stighy98");
        torneo1998.add("1998/themicro");
        torneo1998.add("1998/titania");
        torneo1998.add("1998/tornado");
        torneo1998.add("1998/traker1");
        torneo1998.add("1998/traker2");
        torneo1998.add("1998/vision");

        System.out.println(torneo1998.size() + " robot(s)");
        System.out.print("Loading torneo1999... ");

        torneo1999.add("1999/11");
        torneo1999.add("1999/aeris");
        torneo1999.add("1999/akira");
        torneo1999.add("1999/alezai17");
        torneo1999.add("1999/alfa99");
        torneo1999.add("1999/alien");
        torneo1999.add("1999/ap_5");
        torneo1999.add("1999/bastrd!!");
        torneo1999.add("1999/cancer");
        torneo1999.add("1999/carlo99");
        torneo1999.add("1999/#cimice#");
        torneo1999.add("1999/cortez");
        torneo1999.add("1999/cyborg");
        torneo1999.add("1999/dario");
        torneo1999.add("1999/dav46");
        torneo1999.add("1999/defender");
        torneo1999.add("1999/elisir");
        torneo1999.add("1999/flash6");
        torneo1999.add("1999/hal9000");
        torneo1999.add("1999/ilbestio");
        torneo1999.add("1999/jedi2");
        torneo1999.add("1999/ka_aroth");
        torneo1999.add("1999/kakakatz");
        torneo1999.add("1999/lukather");
        torneo1999.add("1999/mancino");
        torneo1999.add("1999/marko");
        torneo1999.add("1999/mcenrobo");
        torneo1999.add("1999/m_hingis");
        torneo1999.add("1999/minatela");
        torneo1999.add("1999/new");
        torneo1999.add("1999/nexus_2");
        torneo1999.add("1999/nl_3a");
        torneo1999.add("1999/nl_3b");
        torneo1999.add("1999/obiwan");
        torneo1999.add("1999/omega99");
        torneo1999.add("1999/panduro");
        torneo1999.add("1999/panic");
        torneo1999.add("1999/pippo99");
        torneo1999.add("1999/pizarro");
        torneo1999.add("1999/quarto");
        torneo1999.add("1999/quingon");
        torneo1999.add("1999/rudolf_4");
        torneo1999.add("1999/satana");
        torneo1999.add("1999/shock");
        torneo1999.add("1999/songohan");
        torneo1999.add("1999/stealth");
        torneo1999.add("1999/storm");
        torneo1999.add("1999/surrende");
        torneo1999.add("1999/t1001");
        torneo1999.add("1999/themicro");
        torneo1999.add("1999/titania2");
        torneo1999.add("1999/vibrsper");
        torneo1999.add("1999/zero");

        System.out.println(torneo1999.size() + " robot(s)");
        System.out.print("Loading torneo2000... ");

        torneo2000.add("2000/7di9");
        torneo2000.add("2000/bach_2k");
        torneo2000.add("2000/beholder");
        torneo2000.add("2000/boom");
        torneo2000.add("2000/carlo2k");
        torneo2000.add("2000/coppi_2k");
        torneo2000.add("2000/daryl");
        torneo2000.add("2000/dav2000");
        torneo2000.add("2000/def2");
        torneo2000.add("2000/defender");
        torneo2000.add("2000/doppia_g");
        torneo2000.add("2000/flash7");
        torneo2000.add("2000/fremen");
        torneo2000.add("2000/gengis");
        torneo2000.add("2000/jedi3");
        torneo2000.add("2000/kongzill");
        torneo2000.add("2000/mancino");
        torneo2000.add("2000/marine");
        torneo2000.add("2000/m_hingis");
        torneo2000.add("2000/mrsatan");
        torneo2000.add("2000/navaho");
        torneo2000.add("2000/new2");
        torneo2000.add("2000/newzai17");
        torneo2000.add("2000/nl_4a");
        torneo2000.add("2000/nl_4b");
        torneo2000.add("2000/rudolf_5");
        torneo2000.add("2000/sharp");
        torneo2000.add("2000/touch");
        torneo2000.add("2000/vegeth");

        System.out.println(torneo2000.size() + " robot(s)");
        System.out.print("Loading torneo2001... ");

        torneo2001.add("2001/4ever");
        torneo2001.add("2001/artu");
        torneo2001.add("2001/athlon");
        torneo2001.add("2001/bati");
        torneo2001.add("2001/bigkarl");
        torneo2001.add("2001/borg");
        torneo2001.add("2001/burrfoot");
        torneo2001.add("2001/charles");
        torneo2001.add("2001/cisc");
        torneo2001.add("2001/cobra");
        torneo2001.add("2001/copter");
        torneo2001.add("2001/defender");
        torneo2001.add("2001/disco");
        torneo2001.add("2001/dnablack");
        torneo2001.add("2001/dna");
        torneo2001.add("2001/fizban");
        torneo2001.add("2001/gers");
        torneo2001.add("2001/grezbot");
        torneo2001.add("2001/hammer");
        torneo2001.add("2001/harris");
        torneo2001.add("2001/heavnew");
        torneo2001.add("2001/homer");
        torneo2001.add("2001/jedi4");
        torneo2001.add("2001/klr2");
        torneo2001.add("2001/kyashan");
        torneo2001.add("2001/max10");
        torneo2001.add("2001/megazai");
        torneo2001.add("2001/merlino");
        torneo2001.add("2001/mflash2");
        torneo2001.add("2001/microdna");
        torneo2001.add("2001/midi_zai");
        torneo2001.add("2001/mnl_1a");
        torneo2001.add("2001/mnl_1b");
        torneo2001.add("2001/murray");
        torneo2001.add("2001/neo0");
        torneo2001.add("2001/nl_5a");
        torneo2001.add("2001/nl_5b");
        torneo2001.add("2001/pentium4");
        torneo2001.add("2001/pippo1a");
        torneo2001.add("2001/pippo1b");
        torneo2001.add("2001/raistlin");
        torneo2001.add("2001/ridicol");
        torneo2001.add("2001/risc");
        torneo2001.add("2001/rudolf_6");
        torneo2001.add("2001/rudy_xp");
        torneo2001.add("2001/sdc2");
        torneo2001.add("2001/sharp2");
        torneo2001.add("2001/staticii");
        torneo2001.add("2001/thunder");
        torneo2001.add("2001/vampire");
        torneo2001.add("2001/xeon");
        torneo2001.add("2001/zifnab");
        torneo2001.add("2001/zombie");

        System.out.println(torneo2001.size() + " robot(s)");
        System.out.print("Loading torneo2002... ");

        torneo2002.add("2002/01");
        torneo2002.add("2002/adsl");
        torneo2002.add("2002/anakin");
        torneo2002.add("2002/asterix");
        torneo2002.add("2002/attila");
        torneo2002.add("2002/bruenor");
        torneo2002.add("2002/colera");
        torneo2002.add("2002/colosseum");
        torneo2002.add("2002/copter_2");
        torneo2002.add("2002/corner5");
        torneo2002.add("2002/doom2099");
        torneo2002.add("2002/drizzt");
        torneo2002.add("2002/dynamite");
        torneo2002.add("2002/enigma");
        torneo2002.add("2002/groucho");
        torneo2002.add("2002/halman");
        torneo2002.add("2002/harpo");
        torneo2002.add("2002/idefix");
        torneo2002.add("2002/jedi5");
        torneo2002.add("2002/kyash_2");
        torneo2002.add("2002/marco");
        torneo2002.add("2002/mazinga");
        torneo2002.add("2002/medioman");
        torneo2002.add("2002/mg_one");
        torneo2002.add("2002/mind");
        torneo2002.add("2002/moveon");
        torneo2002.add("2002/neo_sifr");
        torneo2002.add("2002/obelix");
        torneo2002.add("2002/ollio");
        torneo2002.add("2002/padawan");
        torneo2002.add("2002/peste");
        torneo2002.add("2002/pippo2a");
        torneo2002.add("2002/pippo2b");
        torneo2002.add("2002/regis");
        torneo2002.add("2002/remus");
        torneo2002.add("2002/romulus");
        torneo2002.add("2002/rudolf_7");
        torneo2002.add("2002/scsi");
        torneo2002.add("2002/serse");
        torneo2002.add("2002/ska");
        torneo2002.add("2002/stanlio");
        torneo2002.add("2002/staticxp");
        torneo2002.add("2002/supernov");
        torneo2002.add("2002/theslayer");
        torneo2002.add("2002/tifo");
        torneo2002.add("2002/tigre");
        torneo2002.add("2002/todos");
        torneo2002.add("2002/tomahawk");
        torneo2002.add("2002/vaiolo");
        torneo2002.add("2002/vauban");
        torneo2002.add("2002/wulfgar");
        torneo2002.add("2002/yerba");
        torneo2002.add("2002/yoyo");
        torneo2002.add("2002/zorn");

        System.out.println(torneo2002.size() + " robot(s)");
        System.out.print("Loading torneo2003... ");

        torneo2003.add("2003/730");
        torneo2003.add("2003/adrian");
        torneo2003.add("2003/aladino");
        torneo2003.add("2003/alcadia");
        torneo2003.add("2003/ares");
        torneo2003.add("2003/barbarian");
        torneo2003.add("2003/blitz");
        torneo2003.add("2003/briscolo");
        torneo2003.add("2003/bruce");
        torneo2003.add("2003/cadderly");
        torneo2003.add("2003/cariddi");
        torneo2003.add("2003/crossover");
        torneo2003.add("2003/cvirus2");
        torneo2003.add("2003/cvirus");
        torneo2003.add("2003/cyborg_2");
        torneo2003.add("2003/danica");
        torneo2003.add("2003/dave");
        torneo2003.add("2003/druzil");
        torneo2003.add("2003/dynacond");
        torneo2003.add("2003/elminster");
        torneo2003.add("2003/falco");
        torneo2003.add("2003/foursquare");
        torneo2003.add("2003/frame");
        torneo2003.add("2003/harlock");
        torneo2003.add("2003/herpes");
        torneo2003.add("2003/ici");
        torneo2003.add("2003/instict");
        torneo2003.add("2003/irpef");
        torneo2003.add("2003/janick");
        torneo2003.add("2003/janu");
        torneo2003.add("2003/jedi6");
        torneo2003.add("2003/knt");
        torneo2003.add("2003/kyash_3c");
        torneo2003.add("2003/kyash_3m");
        torneo2003.add("2003/lbr1");
        torneo2003.add("2003/lbr");
        torneo2003.add("2003/lebbra");
        torneo2003.add("2003/maxicond");
        torneo2003.add("2003/mg_two");
        torneo2003.add("2003/minicond");
        torneo2003.add("2003/morituro");
        torneo2003.add("2003/nautilus");
        torneo2003.add("2003/nemo");
        torneo2003.add("2003/neo_sel");
        torneo2003.add("2003/orione");
        torneo2003.add("2003/piiico");
        torneo2003.add("2003/pippo3b");
        torneo2003.add("2003/pippo3");
        torneo2003.add("2003/red_wolf");
        torneo2003.add("2003/rudolf_8");
        torneo2003.add("2003/scanner");
        torneo2003.add("2003/scilla");
        torneo2003.add("2003/sirio");
        torneo2003.add("2003/sith");
        torneo2003.add("2003/sky");
        torneo2003.add("2003/spaceman");
        torneo2003.add("2003/tartaruga");
        torneo2003.add("2003/unico");
        torneo2003.add("2003/valevan");
        torneo2003.add("2003/virus2");
        torneo2003.add("2003/virus3");
        torneo2003.add("2003/virus4");
        torneo2003.add("2003/virus");
        torneo2003.add("2003/yoda");

        System.out.println(torneo2003.size() + " robot(s)");
        System.out.print("Loading torneo2004... ");

        torneo2004.add("2004/adam");
        torneo2004.add("2004/!alien");
        torneo2004.add("2004/bjt");
        torneo2004.add("2004/b_selim");
        torneo2004.add("2004/!caos");
        torneo2004.add("2004/ciclope");
        torneo2004.add("2004/confusion");
        torneo2004.add("2004/coyote");
        torneo2004.add("2004/diodo");
        torneo2004.add("2004/!dna");
        torneo2004.add("2004/fire");
        torneo2004.add("2004/fisco");
        torneo2004.add("2004/frankie");
        torneo2004.add("2004/geriba");
        torneo2004.add("2004/goofy");
        torneo2004.add("2004/gostar");
        torneo2004.add("2004/gotar2");
        torneo2004.add("2004/gotar");
        torneo2004.add("2004/irap");
        torneo2004.add("2004/ire");
        torneo2004.add("2004/ires");
        torneo2004.add("2004/jedi7");
        torneo2004.add("2004/magneto");
        torneo2004.add("2004/mg_three");
        torneo2004.add("2004/mosfet");
        torneo2004.add("2004/m_selim");
        torneo2004.add("2004/multics");
        torneo2004.add("2004/mystica");
        torneo2004.add("2004/n3g4_jr");
        torneo2004.add("2004/n3g4tivo");
        torneo2004.add("2004/new_mini");
        torneo2004.add("2004/pippo04a");
        torneo2004.add("2004/pippo04b");
        torneo2004.add("2004/poldo");
        torneo2004.add("2004/puma");
        torneo2004.add("2004/rat-man");
        torneo2004.add("2004/ravatto");
        torneo2004.add("2004/revo");
        torneo2004.add("2004/rotar");
        torneo2004.add("2004/rudolf_9");
        torneo2004.add("2004/selim_b");
        torneo2004.add("2004/tempesta");
        torneo2004.add("2004/unlimited");
        torneo2004.add("2004/wgdi");
        torneo2004.add("2004/zener");
        torneo2004.add("2004/!zeus");

        System.out.println(torneo2004.size() + " robot(s)");
        System.out.print("Loading torneo2007... ");

        torneo2007.add("2007/angel");
        torneo2007.add("2007/back");
        torneo2007.add("2007/brontolo");
        torneo2007.add("2007/colosso");
        torneo2007.add("2007/electron");
        torneo2007.add("2007/e");
        torneo2007.add("2007/gongolo");
        torneo2007.add("2007/iceman");
        torneo2007.add("2007/jedi8");
        torneo2007.add("2007/macro1");
        torneo2007.add("2007/mammolo");
        torneo2007.add("2007/microbo1");
        torneo2007.add("2007/microbo2");
        torneo2007.add("2007/midi1");
        torneo2007.add("2007/neutron");
        torneo2007.add("2007/nustyle");
        torneo2007.add("2007/pippo07a");
        torneo2007.add("2007/pippo07b");
        torneo2007.add("2007/pisolo");
        torneo2007.add("2007/proton");
        torneo2007.add("2007/proud");
        torneo2007.add("2007/pyro");
        torneo2007.add("2007/rudolf_x");
        torneo2007.add("2007/rythm");
        torneo2007.add("2007/tobey");
        torneo2007.add("2007/t");
        torneo2007.add("2007/zigozago");
        torneo2007.add("2007/z");

        System.out.println(torneo2007.size() + " robot(s)");
        System.out.print("Loading torneo2010... ");

        torneo2010.add("2010/buffy");
        torneo2010.add("2010/cancella");
        torneo2010.add("2010/change");
        torneo2010.add("2010/copia");
        torneo2010.add("2010/enkidu");
        torneo2010.add("2010/eurialo");
        torneo2010.add("2010/gantu");
        torneo2010.add("2010/hal9010");
        torneo2010.add("2010/incolla");
        torneo2010.add("2010/jedi9");
        torneo2010.add("2010/jumba");
        torneo2010.add("2010/macchia");
        torneo2010.add("2010/niso");
        torneo2010.add("2010/party");
        torneo2010.add("2010/pippo10a");
        torneo2010.add("2010/reuben");
        torneo2010.add("2010/stitch");
        torneo2010.add("2010/suddenly");
        torneo2010.add("2010/sweat");
        torneo2010.add("2010/taglia");
        torneo2010.add("2010/toppa");
        torneo2010.add("2010/wall-e");

        System.out.println(torneo2010.size() + " robot(s)");
        System.out.print("Loading torneo2011... ");

        torneo2011.add("2011/armin");
        torneo2011.add("2011/ataman");
        torneo2011.add("2011/coeurl");
        torneo2011.add("2011/digitale");
        torneo2011.add("2011/gerty");
        torneo2011.add("2011/grendizer");
        torneo2011.add("2011/gru");
        torneo2011.add("2011/guntank");
        torneo2011.add("2011/hal9011");
        torneo2011.add("2011/jedi10");
        torneo2011.add("2011/jeeg");
        torneo2011.add("2011/minion");
        torneo2011.add("2011/nikita");
        torneo2011.add("2011/origano");
        torneo2011.add("2011/ortica");
        torneo2011.add("2011/pain");
        torneo2011.add("2011/piperita");
        torneo2011.add("2011/pippo11a");
        torneo2011.add("2011/pippo11b");
        torneo2011.add("2011/smart");
        torneo2011.add("2011/tannhause");
        torneo2011.add("2011/tantalo");
        torneo2011.add("2011/unmaldestr");
        torneo2011.add("2011/vain");
        torneo2011.add("2011/vector");
        torneo2011.add("2011/wall-e_ii");

        System.out.println(torneo2011.size() + " robot(s)");
        System.out.print("Loading torneo2012... ");

        torneo2012.add("2012/avoider");
        torneo2012.add("2012/beat");
        torneo2012.add("2012/british");
        torneo2012.add("2012/camille");
        torneo2012.add("2012/china");
        torneo2012.add("2012/cliche");
        torneo2012.add("2012/crazy96");
        torneo2012.add("2012/dampyr");
        torneo2012.add("2012/draka");
        torneo2012.add("2012/easyjet");
        torneo2012.add("2012/flash8c");
        torneo2012.add("2012/flash8e");
        torneo2012.add("2012/gerty2");
        torneo2012.add("2012/grezbot2");
        torneo2012.add("2012/gunnyb29");
        torneo2012.add("2012/hal9012");
        torneo2012.add("2012/jedi11");
        torneo2012.add("2012/life");
        torneo2012.add("2012/lufthansa");
        torneo2012.add("2012/lycan");
        torneo2012.add("2012/mister2b");
        torneo2012.add("2012/mister3b");
        torneo2012.add("2012/pippo12a");
        torneo2012.add("2012/pippo12b");
        torneo2012.add("2012/power");
        torneo2012.add("2012/puffomac");
        torneo2012.add("2012/puffomic");
        torneo2012.add("2012/puffomid");
        torneo2012.add("2012/q");
        torneo2012.add("2012/ryanair");
        torneo2012.add("2012/silversurf");
        torneo2012.add("2012/torchio");
        torneo2012.add("2012/wall-e_iii");
        torneo2012.add("2012/yeti");

        System.out.println(torneo2012.size() + " robot(s)");
        System.out.print("Loading torneo2013... ");
        
        torneo2013.add("2013/axolotl");
        torneo2013.add("2013/destro");
        torneo2013.add("2013/eternity");
        torneo2013.add("2013/frisa_13");
        torneo2013.add("2013/gerty3");
        torneo2013.add("2013/ghostrider");
        torneo2013.add("2013/guanaco");
        torneo2013.add("2013/gunnyb13");
        torneo2013.add("2013/hal9013");
        torneo2013.add("2013/jarvis");
        torneo2013.add("2013/jedi12");
        torneo2013.add("2013/john_blaze");
        torneo2013.add("2013/lamela");
        torneo2013.add("2013/lancia13");
        torneo2013.add("2013/leopon");
        torneo2013.add("2013/ncc-1701");
        torneo2013.add("2013/okapi");
        torneo2013.add("2013/ortona_13");
        torneo2013.add("2013/osvaldo");
        torneo2013.add("2013/pippo13a");
        torneo2013.add("2013/pippo13b");
        torneo2013.add("2013/pjanic");
        torneo2013.add("2013/pray");
        torneo2013.add("2013/ride");
        torneo2013.add("2013/ug2k");
        torneo2013.add("2013/wall-e_iv");
        
        System.out.println(torneo2013.size() + " robot(s)");
        System.out.print("Loading torneo2015... ");

        torneo2015.add("2015/antman");
        torneo2015.add("2015/aswhup");
//        torneo2015.add("2015/avoider"); Belongs to 2012
        torneo2015.add("2015/babadook");
        torneo2015.add("2015/bttf");
        torneo2015.add("2015/circles15");
        torneo2015.add("2015/colour");
        torneo2015.add("2015/coppi15ma1");
        torneo2015.add("2015/coppi15ma2");
        torneo2015.add("2015/coppi15mc1");
        torneo2015.add("2015/coppi15md1");
        torneo2015.add("2015/corbu15");
        torneo2015.add("2015/dlrn");
        torneo2015.add("2015/flash9");
        torneo2015.add("2015/frank15");
        torneo2015.add("2015/g13-14");
        torneo2015.add("2015/gargantua");
        torneo2015.add("2015/gerty4");
        torneo2015.add("2015/hal9015");
        torneo2015.add("2015/hulk");
        torneo2015.add("2015/ironman_15");
        torneo2015.add("2015/jedi13");
        torneo2015.add("2015/linabo15");
        torneo2015.add("2015/lluke");
        torneo2015.add("2015/mcfly");
        torneo2015.add("2015/mies15");
        torneo2015.add("2015/mike3");
        torneo2015.add("2015/misdemeano");
        torneo2015.add("2015/music");
        torneo2015.add("2015/one");
        torneo2015.add("2015/pantagruel");
        torneo2015.add("2015/pippo15a");
        torneo2015.add("2015/pippo15b");
        torneo2015.add("2015/puppet");
        torneo2015.add("2015/randguard");
        torneo2015.add("2015/salippo");
        torneo2015.add("2015/sidewalk");
        torneo2015.add("2015/the_old");
        torneo2015.add("2015/thor");
        torneo2015.add("2015/tux");
        torneo2015.add("2015/tyrion");
        torneo2015.add("2015/wall-e_v");

        System.out.println(torneo2015.size() + " robot(s)");
        System.out.print("Loading torneo2020... ");

        torneo2020.add("2020/antman_20");
        torneo2020.add("2020/b4b");
        torneo2020.add("2020/brexit");
        torneo2020.add("2020/carillon");
        torneo2020.add("2020/coppi20ma1");
        torneo2020.add("2020/coppi20ma2");
        torneo2020.add("2020/coppi20mc1");
        torneo2020.add("2020/coppi20md1");
        torneo2020.add("2020/discotek");
        torneo2020.add("2020/dreamland");
        torneo2020.add("2020/flash10");
        torneo2020.add("2020/gerty5");
        torneo2020.add("2020/hal9020");
        torneo2020.add("2020/hulk_20");
        torneo2020.add("2020/ironman_20");
        torneo2020.add("2020/jarvis2");
        torneo2020.add("2020/jedi14");
        torneo2020.add("2020/leavy2");
        torneo2020.add("2020/loneliness");
        torneo2020.add("2020/pippo20a");
        torneo2020.add("2020/pippo20b");
        torneo2020.add("2020/thor_20");
        torneo2020.add("2020/wall-e_vi");
        torneo2020.add("2020/wizard2");

        System.out.println(torneo2020.size() + " robot(s)");
        System.out.print("Loading crobs... ");

        crobs.add("crobs/adversar");
        crobs.add("crobs/agressor");
        crobs.add("crobs/antru");
        crobs.add("crobs/assassin");
        crobs.add("crobs/b4");
        crobs.add("crobs/bishop");
        crobs.add("crobs/bouncer");
        crobs.add("crobs/boxer");
        crobs.add("crobs/cassius");
        crobs.add("crobs/catfish3");
        crobs.add("crobs/chase");
        crobs.add("crobs/chaser");
        crobs.add("crobs/cooper1");
        crobs.add("crobs/cooper2");
        crobs.add("crobs/cornerkl");
        crobs.add("crobs/counter");
        crobs.add("crobs/counter2");
        crobs.add("crobs/cruiser");
        crobs.add("crobs/cspotrun");
        crobs.add("crobs/danimal");
        crobs.add("crobs/dave");
        crobs.add("crobs/di");
        crobs.add("crobs/dirtyh");
        crobs.add("crobs/duck");
        crobs.add("crobs/dumbname");
        crobs.add("crobs/etf_kid");
        crobs.add("crobs/flyby");
        crobs.add("crobs/fred");
        crobs.add("crobs/friendly");
        crobs.add("crobs/grunt");
        crobs.add("crobs/gsmr2");
        crobs.add("crobs/h-k");
        crobs.add("crobs/hac_atak");
        crobs.add("crobs/hak3");
        crobs.add("crobs/hitnrun");
        crobs.add("crobs/hunter");
        crobs.add("crobs/huntlead");
        crobs.add("crobs/intrcptr");
        crobs.add("crobs/jagger");
        crobs.add("crobs/jason100");
        crobs.add("crobs/kamikaze");
        crobs.add("crobs/killer");
        crobs.add("crobs/leader");
        crobs.add("crobs/leavy");
        crobs.add("crobs/lethal");
        crobs.add("crobs/maniac");
        crobs.add("crobs/marvin");
        crobs.add("crobs/mini");
        crobs.add("crobs/ninja");
        crobs.add("crobs/nord");
        crobs.add("crobs/nord2");
        crobs.add("crobs/ogre");
        crobs.add("crobs/ogre2");
        crobs.add("crobs/ogre3");
        crobs.add("crobs/perizoom");
        crobs.add("crobs/pest");
        crobs.add("crobs/phantom");
        crobs.add("crobs/pingpong");
        crobs.add("crobs/politik");
        crobs.add("crobs/pzk");
        crobs.add("crobs/pzkmin");
        crobs.add("crobs/quack");
        crobs.add("crobs/quikshot");
        crobs.add("crobs/rabbit10");
        crobs.add("crobs/rambo3");
        crobs.add("crobs/rapest");
        crobs.add("crobs/reflex");
        crobs.add("crobs/robbie");
        crobs.add("crobs/rook");
        crobs.add("crobs/rungun");
        crobs.add("crobs/samurai");
        crobs.add("crobs/scan");
        crobs.add("crobs/scanlock");
        crobs.add("crobs/scanner");
        crobs.add("crobs/secro");
        crobs.add("crobs/sentry");
        crobs.add("crobs/shark3");
        crobs.add("crobs/shark4");
        crobs.add("crobs/silly");
        crobs.add("crobs/slead");
        crobs.add("crobs/sniper");
        crobs.add("crobs/spinner");
        crobs.add("crobs/spot");
        crobs.add("crobs/squirrel");
        crobs.add("crobs/stalker");
        crobs.add("crobs/stush-1");
        crobs.add("crobs/topgun");
        crobs.add("crobs/tracker");
        crobs.add("crobs/trial4");
        crobs.add("crobs/twedlede");
        crobs.add("crobs/twedledm");
        crobs.add("crobs/venom");
        crobs.add("crobs/watchdog");
        crobs.add("crobs/wizard");
        crobs.add("crobs/xecutner");
        crobs.add("crobs/xhatch");
        crobs.add("crobs/yal");

        System.out.println(crobs.size() + " robot(s)");
        System.out.print("Loading micro... ");

        micro.add("micro/caccola");
        micro.add("micro/carletto");
        micro.add("micro/chobin");
        micro.add("micro/dream");
        micro.add("micro/ld");
        micro.add("micro/lucifer");
        micro.add("micro/marlene");
        micro.add("micro/md8");
        micro.add("micro/md9");
        micro.add("micro/mflash");
        micro.add("micro/minizai");
        micro.add("micro/pacoon");
        micro.add("micro/pikachu");
        micro.add("micro/pippo00a");
        micro.add("micro/pippo00");
        micro.add("micro/pirla");
        micro.add("micro/p");
        micro.add("micro/rudy");
        micro.add("micro/static");
        micro.add("micro/tanzen");
        micro.add("micro/uhm");
        micro.add("micro/zioalfa");
        micro.add("micro/zzz");

        System.out.println(micro.size() + " robot(s)");
    }

    private static class RobotComparator implements Comparator<String> {

        private static final RobotComparator INSTANCE = new RobotComparator();

        @Override
        public int compare(String o1, String o2) {
            return getBasename(o1).compareTo(getBasename(o2));
        }

        public static RobotComparator getInstance() {
            return INSTANCE;
        }
    }
}
