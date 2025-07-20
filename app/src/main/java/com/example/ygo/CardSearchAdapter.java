package com.example.ygo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardSearchAdapter extends RecyclerView.Adapter<CardSearchAdapter.CardViewHolder> {
    private List<Card> cards;
    private OnCardSelectListener listener;

    public interface OnCardSelectListener {
        void onCardSelected(Card card);
    }

    public CardSearchAdapter(List<Card> cards, OnCardSelectListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_search, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.bind(card, listener);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private TextView cardNameText;
        private TextView cardTypeText;
        private TextView cardAttackText;
        private TextView cardIdText;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNameText = itemView.findViewById(R.id.card_name_text);
            cardTypeText = itemView.findViewById(R.id.card_type_text);
            cardAttackText = itemView.findViewById(R.id.card_attack_text);
            cardIdText = itemView.findViewById(R.id.card_id_text);
        }

        public void bind(Card card, OnCardSelectListener listener) {
            cardNameText.setText(card.getName());
            cardTypeText.setText(card.getType());
            cardAttackText.setText(card.getAttack() + " ATK");
            cardIdText.setText("ID: " + card.getId());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardSelected(card);
                }
            });

            // Color coding based on attack power
            int attack = card.getAttack();
            if (attack >= 3000) {
                itemView.setBackgroundColor(0xFFFF5722); // Red for high attack
            } else if (attack >= 2000) {
                itemView.setBackgroundColor(0xFFFF9800); // Orange for medium-high attack
            } else if (attack >= 1500) {
                itemView.setBackgroundColor(0xFFFFEB3B); // Yellow for medium attack
            } else if (attack >= 1000) {
                itemView.setBackgroundColor(0xFF8BC34A); // Light green for low-medium attack
            } else {
                itemView.setBackgroundColor(0xFF4CAF50); // Green for low attack
            }
        }
    }
} 