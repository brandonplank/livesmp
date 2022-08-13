package org.crypticplank.smp.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ComponentUtil {
    public static Component serializedComponent(String text) {
        MiniMessage mm = MiniMessage.miniMessage();
        return mm.deserialize(text);
    }
}
