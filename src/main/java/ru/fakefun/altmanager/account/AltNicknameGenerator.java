package ru.fakefun.altmanager.account;

import java.util.List;
import java.util.Random;

public final class AltNicknameGenerator {
    private static final Random RANDOM = new Random();
    private static final List<String> PREFIXES = List.of(
            "Bebra", "Krevetka", "Shaverma", "Kompot", "Pelmen", "Puzan", "Kefir",
            "Tapok", "Kabachok", "Baton", "Sirnik", "Doshik", "Pupsik", "Varenik"
    );
    private static final List<String> MIDDLES = List.of(
            "Joy", "Boss", "Turbo", "Ultra", "Mega", "Crazy", "Sus", "Flex",
            "Sleep", "Grom", "Chill", "Noob", "Pixel", "Trash", "Milk"
    );

    private AltNicknameGenerator() {
    }

    public static String createUnique(AltAccountStore store) {
        for (int attempt = 0; attempt < 500; attempt++) {
            String nickname = create();
            if (!store.containsIgnoreCase(nickname)) {
                return nickname;
            }
        }

        String fallback;
        do {
            fallback = "Alt" + (100000 + RANDOM.nextInt(900000));
        } while (store.containsIgnoreCase(fallback));
        return fallback;
    }

    private static String create() {
        String prefix = PREFIXES.get(RANDOM.nextInt(PREFIXES.size()));
        String middle = MIDDLES.get(RANDOM.nextInt(MIDDLES.size()));
        int digits = RANDOM.nextInt(1000);
        String nickname = prefix + middle + digits;

        if (nickname.length() > 15) {
            nickname = nickname.substring(0, 12) + RANDOM.nextInt(1000);
        }

        while (nickname.length() < 6) {
            nickname += RANDOM.nextInt(10);
        }

        return nickname;
    }
}
