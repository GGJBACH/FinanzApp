package com.example.quiz_andres_rosales.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quiz_andres_rosales.R;
import com.example.quiz_andres_rosales.db.entity.ExpenseEntity;
import com.example.quiz_andres_rosales.utils.CurrencyUtils;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseEntity expense);
        void onDeleteExpense(ExpenseEntity expense);
    }

    private final List<ExpenseEntity> expenseList;
    private final OnExpenseClickListener listener;

    public ExpenseAdapter(List<ExpenseEntity> expenseList, OnExpenseClickListener listener) {
        this.expenseList = expenseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseEntity expense = expenseList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvExpenseTitle.setText(expense.getTitle());
        holder.tvExpenseCategoryTag.setText(expense.getCategory() != null ? expense.getCategory() : "Otros");
        holder.tvExpenseMethodTag.setText("Método: " + expense.getPaymentMethod());
        holder.tvExpenseAmount.setText("- " + CurrencyUtils.formatCurrency(expense.getAmount()));

        if ("Efectivo".equalsIgnoreCase(expense.getPaymentMethod())) {
            holder.ivPaymentMethodIcon.setImageResource(R.drawable.ic_cash);
            holder.ivPaymentMethodIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.chip_bg_cash));
            holder.ivPaymentMethodIcon.setImageTintList(ContextCompat.getColorStateList(context, R.color.method_cash));
        } else if ("Tarjeta".equalsIgnoreCase(expense.getPaymentMethod())) {
            holder.ivPaymentMethodIcon.setImageResource(R.drawable.ic_card);
            holder.ivPaymentMethodIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.chip_bg_card));
            holder.ivPaymentMethodIcon.setImageTintList(ContextCompat.getColorStateList(context, R.color.method_card));
        } else {
            holder.ivPaymentMethodIcon.setImageResource(R.drawable.ic_transfer);
            holder.ivPaymentMethodIcon.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.chip_bg_transfer));
            holder.ivPaymentMethodIcon.setImageTintList(ContextCompat.getColorStateList(context, R.color.method_transfer));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(expense);
            }
        });

        holder.btnDeleteExpense.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteExpense(expense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPaymentMethodIcon;
        TextView tvExpenseTitle;
        TextView tvExpenseCategoryTag;
        TextView tvExpenseMethodTag;
        TextView tvExpenseAmount;
        ImageView btnDeleteExpense;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPaymentMethodIcon = itemView.findViewById(R.id.ivPaymentMethodIcon);
            tvExpenseTitle = itemView.findViewById(R.id.tvExpenseTitle);
            tvExpenseCategoryTag = itemView.findViewById(R.id.tvExpenseCategoryTag);
            tvExpenseMethodTag = itemView.findViewById(R.id.tvExpenseMethodTag);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            btnDeleteExpense = itemView.findViewById(R.id.btnDeleteExpense);
        }
    }
}