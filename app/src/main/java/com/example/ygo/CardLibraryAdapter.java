package com.example.ygo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardLibraryAdapter extends RecyclerView.Adapter<CardLibraryAdapter.CardViewHolder> {
    private List<Card> cards;
    private OnCardClickListener listener;
    
    public interface OnCardClickListener {
        void onCardClick(Card card);
    }
    
    public CardLibraryAdapter(List<Card> cards, OnCardClickListener listener) {
        this.cards = cards;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_card, parent, false);
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
        private ImageView cardImage;
        private TextView cardNameText;
        private TextView cardTypeText;
        private TextView cardAttackText;
        private TextView cardIdText;
        
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImage = itemView.findViewById(R.id.card_image);
            cardNameText = itemView.findViewById(R.id.card_name_text);
            cardTypeText = itemView.findViewById(R.id.card_type_text);
            cardAttackText = itemView.findViewById(R.id.card_attack_text);
            cardIdText = itemView.findViewById(R.id.card_id_text);
        }
        
        public void bind(Card card, OnCardClickListener listener) {
            cardNameText.setText(card.getName());
            cardTypeText.setText(capitalizeType(card.getType()));
            cardAttackText.setText(card.getAttack() + " ATK");
            cardIdText.setText("ID: " + card.getId());
            
            // Load card image
            loadCardImage(cardImage, card.getId());
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(card);
                }
            });
        }
        
        private void loadCardImage(ImageView imageView, int cardId) {
            try {
                String cardImageName = "c" + String.format("%03d", cardId - 1);
                int resourceId = imageView.getContext().getResources().getIdentifier(cardImageName, "drawable", imageView.getContext().getPackageName());
                
                if (resourceId != 0) {
                    imageView.setImageResource(resourceId);
                } else {
                    imageView.setImageResource(R.drawable.card_back);
                }
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.card_back);
            }
        }
        
        private String capitalizeType(String type) {
            if (type == null || type.isEmpty()) return "Unknown";
            return type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
        }
    }
} 