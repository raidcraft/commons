package de.raidcraft.commons.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "rc_plugin")
public class TPlugin {

    @Id
    private int id;
    private String name;
    private String description;
    private String version;
    private String author;
    private Date lastActive;
}
