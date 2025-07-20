package com.example.ygo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FusionResultAdapter extends RecyclerView.Adapter<FusionResultAdapter.FusionViewHolder> {
    private List<FusionEngine.FusionResult> fusionResults;
    private OnFusionClickListener listener;

    public interface OnFusionClickListener {
        void onFusionClick(FusionEngine.FusionResult fusionResult);
    }

    public FusionResultAdapter(List<FusionEngine.FusionResult> fusionResults, OnFusionClickListener listener) {
        this.fusionResults = fusionResults;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FusionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fusion_result, parent, false);
        return new FusionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FusionViewHolder holder, int position) {
        FusionEngine.FusionResult fusionResult = fusionResults.get(position);
        holder.bind(fusionResult, listener);
    }

    @Override
    public int getItemCount() {
        return fusionResults.size();
    }

    static class FusionViewHolder extends RecyclerView.ViewHolder {
        private TextView material1Text;
        private TextView material2Text;
        private TextView resultText;
        private TextView fusionFormulaText;
        private Button performFusionButton;

        public FusionViewHolder(@NonNull View itemView) {
            super(itemView);
            material1Text = itemView.findViewById(R.id.material1_text);
            material2Text = itemView.findViewById(R.id.material2_text);
            resultText = itemView.findViewById(R.id.result_text);
            fusionFormulaText = itemView.findViewById(R.id.fusion_formula_text);
            performFusionButton = itemView.findViewById(R.id.perform_fusion_button);
        }

        public void bind(FusionEngine.FusionResult fusionResult, OnFusionClickListener listener) {
            Card material1 = fusionResult.getMaterial1();
            Card material2 = fusionResult.getMaterial2();
            Card result = fusionResult.getResult();

            if (fusionResult.getType() == FusionEngine.FusionType.DIRECT) {
                // Direct fusion display
                material1Text.setText(material1.getName() + " (" + material1.getAttack() + " ATK)");
                material2Text.setText(material2.getName() + " (" + material2.getAttack() + " ATK)");
                resultText.setText(result.getName() + " (" + result.getAttack() + " ATK)");

                // Create fusion formula display
                String formula = material1.getName() + " + " + material2.getName() + " = " + result.getName();
                fusionFormulaText.setText(formula);
                
                // Color coding based on attack gain
                int totalMaterialAttack = material1.getAttack() + material2.getAttack();
                int resultAttack = result.getAttack();
                
                if (resultAttack > totalMaterialAttack) {
                    // Good fusion - green tint
                    itemView.setBackgroundColor(0xFF4CAF50); // Light green
                } else if (resultAttack > Math.max(material1.getAttack(), material2.getAttack())) {
                    // Decent fusion - yellow tint
                    itemView.setBackgroundColor(0xFFFFEB3B); // Light yellow
                } else {
                    // Poor fusion - red tint
                    itemView.setBackgroundColor(0xFFF44336); // Light red
                }
            } else if (fusionResult.getType() == FusionEngine.FusionType.CHAINED) {
                // Chained fusion display
                FusionEngine.FusionResult prerequisite = fusionResult.getPrerequisiteFusion();
                Card additionalCard = fusionResult.getAdditionalCard();

                material1Text.setText("(" + prerequisite.getMaterial1().getName() + " + " + prerequisite.getMaterial2().getName() + ")");
                material2Text.setText(additionalCard.getName() + " (" + additionalCard.getAttack() + " ATK)");
                resultText.setText(result.getName() + " (" + result.getAttack() + " ATK)");

                // Create chained fusion formula display
                String formula = "(" + prerequisite.getMaterial1().getName() + " + " + prerequisite.getMaterial2().getName() + " → " +
                        prerequisite.getResult().getName() + ") + " + additionalCard.getName() + " = " + result.getName();
                fusionFormulaText.setText(formula);

                // Color coding for chained fusions - different scheme
                int totalMaterialAttack = prerequisite.getMaterial1().getAttack() + prerequisite.getMaterial2().getAttack() + additionalCard.getAttack();
                int resultAttack = result.getAttack();
                
                if (resultAttack > totalMaterialAttack) {
                    // Good fusion - green tint
                    itemView.setBackgroundColor(0xFF4CAF50); // Light green
                } else if (resultAttack > Math.max(Math.max(prerequisite.getMaterial1().getAttack(), prerequisite.getMaterial2().getAttack()), additionalCard.getAttack())) {
                    // Decent fusion - yellow tint
                    itemView.setBackgroundColor(0xFFFFEB3B); // Light yellow
                } else {
                    // Poor fusion - red tint
                    itemView.setBackgroundColor(0xFFF44336); // Light red
                }
            } else if (fusionResult.getType() == FusionEngine.FusionType.FIELD_DIRECT) {
                // Field direct fusion display
                material1Text.setText(material1.getName() + " (Field) (" + material1.getAttack() + " ATK)");
                material2Text.setText(material2.getName() + " (Hand) (" + material2.getAttack() + " ATK)");
                resultText.setText(result.getName() + " (" + result.getAttack() + " ATK)");

                // Create field fusion formula display
                String formula = material1.getName() + " (Field) + " + material2.getName() + " (Hand) = " + result.getName();
                fusionFormulaText.setText(formula);
                
                // Color coding based on attack gain
                int totalMaterialAttack = material1.getAttack() + material2.getAttack();
                int resultAttack = result.getAttack();
                
                if (resultAttack > totalMaterialAttack) {
                    // Good field fusion - darker green tint
                    itemView.setBackgroundColor(0xFF388E3C); // Darker green
                } else if (resultAttack > Math.max(material1.getAttack(), material2.getAttack())) {
                    // Decent field fusion - orange tint
                    itemView.setBackgroundColor(0xFFFF9800); // Orange
                } else {
                    // Poor field fusion - darker red tint
                    itemView.setBackgroundColor(0xFFD32F2F); // Darker red
                }
            } else if (fusionResult.getType() == FusionEngine.FusionType.FIELD_CHAINED) {
                // Field chained fusion display
                FusionEngine.FusionResult prerequisite = fusionResult.getPrerequisiteFusion();
                Card additionalCard = fusionResult.getAdditionalCard();

                material1Text.setText("(" + prerequisite.getMaterial1().getName() + " (Field) + " + prerequisite.getMaterial2().getName() + " (Hand))");
                material2Text.setText(additionalCard.getName() + " (Hand) (" + additionalCard.getAttack() + " ATK)");
                resultText.setText(result.getName() + " (" + result.getAttack() + " ATK)");

                // Create field chained fusion formula display
                String formula = "(" + prerequisite.getMaterial1().getName() + " (Field) + " + prerequisite.getMaterial2().getName() + " (Hand) → " +
                        prerequisite.getResult().getName() + ") + " + additionalCard.getName() + " (Hand) = " + result.getName();
                fusionFormulaText.setText(formula);

                // Color coding for field chained fusions - different scheme
                int totalMaterialAttack = prerequisite.getMaterial1().getAttack() + prerequisite.getMaterial2().getAttack() + additionalCard.getAttack();
                int resultAttack = result.getAttack();
                
                if (resultAttack > totalMaterialAttack) {
                    // Good field chained fusion - darker green tint
                    itemView.setBackgroundColor(0xFF388E3C); // Darker green
                } else if (resultAttack > Math.max(Math.max(prerequisite.getMaterial1().getAttack(), prerequisite.getMaterial2().getAttack()), additionalCard.getAttack())) {
                    // Decent field chained fusion - orange tint
                    itemView.setBackgroundColor(0xFFFF9800); // Orange
                } else {
                    // Poor field chained fusion - darker red tint
                    itemView.setBackgroundColor(0xFFD32F2F); // Darker red
                }
            }

            performFusionButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFusionClick(fusionResult);
                }
            });
        }
    }
} 