package ru.fakefun.altmanager.account;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import ru.fakefun.altmanager.mixin.MinecraftClientAccessor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public final class SessionSwitcher {
    private SessionSwitcher() {
    }

    public static void switchTo(String nickname) {
        MinecraftClient client = MinecraftClient.getInstance();
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + nickname).getBytes(StandardCharsets.UTF_8));
        Session session = new Session(nickname, uuid, "0", Optional.empty(), Optional.empty());
        ((MinecraftClientAccessor) client).altmanager$setSession(session);
    }
}
