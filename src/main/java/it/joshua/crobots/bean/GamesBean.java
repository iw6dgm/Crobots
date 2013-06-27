package it.joshua.crobots.bean;

import it.joshua.crobots.data.TableName;
import it.joshua.crobots.xml.Match;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Store an unique game with robots and match status info
 * @author joshua
 */
public class GamesBean implements Serializable {

    private static final long serialVersionUID = -5915415480827752091L;
    // Unique ID. It comes from the database
    private final Integer id;
    // F2F, 3VS3 or 4VS4
    private final TableName tableName;
    // Match repetition factor
    private final Integer games;
    // Match status
    private String action;
    // Robots list
    private List<RobotGameBean> robots;

    public Integer getId() {
        return id;
    }

    public TableName getTableName() {
        return tableName;
    }

    public List<RobotGameBean> getRobots() {
        return robots;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getGames() {
        return games;
    }

    private GamesBean(Builder builder) {
        this.id = builder.id;
        this.tableName = builder.tableName;
        this.action = builder.action;
        this.games = builder.games;
        this.robots = builder.robots;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((tableName == null) ? 0 : tableName.hashCode());
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
        GamesBean other = (GamesBean) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (tableName == null) {
            if (other.tableName != null) {
                return false;
            }
        } else if (!tableName.equals(other.tableName)) {
            return false;
        }
        return true;
    }

    public static class Builder implements it.joshua.crobots.Builder<GamesBean> {

        private final Integer id;
        private final TableName tableName;
        private final Integer games;
        private String action;
        private List<RobotGameBean> robots;
        
        public Builder(Match match) {
            this.id = match.getId();
            this.games = match.getGames();
            this.action = match.getAction();
            this.robots = new ArrayList<>();
            switch (match.getTableName()) {
                case "f2f" :
                    this.tableName  = TableName.F2F;
                    break;
                case "3vs3" :
                    this.tableName = TableName.VS3;
                    break;
                case "4vs4" :
                    this.tableName = TableName.VS4;
                    break;
                default:
                    this.tableName = null;
            }
        }
        
        public Builder(Integer id, TableName tableName, Integer games, String action) {
            this.id = id;
            this.tableName = tableName;
            this.games = games;
            this.action = action;
            this.robots = new ArrayList<>();
        }

        @Override
        public GamesBean build() {
            return new GamesBean(this);
        }
    }

    @Override
    public String toString() {
        final String TAB = "\n";

        StringBuilder retValue = new StringBuilder();

        retValue.append("GamesBean ( ")
                .append(super.toString()).append(TAB)
                .append("id = ").append(this.id).append(TAB)
                .append("tableName = ").append(this.tableName).append(TAB)
                .append("action = ").append(this.action).append(TAB)
                .append("games = ").append(this.games).append(TAB)
                .append("robots = ").append(this.robots).append(TAB)
                .append(" )");

        return retValue.toString();
    }
}
