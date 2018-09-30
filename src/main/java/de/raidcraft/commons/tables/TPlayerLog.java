package de.raidcraft.commons.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Dragonfire
 */
@Getter
@Setter
@Entity
@Table(name = "rc_player_logs")
public class TPlayerLog {

    @Id
    private int id;
    private UUID player;
    private String name;
    private Timestamp joinTime;
    private Timestamp quitTime;
    private String world;
    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "log_id")
    private List<TPlayerLogStatistic> statistics = new ArrayList<>();
}
