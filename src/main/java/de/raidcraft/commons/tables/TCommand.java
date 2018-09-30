package de.raidcraft.commons.tables;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Dragonfire
 */
@Getter
@Setter
@Entity
@Table(name = "rc_commands")
public class TCommand {

    @Id
    private int id;
    private String host;
    private String base;
    private String aliases;
    private String description;
    private String usage_; // sql reservered word
    private int min;
    private int max;
    private String flags;
    @Column(columnDefinition = "TEXT")
    private String help_; // sql reservered word
    private String permission;
    private String server;

    public static TCommand parseCommand(Method method, String host, String base) {

        TCommand cmd = new TCommand();
        cmd.setServer(Bukkit.getServerName());
        if (base != null) {
            cmd.setBase(base);
        }
        if (host != null) {
            cmd.setHost(host);
        }
        Command anno_cmd = method.getAnnotation(Command.class);
        if (anno_cmd != null) {
            cmd.setAliases(printArray(anno_cmd.aliases()));
            cmd.setDescription(anno_cmd.desc());
            cmd.setUsage_(anno_cmd.usage());
            cmd.setMin(anno_cmd.min());
            cmd.setMax(anno_cmd.max());
            cmd.setHelp_(anno_cmd.help());
        }
        CommandPermissions anno_perm = method.getAnnotation(CommandPermissions.class);
        if (anno_perm != null) {
            cmd.setPermission(printArray(anno_perm.value()));
        }
        return cmd;
    }

    public static String printArray(String[] stringArray) {

        if (stringArray.length <= 0) {
            return null;
        }
        if (stringArray.length == 1) {
            return stringArray[0];
        }
        return Arrays.toString(stringArray);
    }
}
