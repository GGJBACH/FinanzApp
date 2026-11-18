package com.example.quiz_andres_rosales.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    public static String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }

    public static TextWatcher createCurrencyTextWatcher(EditText editText) {
        return new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^\\d]", "");

                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
                            symbols.setGroupingSeparator('.');
                            DecimalFormat formatter = new DecimalFormat("#,###", symbols);

                            current = formatter.format(parsed);
                            editText.setText(current);
                            editText.setSelection(current.length());
                        } catch (NumberFormatException e) {
                            current = "";
                        }
                    } else {
                        current = "";
                        editText.setText("");
                    }

                    editText.addTextChangedListener(this);
                }
            }
        };
    }

    public static double parseCurrencyValue(String text) {
        if (text == null) return 0;
        String cleanString = text.replaceAll("[^\\d]", "");
        if (cleanString.isEmpty()) return 0;
        try {
            return Double.parseDouble(cleanString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}