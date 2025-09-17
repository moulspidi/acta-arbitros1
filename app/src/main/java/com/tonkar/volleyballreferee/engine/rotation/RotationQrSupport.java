package com.tonkar.volleyballreferee.engine.rotation;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class RotationQrSupport {
    private RotationQrSupport() { }

    private static final String SP = "rotation_store";
    private static final String HOME = "home";
    private static final String AWAY = "away";

    public static void saveHome(Context c, List<String> six) { save(c, HOME, six); }
    public static void saveAway(Context c, List<String> six) { save(c, AWAY, six); }
    public static List<String> getHome(Context c) { return get(c, HOME); }
    public static List<String> getAway(Context c) { return get(c, AWAY); }

    public static boolean applyFromQr(Context c, String payload) {
        if (payload == null) return false;
        String t = payload.trim();
        if (t.isEmpty()) return false;

        if (t.startsWith("{")) {
            try {
                JSONObject o = new JSONObject(t);
                List<String> h = toList(o.optJSONArray("home"));
                List<String> a = toList(o.optJSONArray("away"));
                boolean ok = false;
                if (h.size() == 6) { saveHome(c, h); ok = true; }
                if (a.size() == 6) { saveAway(c, a); ok = true; }
                return ok;
            } catch (JSONException e) {
                return false;
            }
        }

        boolean ok2 = false;
        String[] parts = t.split("\|");
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim().toUpperCase();
            List<String> vals = csv6(kv[1]);
            if (vals.size() == 6) {
                if (key.startsWith("H")) { saveHome(c, vals); ok2 = true; }
                if (key.startsWith("A")) { saveAway(c, vals); ok2 = true; }
            }
        }
        return ok2;
    }

    private static void save(Context c, String k, List<String> six) {
        SharedPreferences sp = c.getSharedPreferences(SP, Context.MODE_PRIVATE);
        sp.edit().putString(k, joinCsv(six)).apply();
    }

    private static List<String> get(Context c, String k) {
        SharedPreferences sp = c.getSharedPreferences(SP, Context.MODE_PRIVATE);
        String s = sp.getString(k, "");
        return csv6(s);
    }

    private static List<String> toList(JSONArray arr) {
        List<String> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            Object v = arr.opt(i);
            if (v != null) out.add(String.valueOf(v));
        }
        return out;
    }

    private static List<String> csv6(String s) {
        List<String> out = new ArrayList<>();
        if (s == null) return out;
        String[] tokens = s.split(",");
        for (String t : tokens) {
            String v = t.trim();
            if (!v.isEmpty()) out.add(v);
        }
        return out;
    }

    private static String joinCsv(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
