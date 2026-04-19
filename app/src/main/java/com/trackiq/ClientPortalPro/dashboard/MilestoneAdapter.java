package com.trackiq.ClientPortalPro.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.trackiq.ClientPortalPro.R;
import com.trackiq.ClientPortalPro.databinding.ItemMilestoneBinding;

import java.util.List;

public class MilestoneAdapter extends RecyclerView.Adapter<MilestoneAdapter.MilestoneViewHolder> {

    private final List<Milestone> milestoneList;
    private final Context context;

    public MilestoneAdapter(Context context, List<Milestone> milestoneList) {
        this.context = context;
        this.milestoneList = milestoneList;
    }

    @NonNull
    @Override
    public MilestoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMilestoneBinding binding = ItemMilestoneBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MilestoneViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MilestoneViewHolder holder, int position) {
        Milestone milestone = milestoneList.get(position);
        holder.binding.tvMilestoneTitle.setText(milestone.getTitle());

        // Handle Line Visibility (first and last items)
        holder.binding.lineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.binding.lineBottom.setVisibility(position == milestoneList.size() - 1 ? View.INVISIBLE : View.VISIBLE);

        // Define Brand Colors
        int colorSuccess = ContextCompat.getColor(context, R.color.status_success);
        int colorAccent = ContextCompat.getColor(context, R.color.brand_accent);
        int colorGray = Color.parseColor("#CBD5E1"); // Light gray for pending

        // Apply Dynamic Styling based on Status
        if (milestone.getStatus() == Milestone.STATUS_COMPLETED) {
            holder.binding.indicatorCircle.setStrokeColor(colorSuccess);
            holder.binding.indicatorCircle.setCardBackgroundColor(colorSuccess);
            holder.binding.tvMilestoneTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.binding.lineTop.setBackgroundColor(colorSuccess);
            if (position < milestoneList.size() - 1 && milestoneList.get(position + 1).getStatus() != Milestone.STATUS_PENDING) {
                holder.binding.lineBottom.setBackgroundColor(colorSuccess);
            } else {
                holder.binding.lineBottom.setBackgroundColor(colorGray);
            }

        } else if (milestone.getStatus() == Milestone.STATUS_ACTIVE) {
            holder.binding.indicatorCircle.setStrokeColor(colorAccent);
            holder.binding.indicatorCircle.setCardBackgroundColor(Color.TRANSPARENT);
            holder.binding.indicatorCircle.setStrokeWidth(6);
            holder.binding.tvMilestoneTitle.setTextColor(colorAccent);
            holder.binding.lineTop.setBackgroundColor(colorSuccess);
            holder.binding.lineBottom.setBackgroundColor(colorGray);

        } else { // PENDING
            holder.binding.indicatorCircle.setStrokeColor(colorGray);
            holder.binding.indicatorCircle.setCardBackgroundColor(Color.TRANSPARENT);
            holder.binding.indicatorCircle.setStrokeWidth(3);
            holder.binding.tvMilestoneTitle.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            holder.binding.lineTop.setBackgroundColor(colorGray);
            holder.binding.lineBottom.setBackgroundColor(colorGray);
        }
    }

    @Override
    public int getItemCount() {
        return milestoneList.size();
    }

    static class MilestoneViewHolder extends RecyclerView.ViewHolder {
        ItemMilestoneBinding binding;

        public MilestoneViewHolder(ItemMilestoneBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
