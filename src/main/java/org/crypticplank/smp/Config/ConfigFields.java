package org.crypticplank.smp.Config;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigFields {
    private final Plugin plugin;

    public static ConfigField PREFS_KICK_OFFLINE_PLAYERS = new ConfigField(
            "preferences.kick_offline_players",
            Boolean.class,
            true,
            "Whether or not players should be kicked for being offline"
    );

    public static ConfigField PREFS_KICK_OP = new ConfigField(
            "preferences.kick_op",
            Boolean.class,
            false,
            "Whether or not Admins should be kicked for being offline"
    );

    public static ConfigField PREFS_NON_OP_LINK_OTHER_PLAYERS = new ConfigField(
            "preferences.non_op_link_other_players",
            Boolean.class,
            false,
            "Whether or not regular Users can change other users Twitch registration"
    );

    public static ConfigField PREFS_MIN_VIEWERS_ENABLED = new ConfigField(
            "preferences.minimum_viewers.enabled",
            Boolean.class,
            false,
            "Kick players for having too low viewership"
    );

    public static ConfigField PREFS_MIN_VIEWERS_THRESHOLD  = new ConfigField(
            "preferences.minimum_viewers.threshold",
            Integer.class,
            250,
            "Threshold to kick players. Viewership < Threshold = Kick"
    );

    public static ConfigField MESSAGES_NO_TWITCH = new ConfigField(
            "messages.no_twitch",
            String.class,
            "Please link your twitch. Use /link username",
            "Message displayed when a player is frozen for not having a linked Twitch"
    );

    public static ConfigField MESSAGES_OFFLINE = new ConfigField(
            "messages.offline",
            String.class,
            "Please start streaming before you connect\nIf you just started streaming, please allow time for\nthe twitch api to be updated.",
            "Kick Message shown to user when they're kicked for being offline"
    );

    public static ConfigField MESSAGES_NOT_ENOUGH_VIEWERS = new ConfigField(
            "messages.not_enough_viewers",
            String.class,
            "You must have at least %d views to stream!",
            "Kick Message shown to user when they're kicked for not having enough viewers"
    );

    public static ConfigField MOTD_ENABLED = new ConfigField(
            "motd.enabled",
            Boolean.class,
            true,
            "Enable custom MOTD messages"
    );

    public static ConfigField PERMISSIONS_SMP_RELOAD_DEFAULT = new ConfigField(
            "permissions.smp-reload.default",
            Boolean.class,
            false,
            "Allows everyone to use the smpreload command"
    );

    public static ConfigField PERMISSIONS_MESSAGE = new ConfigField(
            "permissions.message",
            String.class,
            ChatColor.RED + "You do not have permission to use this!",
            "Message when someone can't run a command"
    );

    public static ConfigField UTIL_PLAYER_PLACED_SHRIEKER  = new ConfigField(
            "util.player_placed_shrieker_can_spawn_warden",
            Boolean.class,
            true,
            "Players can place shriekers that summon wardens"
    );

    public static ConfigField UTIL_MICHAEL_JACKSON_EXPERIENCE_ENABLED  = new ConfigField(
            "util.michael_jackson_experience.enabled",
            Boolean.class,
            true,
            "Funny :)"
    );

    public static ConfigField UTIL_MICHAEL_JACKSON_EXPERIENCE_MESSAGES  = new ConfigField(
            "util.michael_jackson_experience.messages",
            ArrayList.class,
            new ArrayList<>(List.of(
                    "%s has noclipped out of reality",
                    "%s has done a fucky wucky and now its time for them to enter the forever box",
                    "%s has said the nword and was sent into the hood"
            )),
            "Funny :)"
    );

    public static ConfigField UTIL_MICHAEL_JACKSON_EXPERIENCE_DEATH_MESSAGES  = new ConfigField(
            "util.michael_jackson_experience.death_messages",
            ArrayList.class,
            new ArrayList<>(List.of(
                    "%s died from the Michael Jackson experience",
                    "%s could not hehe",
                    "Michael Jackson killed %s"
            )),
            "Funny :)"
    );

    public ConfigFields(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the ConfigField of the given name.
     * @param name
     * @return
     */
    public ConfigField valueOf(String name) {
        ConfigField ret = null;
        for(Field f: allConfigFields()) {
            if(f.getName().equals(name)) {
                try {
                    ret = (ConfigField)f.get(null);
                } catch (IllegalAccessException e) {
                    plugin.getLogger().warning(ChatColor.RED + "Failed to read Config Property");
                    e.printStackTrace();
                    ret = null;
                }
            }
        }
        return ret;
    }

    /**
     * Returns all the ConfigFields in the class.
     * @return
     */
    public List<ConfigField> all() {
        List<ConfigField> result = new ArrayList<>();
        for(Field f: allConfigFields()) {
            try {
                result.add((ConfigField)f.get(null));
            } catch (IllegalAccessException e) {
                plugin.getLogger().warning(ChatColor.RED + "Failed to read Config Property");
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Returns the names of the ConfigFields.
     * @return
     */
    public List<String> names() {
        List<String> result = new ArrayList<>();
        for(Field f: allConfigFields()) {
            result.add(f.getName());
        }
        return result;
    }

    /**
     * Returns all fields in this class of the type ConfigField
     * @return
     */
    private List<Field> allConfigFields(){
        List<Field> result = new ArrayList<>();
        Field[] fields = ConfigFields.class.getDeclaredFields();
        for(Field f : fields){
            if(ConfigField.class.isAssignableFrom(f.getType())){
                result.add(f);
            }
        }
        return result;
    }
}
