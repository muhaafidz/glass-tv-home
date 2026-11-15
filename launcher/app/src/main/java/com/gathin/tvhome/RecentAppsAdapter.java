package com.gathin.tvhome;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecentAppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_APP = 1;
    private static final int VIEW_TYPE_CLOSE_ALL = 2;

    public interface OnRecentAppInteractionListener {
        void onAppClicked(RecentAppItem item);
        void onAppClosed(RecentAppItem item, int position, View itemView);
        void onCloseAllClicked();
    }

    private final List<RecentAppItem> items = new ArrayList<>();
    private final OnRecentAppInteractionListener listener;

    public RecentAppsAdapter(OnRecentAppInteractionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RecentAppItem> newItems) {
        items.clear();
        if (newItems != null && !newItems.isEmpty()) {
            items.addAll(newItems);
            // Append the Close All card at the end of the carousel
            items.add(RecentAppItem.createCloseAll());
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            // If only Close All remains, clear everything
            if (items.size() == 1 && items.get(0).isCloseAll) {
                items.clear();
                notifyDataSetChanged();
            }
        }
    }

    public void clearAll() {
        items.clear();
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<RecentAppItem> getItems() {
        return items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isCloseAll ? VIEW_TYPE_CLOSE_ALL : VIEW_TYPE_APP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_CLOSE_ALL) {
            View view = inflater.inflate(R.layout.item_recent_close_all, parent, false);
            return new CloseAllViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_recent_app, parent, false);
            return new AppViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecentAppItem item = items.get(position);
        if (holder instanceof AppViewHolder) {
            ((AppViewHolder) holder).bind(item, listener, position);
        } else if (holder instanceof CloseAllViewHolder) {
            ((CloseAllViewHolder) holder).bind(listener, position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static int blend(int base, int accent, float amount) {
        int r = Math.round(((base >> 16) & 0xFF) * (1 - amount) + ((accent >> 16) & 0xFF) * amount);
        int g = Math.round(((base >> 8) & 0xFF) * (1 - amount) + ((accent >> 8) & 0xFF) * amount);
        int b = Math.round((base & 0xFF) * (1 - amount) + (accent & 0xFF) * amount);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout topPill;
        private final ImageView ivSmallIcon;
        private final TextView tvAppLabel;
        private final FrameLayout cardContainer;
        private final FrameLayout squircleContainer;
        private final ImageView ivCardIcon;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            topPill = itemView.findViewById(R.id.topPill);
            ivSmallIcon = itemView.findViewById(R.id.ivSmallIcon);
            tvAppLabel = itemView.findViewById(R.id.tvAppLabel);
            cardContainer = itemView.findViewById(R.id.cardContainer);
            squircleContainer = itemView.findViewById(R.id.squircleContainer);
            ivCardIcon = itemView.findViewById(R.id.ivCardIcon);
        }

        public void bind(RecentAppItem item, OnRecentAppInteractionListener listener, int position) {
            tvAppLabel.setText(item.label);
            if (item.icon != null) {
                ivSmallIcon.setImageDrawable(item.icon);
                ivCardIcon.setImageDrawable(item.icon);
            }

            int brand = 0xFF2EF2C0;
            String p = item.packageName.toLowerCase(Locale.US);
            if (p.contains(".tv") || p.contains("livetv")) brand = 0xFFFF6B6B;
            else if (p.contains("file") || p.contains("explorer")) brand = 0xFFFFB84D;
            else if (p.contains("netflix") || p.contains("prime")) brand = 0xFFFF6B6B;
            else if (p.contains("youtube")) brand = 0xFFFF6B6B;
            else if (p.contains("play")) brand = 0xFF4DD8FF;

            float density = itemView.getResources().getDisplayMetrics().density;
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(blend(Design.SURFACE_RAISED, brand, 0.12f));
            cardBg.setStroke(Math.round(1f * density), Design.HAIRLINE);
            cardBg.setCornerRadius(26 * density);
            GradientDrawable cardFocus = new GradientDrawable();
            cardFocus.setColor(blend(Design.SURFACE_RAISED, brand, 0.20f));
            cardFocus.setStroke(Math.round(2f * density), Design.ACCENT);
            cardFocus.setCornerRadius(26 * density);
            cardContainer.setBackground(cardBg);

            GradientDrawable squircleBg = new GradientDrawable();
            squircleBg.setCornerRadius(22 * density);
            squircleBg.setColor(Design.SURFACE);
            squircleBg.setStroke(Math.round(1f * density), Design.HAIRLINE);
            squircleContainer.setBackground(squircleBg);

            cardContainer.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    itemView.setTranslationZ(60f);
                    v.setBackground(cardFocus);
                    v.animate().scaleX(1.06f).scaleY(1.06f).setDuration(160).start();
                    topPill.setBackgroundResource(R.drawable.bg_app_pill_focused);
                    tvAppLabel.setTextColor(0xFF06120E);
                } else {
                    itemView.setTranslationZ(0f);
                    v.setBackground(cardBg);
                    v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(160).start();
                    topPill.setBackgroundResource(R.drawable.bg_app_pill_unfocused);
                    tvAppLabel.setTextColor(0xFFFFFFFF);
                }
            });

            cardContainer.setOnClickListener(v -> {
                if (listener != null) listener.onAppClicked(item);
            });

            cardContainer.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (listener != null) {
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onAppClosed(item, pos, itemView);
                        }
                    }
                    return true;
                }
                return false;
            });
        }
    }

    static class CloseAllViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout btnCircle;

        public CloseAllViewHolder(@NonNull View itemView) {
            super(itemView);
            btnCircle = itemView.findViewById(R.id.btnCircle);
        }

        public void bind(OnRecentAppInteractionListener listener, int position) {
            btnCircle.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    itemView.setTranslationZ(60f);
                    btnCircle.animate().scaleX(1.16f).scaleY(1.16f).setDuration(160).start();
                } else {
                    itemView.setTranslationZ(0f);
                    btnCircle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(160).start();
                }
            });

            btnCircle.setOnClickListener(v -> {
                if (listener != null) listener.onCloseAllClicked();
            });

            btnCircle.setOnKeyListener((v, keyCode, event) -> {
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (listener != null) listener.onCloseAllClicked();
                    return true;
                }
                return false;
            });
        }
    }
}
