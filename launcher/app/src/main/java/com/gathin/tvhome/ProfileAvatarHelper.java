package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Outline;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Universal helper that binds a UserProfile's avatar (Watak Tunggal Lottie, Animated GIF, or Custom Image)
 * inside any ViewGroup with smooth circular clipping and hardware acceleration.
 */
public final class ProfileAvatarHelper {

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final ExecutorService DECODE_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "avatar-decode");
        t.setDaemon(true);
        return t;
    });

    private static final class BoundAvatar {
        final String signature;

        BoundAvatar(String signature) {
            this.signature = signature;
        }
    }

    private ProfileAvatarHelper() {}

    private static String avatarSignature(ProfileManager.UserProfile profile, Context context) {
        if (profile == null) return "null";
        boolean animEnabled = !context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
                .getBoolean("reduced_motion", false);
        return profile.id + "|" + profile.avatarType + "|" + profile.avatarPath + "|"
                + profile.lottieBgColor + "|" + profile.lottieCharColor + "|"
                + profile.lottieFaceIndex + "|" + animEnabled;
    }

    public static void bindAvatar(ViewGroup container, ProfileManager.UserProfile profile, Context context) {
        if (container == null || context == null) return;

        String signature = avatarSignature(profile, context);
        Object tag = container.getTag();
        if (tag instanceof BoundAvatar && ((BoundAvatar) tag).signature.equals(signature)) return;
        container.setTag(new BoundAvatar(signature));

        container.removeAllViews();

        container.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getWidth() / 2f);
            }
        });
        container.setClipToOutline(true);

        boolean animEnabled = !context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
                .getBoolean("reduced_motion", false);

        if (profile == null || ProfileManager.AVATAR_TYPE_LOTTIE.equals(profile.avatarType)) {
            // 1. Watak Tunggal (Lottie Animation)
            LottieAnimationView lottie = new LottieAnimationView(context);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
            );
            lottie.setLayoutParams(lp);
            container.addView(lottie);

            int bg = (profile != null && profile.lottieBgColor != 0) ? profile.lottieBgColor : AvatarThemeManager.DEFAULT_BG_COLOR;
            int chr = (profile != null && profile.lottieCharColor != 0) ? profile.lottieCharColor : AvatarThemeManager.DEFAULT_CHAR_COLOR;
            int face = (profile != null) ? profile.lottieFaceIndex : AvatarThemeManager.DEFAULT_FACE_INDEX;

            AvatarThemeManager.applyAvatar(lottie, bg, chr, face, context);
            if (!animEnabled || DevicePerformance.isLowSpec(context)) {
                lottie.pauseAnimation();
            }

        } else {
            // 2 & 3. Animated GIF or Custom Image
            ImageView iv = new ImageView(context);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
            );
            iv.setLayoutParams(lp);
            container.addView(iv);

            loadGifOrImage(iv, profile.avatarPath, context);
        }

        // Circular frosted liquid glass border overlay (ReactDrops Soap Bubble style)
        float density = context.getResources().getDisplayMetrics().density;
        View glassRim = new View(context);
        GradientDrawable rimGd = new GradientDrawable();
        rimGd.setShape(GradientDrawable.OVAL);
        rimGd.setColor(Color.TRANSPARENT);
        rimGd.setStroke(Math.max(1, Math.round(1.5f * density)), 0x5967E8F9); // Translucent Soap Bubble iridescent glass rim
        glassRim.setBackground(rimGd);
        container.addView(glassRim, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    public static void loadGifOrImage(ImageView iv, String path, Context context) {
        if (iv == null || context == null || path == null || path.isEmpty()) return;

        File localFile = new File(path);
        if (localFile.exists()) {
            displayLocalFile(iv, localFile);
            return;
        }

        // Check if cached in avatars dir
        File avatarsDir = new File(context.getFilesDir(), "avatars");
        if (!avatarsDir.exists()) avatarsDir.mkdirs();
        String safeName = "gif_" + Math.abs(path.hashCode()) + ".gif";
        File cached = new File(avatarsDir, safeName);
        if (cached.exists()) {
            displayLocalFile(iv, cached);
            return;
        }

        // If remote URL, fetch in background thread
        if (path.startsWith("http://") || path.startsWith("https://")) {
            new Thread(() -> {
                try {
                    URL u = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(12000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (GlassTV; Android)");
                    conn.connect();

                    if (conn.getResponseCode() == 200) {
                        try (InputStream in = conn.getInputStream();
                             FileOutputStream out = new FileOutputStream(cached)) {
                            byte[] buf = new byte[8192];
                            int r;
                            while ((r = in.read(buf)) != -1) {
                                out.write(buf, 0, r);
                            }
                        }
                        MAIN_HANDLER.post(() -> displayLocalFile(iv, cached));
                    }
                } catch (Exception ignored) {}
            }).start();
        }
    }

    private static void displayLocalFile(ImageView iv, File file) {
        if (iv == null || file == null || !file.exists()) return;

        final String token = file.getAbsolutePath() + "|" + file.lastModified() + "|" + file.length();
        iv.setTag(token);

        DECODE_EXECUTOR.execute(() -> {
            Drawable drawable = null;
            Bitmap bitmap = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    ImageDecoder.Source src = ImageDecoder.createSource(file);
                    drawable = ImageDecoder.decodeDrawable(src);
                } catch (Exception ignored) {}
            }
            if (drawable == null) {
                try {
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                } catch (Exception ignored) {}
            }

            final Drawable decodedDrawable = drawable;
            final Bitmap decodedBitmap = bitmap;
            MAIN_HANDLER.post(() -> {
                if (!token.equals(iv.getTag())) return;
                if (decodedDrawable != null) {
                    iv.setImageDrawable(decodedDrawable);
                    if (decodedDrawable instanceof AnimatedImageDrawable) {
                        AnimatedImageDrawable aid = (AnimatedImageDrawable) decodedDrawable;
                        boolean animEnabled = !DevicePerformance.isLowSpec(iv.getContext());
                        try {
                            if (animEnabled) {
                                animEnabled = !iv.getContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
                                        .getBoolean("reduced_motion", false);
                            }
                        } catch (Exception ignored) {}

                        if (animEnabled) {
                            aid.setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                            aid.start();
                        } else {
                            aid.stop();
                        }
                    }
                } else if (decodedBitmap != null) {
                    iv.setImageBitmap(decodedBitmap);
                }
            });
        });
    }
}
