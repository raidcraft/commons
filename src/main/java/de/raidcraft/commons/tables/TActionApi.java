package de.raidcraft.commons.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Dragonfire
 */
@Getter
@Setter
@Entity
@Table(name = "rc_actionapi")
public class TActionApi {


    @Id
    private int id;
    private String action_type;
    private String name;
    private String description;
    private String server;
    @Lob
    private String conf;
    private boolean active;
    private Date lastActive;
}
