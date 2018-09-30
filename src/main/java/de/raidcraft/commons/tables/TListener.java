package de.raidcraft.commons.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * @author Dragonfire
 */
@Getter
@Setter
@Entity
@Table(name = "rc_listener", uniqueConstraints =
@UniqueConstraint(columnNames = {"listener", "server"}))
public class TListener {


    @Id
    private int id;
    private String listener;
    private String plugin;
    private Date lastLoaded = new Date();
    private String server;
}
