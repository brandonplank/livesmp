package org.crypticplank.smp.Util;

import org.crypticplank.smp.Config.ConfigField;
import org.crypticplank.smp.Smp;

public class ConfigHelper {
    /**
     * Returns an Object from a given ConfigField.
     * @param field
     * @return
     */
    public static Object get(ConfigField field) {
        return Smp.getPlugin(Smp.class).getConfig().getObject(field.key, field.type, field.def);
    }
}
