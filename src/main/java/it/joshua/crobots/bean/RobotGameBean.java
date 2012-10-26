package it.joshua.crobots.bean;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class RobotGameBean implements Serializable {

    private static final long serialVersionUID = 7391478408065757891L;
    private static final WeakHashMap<RobotGameBean, WeakReference<RobotGameBean>> flyweightData =
            new WeakHashMap<>();
    private final String robot;
    private Integer win;
    private Integer tie;
    private Integer points;

    public String getRobot() {
        return robot;
    }

    private RobotGameBean(String robot, Integer win, Integer tie, Integer points) {
        super();
        this.robot = robot;
        this.win = win;
        this.tie = tie;
        this.points = points;
    }

    private RobotGameBean(String robot) {
        super();
        this.robot = robot;
    }
    
    public static RobotGameBean create(String robot) {
        RobotGameBean bean = new RobotGameBean(robot);
        if (!flyweightData.containsKey(bean)) {
            flyweightData.put(bean, new WeakReference(bean));
        }
        return flyweightData.get(bean).get();
    }
    
    public static RobotGameBean create(String robot, Integer win, Integer tie, Integer points) {
        RobotGameBean bean = new RobotGameBean(robot, win, tie, points);
        if (!flyweightData.containsKey(bean)) {
            flyweightData.put(bean, new WeakReference(bean));
        }
        return flyweightData.get(bean).get();
    }

    public void setWin(Integer win) {
        this.win = win;
    }

    public void setTie(Integer tie) {
        this.tie = tie;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getWin() {
        return win;
    }

    public Integer getTie() {
        return tie;
    }

    public Integer getPoints() {
        return points;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((robot == null) ? 0 : robot.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RobotGameBean other = (RobotGameBean) obj;
        if (robot == null) {
            if (other.robot != null) {
                return false;
            }
        } else if (!robot.equals(other.robot)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final String TAB = "\n";

        StringBuilder retValue = new StringBuilder();

        retValue.append("RobotGameBean ( ")
                .append(super.toString()).append(TAB)
                .append("robot = ").append(this.robot).append(TAB)
                .append("win = ").append(this.win).append(TAB)
                .append("tie = ").append(this.tie).append(TAB)
                .append("points = ").append(this.points).append(TAB)
                .append(" )");

        return retValue.toString();
    }
}
