package com.example.quiz_andres_rosales.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_andres_rosales.R;
import com.example.quiz_andres_rosales.db.entity.ReminderEntity;
import com.example.quiz_andres_rosales.utils.CurrencyUtils;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    public interface OnReminderActionListener {
        void onReminderStatusChanged(ReminderEntity reminder, boolean isPaid);
        void onDeleteReminder(ReminderEntity reminder);
    }

    private final List<ReminderEntity> reminderList;
    private final OnReminderActionListener listener;

    public ReminderAdapter(List<ReminderEntity> reminderList, OnReminderActionListener listener) {
        this.reminderList = reminderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderEntity reminder = reminderList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvReminderTitle.setText(reminder.getTitle());
        holder.tvReminderDueDate.setText("Vence: Día " + reminder.getDueDay() + " de cada mes");
        holder.tvReminderAmount.setText(CurrencyUtils.formatCurrency(reminder.getAmount()));

        if (reminder.isFixed()) {
            holder.tvReminderFixedTag.setText("Gasto Fijo Base");
            holder.tvReminderFixedTag.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.chip_bg_card)));
            holder.tvReminderFixedTag.setTextColor(ContextCompat.getColor(context, R.color.primary));
            holder.btnDeleteReminder.setVisibility(View.GONE);
        } else {
            holder.tvReminderFixedTag.setText("Recordatorio Extra");
            holder.tvReminderFixedTag.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.chip_bg_transfer)));
            holder.tvReminderFixedTag.setTextColor(ContextCompat.getColor(context, R.color.method_transfer));
            holder.btnDeleteReminder.setVisibility(View.VISIBLE);
        }

        String cat = reminder.getCategory();
        if ("Celular".equalsIgnoreCase(cat)) {
            holder.ivReminderIcon.setImageResource(R.drawable.ic_phone);
        } else if ("Plataformas".equalsIgnoreCase(cat)) {
            holder.ivReminderIcon.setImageResource(R.drawable.ic_tv);
        } else if ("Tarjeta de Crédito".equalsIgnoreCase(cat)) {
            holder.ivReminderIcon.setImageResource(R.drawable.ic_card);
        } else {
            holder.ivReminderIcon.setImageResource(R.drawable.ic_reminders);
        }

        holder.cbPaidStatus.setOnCheckedChangeListener(null);
        holder.cbPaidStatus.setChecked(reminder.isPaid());

        if (reminder.isPaid()) {
            holder.tvReminderTitle.setPaintFlags(holder.tvReminderTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvReminderAmount.setTextColor(ContextCompat.getColor(context, R.color.accent_green));
        } else {
            holder.tvReminderTitle.setPaintFlags(holder.tvReminderTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvReminderAmount.setTextColor(ContextCompat.getColor(context, R.color.warning_orange));
        }

        holder.cbPaidStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onReminderStatusChanged(reminder, isChecked);
            }
        });

        holder.btnDeleteReminder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteReminder(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbPaidStatus;
        ImageView ivReminderIcon;
        TextView tvReminderTitle;
        TextView tvReminderDueDate;
        TextView tvReminderFixedTag;
        TextView tvReminderAmount;
        ImageView btnDeleteReminder;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            cbPaidStatus = itemView.findViewById(R.id.cbPaidStatus);
            ivReminderIcon = itemView.findViewById(R.id.ivReminderIcon);
            tvReminderTitle = itemView.findViewById(R.id.tvReminderTitle);
            tvReminderDueDate = itemView.findViewById(R.id.tvReminderDueDate);
            tvReminderFixedTag = itemView.findViewById(R.id.tvReminderFixedTag);
            tvReminderAmount = itemView.findViewById(R.id.tvReminderAmount);
            btnDeleteReminder = itemView.findViewById(R.id.btnDeleteReminder);
        }
    }
}