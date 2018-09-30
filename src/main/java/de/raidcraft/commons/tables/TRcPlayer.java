package de.raidcraft.commons.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Dragonfire
 */
@Getter
@Setter
@Entity
@Table(name = "rc_players")
public class TRcPlayer {

    @Id
    private int id;
    private UUID uuid;
    private String lastName;
    private Date lastJoined;
    private Date firstJoined;
    private List<TPlayerLog> logs = new ArrayList<>();
}
