package com.tonkar.volleyballreferee.ui.rotation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;
import java.util.List;

public class RotationOverlayView extends View {
    private List<String> home = Arrays.asList("", "", "", "", "", "");
    private List<String> away = Arrays.asList("", "", "", "", "", "");

    private final Paint pHome = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pAway = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;

    public RotationOverlayView(Context c) { this(c, null); }
    public RotationOverlayView(Context c, AttributeSet a) {
        super(c, a);
        density = getResources().getDisplayMetrics().density;
        pHome.setColor(0xFFE85D1A); // naranja
        pAway.setColor(0xFF0E5CC6); // azul
        pText.setColor(0xFFFFFFFF);
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTextSize(16 * density);
    }

    public void setData(List<String> home, List<String> away) {
        if (home != null && home.size() == 6) this.home = home;
        if (away != null && away.size() == 6) this.away = away;
        invalidate();
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        float pad = 12 * density;
        float left = pad, top = pad, right = w - pad, bottom = h - pad;
        float midX = (left + right) / 2f;

        drawTeam(c, left, top,  midX, bottom, pHome, home);
        drawTeam(c, midX, top, right, bottom, pAway, away);
    }

    private void drawTeam(Canvas c, float left, float top, float right, float bottom, Paint fill, List<String> nums) {
        float cols = 2f, rows = 3f;
        float cw = (right - left) / cols;
        float ch = (bottom - top) / rows;
        float r = Math.min(cw, ch) * 0.32f;

        int idx = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                float cx = left + col * cw + cw / 2f;
                float cy = top + row * ch + ch / 2f;
                RectF oval = new RectF(cx - r, cy - r, cx + r, cy + r);
                c.drawOval(oval, fill);
                String txt = nums.get(idx < nums.size() ? idx : 0);
                Paint.FontMetrics fm = pText.getFontMetrics();
                float ty = cy - (fm.ascent + fm.descent) / 2;
                c.drawText(txt, cx, ty, pText);
                idx++;
            }
        }
    }
}
