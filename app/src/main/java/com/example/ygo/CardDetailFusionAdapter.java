package com.example.ygo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardDetailFusionAdapter extends RecyclerView.Adapter<CardDetailFusionAdapter.FusionInfoViewHolder> {
    private List<CardFusionInfo> fusionInfos;
    private static OnResultCardClickListener resultCardClickListener;
    private static FusionEngine fusionEngine;
    
    public interface OnResultCardClickListener {
        void onResultCardClick(Card card);
    }
    
    public CardDetailFusionAdapter(List<CardFusionInfo> fusionInfos, OnResultCardClickListener listener, FusionEngine fusionEngine) {
        this.fusionInfos = fusionInfos;
        this.resultCardClickListener = listener;
        this.fusionEngine = fusionEngine;
    }
    
    public void updateData(List<CardFusionInfo> newFusionInfos) {
        this.fusionInfos.clear();
        this.fusionInfos.addAll(newFusionInfos);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public FusionInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fusion_info, parent, false);
        return new FusionInfoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FusionInfoViewHolder holder, int position) {
        CardFusionInfo fusionInfo = fusionInfos.get(position);
        holder.bind(fusionInfo);
    }
    
    @Override
    public int getItemCount() {
        return fusionInfos.size();
    }
    
    static class FusionInfoViewHolder extends RecyclerView.ViewHolder {
        private TextView fusionTypeText;
        private TextView fusionDescriptionText;
        private TextView fusionDetailsText;
        
        public FusionInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            fusionTypeText = itemView.findViewById(R.id.fusion_type_text);
            fusionDescriptionText = itemView.findViewById(R.id.fusion_description_text);
            fusionDetailsText = itemView.findViewById(R.id.fusion_details_text);
        }
        
        public void bind(CardFusionInfo fusionInfo) {
            // Set fusion type with appropriate color
            switch (fusionInfo.getType()) {
                case DIRECT_FUSION:
                    fusionTypeText.setText("Direct Fusion");
                    fusionTypeText.setTextColor(0xFF4CAF50); // Green
                    break;
                case AS_MATERIAL:
                    fusionTypeText.setText("Used as Material");
                    fusionTypeText.setTextColor(0xFF2196F3); // Blue
                    break;
                case AS_RESULT:
                    fusionTypeText.setText("Fusion Result");
                    fusionTypeText.setTextColor(0xFFFF9800); // Orange
                    break;
            }
            
            // Build enhanced description showing which materials can be fused
            String description = buildEnhancedDescription(fusionInfo);
            fusionDescriptionText.setText(description);
            
            // Set additional details based on fusion type
            String details = "";
            if (fusionInfo.isChained()) {
                details = "Chained fusion involving 3 cards";
            } else {
                details = "Direct fusion involving 2 cards";
            }
            
            if (fusionInfo.getResult() != null) {
                details += " • Result ATK: " + fusionInfo.getResult().getAttack();
            }
            
            fusionDetailsText.setText(details);
            
            // Set background color based on fusion type
            switch (fusionInfo.getType()) {
                case DIRECT_FUSION:
                    itemView.setBackgroundColor(0xFFE8F5E8); // Light green
                    break;
                case AS_MATERIAL:
                    itemView.setBackgroundColor(0xFFE3F2FD); // Light blue
                    break;
                case AS_RESULT:
                    itemView.setBackgroundColor(0xFFFFF3E0); // Light orange
                    break;
            }
            
            // Make the description clickable to explore material fusion recipes
            if (hasFusableMaterials(fusionInfo)) {
                fusionDescriptionText.setTextColor(0xFF2196F3); // Blue for clickable
                fusionDescriptionText.setOnClickListener(v -> {
                    if (resultCardClickListener != null) {
                        showMaterialFusionOptions(fusionInfo);
                    }
                });
                // Add underline effect for clickable items
                fusionDescriptionText.setPaintFlags(fusionDescriptionText.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            } else {
                fusionDescriptionText.setTextColor(0xFF000000); // Black for non-clickable
                fusionDescriptionText.setOnClickListener(null);
                fusionDescriptionText.setPaintFlags(fusionDescriptionText.getPaintFlags() & ~android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            }
        }
        
        private String buildEnhancedDescription(CardFusionInfo fusionInfo) {
            StringBuilder description = new StringBuilder();
            
            // Handle card1
            Card card1 = fusionInfo.getCard1();
            if (card1 != null) {
                description.append(getCardNameWithFusionIndicator(card1));
            }
            
            // Handle card2
            Card card2 = fusionInfo.getCard2();
            if (card2 != null) {
                description.append(" + ").append(getCardNameWithFusionIndicator(card2));
            }
            
            // Handle material3 for chained fusions
            Card material3 = fusionInfo.getMaterial3();
            if (material3 != null) {
                description.append(" + ").append(getCardNameWithFusionIndicator(material3));
            }
            
            // Add result
            Card result = fusionInfo.getResult();
            if (result != null) {
                description.append(" = ").append(result.getName());
            }
            
            return description.toString();
        }
        
        private String getCardNameWithFusionIndicator(Card card) {
            if (card == null || fusionEngine == null) {
                return card != null ? card.getName() : "Unknown";
            }
            
            // Check if this card can be made by fusion
            List<CardFusionInfo> cardRecipes = fusionEngine.getFusionsResultingIn(card.getId());
            if (!cardRecipes.isEmpty()) {
                return card.getName() + " (can be fused)";
            }
            
            return card.getName();
        }
        
        private boolean hasFusableMaterials(CardFusionInfo fusionInfo) {
            if (fusionEngine == null) return false;
            
            // Check if any of the material cards can be made by fusion
            Card card1 = fusionInfo.getCard1();
            Card card2 = fusionInfo.getCard2();
            Card material3 = fusionInfo.getMaterial3();
            
            if (card1 != null && !fusionEngine.getFusionsResultingIn(card1.getId()).isEmpty()) {
                return true;
            }
            if (card2 != null && !fusionEngine.getFusionsResultingIn(card2.getId()).isEmpty()) {
                return true;
            }
            if (material3 != null && !fusionEngine.getFusionsResultingIn(material3.getId()).isEmpty()) {
                return true;
            }
            
            return false;
        }
        
        private void showMaterialFusionOptions(CardFusionInfo fusionInfo) {
            // Create a dialog to show which material cards can be explored
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Explore Material Fusion Recipes");
            
            java.util.List<String> options = new java.util.ArrayList<>();
            java.util.List<Card> clickableCards = new java.util.ArrayList<>();
            
            // Check each material card
            Card card1 = fusionInfo.getCard1();
            Card card2 = fusionInfo.getCard2();
            Card material3 = fusionInfo.getMaterial3();
            
            if (card1 != null && !fusionEngine.getFusionsResultingIn(card1.getId()).isEmpty()) {
                int recipeCount = fusionEngine.getFusionsResultingIn(card1.getId()).size();
                options.add(card1.getName() + " (" + recipeCount + " recipe" + (recipeCount > 1 ? "s" : "") + ")");
                clickableCards.add(card1);
            }
            
            if (card2 != null && !fusionEngine.getFusionsResultingIn(card2.getId()).isEmpty()) {
                int recipeCount = fusionEngine.getFusionsResultingIn(card2.getId()).size();
                options.add(card2.getName() + " (" + recipeCount + " recipe" + (recipeCount > 1 ? "s" : "") + ")");
                clickableCards.add(card2);
            }
            
            if (material3 != null && !fusionEngine.getFusionsResultingIn(material3.getId()).isEmpty()) {
                int recipeCount = fusionEngine.getFusionsResultingIn(material3.getId()).size();
                options.add(material3.getName() + " (" + recipeCount + " recipe" + (recipeCount > 1 ? "s" : "") + ")");
                clickableCards.add(material3);
            }
            
            if (!options.isEmpty()) {
                builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
                    Card selectedCard = clickableCards.get(which);
                    if (resultCardClickListener != null) {
                        resultCardClickListener.onResultCardClick(selectedCard);
                    }
                });
                
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        }
    }
} 