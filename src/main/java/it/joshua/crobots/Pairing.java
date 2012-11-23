/**
 *
 */
package it.joshua.crobots;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mcamangi
 *
 */
public class Pairing {

    private static List<String> torneo90 = new ArrayList<String>();
    private static List<String> torneo91 = new ArrayList<String>();
    private static List<String> torneo92 = new ArrayList<String>();
    private static List<String> torneo93 = new ArrayList<String>();
    private static List<String> torneo94 = new ArrayList<String>();
    private static List<String> torneo95 = new ArrayList<String>();
    private static List<String> torneo96 = new ArrayList<String>();
    private static List<String> torneo97 = new ArrayList<String>();
    private static List<String> torneo98 = new ArrayList<String>();
    private static List<String> torneo99 = new ArrayList<String>();
    private static List<String> torneo2000 = new ArrayList<String>();
    private static List<String> torneo2001 = new ArrayList<String>();
    private static List<String> torneo2002 = new ArrayList<String>();
    private static List<String> torneo2003 = new ArrayList<String>();
    private static List<String> torneo2004 = new ArrayList<String>();
    private static List<String> torneo2007 = new ArrayList<String>();
    private static List<String> torneo2010 = new ArrayList<String>();
    private static List<String> torneo2011 = new ArrayList<String>();
    private static List<String> torneo2012 = new ArrayList<String>();
    private static List<String> micro = new ArrayList<String>();
    private static List<String> crobs = new ArrayList<String>();
    private static List<String> aminet = new ArrayList<String>();
    private static List<String> cplusplus = new ArrayList<String>();
    private static List<List<String>> tournaments = new ArrayList<List<String>>();
    private static List<String> round = new ArrayList<String>();
    private static List<List<String>> rounds = new ArrayList<List<String>>();
    private static boolean withConflicts;
    private static int robots;

    /**
     * @param args
     */
    public static void main(String[] args) {
        setup();
        countRobots();
        int attempts = 0;
        do {
            withConflicts = false;
            System.out.println("ATTEMPT : " + ++attempts);
            shuffle();
            collect();
            alternativePairing();
            checkConflicts();
        } while (withConflicts);
        show();
        buildConfigFile();
    }

    private static String getBasename(String filename) {
        File f = new File(filename);
        return f.getName();
    }

    private static void checkConflicts() {
        for (List<String> round : rounds) {
            List<String> nameArray = new ArrayList<String>();
            String baseName;
            for (String s : round) {
                baseName = getBasename(s);
                if (nameArray.contains(baseName)) {
                    withConflicts = true;
                    break;
                } else {
                    nameArray.add(baseName);
                }
            }
            if (withConflicts) {
                break;
            }
        }
    }

    private static void show() {
        int n = 1;
        for (List<String> round : rounds) {
            if (round != null && round.size() > 0) {
                System.out.println("------- Group " + n++ + " ------");
                for (String s : round) {
                    System.out.println(s);
                }
            }
        }
    }

    private static void buildConfigFile() {
        int n = 1;
        for (List<String> round : rounds) {
            int count = 0;
            if (round != null && round.size() > 0) {
                System.out.println("------- CFG " + n++ + " ------");
                System.out.println("LABEL=group" + n);
                System.out.print("LIST=(");
                for (String s : round) {
                    if (count++ != 0) {
                        System.out.print(" ");
                    }
                    System.out.print(s);
                }
                System.out.println(")");
            }
        }
    }

    private static void collect() {
        tournaments = new ArrayList<List<String>>();
        tournaments.add(torneo90);
        tournaments.add(torneo91);
        tournaments.add(torneo92);
        tournaments.add(torneo93);
        tournaments.add(torneo94);
        tournaments.add(torneo95);
        tournaments.add(torneo96);
        tournaments.add(torneo97);
        tournaments.add(torneo98);
        tournaments.add(torneo99);
        tournaments.add(torneo2000);
        tournaments.add(torneo2001);
        tournaments.add(torneo2002);
        tournaments.add(torneo2003);
        tournaments.add(torneo2004);
        tournaments.add(torneo2007);
        tournaments.add(torneo2010);
        tournaments.add(torneo2011);
        tournaments.add(torneo2012);
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
        robots += torneo90.size();
        robots += torneo91.size();
        robots += torneo92.size();
        robots += torneo93.size();
        robots += torneo94.size();
        robots += torneo95.size();
        robots += torneo96.size();
        robots += torneo97.size();
        robots += torneo98.size();
        robots += torneo99.size();
        robots += torneo2000.size();
        robots += torneo2001.size();
        robots += torneo2002.size();
        robots += torneo2003.size();
        robots += torneo2004.size();
        robots += torneo2007.size();
        robots += torneo2010.size();
        robots += torneo2011.size();
        robots += torneo2012.size();
        System.out.println("TOTAL Robots :" + robots);
    }

    private static void alternativePairing() {
        int groupSize = 67;
        int groupCount = (robots / groupSize) + ((robots % groupSize) > 0 ? 1 : 0);

        int groupIndex = 0;

        rounds = new ArrayList<List<String>>();

        for (int i = 0; i < groupCount; i++) {
            round = new ArrayList<String>();
            rounds.add(round);
        }

        for (List<String> tournament : tournaments) {
            for (String r : tournament) {
                rounds.get(groupIndex++).add(r);
                if (groupIndex == groupCount) {
                    groupIndex = 0;
                }
            }
        }

    }

    private static void shuffle() {
        Collections.shuffle(torneo90);
        Collections.shuffle(torneo91);
        Collections.shuffle(torneo92);
        Collections.shuffle(torneo93);
        Collections.shuffle(torneo94);
        Collections.shuffle(torneo95);
        Collections.shuffle(torneo96);
        Collections.shuffle(torneo97);
        Collections.shuffle(torneo98);
        Collections.shuffle(torneo99);
        Collections.shuffle(torneo2000);
        Collections.shuffle(torneo2001);
        Collections.shuffle(torneo2002);
        Collections.shuffle(torneo2003);
        Collections.shuffle(torneo2004);
        Collections.shuffle(torneo2007);
        Collections.shuffle(torneo2010);
        Collections.shuffle(torneo2011);
        Collections.shuffle(torneo2012);
        Collections.shuffle(crobs);
        Collections.shuffle(micro);
        Collections.shuffle(aminet);
        Collections.shuffle(cplusplus);
    }

    private static void setup() {
        System.out.print("Loading cplusplus... ");

        cplusplus.add("cplusplus/selvaggio.ro");
        cplusplus.add("cplusplus/vikingo.ro");

        System.out.println(cplusplus.size() + " robot(s)");
        System.out.print("Loading aminet... ");

        aminet.add("aminet/anticlock.ro");
        aminet.add("aminet/beaver.ro");
        aminet.add("aminet/blindschl.ro");
        aminet.add("aminet/blindschl2.ro");
        aminet.add("aminet/mirobot.ro");
        aminet.add("aminet/opfer.ro");
        aminet.add("aminet/schwan.ro");
        aminet.add("aminet/tron.ro");

        System.out.println(aminet.size() + " robot(s)");
        System.out.print("Loading torneo90... ");

        torneo90.add("torneo90/et_1.ro");
        torneo90.add("torneo90/et_2.ro");
        torneo90.add("torneo90/hunter.ro");
        torneo90.add("torneo90/killer.ro");
        torneo90.add("torneo90/nexus_1.ro");
        torneo90.add("torneo90/rob1.ro");
        torneo90.add("torneo90/scanner.ro");
        torneo90.add("torneo90/york.ro");

        System.out.println(torneo90.size() + " robot(s)");
        System.out.print("Loading torneo91... ");

        torneo91.add("torneo91/blade3.ro");
        torneo91.add("torneo91/casimiro.ro");
        torneo91.add("torneo91/ccyber.ro");
        torneo91.add("torneo91/clover.ro");
        torneo91.add("torneo91/diagonal.ro");
        torneo91.add("torneo91/et_3.ro");
        torneo91.add("torneo91/f1.ro");
        torneo91.add("torneo91/fdig.ro");
        torneo91.add("torneo91/geltrude.ro");
        torneo91.add("torneo91/genius_j.ro");
        torneo91.add("torneo91/gira.ro");
        torneo91.add("torneo91/gunner.ro");
        torneo91.add("torneo91/jazz.ro");
        torneo91.add("torneo91/nexus_2.ro");
        torneo91.add("torneo91/paolo101.ro");
        torneo91.add("torneo91/paolo77.ro");
        torneo91.add("torneo91/poor.ro");
        torneo91.add("torneo91/qibo.ro");
        torneo91.add("torneo91/robocop.ro");
        torneo91.add("torneo91/runner.ro");
        torneo91.add("torneo91/sara_6.ro");
        torneo91.add("torneo91/seeker.ro");
        torneo91.add("torneo91/warrior2.ro");

        System.out.println(torneo91.size() + " robot(s)");
        System.out.print("Loading torneo92... ");

        torneo92.add("torneo92/666.ro");
        torneo92.add("torneo92/ap_1.ro");
        torneo92.add("torneo92/assassin.ro");
        torneo92.add("torneo92/baeos.ro");
        torneo92.add("torneo92/banzel.ro");
        torneo92.add("torneo92/bronx-00.ro");
        torneo92.add("torneo92/bry_bry.ro");
        torneo92.add("torneo92/crazy.ro");
        torneo92.add("torneo92/cube.ro");
        torneo92.add("torneo92/cw.ro");
        torneo92.add("torneo92/d47.ro");
        torneo92.add("torneo92/daitan3.ro");
        torneo92.add("torneo92/dancer.ro");
        torneo92.add("torneo92/deluxe.ro");
        torneo92.add("torneo92/dorsai.ro");
        torneo92.add("torneo92/et_4.ro");
        torneo92.add("torneo92/et_5.ro");
        torneo92.add("torneo92/flash.ro");
        torneo92.add("torneo92/genesis.ro");
        torneo92.add("torneo92/hunter.ro");
        torneo92.add("torneo92/ice.ro");
        torneo92.add("torneo92/jack.ro");
        torneo92.add("torneo92/jager.ro");
        torneo92.add("torneo92/johnny.ro");
        torneo92.add("torneo92/lead1.ro");
        torneo92.add("torneo92/marika.ro");
        torneo92.add("torneo92/mimo6new.ro");
        torneo92.add("torneo92/mrcc.ro");
        torneo92.add("torneo92/mut.ro");
        torneo92.add("torneo92/ninus6.ro");
        torneo92.add("torneo92/nl_1a.ro");
        torneo92.add("torneo92/nl_1b.ro");
        torneo92.add("torneo92/ola.ro");
        torneo92.add("torneo92/paolo.ro");
        torneo92.add("torneo92/pavido.ro");
        torneo92.add("torneo92/phobos_1.ro");
        torneo92.add("torneo92/pippo92.ro");
        torneo92.add("torneo92/pippo.ro");
        torneo92.add("torneo92/raid.ro");
        torneo92.add("torneo92/random.ro");
        torneo92.add("torneo92/revenge3.ro");
        torneo92.add("torneo92/robbie.ro");
        torneo92.add("torneo92/robocop2.ro");
        torneo92.add("torneo92/robocop.ro");
        torneo92.add("torneo92/sassy.ro");
        torneo92.add("torneo92/spider.ro");
        torneo92.add("torneo92/sp.ro");
        torneo92.add("torneo92/superv.ro");
        torneo92.add("torneo92/t1000.ro");
        torneo92.add("torneo92/thunder.ro");
        torneo92.add("torneo92/triangol.ro");
        torneo92.add("torneo92/trio.ro");
        torneo92.add("torneo92/uanino.ro");
        torneo92.add("torneo92/warrior3.ro");
        torneo92.add("torneo92/xdraw2.ro");
        torneo92.add("torneo92/zorro.ro");

        System.out.println(torneo92.size() + " robot(s)");
        System.out.print("Loading torneo93... ");

        torneo93.add("torneo93/am_174.ro");
        torneo93.add("torneo93/ap_2.ro");
        torneo93.add("torneo93/ares.ro");
        torneo93.add("torneo93/argon.ro");
        torneo93.add("torneo93/aspide.ro");
        torneo93.add("torneo93/beast.ro");
        torneo93.add("torneo93/biro.ro");
        torneo93.add("torneo93/blade8.ro");
        torneo93.add("torneo93/boom.ro");
        torneo93.add("torneo93/brain.ro");
        torneo93.add("torneo93/cantor.ro");
        torneo93.add("torneo93/castore.ro");
        torneo93.add("torneo93/casual.ro");
        torneo93.add("torneo93/corner1d.ro");
        torneo93.add("torneo93/corner3.ro");
        torneo93.add("torneo93/courage.ro");
        torneo93.add("torneo93/(c).ro");
        torneo93.add("torneo93/crob1.ro");
        torneo93.add("torneo93/deluxe_2.ro");
        torneo93.add("torneo93/deluxe_3.ro");
        torneo93.add("torneo93/didimo.ro");
        torneo93.add("torneo93/duke.ro");
        torneo93.add("torneo93/elija.ro");
        torneo93.add("torneo93/fermo.ro");
        torneo93.add("torneo93/flash2.ro");
        torneo93.add("torneo93/food5.ro");
        torneo93.add("torneo93/godel.ro");
        torneo93.add("torneo93/gunnyboy.ro");
        torneo93.add("torneo93/hamp1.ro");
        torneo93.add("torneo93/hamp2.ro");
        torneo93.add("torneo93/hell.ro");
        torneo93.add("torneo93/horse.ro");
        torneo93.add("torneo93/isaac.ro");
        torneo93.add("torneo93/kami.ro");
        torneo93.add("torneo93/lazy.ro");
        torneo93.add("torneo93/mimo13.ro");
        torneo93.add("torneo93/mister2.ro");
        torneo93.add("torneo93/mister3.ro");
        torneo93.add("torneo93/mohawk.ro");
        torneo93.add("torneo93/mutation.ro");
        torneo93.add("torneo93/ninus17.ro");
        torneo93.add("torneo93/nl_2a.ro");
        torneo93.add("torneo93/nl_2b.ro");
        torneo93.add("torneo93/p68.ro");
        torneo93.add("torneo93/p69.ro");
        torneo93.add("torneo93/penta.ro");
        torneo93.add("torneo93/phobos_2.ro");
        torneo93.add("torneo93/pippo93.ro");
        torneo93.add("torneo93/pognant.ro");
        torneo93.add("torneo93/poirot.ro");
        torneo93.add("torneo93/polluce.ro");
        torneo93.add("torneo93/premana.ro");
        torneo93.add("torneo93/puyopuyo.ro");
        torneo93.add("torneo93/raid2.ro");
        torneo93.add("torneo93/rapper.ro");
        torneo93.add("torneo93/r_cyborg.ro");
        torneo93.add("torneo93/r_daneel.ro");
        torneo93.add("torneo93/robocop3.ro");
        torneo93.add("torneo93/spartaco.ro");
        torneo93.add("torneo93/target.ro");
        torneo93.add("torneo93/tm.ro");
        torneo93.add("torneo93/torneo.ro");
        torneo93.add("torneo93/vannina.ro");
        torneo93.add("torneo93/vocus.ro");
        torneo93.add("torneo93/warrior4.ro");
        torneo93.add("torneo93/wassilij.ro");
        torneo93.add("torneo93/wolfgang.ro");
        torneo93.add("torneo93/zulu.ro");

        System.out.println(torneo93.size() + " robot(s)");
        System.out.print("Loading torneo94... ");

        torneo94.add("torneo94/8bismark.ro");
        torneo94.add("torneo94/anglek2.ro");
        torneo94.add("torneo94/apache.ro");
        torneo94.add("torneo94/bachopin.ro");
        torneo94.add("torneo94/baubau.ro");
        torneo94.add("torneo94/biro.ro");
        torneo94.add("torneo94/blob.ro");
        torneo94.add("torneo94/circlek1.ro");
        torneo94.add("torneo94/corner3b.ro");
        torneo94.add("torneo94/corner4.ro");
        torneo94.add("torneo94/deluxe_4.ro");
        torneo94.add("torneo94/deluxe_5.ro");
        torneo94.add("torneo94/didimo.ro");
        torneo94.add("torneo94/dima10.ro");
        torneo94.add("torneo94/dima9.ro");
        torneo94.add("torneo94/emanuela.ro");
        torneo94.add("torneo94/ematico.ro");
        torneo94.add("torneo94/fastfood.ro");
        torneo94.add("torneo94/flash3.ro");
        torneo94.add("torneo94/funky.ro");
        torneo94.add("torneo94/giali1.ro");
        torneo94.add("torneo94/hal9000.ro");
        torneo94.add("torneo94/heavens.ro");
        torneo94.add("torneo94/horse2.ro");
        torneo94.add("torneo94/iching.ro");
        torneo94.add("torneo94/jet.ro");
        torneo94.add("torneo94/ken.ro");
        torneo94.add("torneo94/lazyii.ro");
        torneo94.add("torneo94/matrox.ro");
        torneo94.add("torneo94/maverick.ro");
        torneo94.add("torneo94/miaomiao.ro");
        torneo94.add("torneo94/nemesi.ro");
        torneo94.add("torneo94/ninus75.ro");
        torneo94.add("torneo94/patcioca.ro");
        torneo94.add("torneo94/pioppo.ro");
        torneo94.add("torneo94/pippo94a.ro");
        torneo94.add("torneo94/pippo94b.ro");
        torneo94.add("torneo94/polipo.ro");
        torneo94.add("torneo94/randwall.ro");
        torneo94.add("torneo94/robot1.ro");
        torneo94.add("torneo94/robot2.ro");
        torneo94.add("torneo94/sdix3.ro");
        torneo94.add("torneo94/sgnaus.ro");
        torneo94.add("torneo94/shadow.ro");
        torneo94.add("torneo94/superfly.ro");
        torneo94.add("torneo94/the_dam.ro");
        torneo94.add("torneo94/t-rex.ro");

        System.out.println(torneo94.size() + " robot(s)");
        System.out.print("Loading torneo95... ");

        torneo95.add("torneo95/andrea.ro");
        torneo95.add("torneo95/animal.ro");
        torneo95.add("torneo95/apache95.ro");
        torneo95.add("torneo95/archer.ro");
        torneo95.add("torneo95/b115e2.ro");
        torneo95.add("torneo95/b52.ro");
        torneo95.add("torneo95/biro.ro");
        torneo95.add("torneo95/boss.ro");
        torneo95.add("torneo95/camillo.ro");
        torneo95.add("torneo95/carlo.ro");
        torneo95.add("torneo95/circle.ro");
        torneo95.add("torneo95/cri95.ro");
        torneo95.add("torneo95/diablo.ro");
        torneo95.add("torneo95/flash4.ro");
        torneo95.add("torneo95/hal9000.ro");
        torneo95.add("torneo95/heavens.ro");
        torneo95.add("torneo95/horse3.ro");
        torneo95.add("torneo95/kenii.ro");
        torneo95.add("torneo95/losendos.ro");
        torneo95.add("torneo95/mikezhar.ro");
        torneo95.add("torneo95/ninus99.ro");
        torneo95.add("torneo95/paccu.ro");
        torneo95.add("torneo95/passion.ro");
        torneo95.add("torneo95/peribolo.ro");
        torneo95.add("torneo95/pippo95.ro");
        torneo95.add("torneo95/rambo.ro");
        torneo95.add("torneo95/rocco.ro");
        torneo95.add("torneo95/saxy.ro");
        torneo95.add("torneo95/sel.ro");
        torneo95.add("torneo95/skizzo.ro");
        torneo95.add("torneo95/star.ro");
        torneo95.add("torneo95/stinger.ro");
        torneo95.add("torneo95/tabori-1.ro");
        torneo95.add("torneo95/tabori-2.ro");
        torneo95.add("torneo95/tequila.ro");
        torneo95.add("torneo95/tmii.ro");
        torneo95.add("torneo95/tox.ro");
        torneo95.add("torneo95/t-rex.ro");
        torneo95.add("torneo95/tricky.ro");
        torneo95.add("torneo95/twins.ro");
        torneo95.add("torneo95/upv-9596.ro");
        torneo95.add("torneo95/xenon.ro");

        System.out.println(torneo95.size() + " robot(s)");
        System.out.print("Loading torneo96... ");

        torneo96.add("torneo96/aleph.ro");
        torneo96.add("torneo96/andrea96.ro");
        torneo96.add("torneo96/ap_4.ro");
        torneo96.add("torneo96/carlo96.ro");
        torneo96.add("torneo96/diablo2.ro");
        torneo96.add("torneo96/drago5.ro");
        torneo96.add("torneo96/d_ray.ro");
        torneo96.add("torneo96/fb3.ro");
        torneo96.add("torneo96/gevbass.ro");
        torneo96.add("torneo96/golem.ro");
        torneo96.add("torneo96/gpo2.ro");
        torneo96.add("torneo96/hal9000.ro");
        torneo96.add("torneo96/heavnew.ro");
        torneo96.add("torneo96/hider2.ro");
        torneo96.add("torneo96/infinity.ro");
        torneo96.add("torneo96/jaja.ro");
        torneo96.add("torneo96/memories.ro");
        torneo96.add("torneo96/murdoc.ro");
        torneo96.add("torneo96/natas.ro");
        torneo96.add("torneo96/newb52.ro");
        torneo96.add("torneo96/pacio.ro");
        torneo96.add("torneo96/pippo96a.ro");
        torneo96.add("torneo96/pippo96b.ro");
        torneo96.add("torneo96/!.ro");
        torneo96.add("torneo96/risk.ro");
        torneo96.add("torneo96/robot1.ro");
        torneo96.add("torneo96/robot2.ro");
        torneo96.add("torneo96/rudolf.ro");
        torneo96.add("torneo96/second3.ro");
        torneo96.add("torneo96/s-seven.ro");
        torneo96.add("torneo96/tatank_3.ro");
        torneo96.add("torneo96/tronco.ro");
        torneo96.add("torneo96/uht.ro");
        torneo96.add("torneo96/xabaras.ro");
        torneo96.add("torneo96/yuri.ro");

        System.out.println(torneo96.size() + " robot(s)");
        System.out.print("Loading torneo97... ");

        torneo97.add("torneo97/1&1.ro");
        torneo97.add("torneo97/abyss.ro");
        torneo97.add("torneo97/ai1.ro");
        torneo97.add("torneo97/andrea97.ro");
        torneo97.add("torneo97/arale.ro");
        torneo97.add("torneo97/belva.ro");
        torneo97.add("torneo97/carlo97.ro");
        torneo97.add("torneo97/ciccio.ro");
        torneo97.add("torneo97/colossus.ro");
        torneo97.add("torneo97/diablo3.ro");
        torneo97.add("torneo97/diabolik.ro");
        torneo97.add("torneo97/drago6.ro");
        torneo97.add("torneo97/erica.ro");
        torneo97.add("torneo97/fable.ro");
        torneo97.add("torneo97/flash5.ro");
        torneo97.add("torneo97/fya.ro");
        torneo97.add("torneo97/gevbass2.ro");
        torneo97.add("torneo97/golem2.ro");
        torneo97.add("torneo97/gundam.ro");
        torneo97.add("torneo97/hal9000.ro");
        torneo97.add("torneo97/jedi.ro");
        torneo97.add("torneo97/kill!.ro");
        torneo97.add("torneo97/me-110c.ro");
        torneo97.add("torneo97/ncmplt.ro");
        torneo97.add("torneo97/paperone.ro");
        torneo97.add("torneo97/pippo97.ro");
        torneo97.add("torneo97/raid3.ro");
        torneo97.add("torneo97/robivinf.ro");
        torneo97.add("torneo97/rudolf_2.ro");

        System.out.println(torneo97.size() + " robot(s)");
        System.out.print("Loading torneo98... ");

        torneo98.add("torneo98/ai2.ro");
        torneo98.add("torneo98/bartali.ro");
        torneo98.add("torneo98/carla.ro");
        torneo98.add("torneo98/coppi.ro");
        torneo98.add("torneo98/dia.ro");
        torneo98.add("torneo98/dicin.ro");
        torneo98.add("torneo98/eva00.ro");
        torneo98.add("torneo98/eva01.ro");
        torneo98.add("torneo98/freedom.ro");
        torneo98.add("torneo98/fscan.ro");
        torneo98.add("torneo98/goblin.ro");
        torneo98.add("torneo98/goldrake.ro");
        torneo98.add("torneo98/hal9000.ro");
        torneo98.add("torneo98/heavnew.ro");
        torneo98.add("torneo98/maxheav.ro");
        torneo98.add("torneo98/ninja.ro");
        torneo98.add("torneo98/paranoid.ro");
        torneo98.add("torneo98/pippo98.ro");
        torneo98.add("torneo98/plump.ro");
        torneo98.add("torneo98/quarto.ro");
        torneo98.add("torneo98/rattolo.ro");
        torneo98.add("torneo98/rudolf_3.ro");
        torneo98.add("torneo98/son-goku.ro");
        torneo98.add("torneo98/sottolin.ro");
        torneo98.add("torneo98/stay.ro");
        torneo98.add("torneo98/stighy98.ro");
        torneo98.add("torneo98/themicro.ro");
        torneo98.add("torneo98/titania.ro");
        torneo98.add("torneo98/tornado.ro");
        torneo98.add("torneo98/traker1.ro");
        torneo98.add("torneo98/traker2.ro");
        torneo98.add("torneo98/vision.ro");

        System.out.println(torneo98.size() + " robot(s)");
        System.out.print("Loading torneo99... ");

        torneo99.add("torneo99/11.ro");
        torneo99.add("torneo99/aeris.ro");
        torneo99.add("torneo99/akira.ro");
        torneo99.add("torneo99/alezai17.ro");
        torneo99.add("torneo99/alfa99.ro");
        torneo99.add("torneo99/alien.ro");
        torneo99.add("torneo99/ap_5.ro");
        torneo99.add("torneo99/bastrd!!.ro");
        torneo99.add("torneo99/cancer.ro");
        torneo99.add("torneo99/carlo99.ro");
        torneo99.add("torneo99/#cimice#.ro");
        torneo99.add("torneo99/cortez.ro");
        torneo99.add("torneo99/cyborg.ro");
        torneo99.add("torneo99/dario.ro");
        torneo99.add("torneo99/dav46.ro");
        torneo99.add("torneo99/defender.ro");
        torneo99.add("torneo99/elisir.ro");
        torneo99.add("torneo99/flash6.ro");
        torneo99.add("torneo99/hal9000.ro");
        torneo99.add("torneo99/ilbestio.ro");
        torneo99.add("torneo99/jedi2.ro");
        torneo99.add("torneo99/ka_aroth.ro");
        torneo99.add("torneo99/kakakatz.ro");
        torneo99.add("torneo99/lukather.ro");
        torneo99.add("torneo99/mancino.ro");
        torneo99.add("torneo99/marko.ro");
        torneo99.add("torneo99/mcenrobo.ro");
        torneo99.add("torneo99/m_hingis.ro");
        torneo99.add("torneo99/minatela.ro");
        torneo99.add("torneo99/new.ro");
        torneo99.add("torneo99/nexus_2.ro");
        torneo99.add("torneo99/nl_3a.ro");
        torneo99.add("torneo99/nl_3b.ro");
        torneo99.add("torneo99/obiwan.ro");
        torneo99.add("torneo99/omega99.ro");
        torneo99.add("torneo99/panduro.ro");
        torneo99.add("torneo99/panic.ro");
        torneo99.add("torneo99/pippo99.ro");
        torneo99.add("torneo99/pizarro.ro");
        torneo99.add("torneo99/quarto.ro");
        torneo99.add("torneo99/quingon.ro");
        torneo99.add("torneo99/rudolf_4.ro");
        torneo99.add("torneo99/satana.ro");
        torneo99.add("torneo99/shock.ro");
        torneo99.add("torneo99/songohan.ro");
        torneo99.add("torneo99/stealth.ro");
        torneo99.add("torneo99/storm.ro");
        torneo99.add("torneo99/surrende.ro");
        torneo99.add("torneo99/t1001.ro");
        torneo99.add("torneo99/themicro.ro");
        torneo99.add("torneo99/titania2.ro");
        torneo99.add("torneo99/vibrsper.ro");
        torneo99.add("torneo99/zero.ro");

        System.out.println(torneo99.size() + " robot(s)");
        System.out.print("Loading torneo2000... ");

        torneo2000.add("torneo2000/7di9.ro");
        torneo2000.add("torneo2000/bach_2k.ro");
        torneo2000.add("torneo2000/beholder.ro");
        torneo2000.add("torneo2000/boom.ro");
        torneo2000.add("torneo2000/carlo2k.ro");
        torneo2000.add("torneo2000/coppi_2k.ro");
        torneo2000.add("torneo2000/daryl.ro");
        torneo2000.add("torneo2000/dav2000.ro");
        torneo2000.add("torneo2000/def2.ro");
        torneo2000.add("torneo2000/defender.ro");
        torneo2000.add("torneo2000/doppia_g.ro");
        torneo2000.add("torneo2000/flash7.ro");
        torneo2000.add("torneo2000/fremen.ro");
        torneo2000.add("torneo2000/gengis.ro");
        torneo2000.add("torneo2000/jedi3.ro");
        torneo2000.add("torneo2000/kongzill.ro");
        torneo2000.add("torneo2000/mancino.ro");
        torneo2000.add("torneo2000/marine.ro");
        torneo2000.add("torneo2000/m_hingis.ro");
        torneo2000.add("torneo2000/mrsatan.ro");
        torneo2000.add("torneo2000/navaho.ro");
        torneo2000.add("torneo2000/new2.ro");
        torneo2000.add("torneo2000/newzai17.ro");
        torneo2000.add("torneo2000/nl_4a.ro");
        torneo2000.add("torneo2000/nl_4b.ro");
        torneo2000.add("torneo2000/rudolf_5.ro");
        torneo2000.add("torneo2000/sharp.ro");
        torneo2000.add("torneo2000/touch.ro");
        torneo2000.add("torneo2000/vegeth.ro");

        System.out.println(torneo2000.size() + " robot(s)");
        System.out.print("Loading torneo2001... ");

        torneo2001.add("torneo2001/4ever.ro");
        torneo2001.add("torneo2001/artu.ro");
        torneo2001.add("torneo2001/athlon.ro");
        torneo2001.add("torneo2001/bati.ro");
        torneo2001.add("torneo2001/bigkarl.ro");
        torneo2001.add("torneo2001/borg.ro");
        torneo2001.add("torneo2001/burrfoot.ro");
        torneo2001.add("torneo2001/charles.ro");
        torneo2001.add("torneo2001/cisc.ro");
        torneo2001.add("torneo2001/cobra.ro");
        torneo2001.add("torneo2001/copter.ro");
        torneo2001.add("torneo2001/defender.ro");
        torneo2001.add("torneo2001/disco.ro");
        torneo2001.add("torneo2001/dnablack.ro");
        torneo2001.add("torneo2001/dna.ro");
        torneo2001.add("torneo2001/fizban.ro");
        torneo2001.add("torneo2001/gers.ro");
        torneo2001.add("torneo2001/grezbot.ro");
        torneo2001.add("torneo2001/hammer.ro");
        torneo2001.add("torneo2001/harris.ro");
        torneo2001.add("torneo2001/heavnew.ro");
        torneo2001.add("torneo2001/homer.ro");
        torneo2001.add("torneo2001/jedi4.ro");
        torneo2001.add("torneo2001/klr2.ro");
        torneo2001.add("torneo2001/kyashan.ro");
        torneo2001.add("torneo2001/max10.ro");
        torneo2001.add("torneo2001/megazai.ro");
        torneo2001.add("torneo2001/merlino.ro");
        torneo2001.add("torneo2001/mflash2.ro");
        torneo2001.add("torneo2001/microdna.ro");
        torneo2001.add("torneo2001/midi_zai.ro");
        torneo2001.add("torneo2001/mnl_1a.ro");
        torneo2001.add("torneo2001/mnl_1b.ro");
        torneo2001.add("torneo2001/murray.ro");
        torneo2001.add("torneo2001/neo0.ro");
        torneo2001.add("torneo2001/nl_5a.ro");
        torneo2001.add("torneo2001/nl_5b.ro");
        torneo2001.add("torneo2001/pentium4.ro");
        torneo2001.add("torneo2001/pippo1a.ro");
        torneo2001.add("torneo2001/pippo1b.ro");
        torneo2001.add("torneo2001/raistlin.ro");
        torneo2001.add("torneo2001/ridicol.ro");
        torneo2001.add("torneo2001/risc.ro");
        torneo2001.add("torneo2001/rudolf_6.ro");
        torneo2001.add("torneo2001/rudy_xp.ro");
        torneo2001.add("torneo2001/sdc2.ro");
        torneo2001.add("torneo2001/sharp2.ro");
        torneo2001.add("torneo2001/staticii.ro");
        torneo2001.add("torneo2001/thunder.ro");
        torneo2001.add("torneo2001/vampire.ro");
        torneo2001.add("torneo2001/xeon.ro");
        torneo2001.add("torneo2001/zifnab.ro");
        torneo2001.add("torneo2001/zombie.ro");

        System.out.println(torneo2001.size() + " robot(s)");
        System.out.print("Loading torneo2002... ");

        torneo2002.add("torneo2002/01.ro");
        torneo2002.add("torneo2002/adsl.ro");
        torneo2002.add("torneo2002/anakin.ro");
        torneo2002.add("torneo2002/asterix.ro");
        torneo2002.add("torneo2002/attila.ro");
        torneo2002.add("torneo2002/bruenor.ro");
        torneo2002.add("torneo2002/colera.ro");
        torneo2002.add("torneo2002/colosseum.ro");
        torneo2002.add("torneo2002/copter_2.ro");
        torneo2002.add("torneo2002/corner5.ro");
        torneo2002.add("torneo2002/doom2099.ro");
        torneo2002.add("torneo2002/drizzt.ro");
        torneo2002.add("torneo2002/dynamite.ro");
        torneo2002.add("torneo2002/enigma.ro");
        torneo2002.add("torneo2002/groucho.ro");
        torneo2002.add("torneo2002/halman.ro");
        torneo2002.add("torneo2002/harpo.ro");
        torneo2002.add("torneo2002/idefix.ro");
        torneo2002.add("torneo2002/jedi5.ro");
        torneo2002.add("torneo2002/kyash_2.ro");
        torneo2002.add("torneo2002/marco.ro");
        torneo2002.add("torneo2002/mazinga.ro");
        torneo2002.add("torneo2002/medioman.ro");
        torneo2002.add("torneo2002/mg_one.ro");
        torneo2002.add("torneo2002/mind.ro");
        torneo2002.add("torneo2002/moveon.ro");
        torneo2002.add("torneo2002/neo_sifr.ro");
        torneo2002.add("torneo2002/obelix.ro");
        torneo2002.add("torneo2002/ollio.ro");
        torneo2002.add("torneo2002/padawan.ro");
        torneo2002.add("torneo2002/peste.ro");
        torneo2002.add("torneo2002/pippo2a.ro");
        torneo2002.add("torneo2002/pippo2b.ro");
        torneo2002.add("torneo2002/regis.ro");
        torneo2002.add("torneo2002/remus.ro");
        torneo2002.add("torneo2002/romulus.ro");
        torneo2002.add("torneo2002/rudolf_7.ro");
        torneo2002.add("torneo2002/scsi.ro");
        torneo2002.add("torneo2002/serse.ro");
        torneo2002.add("torneo2002/ska.ro");
        torneo2002.add("torneo2002/stanlio.ro");
        torneo2002.add("torneo2002/staticxp.ro");
        torneo2002.add("torneo2002/supernov.ro");
        torneo2002.add("torneo2002/theslayer.ro");
        torneo2002.add("torneo2002/tifo.ro");
        torneo2002.add("torneo2002/tigre.ro");
        torneo2002.add("torneo2002/todos.ro");
        torneo2002.add("torneo2002/tomahawk.ro");
        torneo2002.add("torneo2002/vaiolo.ro");
        torneo2002.add("torneo2002/vauban.ro");
        torneo2002.add("torneo2002/wulfgar.ro");
        torneo2002.add("torneo2002/yerba.ro");
        torneo2002.add("torneo2002/yoyo.ro");
        torneo2002.add("torneo2002/zorn.ro");

        System.out.println(torneo2002.size() + " robot(s)");
        System.out.print("Loading torneo2003... ");

        torneo2003.add("torneo2003/730.ro");
        torneo2003.add("torneo2003/adrian.ro");
        torneo2003.add("torneo2003/aladino.ro");
        torneo2003.add("torneo2003/alcadia.ro");
        torneo2003.add("torneo2003/ares.ro");
        torneo2003.add("torneo2003/barbarian.ro");
        torneo2003.add("torneo2003/blitz.ro");
        torneo2003.add("torneo2003/briscolo.ro");
        torneo2003.add("torneo2003/bruce.ro");
        torneo2003.add("torneo2003/cadderly.ro");
        torneo2003.add("torneo2003/cariddi.ro");
        torneo2003.add("torneo2003/crossover.ro");
        torneo2003.add("torneo2003/cvirus2.ro");
        torneo2003.add("torneo2003/cvirus.ro");
        torneo2003.add("torneo2003/cyborg_2.ro");
        torneo2003.add("torneo2003/danica.ro");
        torneo2003.add("torneo2003/dave.ro");
        torneo2003.add("torneo2003/druzil.ro");
        torneo2003.add("torneo2003/dynacond.ro");
        torneo2003.add("torneo2003/elminster.ro");
        torneo2003.add("torneo2003/falco.ro");
        torneo2003.add("torneo2003/foursquare.ro");
        torneo2003.add("torneo2003/frame.ro");
        torneo2003.add("torneo2003/harlock.ro");
        torneo2003.add("torneo2003/herpes.ro");
        torneo2003.add("torneo2003/ici.ro");
        torneo2003.add("torneo2003/instict.ro");
        torneo2003.add("torneo2003/irpef.ro");
        torneo2003.add("torneo2003/janick.ro");
        torneo2003.add("torneo2003/janu.ro");
        torneo2003.add("torneo2003/jedi6.ro");
        torneo2003.add("torneo2003/knt.ro");
        torneo2003.add("torneo2003/kyash_3c.ro");
        torneo2003.add("torneo2003/kyash_3m.ro");
        torneo2003.add("torneo2003/lbr1.ro");
        torneo2003.add("torneo2003/lbr.ro");
        torneo2003.add("torneo2003/lebbra.ro");
        torneo2003.add("torneo2003/maxicond.ro");
        torneo2003.add("torneo2003/mg_two.ro");
        torneo2003.add("torneo2003/minicond.ro");
        torneo2003.add("torneo2003/morituro.ro");
        torneo2003.add("torneo2003/nautilus.ro");
        torneo2003.add("torneo2003/nemo.ro");
        torneo2003.add("torneo2003/neo_sel.ro");
        torneo2003.add("torneo2003/orione.ro");
        torneo2003.add("torneo2003/piiico.ro");
        torneo2003.add("torneo2003/pippo3b.ro");
        torneo2003.add("torneo2003/pippo3.ro");
        torneo2003.add("torneo2003/red_wolf.ro");
        torneo2003.add("torneo2003/rudolf_8.ro");
        torneo2003.add("torneo2003/scanner.ro");
        torneo2003.add("torneo2003/scilla.ro");
        torneo2003.add("torneo2003/sirio.ro");
        torneo2003.add("torneo2003/sith.ro");
        torneo2003.add("torneo2003/sky.ro");
        torneo2003.add("torneo2003/spaceman.ro");
        torneo2003.add("torneo2003/tartaruga.ro");
        torneo2003.add("torneo2003/unico.ro");
        torneo2003.add("torneo2003/valevan.ro");
        torneo2003.add("torneo2003/virus2.ro");
        torneo2003.add("torneo2003/virus3.ro");
        torneo2003.add("torneo2003/virus4.ro");
        torneo2003.add("torneo2003/virus.ro");
        torneo2003.add("torneo2003/yoda.ro");

        System.out.println(torneo2003.size() + " robot(s)");
        System.out.print("Loading torneo2004... ");

        torneo2004.add("torneo2004/adam.ro");
        torneo2004.add("torneo2004/!alien.ro");
        torneo2004.add("torneo2004/bjt.ro");
        torneo2004.add("torneo2004/b_selim.ro");
        torneo2004.add("torneo2004/!caos.ro");
        torneo2004.add("torneo2004/ciclope.ro");
        torneo2004.add("torneo2004/confusion.ro");
        torneo2004.add("torneo2004/coyote.ro");
        torneo2004.add("torneo2004/diodo.ro");
        torneo2004.add("torneo2004/!dna.ro");
        torneo2004.add("torneo2004/fire.ro");
        torneo2004.add("torneo2004/fisco.ro");
        torneo2004.add("torneo2004/frankie.ro");
        torneo2004.add("torneo2004/geriba.ro");
        torneo2004.add("torneo2004/goofy.ro");
        torneo2004.add("torneo2004/gostar.ro");
        torneo2004.add("torneo2004/gotar2.ro");
        torneo2004.add("torneo2004/gotar.ro");
        torneo2004.add("torneo2004/irap.ro");
        torneo2004.add("torneo2004/ire.ro");
        torneo2004.add("torneo2004/ires.ro");
        torneo2004.add("torneo2004/jedi7.ro");
        torneo2004.add("torneo2004/magneto.ro");
        torneo2004.add("torneo2004/mg_three.ro");
        torneo2004.add("torneo2004/mosfet.ro");
        torneo2004.add("torneo2004/m_selim.ro");
        torneo2004.add("torneo2004/multics.ro");
        torneo2004.add("torneo2004/mystica.ro");
        torneo2004.add("torneo2004/n3g4_jr.ro");
        torneo2004.add("torneo2004/n3g4tivo.ro");
        torneo2004.add("torneo2004/new_mini.ro");
        torneo2004.add("torneo2004/pippo04a.ro");
        torneo2004.add("torneo2004/pippo04b.ro");
        torneo2004.add("torneo2004/poldo.ro");
        torneo2004.add("torneo2004/puma.ro");
        torneo2004.add("torneo2004/rat-man.ro");
        torneo2004.add("torneo2004/ravatto.ro");
        torneo2004.add("torneo2004/revo.ro");
        torneo2004.add("torneo2004/rotar.ro");
        torneo2004.add("torneo2004/rudolf_9.ro");
        torneo2004.add("torneo2004/selim_b.ro");
        torneo2004.add("torneo2004/tempesta.ro");
        torneo2004.add("torneo2004/unlimited.ro");
        torneo2004.add("torneo2004/wgdi.ro");
        torneo2004.add("torneo2004/zener.ro");
        torneo2004.add("torneo2004/!zeus.ro");

        System.out.println(torneo2004.size() + " robot(s)");
        System.out.print("Loading torneo2007... ");

        torneo2007.add("torneo2007/angel.ro");
        torneo2007.add("torneo2007/back.ro");
        torneo2007.add("torneo2007/brontolo.ro");
        torneo2007.add("torneo2007/colosso.ro");
        torneo2007.add("torneo2007/electron.ro");
        torneo2007.add("torneo2007/e.ro");
        torneo2007.add("torneo2007/gongolo.ro");
        torneo2007.add("torneo2007/iceman.ro");
        torneo2007.add("torneo2007/jedi8.ro");
        torneo2007.add("torneo2007/macro1.ro");
        torneo2007.add("torneo2007/mammolo.ro");
        torneo2007.add("torneo2007/microbo1.ro");
        torneo2007.add("torneo2007/microbo2.ro");
        torneo2007.add("torneo2007/midi1.ro");
        torneo2007.add("torneo2007/neutron.ro");
        torneo2007.add("torneo2007/nustyle.ro");
        torneo2007.add("torneo2007/pippo07a.ro");
        torneo2007.add("torneo2007/pippo07b.ro");
        torneo2007.add("torneo2007/pisolo.ro");
        torneo2007.add("torneo2007/proton.ro");
        torneo2007.add("torneo2007/proud.ro");
        torneo2007.add("torneo2007/pyro.ro");
        torneo2007.add("torneo2007/rudolf_x.ro");
        torneo2007.add("torneo2007/rythm.ro");
        torneo2007.add("torneo2007/tobey.ro");
        torneo2007.add("torneo2007/t.ro");
        torneo2007.add("torneo2007/zigozago.ro");
        torneo2007.add("torneo2007/z.ro");

        System.out.println(torneo2007.size() + " robot(s)");
        System.out.print("Loading torneo2010... ");

        torneo2010.add("torneo2010/buffy.ro");
        torneo2010.add("torneo2010/cancella.ro");
        torneo2010.add("torneo2010/change.ro");
        torneo2010.add("torneo2010/copia.ro");
        torneo2010.add("torneo2010/enkidu.ro");
        torneo2010.add("torneo2010/eurialo.ro");
        torneo2010.add("torneo2010/gantu.ro");
        torneo2010.add("torneo2010/hal9010.ro");
        torneo2010.add("torneo2010/incolla.ro");
        torneo2010.add("torneo2010/jedi9.ro");
        torneo2010.add("torneo2010/jumba.ro");
        torneo2010.add("torneo2010/macchia.ro");
        torneo2010.add("torneo2010/niso.ro");
        torneo2010.add("torneo2010/party.ro");
        torneo2010.add("torneo2010/pippo10a.ro");
        torneo2010.add("torneo2010/reuben.ro");
        torneo2010.add("torneo2010/stitch.ro");
        torneo2010.add("torneo2010/suddenly.ro");
        torneo2010.add("torneo2010/sweat.ro");
        torneo2010.add("torneo2010/taglia.ro");
        torneo2010.add("torneo2010/toppa.ro");
        torneo2010.add("torneo2010/wall-e.ro");

        System.out.println(torneo2010.size() + " robot(s)");
        System.out.print("Loading torneo2011... ");

        torneo2011.add("torneo2011/armin.ro");
        torneo2011.add("torneo2011/ataman.ro");
        torneo2011.add("torneo2011/coeurl.ro");
        torneo2011.add("torneo2011/digitale.ro");
        torneo2011.add("torneo2011/gerty.ro");
        torneo2011.add("torneo2011/grendizer.ro");
        torneo2011.add("torneo2011/gru.ro");
        torneo2011.add("torneo2011/guntank.ro");
        torneo2011.add("torneo2011/hal9011.ro");
        torneo2011.add("torneo2011/jedi10.ro");
        torneo2011.add("torneo2011/jeeg.ro");
        torneo2011.add("torneo2011/minion.ro");
        torneo2011.add("torneo2011/nikita.ro");
        torneo2011.add("torneo2011/origano.ro");
        torneo2011.add("torneo2011/ortica.ro");
        torneo2011.add("torneo2011/pain.ro");
        torneo2011.add("torneo2011/piperita.ro");
        torneo2011.add("torneo2011/pippo11a.ro");
        torneo2011.add("torneo2011/pippo11b.ro");
        torneo2011.add("torneo2011/smart.ro");
        torneo2011.add("torneo2011/tannhause.ro");
        torneo2011.add("torneo2011/tantalo.ro");
        torneo2011.add("torneo2011/unmaldestr.ro");
        torneo2011.add("torneo2011/vain.ro");
        torneo2011.add("torneo2011/vector.ro");
        torneo2011.add("torneo2011/wall-e_ii.ro");

        System.out.println(torneo2011.size() + " robot(s)");
        System.out.print("Loading torneo2012... ");

        torneo2012.add("torneo2012/avoider.ro");
        torneo2012.add("torneo2012/beat.ro");
        torneo2012.add("torneo2012/british.ro");
        torneo2012.add("torneo2012/camille.ro");
        torneo2012.add("torneo2012/china.ro");
        torneo2012.add("torneo2012/cliche.ro");
        torneo2012.add("torneo2012/crazy96.ro");
        torneo2012.add("torneo2012/dampyr.ro");
        torneo2012.add("torneo2012/draka.ro");
        torneo2012.add("torneo2012/easyjet.ro");
        torneo2012.add("torneo2012/flash8c.ro");
        torneo2012.add("torneo2012/flash8e.ro");
        torneo2012.add("torneo2012/gerty2.ro");
        torneo2012.add("torneo2012/grezbot2.ro");
        torneo2012.add("torneo2012/gunnyb29.ro");
        torneo2012.add("torneo2012/hal9012.ro");
        torneo2012.add("torneo2012/jedi11.ro");
        torneo2012.add("torneo2012/life.ro");
        torneo2012.add("torneo2012/lufthansa.ro");
        torneo2012.add("torneo2012/lycan.ro");
        torneo2012.add("torneo2012/mister2b.ro");
        torneo2012.add("torneo2012/mister3b.ro");
        torneo2012.add("torneo2012/pippo12a.ro");
        torneo2012.add("torneo2012/pippo12b.ro");
        torneo2012.add("torneo2012/power.ro");
        torneo2012.add("torneo2012/puffomac.ro");
        torneo2012.add("torneo2012/puffomic.ro");
        torneo2012.add("torneo2012/puffomid.ro");
        torneo2012.add("torneo2012/q.ro");
        torneo2012.add("torneo2012/ryanair.ro");
        torneo2012.add("torneo2012/silversurf.ro");
        torneo2012.add("torneo2012/torchio.ro");
        torneo2012.add("torneo2012/wall-e_iii.ro");
        torneo2012.add("torneo2012/yeti.ro");

        System.out.println(torneo2012.size() + " robot(s)");
        System.out.print("Loading crobs... ");

        crobs.add("crobs/adversar.ro");
        crobs.add("crobs/agressor.ro");
        crobs.add("crobs/antru.ro");
        crobs.add("crobs/assassin.ro");
        crobs.add("crobs/b4.ro");
        crobs.add("crobs/bishop.ro");
        crobs.add("crobs/bouncer.ro");
        crobs.add("crobs/boxer.ro");
        crobs.add("crobs/cassius.ro");
        crobs.add("crobs/catfish3.ro");
        crobs.add("crobs/chase.ro");
        crobs.add("crobs/chaser.ro");
        crobs.add("crobs/cooper1.ro");
        crobs.add("crobs/cooper2.ro");
        crobs.add("crobs/cornerkl.ro");
        crobs.add("crobs/counter.ro");
        crobs.add("crobs/counter2.ro");
        crobs.add("crobs/cruiser.ro");
        crobs.add("crobs/cspotrun.ro");
        crobs.add("crobs/danimal.ro");
        crobs.add("crobs/dave.ro");
        crobs.add("crobs/di.ro");
        crobs.add("crobs/dirtyh.ro");
        crobs.add("crobs/duck.ro");
        crobs.add("crobs/dumbname.ro");
        crobs.add("crobs/etf_kid.ro");
        crobs.add("crobs/flyby.ro");
        crobs.add("crobs/fred.ro");
        crobs.add("crobs/friendly.ro");
        crobs.add("crobs/grunt.ro");
        crobs.add("crobs/gsmr2.ro");
        crobs.add("crobs/h-k.ro");
        crobs.add("crobs/hac_atak.ro");
        crobs.add("crobs/hak3.ro");
        crobs.add("crobs/hitnrun.ro");
        crobs.add("crobs/hunter.ro");
        crobs.add("crobs/huntlead.ro");
        crobs.add("crobs/intrcptr.ro");
        crobs.add("crobs/jagger.ro");
        crobs.add("crobs/jason100.ro");
        crobs.add("crobs/kamikaze.ro");
        crobs.add("crobs/killer.ro");
        crobs.add("crobs/leader.ro");
        crobs.add("crobs/leavy.ro");
        crobs.add("crobs/lethal.ro");
        crobs.add("crobs/maniac.ro");
        crobs.add("crobs/marvin.ro");
        crobs.add("crobs/mini.ro");
        crobs.add("crobs/ninja.ro");
        crobs.add("crobs/nord.ro");
        crobs.add("crobs/nord2.ro");
        crobs.add("crobs/ogre.ro");
        crobs.add("crobs/ogre2.ro");
        crobs.add("crobs/ogre3.ro");
        crobs.add("crobs/perizoom.ro");
        crobs.add("crobs/pest.ro");
        crobs.add("crobs/phantom.ro");
        crobs.add("crobs/pingpong.ro");
        crobs.add("crobs/politik.ro");
        crobs.add("crobs/pzk.ro");
        crobs.add("crobs/pzkmin.ro");
        crobs.add("crobs/quack.ro");
        crobs.add("crobs/quikshot.ro");
        crobs.add("crobs/rabbit10.ro");
        crobs.add("crobs/rambo3.ro");
        crobs.add("crobs/rapest.ro");
        crobs.add("crobs/reflex.ro");
        crobs.add("crobs/robbie.ro");
        crobs.add("crobs/rook.ro");
        crobs.add("crobs/rungun.ro");
        crobs.add("crobs/samurai.ro");
        crobs.add("crobs/scan.ro");
        crobs.add("crobs/scanlock.ro");
        crobs.add("crobs/scanner.ro");
        crobs.add("crobs/secro.ro");
        crobs.add("crobs/sentry.ro");
        crobs.add("crobs/shark3.ro");
        crobs.add("crobs/shark4.ro");
        crobs.add("crobs/silly.ro");
        crobs.add("crobs/slead.ro");
        crobs.add("crobs/sniper.ro");
        crobs.add("crobs/spinner.ro");
        crobs.add("crobs/spot.ro");
        crobs.add("crobs/squirrel.ro");
        crobs.add("crobs/stalker.ro");
        crobs.add("crobs/stush-1.ro");
        crobs.add("crobs/topgun.ro");
        crobs.add("crobs/tracker.ro");
        crobs.add("crobs/trial4.ro");
        crobs.add("crobs/twedlede.ro");
        crobs.add("crobs/twedledm.ro");
        crobs.add("crobs/venom.ro");
        crobs.add("crobs/watchdog.ro");
        crobs.add("crobs/wizard.ro");
        crobs.add("crobs/xecutner.ro");
        crobs.add("crobs/xhatch.ro");
        crobs.add("crobs/yal.ro");

        System.out.println(crobs.size() + " robot(s)");
        System.out.print("Loading micro... ");

        micro.add("micro/caccola.ro");
        micro.add("micro/carletto.ro");
        micro.add("micro/chobin.ro");
        micro.add("micro/dream.ro");
        micro.add("micro/ld.ro");
        micro.add("micro/lucifer.ro");
        micro.add("micro/marlene.ro");
        micro.add("micro/md8.ro");
        micro.add("micro/md9.ro");
        micro.add("micro/mflash.ro");
        micro.add("micro/minizai.ro");
        micro.add("micro/pacoon.ro");
        micro.add("micro/pikachu.ro");
        micro.add("micro/pippo00a.ro");
        micro.add("micro/pippo00.ro");
        micro.add("micro/pirla.ro");
        micro.add("micro/p.ro");
        micro.add("micro/rudy.ro");
        micro.add("micro/static.ro");
        micro.add("micro/tanzen.ro");
        micro.add("micro/uhm.ro");
        micro.add("micro/zioalfa.ro");
        micro.add("micro/zzz.ro");

        System.out.println(micro.size() + " robot(s)");
    }
}
