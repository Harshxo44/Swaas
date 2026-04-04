package com.swaas.app.ui.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.swaas.app.R;
import com.swaas.app.databinding.ItemWaterBodyBinding;
import com.swaas.app.db.WaterBodyEntity;
import com.swaas.app.utils.MarkerColorHelper;
import com.swaas.app.utils.PrecautionHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for water body list items.
 * Features: animated card entrance, color-coded safety badges, DiffUtil for smooth updates.
 */
public class WaterBodyAdapter extends RecyclerView.Adapter<WaterBodyAdapter.ViewHolder> {

    private List<WaterBodyEntity> items;
    private final OnItemClickListener listener;
    private int lastAnimatedPosition = -1;

    public interface OnItemClickListener {
        void onClick(WaterBodyEntity entity);
    }

    public WaterBodyAdapter(List<WaterBodyEntity> items, OnItemClickListener listener) {
        this.items = new ArrayList<>(items);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWaterBodyBinding binding = ItemWaterBodyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
        animateCardEntrance(holder.itemView, position);
    }

    /** Smooth slide-up + fade-in animation for each card as it enters the screen. */
    private void animateCardEntrance(View view, int position) {
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(80f);
            view.setAlpha(0f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(view, "translationY", 80f, 0f),
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            );
            set.setDuration(350);
            set.setStartDelay(position * 50L); // Staggered entrance
            set.setInterpolator(new DecelerateInterpolator(1.5f));
            set.start();
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void updateList(List<WaterBodyEntity> newList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return items.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int oldPos, int newPos) {
                return items.get(oldPos).getId().equals(newList.get(newPos).getId());
            }
            @Override public boolean areContentsTheSame(int oldPos, int newPos) {
                WaterBodyEntity o = items.get(oldPos), n = newList.get(newPos);
                return o.getSafetyScore() == n.getSafetyScore() &&
                        o.getSafetyLevel().equals(n.getSafetyLevel()) &&
                        o.getName().equals(n.getName());
            }
        });
        this.items = new ArrayList<>(newList);
        result.dispatchUpdatesTo(this);
    }

    public List<WaterBodyEntity> getCurrentList() { return new ArrayList<>(items); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemWaterBodyBinding b;

        ViewHolder(ItemWaterBodyBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void bind(WaterBodyEntity entity) {
            b.tvName.setText(entity.getName());
            b.tvSafetyLevel.setText(entity.getSafetyLevel());
            b.tvSafetyScore.setText(String.format(Locale.getDefault(),
                    "%.0f / 100", entity.getSafetyScore()));
            b.tvPh.setText(String.format(Locale.getDefault(), "pH %.1f", entity.getPh()));
            b.tvTds.setText(String.format(Locale.getDefault(), "TDS %.0f mg/L", entity.getTds()));
            b.tvPrecaution.setText(PrecautionHelper.getShortPrecaution(entity.getSafetyLevel()));

            // Color coding
            int textColor = MarkerColorHelper.getSafetyColor(entity.getSafetyLevel());
            int bgColor = MarkerColorHelper.getSafetyBackgroundColor(entity.getSafetyLevel());
            b.tvSafetyLevel.setTextColor(textColor);
            b.cardSafetyBadge.setCardBackgroundColor(bgColor);

            // Progress bar
            b.progressSafety.setProgress((int) entity.getSafetyScore());
            b.progressSafety.setIndicatorColor(textColor);

            // Last updated
            if (entity.getLastUpdatedMillis() > 0) {
                String formatted = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(new Date(entity.getLastUpdatedMillis()));
                b.tvLastUpdated.setText("Updated: " + formatted);
            }

            // Click listener with press feedback
            b.cardRoot.setOnClickListener(v -> {
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80)
                        .withEndAction(() ->
                                v.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                        .start();
                listener.onClick(entity);
            });
        }
    }
}
