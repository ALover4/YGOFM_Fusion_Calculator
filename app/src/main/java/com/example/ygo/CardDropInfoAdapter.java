package com.example.ygo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardDropInfoAdapter extends RecyclerView.Adapter<CardDropInfoAdapter.DropInfoViewHolder> {
    private List<CardLibraryActivity.DropInfo> dropInfos;
    
    public CardDropInfoAdapter(List<CardLibraryActivity.DropInfo> dropInfos) {
        this.dropInfos = dropInfos;
    }
    
    @NonNull
    @Override
    public DropInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drop_info, parent, false);
        return new DropInfoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DropInfoViewHolder holder, int position) {
        CardLibraryActivity.DropInfo dropInfo = dropInfos.get(position);
        holder.bind(dropInfo);
    }
    
    @Override
    public int getItemCount() {
        return dropInfos.size();
    }
    
    static class DropInfoViewHolder extends RecyclerView.ViewHolder {
        private TextView opponentNameText;
        private TextView difficultyText;
        private TextView dropRateText;
        
        public DropInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            opponentNameText = itemView.findViewById(R.id.opponent_name_text);
            difficultyText = itemView.findViewById(R.id.difficulty_text);
            dropRateText = itemView.findViewById(R.id.drop_rate_text);
        }
        
        public void bind(CardLibraryActivity.DropInfo dropInfo) {
            opponentNameText.setText(dropInfo.getOpponentName());
            difficultyText.setText(dropInfo.getDifficulty());
            
            // Calculate percentage from the probability out of 2048
            double percentage = (dropInfo.getDropRate() / 2048.0) * 100;
            String dropRateDisplay = String.format("%d/2048 (%.2f%%)", dropInfo.getDropRate(), percentage);
            dropRateText.setText(dropRateDisplay);
        }
    }
} 