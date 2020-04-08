package dev.hihi.questphoneapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlacklistUtils {
    private static final String BLACKLIST_FILE = "blacklist";
    private static final String BLACKLIST_KEY = "blacklist_key";

    private static List<String> BLACKLIST_KEYWORDS = new ArrayList<>();
    static {
        BLACKLIST_KEYWORDS.add("Tap to ask your Assistant about this song");
    }

    public static Set<String> getBlacklist(Context context) {
        SharedPreferences sp = context.getSharedPreferences(BLACKLIST_FILE, Context.MODE_PRIVATE);
        Set<String> set = sp.getStringSet(BLACKLIST_KEY, new HashSet<String>());
        return new HashSet<>(set);
    }

    public static void saveBlacklist(Context context, Set<String> blacklist) {
        SharedPreferences sp = context.getSharedPreferences(BLACKLIST_FILE, Context.MODE_PRIVATE);
        sp.edit().putStringSet(BLACKLIST_KEY, blacklist).commit();
    }

    public static void addBlacklist(Context context, String pkg) {
        Set<String> blacklist = getBlacklist(context);
        blacklist.add(pkg);
        saveBlacklist(context, blacklist);
    }

    public static void removeBlacklist(Context context, String pkg) {
        Set<String> blacklist = getBlacklist(context);
        blacklist.remove(pkg);
        saveBlacklist(context, blacklist);
    }

    public static boolean hasBlacklistKeywords(String s) {
        for (String keyword : BLACKLIST_KEYWORDS) {
            if (s.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
