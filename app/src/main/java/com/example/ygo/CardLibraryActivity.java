package com.example.ygo;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardLibraryActivity extends AppCompatActivity {
    
    // Inner class for drop information
    public static class DropInfo {
        private String opponentName;
        private String difficulty;
        private int dropRate; // This is the probability out of 2048, not a percentage
        
        public DropInfo(String opponentName, String difficulty, int dropRate) {
            this.opponentName = opponentName;
            this.difficulty = difficulty;
            this.dropRate = dropRate;
        }
        
        public String getOpponentName() { return opponentName; }
        public String getDifficulty() { return difficulty; }
        public int getDropRate() { return dropRate; }
        
        @Override
        public String toString() {
            double percentage = (dropRate / 2048.0) * 100;
            return String.format("%s (%s) - %d/2048 (%.2f%%)", opponentName, difficulty, dropRate, percentage);
        }
    }
    
    private FusionEngine fusionEngine;
    private List<Card> allCards;
    private List<Card> displayedCards;
    private CardLibraryAdapter libraryAdapter;
    private CardDetailFusionAdapter detailFusionAdapter;
    
    // Drop information cache
    private Map<Integer, List<DropInfo>> cardDropsMap;
    
    // UI Components
    private EditText searchInput;
    private Button clearSearchButton;
    private TextView cardsCountText;
    private TextView searchResultsText;
    private RecyclerView cardsRecycler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_library);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Card Library");
        }
        
        // Initialize data
        fusionEngine = FusionEngine.getInstance();
        allCards = new ArrayList<>();
        displayedCards = new ArrayList<>();
        cardDropsMap = new HashMap<>();
        
        initializeUI();
        loadCards();
        loadDropInformation();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    private void initializeUI() {
        searchInput = findViewById(R.id.search_input);
        clearSearchButton = findViewById(R.id.clear_search_button);
        cardsCountText = findViewById(R.id.cards_count_text);
        searchResultsText = findViewById(R.id.search_results_text);
        cardsRecycler = findViewById(R.id.cards_recycler);
        
        // Setup RecyclerView with Grid Layout (3 columns)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        cardsRecycler.setLayoutManager(gridLayoutManager);
        
        libraryAdapter = new CardLibraryAdapter(displayedCards, this::showCardDetail);
        cardsRecycler.setAdapter(libraryAdapter);
        
        // Setup search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString().trim());
            }
        });
        
        clearSearchButton.setOnClickListener(v -> {
            searchInput.setText("");
            performSearch("");
        });
    }
    
    private void loadCards() {
        // Get all cards from FusionEngine
        allCards.clear();
        allCards.addAll(fusionEngine.getAllCards());
        
        // Initially display all cards
        displayedCards.clear();
        displayedCards.addAll(allCards);
        
        updateUI();
    }
    
    private void performSearch(String query) {
        displayedCards.clear();
        
        if (query.isEmpty()) {
            displayedCards.addAll(allCards);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Card card : allCards) {
                if (card.getName().toLowerCase().contains(lowerQuery)) {
                    displayedCards.add(card);
                }
            }
        }
        
        updateUI();
    }
    
    private void updateUI() {
        libraryAdapter.notifyDataSetChanged();
        cardsCountText.setText("Total Cards: " + allCards.size());
        searchResultsText.setText("Showing: " + displayedCards.size());
    }
    
    private void showCardDetail(Card card) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_card_detail, null);
        
        // Get dialog elements
        ImageView cardImageDetail = dialogView.findViewById(R.id.card_image_detail);
        TextView cardNameDetail = dialogView.findViewById(R.id.card_name_detail);
        TextView cardIdDetail = dialogView.findViewById(R.id.card_id_detail);
        TextView cardTypeDetail = dialogView.findViewById(R.id.card_type_detail);
        TextView cardAttackDetail = dialogView.findViewById(R.id.card_attack_detail);
        TextView dropCountText = dialogView.findViewById(R.id.drop_count_text);
        TextView noDropsText = dialogView.findViewById(R.id.no_drops_text);
        RecyclerView dropsRecycler = dialogView.findViewById(R.id.drops_recycler);
        EditText fusionSearchInput = dialogView.findViewById(R.id.fusion_search_input);
        Button clearFusionSearchButton = dialogView.findViewById(R.id.clear_fusion_search_button);
        TextView directFusionsCount = dialogView.findViewById(R.id.direct_fusions_count);
        TextView materialFusionsCount = dialogView.findViewById(R.id.material_fusions_count);
        TextView resultFusionsCount = dialogView.findViewById(R.id.result_fusions_count);
        TextView showingFusionsCount = dialogView.findViewById(R.id.showing_fusions_count);
        TextView noFusionsText = dialogView.findViewById(R.id.no_fusions_text);
        RecyclerView fusionsRecycler = dialogView.findViewById(R.id.fusions_recycler);
        Button closeButton = dialogView.findViewById(R.id.close_button);
        
        // Enable marquee scrolling for fusion count buttons
        directFusionsCount.setSelected(true);
        materialFusionsCount.setSelected(true);
        resultFusionsCount.setSelected(true);
        
        // Set card information
        loadCardImage(cardImageDetail, card.getId());
        cardNameDetail.setText(card.getName());
        cardIdDetail.setText("ID: " + card.getId());
        cardTypeDetail.setText("Type: " + capitalizeType(card.getType()));
        cardAttackDetail.setText("Attack: " + card.getAttack());
        
        // Setup drop information
        List<DropInfo> dropInfos = getDropInformation(card.getId());
        if (dropInfos.isEmpty()) {
            dropCountText.setText("Available from: 0 opponents");
            noDropsText.setVisibility(View.VISIBLE);
            dropsRecycler.setVisibility(View.GONE);
        } else {
            dropCountText.setText("Available from: " + dropInfos.size() + " opponents");
            noDropsText.setVisibility(View.GONE);
            dropsRecycler.setVisibility(View.VISIBLE);
            
            CardDropInfoAdapter dropAdapter = new CardDropInfoAdapter(dropInfos);
            dropsRecycler.setLayoutManager(new LinearLayoutManager(this));
            dropsRecycler.setAdapter(dropAdapter);
        }
        
        // Get all fusions involving this card
        List<CardFusionInfo> allCardFusions = getCardFusions(card);
        List<CardFusionInfo> displayedFusions = new ArrayList<>(allCardFusions);
        
        // Track current filter state
        final CardFusionInfo.FusionType[] currentFilter = {null}; // null means no filter (show all)
        
        // Update fusion counts display
        Runnable updateCounts = () -> {
            // Count original fusion types
            int directCount = 0, materialCount = 0, resultCount = 0;
            for (CardFusionInfo fusion : allCardFusions) {
                switch (fusion.getType()) {
                    case DIRECT_FUSION:
                        directCount++;
                        break;
                    case AS_MATERIAL:
                        materialCount++;
                        break;
                    case AS_RESULT:
                        resultCount++;
                        break;
                }
            }
            
            // Update text and style based on current filter
            directFusionsCount.setText("Direct: " + directCount);
            materialFusionsCount.setText("As Material: " + materialCount);
            resultFusionsCount.setText("As Result: " + resultCount);
            showingFusionsCount.setText("Showing: " + displayedFusions.size());
            
            // Update visual appearance based on current filter
            directFusionsCount.setAlpha(currentFilter[0] == CardFusionInfo.FusionType.DIRECT_FUSION ? 1.0f : 0.7f);
            materialFusionsCount.setAlpha(currentFilter[0] == CardFusionInfo.FusionType.AS_MATERIAL ? 1.0f : 0.7f);
            resultFusionsCount.setAlpha(currentFilter[0] == CardFusionInfo.FusionType.AS_RESULT ? 1.0f : 0.7f);
            
            // Add underline for active filter
            if (currentFilter[0] == CardFusionInfo.FusionType.DIRECT_FUSION) {
                directFusionsCount.setPaintFlags(directFusionsCount.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            } else {
                directFusionsCount.setPaintFlags(directFusionsCount.getPaintFlags() & ~android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            }
            
            if (currentFilter[0] == CardFusionInfo.FusionType.AS_MATERIAL) {
                materialFusionsCount.setPaintFlags(materialFusionsCount.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            } else {
                materialFusionsCount.setPaintFlags(materialFusionsCount.getPaintFlags() & ~android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            }
            
            if (currentFilter[0] == CardFusionInfo.FusionType.AS_RESULT) {
                resultFusionsCount.setPaintFlags(resultFusionsCount.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            } else {
                resultFusionsCount.setPaintFlags(resultFusionsCount.getPaintFlags() & ~android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            }
            
            if (displayedFusions.isEmpty()) {
                if (allCardFusions.isEmpty()) {
                    noFusionsText.setText("This card has no fusion recipes.");
                } else {
                    noFusionsText.setText("No fusions match your current filter or search.");
                }
                noFusionsText.setVisibility(View.VISIBLE);
                fusionsRecycler.setVisibility(View.GONE);
            } else {
                noFusionsText.setVisibility(View.GONE);
                fusionsRecycler.setVisibility(View.VISIBLE);
            }
        };
        
        // Function to apply current filter
        Runnable applyFilter = () -> {
            String searchQuery = fusionSearchInput.getText().toString().trim().toLowerCase();
            displayedFusions.clear();
            
            for (CardFusionInfo fusion : allCardFusions) {
                // First check if it matches the type filter
                boolean matchesTypeFilter = (currentFilter[0] == null) || (fusion.getType() == currentFilter[0]);
                
                if (!matchesTypeFilter) {
                    continue;
                }
                
                // Then check if it matches the search query
                boolean matchesSearch = true;
                if (!searchQuery.isEmpty()) {
                    matchesSearch = false;
                    
                    if (fusion.getCard1() != null && fusion.getCard1().getName().toLowerCase().contains(searchQuery)) {
                        matchesSearch = true;
                    }
                    if (fusion.getCard2() != null && fusion.getCard2().getName().toLowerCase().contains(searchQuery)) {
                        matchesSearch = true;
                    }
                    if (fusion.getResult() != null && fusion.getResult().getName().toLowerCase().contains(searchQuery)) {
                        matchesSearch = true;
                    }
                    if (fusion.getMaterial3() != null && fusion.getMaterial3().getName().toLowerCase().contains(searchQuery)) {
                        matchesSearch = true;
                    }
                }
                
                if (matchesSearch) {
                    displayedFusions.add(fusion);
                }
            }
            
            detailFusionAdapter.notifyDataSetChanged();
            updateCounts.run();
        };
        
        // Add click listeners for filter buttons
        directFusionsCount.setOnClickListener(v -> {
            if (currentFilter[0] == CardFusionInfo.FusionType.DIRECT_FUSION) {
                // If already filtered by direct, clear filter
                currentFilter[0] = null;
            } else {
                // Apply direct fusion filter
                currentFilter[0] = CardFusionInfo.FusionType.DIRECT_FUSION;
            }
            applyFilter.run();
        });
        
        materialFusionsCount.setOnClickListener(v -> {
            if (currentFilter[0] == CardFusionInfo.FusionType.AS_MATERIAL) {
                // If already filtered by material, clear filter
                currentFilter[0] = null;
            } else {
                // Apply material fusion filter
                currentFilter[0] = CardFusionInfo.FusionType.AS_MATERIAL;
            }
            applyFilter.run();
        });
        
        resultFusionsCount.setOnClickListener(v -> {
            if (currentFilter[0] == CardFusionInfo.FusionType.AS_RESULT) {
                // If already filtered by result, clear filter
                currentFilter[0] = null;
            } else {
                // Apply result fusion filter
                currentFilter[0] = CardFusionInfo.FusionType.AS_RESULT;
            }
            applyFilter.run();
        });
        
        // Setup fusions RecyclerView
        detailFusionAdapter = new CardDetailFusionAdapter(displayedFusions, this::showNestedCardDetail, fusionEngine);
        fusionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        fusionsRecycler.setAdapter(detailFusionAdapter);
        
        // Setup fusion search functionality
        fusionSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                applyFilter.run();
            }
        });
        
        clearFusionSearchButton.setOnClickListener(v -> {
            fusionSearchInput.setText("");
            currentFilter[0] = null; // Also clear the type filter
            applyFilter.run();
        });
        
        // Initial count update
        updateCounts.run();
        
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private List<CardFusionInfo> getCardFusions(Card card) {
        List<CardFusionInfo> fusions = new ArrayList<>();
        
        // Get direct fusions where this card can fuse with others
        List<Card> directFusions = fusionEngine.getDirectFusionsForCard(card.getId());
        for (Card partner : directFusions) {
            Card result = fusionEngine.getFusionResult(card.getId(), partner.getId());
            if (result != null) {
                fusions.add(new CardFusionInfo(
                    CardFusionInfo.FusionType.DIRECT_FUSION,
                    card.getName() + " + " + partner.getName() + " = " + result.getName(),
                    card, partner, result
                ));
            }
        }
        
        // Get fusions where this card is used as material
        List<CardFusionInfo> materialFusions = fusionEngine.getFusionsUsingCard(card.getId());
        fusions.addAll(materialFusions);
        
        // Get fusions where this card is the result
        List<CardFusionInfo> resultFusions = fusionEngine.getFusionsResultingIn(card.getId());
        fusions.addAll(resultFusions);
        
        return fusions;
    }
    
    private void loadCardImage(ImageView imageView, int cardId) {
        try {
            String cardImageName = "c" + String.format("%03d", cardId - 1);
            int resourceId = getResources().getIdentifier(cardImageName, "drawable", getPackageName());
            
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
    
    private void loadDropInformation() {
        try {
            InputStream inputStream = getAssets().open("carddata/droplist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            String line;
            String currentDifficulty = "";
            String currentOpponent = "";
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }
                
                // Check if this is a difficulty section
                if (line.contains("SECTION") || line.contains("POW") || line.contains("TECH")) {
                    currentDifficulty = line;
                    continue;
                }
                
                // Check if this is an opponent (starts with "Spoiler:")
                if (line.startsWith("Spoiler:")) {
                    currentOpponent = line.substring(8).trim(); // Remove "Spoiler: " prefix
                    continue;
                }
                
                // Check if this is a card drop entry (contains numbers)
                if (!line.isEmpty() && Character.isDigit(line.charAt(0))) {
                    try {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 3) {
                            int cardId = Integer.parseInt(parts[0]);
                            int dropRate = Integer.parseInt(parts[parts.length - 1]); // Last number is drop rate
                            
                            // Create drop info
                            DropInfo dropInfo = new DropInfo(currentOpponent, currentDifficulty, dropRate);
                            
                            // Add to map
                            if (!cardDropsMap.containsKey(cardId)) {
                                cardDropsMap.put(cardId, new ArrayList<>());
                            }
                            cardDropsMap.get(cardId).add(dropInfo);
                        }
                    } catch (NumberFormatException e) {
                        // Skip malformed lines
                    }
                }
            }
            
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading drop information", Toast.LENGTH_SHORT).show();
        }
    }
    
    private List<DropInfo> getDropInformation(int cardId) {
        List<DropInfo> dropInfos = cardDropsMap.get(cardId);
        return dropInfos != null ? dropInfos : new ArrayList<>();
    }
    
    private void showNestedCardDetail(Card card) {
        // Open a new card detail dialog for the clicked result card
        // This creates a recursive exploration of fusion chains
        showCardDetail(card);
    }
} 