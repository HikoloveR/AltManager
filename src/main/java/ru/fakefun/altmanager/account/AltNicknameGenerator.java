package ru.fakefun.altmanager.account;

import java.util.List;
import java.util.Random;
import java.util.Set;

public final class AltNicknameGenerator {
    private static final Random RANDOM = new Random();

    private static final Set<String> USED_NICKNAMES = new java.util.concurrent.ConcurrentHashMap<String, Boolean>().keySet(true);

    private static final List<String> PREFIXES = List.of(
            "Giga", "Turbo", "Pivo", "Chlen", "Bebra", "Aboba", "Mega", "Sverh", "Anti",
            "Nano", "Chotko", "Puzo", "Lutiy", "Zloy", "Gniloy", "Krutoy", "Bezbash", "Psyho",
            "Baton", "Kompot", "Kefir", "Pelmen", "Tapok", "Sirnik", "Doshik", "Pupsik", "Varenik",
            "Shrek", "Enot", "Surok", "Bobr", "Karasik", "Povar", "Keks", "Tarakan", "Eshka",
            "Adskiy", "Bistriy", "Velikiy", "Dikiy", "Jirniy", "Modniy", "Bedniy", "Mokriy",
            "Crazy", "Sus", "Flex", "Grom", "Chill", "Noob", "Pixel", "Trash", "Milk", "Joy",
            "Boss", "Sleep", "Bomba", "Dunya", "Petya", "Vasya", "Jora", "Misha", "Grisha",
            "Sanek", "Dimon", "Vovan", "Toha", "Leha", "Kesha", "Gena", "Sanya", "Shurik",
            "Monstr", "Zver", "Tupik", "Pups", "Keksik", "Zefir", "Limon", "Bulka",
            "Makaron", "Baron", "Nosok", "Pistolet", "Omlet", "Karton", "Venik", "Bulba",
            "Kartoshka", "Sosiska", "Salat", "Sous", "Grib", "Kust", "List", "Dojd", "Veter"
    );

    private static final List<String> MIDDLES = List.of(
            "slav", "jor", "rez", "kek", "lol", "sus", "pups", "vonyuh", "gnil", "dryas",
            "ubica", "grom", "flex", "chill", "noob", "trash", "krot", "bobr", "svin",
            "boss", "mega", "ultra", "turbo", "giga", "nano", "super", "hyper", "cyber",
            "pixel", "shadow", "dark", "light", "white", "black", "red", "blue", "green",
            "gold", "silver", "iron", "steel", "stone", "wood", "fire", "water", "wind",
            "ice", "toxic", "poison", "acid", "radio", "electro", "quantum", "cosmo",
            "space", "star", "moon", "sun", "sky", "cloud", "storm", "rain", "snow",
            "demon", "angel", "ghost", "spirit", "phantom", "specter", "wraith", "lich",
            "mage", "wizard", "druid", "rogue", "ninja", "samurai", "knight", "warrior",
            "hunter", "archer", "scout", "soldier", "guard", "king", "prince", "lord",
            "duke", "baron", "count", "emperor", "czar", "khan", "chief", "leader",
            "master", "expert", "pro", "hack", "crack", "cheat", "troll", "clown", "jester"
    );

    private static final List<String> NOUNS = List.of(
            "Pelmen", "Kabachok", "Tapok", "Baton", "Sirnik", "Doshik", "Pupsik", "Varenik",
            "Ogurec", "Krevetka", "Shaverma", "Kompot", "Puzan", "Kefir", "Cheburek", "Enot",
            "Surok", "Gamer", "Noob", "Pro", "Hacker", "Pikachu", "Sosiska", "Karas", "Pivo",
            "Klop", "Shrek", "Slon", "Gnom", "Kaban", "Baran", "Chuvak", "Pacan", "Bomj",
            "Malchik", "Ded", "Krot", "Kukold", "Karasik", "Povar", "Nosok", "Bobr", "Keks",
            "Sosison", "Tarakan", "Eshka", "Jiguli", "Lada", "Volga", "Kamaz", "Traktor",
            "Samokat", "Velo", "Moped", "Baik", "Samolet", "Vertolet", "Raketa", "Sputnik",
            "Kosmos", "Planeta", "Zvezda", "Komet", "Meteor", "Galaktika", "Vselennaya",
            "Ozero", "Reka", "More", "Okean", "Gora", "Les", "Pole", "Boloto", "Pustinya",
            "Dvorec", "Zamok", "Dom", "Hata", "Saray", "Garaj", "Zavod", "Shahta", "Klub",
            "Bar", "Pab", "Kafe", "Restoran", "Stolovka", "Kuhnya", "Komp", "Telefon",
            "Televizor", "Radio", "Pleser", "Konsol", "Djoistik", "Klava", "Mishka", "Monitor"
    );

    private static final List<String> SUFFIXES = List.of(
            "1337", "228", "777", "666", "2026", "999", "007", "77", "88", "123", "555", "3000"
    );

    private AltNicknameGenerator() {
    }

    public static String createUnique(AltAccountStore store) {
        for (int attempt = 0; attempt < 2000; attempt++) {
            String nickname = create();
            String lower = nickname.toLowerCase();
            if (!store.containsIgnoreCase(nickname) && !USED_NICKNAMES.contains(lower)) {
                USED_NICKNAMES.add(lower);
                return nickname;
            }
        }

        String fallback;
        do {
            fallback = "Alt" + (100000 + RANDOM.nextInt(900000));
        } while (store.containsIgnoreCase(fallback) || USED_NICKNAMES.contains(fallback.toLowerCase()));
        USED_NICKNAMES.add(fallback.toLowerCase());
        return fallback;
    }

    private static String create() {
        int strategy = RANDOM.nextInt(5);
        String baseName;

        if (strategy == 0) {
            baseName = PREFIXES.get(RANDOM.nextInt(PREFIXES.size())) 
                     + MIDDLES.get(RANDOM.nextInt(MIDDLES.size())) 
                     + NOUNS.get(RANDOM.nextInt(NOUNS.size()));
        } else if (strategy == 1) {
            baseName = PREFIXES.get(RANDOM.nextInt(PREFIXES.size())) 
                     + "_" 
                     + NOUNS.get(RANDOM.nextInt(NOUNS.size()));
        } else if (strategy == 2) {
            baseName = NOUNS.get(RANDOM.nextInt(NOUNS.size())) 
                     + "_" 
                     + MIDDLES.get(RANDOM.nextInt(MIDDLES.size()));
        } else if (strategy == 3) {
            String name = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
            if (name.length() > 10) {
                name = name.substring(0, 8);
            }
            baseName = "xX_" + name + "_Xx";
        } else {
            baseName = PREFIXES.get(RANDOM.nextInt(PREFIXES.size())) 
                     + MIDDLES.get(RANDOM.nextInt(MIDDLES.size()));
        }

        String suffix;
        if (RANDOM.nextInt(3) == 0) {
            suffix = SUFFIXES.get(RANDOM.nextInt(SUFFIXES.size()));
        } else {
            suffix = String.valueOf(100 + RANDOM.nextInt(9900));
        }

        String separator = (RANDOM.nextBoolean() && baseName.length() + suffix.length() < 15 && !baseName.contains("_")) ? "_" : "";
        
        String nickname = baseName;
        int maxBaseLen = 16 - separator.length() - suffix.length();
        if (nickname.length() > maxBaseLen) {
            nickname = nickname.substring(0, maxBaseLen);
        }
        
        nickname = nickname + separator + suffix;

        if (RANDOM.nextInt(5) == 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nickname.length(); i++) {
                char c = nickname.charAt(i);
                if (Character.isLetter(c)) {
                    sb.append(i % 2 == 0 ? Character.toLowerCase(c) : Character.toUpperCase(c));
                } else {
                    sb.append(c);
                }
            }
            nickname = sb.toString();
        }

        while (nickname.length() < 6) {
            nickname += RANDOM.nextInt(10);
        }

        return nickname;
    }
}
