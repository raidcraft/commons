package de.raidcraft.commons.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author mdoering
 */
@Getter
@Setter
@Entity
@Table(name = "rc_player_log_stats")
public class TPlayerLogStatistic {

    @Id
    private int id;
    @ManyToOne
    private TPlayerLog log;
    private String statistic;
    private int logonValue;
    private int logoffValue;
}
