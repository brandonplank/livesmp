package org.crypticplank.smp.Config;

public class ConfigField {
    public String key;
    public Class type;
    public Object def;
    public String help;

    public ConfigField(String key, Class<?> type, Object def, String help) {
        this.key = key;
        this.type = type;
        this.def = def;
        this.help = help;
    }
}
