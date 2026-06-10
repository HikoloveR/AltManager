package ru.fakefun.altmanager.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AltAccountStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path SAVE_PATH = FabricLoader.getInstance().getConfigDir().resolve("altmanager_accounts.json");

    private final List<String> accounts = new ArrayList<>();
    private final List<String> pinnedAccounts = new ArrayList<>();
    private String selectedAccount = "";

    public void load() {
        accounts.clear();
        pinnedAccounts.clear();
        selectedAccount = "";

        if (!Files.exists(SAVE_PATH)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(SAVE_PATH)) {
            SaveData data = GSON.fromJson(reader, SaveData.class);
            if (data == null) {
                return;
            }

            if (data.pinnedAccounts != null) {
                for (String pinned : data.pinnedAccounts) {
                    String normalized = normalize(pinned);
                    if (!normalized.isEmpty()) {
                        String key = normalized.toLowerCase(Locale.ROOT);
                        if (!pinnedAccounts.contains(key)) {
                            pinnedAccounts.add(key);
                        }
                    }
                }
            }

            if (data.accounts != null) {
                Set<String> seen = new LinkedHashSet<>();
                for (String account : data.accounts) {
                    String normalized = normalize(account);
                    if (!normalized.isEmpty() && seen.add(normalized.toLowerCase(Locale.ROOT))) {
                        accounts.add(normalized);
                    }
                }
            }

            selectedAccount = normalize(data.selectedAccount);
            if (!accounts.contains(selectedAccount)) {
                selectedAccount = "";
            }
            reorder();
        } catch (IOException | JsonSyntaxException ignored) {
            accounts.clear();
            pinnedAccounts.clear();
            selectedAccount = "";
        }
    }

    public void save() {
        try {
            Files.createDirectories(SAVE_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(SAVE_PATH)) {
                SaveData data = new SaveData();
                data.accounts = accounts;
                data.pinnedAccounts = pinnedAccounts;
                data.selectedAccount = selectedAccount;
                GSON.toJson(data, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public List<String> accounts() {
        return Collections.unmodifiableList(accounts);
    }

    public String selectedAccount() {
        return selectedAccount;
    }

    public boolean add(String account) {
        String normalized = normalize(account);
        if (!isValidNickname(normalized)) {
            return false;
        }

        for (String existing : accounts) {
            if (existing.equalsIgnoreCase(normalized)) {
                return false;
            }
        }

        accounts.add(normalized);
        reorder();
        save();
        return true;
    }

    public void reorder() {
        List<String> pinned = new ArrayList<>();
        List<String> unpinned = new ArrayList<>();
        
        for (String pin : pinnedAccounts) {
            for (String account : accounts) {
                if (account.equalsIgnoreCase(pin)) {
                    pinned.add(account);
                    break;
                }
            }
        }
        
        for (String account : accounts) {
            if (isPinned(account) && !pinned.contains(account)) {
                pinned.add(account);
            }
        }
        
        for (String account : accounts) {
            if (!isPinned(account)) {
                unpinned.add(account);
            }
        }
        
        accounts.clear();
        accounts.addAll(pinned);
        accounts.addAll(unpinned);
    }

    public int getPinnedCount() {
        int count = 0;
        for (String account : accounts) {
            if (isPinned(account)) {
                count++;
            }
        }
        return count;
    }

    public void movePinned(int fromIndex, int toIndex) {
        if (fromIndex >= 0 && fromIndex < accounts.size() && toIndex >= 0 && toIndex < accounts.size()) {
            String fromAcc = accounts.get(fromIndex);
            String toAcc = accounts.get(toIndex);
            String fromKey = normalize(fromAcc).toLowerCase(Locale.ROOT);
            String toKey = normalize(toAcc).toLowerCase(Locale.ROOT);

            int fromPinIdx = pinnedAccounts.indexOf(fromKey);
            int toPinIdx = pinnedAccounts.indexOf(toKey);
            if (fromPinIdx != -1 && toPinIdx != -1) {
                String moved = pinnedAccounts.remove(fromPinIdx);
                pinnedAccounts.add(toPinIdx, moved);
                reorder();
                save();
            }
        }
    }

    public boolean isPinned(String account) {
        return pinnedAccounts.contains(normalize(account).toLowerCase(Locale.ROOT));
    }

    public void setPinned(String account, boolean pinned) {
        String key = normalize(account).toLowerCase(Locale.ROOT);
        if (pinned) {
            if (!pinnedAccounts.contains(key)) {
                pinnedAccounts.add(key);
            }
        } else {
            pinnedAccounts.remove(key);
        }
        reorder();
        save();
    }

    public void clear() {
        accounts.removeIf(account -> !isPinned(account));
        if (!accounts.contains(selectedAccount)) {
            selectedAccount = "";
        }
        reorder();
        save();
    }

    public void remove(String account) {
        accounts.removeIf(existing -> existing.equalsIgnoreCase(account));
        pinnedAccounts.remove(normalize(account).toLowerCase(Locale.ROOT));
        if (selectedAccount.equalsIgnoreCase(account)) {
            selectedAccount = "";
        }
        reorder();
        save();
    }

    public void select(String account) {
        selectedAccount = normalize(account);
        save();
    }

    public boolean containsIgnoreCase(String account) {
        for (String existing : accounts) {
            if (existing.equalsIgnoreCase(account)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidNickname(String account) {
        if (account.length() < 3 || account.length() > 16) {
            return false;
        }

        for (int i = 0; i < account.length(); i++) {
            char c = account.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_')) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidNicknameInput(String account) {
        if (account == null || account.length() > 16) {
            return false;
        }

        for (int i = 0; i < account.length(); i++) {
            char c = account.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_')) {
                return false;
            }
        }

        return true;
    }

    private static String normalize(String account) {
        return account == null ? "" : account.trim();
    }

    private static final class SaveData {
        private List<String> accounts = List.of();
        private List<String> pinnedAccounts = List.of();
        private String selectedAccount = "";
    }
}
