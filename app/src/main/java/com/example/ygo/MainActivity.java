package com.example.ygo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class MainActivity extends AppCompatActivity {
    private FusionEngine fusionEngine;
    private List<Card> hand;
    private List<Card> field;
    private List<FusionEngine.FusionResult> currentFusions;
    
    // UI Components
    private LinearLayout handLayout;
    private LinearLayout fieldLayout;
    private RecyclerView fusionResultsRecycler;
    private FusionResultAdapter fusionAdapter;
    private Button addCardButton;
    private TextView handStatusText;
    private TextView fieldStatusText;
    
    // Filter/Sort buttons
    private Button sortByAttackButton;
    private Button sortByCardCountButton;
    private Button clearFiltersButton;

    // Sort types
    private enum SortType {
        NONE,       // Default order
        ATTACK,     // Sort by highest attack first
        CARD_COUNT  // Sort by fewest cards first
    }
    
    private SortType currentSortType = SortType.NONE;
    
    // Hand compacting setting
    private boolean autoCompactHand = true; // Default to enabled
    
    // Auto close dialog setting
    private boolean autoCloseDialog = false; // Default to disabled
    
    // Card memory system
    private SharedPreferences cardMemoryPrefs;
    private static final String CARD_MEMORY_PREFS = "card_memory";
    private static final String KEY_CARD_USAGE_COUNT = "usage_count_";
    private static final String KEY_CARD_LAST_USED = "last_used_";
    
    private static final int MAX_HAND_SIZE = 15;
    private static final int DEFAULT_HAND_SIZE = 6;
    private static final int FIELD_SIZE = 5;
    private int currentHandSize = DEFAULT_HAND_SIZE;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Initialize card memory system
        cardMemoryPrefs = getSharedPreferences(CARD_MEMORY_PREFS, Context.MODE_PRIVATE);
        
        // Initialize data
        fusionEngine = FusionEngine.getInstance();
        fusionEngine.loadData(this);
        
        hand = new ArrayList<>();
        field = new ArrayList<>();
        currentFusions = new ArrayList<>();
        
        // Initialize hand with default slots (6)
        for (int i = 0; i < MAX_HAND_SIZE; i++) {
            hand.add(new Card());
        }
        
        // Initialize field with 5 slots
        for (int i = 0; i < FIELD_SIZE; i++) {
            field.add(new Card());
        }
        
        initializeUI();
        setupFusionRecycler();
        updateUI();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        updateMenuItems(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_library) {
            Intent intent = new Intent(this, CardLibraryActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_reset) {
            showResetConfirmationDialog();
            return true;
        } else if (item.getItemId() == R.id.action_toggle_reposition) {
            autoCompactHand = !autoCompactHand;
            updateMenuItems(null);
            String status = autoCompactHand ? "enabled" : "disabled";
            Toast.makeText(this, "Auto card repositioning " + status, Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_toggle_auto_close) {
            autoCloseDialog = !autoCloseDialog;
            updateMenuItems(null);
            String status = autoCloseDialog ? "enabled" : "disabled";
            Toast.makeText(this, "Auto close dialog " + status, Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_information) {
            showInformationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showResetConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset All Cards");
        builder.setMessage("Are you sure you want to clear all cards from hand and field?");
        builder.setPositiveButton("Reset", (dialog, which) -> resetAllCards());
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void resetAllCards() {
        // Clear all hand slots
        for (int i = 0; i < hand.size(); i++) {
            hand.get(i).clear();
        }
        
        // Clear all field slots
        for (int i = 0; i < field.size(); i++) {
            field.get(i).clear();
        }
        
        // Reset hand size to default
        currentHandSize = DEFAULT_HAND_SIZE;
        
        // Reset sorting
        currentSortType = SortType.NONE;
        updateButtonStates();
        
        // Update UI
        updateUI();
        
        Toast.makeText(this, "All cards cleared!", Toast.LENGTH_SHORT).show();
    }
    
    private void initializeUI() {
        handLayout = findViewById(R.id.hand_layout);
        fieldLayout = findViewById(R.id.field_layout);
        fusionResultsRecycler = findViewById(R.id.fusion_results_recycler);
        addCardButton = findViewById(R.id.add_card_button);
        handStatusText = findViewById(R.id.hand_status_text);
        fieldStatusText = findViewById(R.id.field_status_text);
        
        // Initialize filter buttons
        sortByAttackButton = findViewById(R.id.sort_by_attack_button);
        sortByCardCountButton = findViewById(R.id.sort_by_card_count_button);
        clearFiltersButton = findViewById(R.id.clear_filters_button);
        
        addCardButton.setOnClickListener(v -> showAddCardDialog());
        
        // Set up filter button listeners
        sortByAttackButton.setOnClickListener(v -> {
            currentSortType = SortType.ATTACK;
            updateButtonStates();
            applySorting();
            Toast.makeText(this, "Sorted by Attack (Highest first)", Toast.LENGTH_SHORT).show();
        });
        
        sortByCardCountButton.setOnClickListener(v -> {
            currentSortType = SortType.CARD_COUNT;
            updateButtonStates();
            applySorting();
            Toast.makeText(this, "Sorted by Card Count (Fewest first)", Toast.LENGTH_SHORT).show();
        });
        
        clearFiltersButton.setOnClickListener(v -> {
            currentSortType = SortType.NONE;
            updateButtonStates();
            applySorting();
            Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show();
        });
        
        updateButtonStates();
        createHandSlots();
        createFieldSlots();
    }
    
    private void createHandSlots() {
        handLayout.removeAllViews();
        
        // Create visible hand slots (current hand size)
        for (int i = 0; i < currentHandSize; i++) {
            final int position = i;
            View cardView = createCardView(hand.get(i), position, true);
            handLayout.addView(cardView);
        }
        
        // Add "Add Slot" button if hand can be expanded
        if (currentHandSize < MAX_HAND_SIZE) {
            View addSlotView = createAddSlotView();
            handLayout.addView(addSlotView);
        }
    }
    
    private void createFieldSlots() {
        fieldLayout.removeAllViews();
        
        for (int i = 0; i < FIELD_SIZE; i++) {
            final int position = i;
            View cardView = createCardView(field.get(i), position, false);
            fieldLayout.addView(cardView);
        }
    }
    
    private View createCardView(Card card, int position, boolean isHand) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.card_slot, null);
        
        TextView cardNameText = cardView.findViewById(R.id.card_name_text);
        TextView cardStatsText = cardView.findViewById(R.id.card_stats_text);
        ImageView cardImage = cardView.findViewById(R.id.card_image);
        Button removeButton = cardView.findViewById(R.id.remove_card_button);
        Button moveButton = cardView.findViewById(R.id.move_card_button);
        
        if (card.isEmpty()) {
            cardNameText.setText("Empty Slot");
            cardStatsText.setText("");
            cardImage.setImageResource(R.drawable.empty_card);
            removeButton.setVisibility(View.GONE);
            moveButton.setVisibility(View.GONE);
        } else {
            cardNameText.setText(card.getName());
            cardStatsText.setText(card.getType() + " - " + card.getAttack() + " ATK");
            
            // Load actual card image based on ID
            loadCardImage(cardImage, card.getId());
            
            removeButton.setVisibility(View.VISIBLE);
            moveButton.setVisibility(isHand ? View.VISIBLE : View.GONE);
        }
        
        // Remove card button
        removeButton.setOnClickListener(v -> {
            if (isHand) {
                hand.get(position).clear();
                // Compact hand after manual removal (if enabled)
                if (autoCompactHand) {
                    compactHand();
                }
            } else {
                field.get(position).clear();
            }
            updateUI();
        });
        
        // Move card to field button (only for hand cards)
        moveButton.setOnClickListener(v -> {
            if (isHand && !card.isEmpty()) {
                moveCardToField(position);
            }
        });
        
        // Click to add/edit card
        cardView.setOnClickListener(v -> {
            if (card.isEmpty()) {
                showCardSearchDialog(position, isHand);
            } else {
                showCardOptionsDialog(card, position, isHand);
            }
        });
        
        return cardView;
    }
    
    private View createAddSlotView() {
        View addSlotView = LayoutInflater.from(this).inflate(R.layout.card_slot, null);
        
        TextView cardNameText = addSlotView.findViewById(R.id.card_name_text);
        TextView cardStatsText = addSlotView.findViewById(R.id.card_stats_text);
        ImageView cardImage = addSlotView.findViewById(R.id.card_image);
        Button removeButton = addSlotView.findViewById(R.id.remove_card_button);
        Button moveButton = addSlotView.findViewById(R.id.move_card_button);
        
        // Style as "Add Slot" button
        cardNameText.setText("Add Slot");
        cardStatsText.setText("Tap to expand");
        cardImage.setImageResource(R.drawable.ic_add);
        removeButton.setVisibility(View.GONE);
        moveButton.setVisibility(View.GONE);
        
        // Set click listener to expand hand
        addSlotView.setOnClickListener(v -> {
            if (currentHandSize < MAX_HAND_SIZE) {
                currentHandSize++;
                updateUI();
                Toast.makeText(this, "Hand expanded to " + currentHandSize + " slots", Toast.LENGTH_SHORT).show();
            }
        });
        
        return addSlotView;
    }
    
    private void loadCardImage(ImageView imageView, int cardId) {
        try {
            // Format card ID to match file naming convention (c000.jpg, c001.jpg, etc.)
            // Subtract 1 because card IDs start from 1 but files start from c000
            String cardImageName = "c" + String.format("%03d", cardId - 1);
            
            // Get resource ID for the card image
            int resourceId = getResources().getIdentifier(cardImageName, "drawable", getPackageName());
            
            if (resourceId != 0) {
                imageView.setImageResource(resourceId);
            } else {
                // If card image not found, use default card back
                imageView.setImageResource(R.drawable.card_back);
            }
        } catch (Exception e) {
            // If any error occurs, use default card back
            imageView.setImageResource(R.drawable.card_back);
        }
    }
    
    private void showAddCardDialog() {
        // Find first empty slot in current visible hand, or use slot 0 if all full
        int firstEmptySlot = 0;
        for (int i = 0; i < currentHandSize; i++) {
            if (hand.get(i).isEmpty()) {
                firstEmptySlot = i;
                break;
            }
        }
        
        showEnhancedCardSearchDialog(firstEmptySlot, true);
    }
    
    private void showCardSearchDialog(int position, boolean isHand) {
        showEnhancedCardSearchDialog(position, isHand);
    }
    
    private void showEnhancedCardSearchDialog(int initialPosition, boolean initialIsHand) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_card, null);
        
        // Get dialog elements
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView slotIndicator = dialogView.findViewById(R.id.slot_indicator);
        Button previousButton = dialogView.findViewById(R.id.previous_slot_button);
        Button nextButton = dialogView.findViewById(R.id.next_slot_button);
        EditText cardNameInput = dialogView.findViewById(R.id.card_name_input);
        EditText cardIdInput = dialogView.findViewById(R.id.card_id_input);
        Button clearButton = dialogView.findViewById(R.id.clear_search_button);
        RecyclerView searchResults = dialogView.findViewById(R.id.search_results);
        
        // Track current slot (1-5 hand, 6-10 field)
        int[] currentSlot = {convertToGlobalSlot(initialPosition, initialIsHand)};
        
        // Create dialog first so we can reference it in the callback
        builder.setView(dialogView);
        builder.setNegativeButton("Close", null);
        AlertDialog dialog = builder.create();
        
        // Setup search results
        List<Card> searchResultsList = new ArrayList<>();
        CardSearchAdapter searchAdapter = new CardSearchAdapter(searchResultsList, card -> {
            int[] localPos = convertFromGlobalSlot(currentSlot[0]);
            int position = localPos[0];
            boolean isHand = localPos[1] == 1;
            
            // Place the card
            if (isHand) {
                hand.set(position, new Card(card.getId(), card.getName(), card.getType(), card.getAttack()));
            } else {
                field.set(position, new Card(card.getId(), card.getName(), card.getType(), card.getAttack()));
            }
            
            // Save card usage to memory
            saveCardUsage(card);
            
            // Show toast feedback
            String location = isHand ? "hand" : "field";
            String slotNumber = isHand ? String.valueOf(currentSlot[0]) : String.valueOf(currentSlot[0] - 94);
            Toast.makeText(MainActivity.this, 
                card.getName() + " placed in " + location + " slot " + slotNumber + "!", 
                Toast.LENGTH_SHORT).show();
            
            updateUI();
            
            // Close the dialog after successful placement only if auto-close is enabled
            if (autoCloseDialog) {
                dialog.dismiss();
            }
        });
        
        searchResults.setLayoutManager(new LinearLayoutManager(this));
        searchResults.setAdapter(searchAdapter);
        
        // Update UI for current slot with validation
        Runnable updateSlotUI = () -> {
            // Validate current slot and adjust if needed
            if (currentSlot[0] <= 15) {
                // Hand slot - make sure it's within current hand size
                if (currentSlot[0] > currentHandSize) {
                    currentSlot[0] = currentHandSize;
                }
            }
            
            int[] localPos = convertFromGlobalSlot(currentSlot[0]);
            int position = localPos[0];
            boolean isHand = localPos[1] == 1;
            
            String area = isHand ? "Hand" : "Field";
            String slotNum = isHand ? String.valueOf(currentSlot[0]) : String.valueOf(currentSlot[0]);
            slotIndicator.setText(area + " Slot " + slotNum);
            
            // Show current card info if any
            Card currentCard = isHand ? hand.get(position) : field.get(position);
            if (!currentCard.isEmpty()) {
                dialogTitle.setText("Replace: " + currentCard.getName());
            } else {
                dialogTitle.setText("Select Card");
            }
        };
        
        // Live search functionality
        TextWatcher liveSearchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString().trim(), cardIdInput.getText().toString().trim(), 
                            searchResultsList, searchAdapter);
            }
        };
        
        TextWatcher idSearchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                performSearch(cardNameInput.getText().toString().trim(), s.toString().trim(), 
                            searchResultsList, searchAdapter);
            }
        };
        
        cardNameInput.addTextChangedListener(liveSearchWatcher);
        cardIdInput.addTextChangedListener(idSearchWatcher);
        
        // Navigation functionality
        previousButton.setOnClickListener(v -> {
            if (currentSlot[0] == 1) {
                // Wrap from hand slot 1 to field slot 99
                currentSlot[0] = 99;
            } else if (currentSlot[0] == 95) {
                // Wrap from field slot 95 to current max hand slot
                currentSlot[0] = currentHandSize;
            } else {
                currentSlot[0] = currentSlot[0] - 1;
            }
            updateSlotUI.run();
        });
        
        nextButton.setOnClickListener(v -> {
            if (currentSlot[0] == currentHandSize) {
                // Wrap from max hand slot to field slot 95
                currentSlot[0] = 95;
            } else if (currentSlot[0] == 99) {
                // Wrap from field slot 99 to hand slot 1
                currentSlot[0] = 1;
            } else {
                currentSlot[0] = currentSlot[0] + 1;
            }
            updateSlotUI.run();
        });
        
        // Clear search functionality
        clearButton.setOnClickListener(v -> {
            cardNameInput.setText("");
            cardIdInput.setText("");
            searchResultsList.clear();
            searchAdapter.notifyDataSetChanged();
        });
        
        // Initial UI setup
        updateSlotUI.run();
        
        dialog.show();
    }
    
    // Helper method to convert position + isHand to global slot (1-15 hand, 95-99 field)
    private int convertToGlobalSlot(int position, boolean isHand) {
        if (isHand) {
            return position + 1; // Hand slots 1-15
        } else {
            return position + 95; // Field slots 95-99
        }
    }
    
    // Helper method to convert global slot back to position + isHand
    private int[] convertFromGlobalSlot(int globalSlot) {
        if (globalSlot <= 15) {
            return new int[]{globalSlot - 1, 1}; // {position, 1=hand}
        } else if (globalSlot >= 95 && globalSlot <= 99) {
            return new int[]{globalSlot - 95, 0}; // {position, 0=field}
        } else {
            // Invalid slot, default to hand slot 1
            return new int[]{0, 1};
        }
    }
    
    // Perform search and update results
    private void performSearch(String name, String idStr, List<Card> searchResultsList, CardSearchAdapter searchAdapter) {
        searchResultsList.clear();
        
        if (!name.isEmpty()) {
            List<Card> results = fusionEngine.searchCards(name);
            // Sort results to prioritize previously used cards
            results.sort((card1, card2) -> {
                int usage1 = getCardUsageCount(card1.getId());
                int usage2 = getCardUsageCount(card2.getId());
                long lastUsed1 = getCardLastUsed(card1.getId());
                long lastUsed2 = getCardLastUsed(card2.getId());
                
                // First priority: cards that have been used before (usage > 0)
                if (usage1 > 0 && usage2 == 0) return -1;
                if (usage1 == 0 && usage2 > 0) return 1;
                
                // Second priority: among used cards, sort by last used time (most recent first)
                if (usage1 > 0 && usage2 > 0) {
                    return Long.compare(lastUsed2, lastUsed1);
                }
                
                // Third priority: for unused cards, sort alphabetically
                return card1.getName().compareToIgnoreCase(card2.getName());
            });
            searchResultsList.addAll(results);
        } else if (!idStr.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                Card card = fusionEngine.findCardById(id);
                if (card != null) {
                    searchResultsList.add(card);
                }
            } catch (NumberFormatException e) {
                // Invalid ID, ignore
            }
        }
        
        searchAdapter.notifyDataSetChanged();
    }
    
    /**
     * Save a card's usage to memory system
     */
    private void saveCardUsage(Card card) {
        SharedPreferences.Editor editor = cardMemoryPrefs.edit();
        String usageKey = KEY_CARD_USAGE_COUNT + card.getId();
        String lastUsedKey = KEY_CARD_LAST_USED + card.getId();
        
        // Increment usage count
        int currentUsage = cardMemoryPrefs.getInt(usageKey, 0);
        editor.putInt(usageKey, currentUsage + 1);
        
        // Update last used timestamp
        editor.putLong(lastUsedKey, System.currentTimeMillis());
        
        editor.apply();
    }
    
    /**
     * Get card usage count from memory
     */
    private int getCardUsageCount(int cardId) {
        return cardMemoryPrefs.getInt(KEY_CARD_USAGE_COUNT + cardId, 0);
    }
    
    /**
     * Get card last used timestamp from memory
     */
    private long getCardLastUsed(int cardId) {
        return cardMemoryPrefs.getLong(KEY_CARD_LAST_USED + cardId, 0);
    }
    
    private void showCardOptionsDialog(Card card, int position, boolean isHand) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(card.getName());
        
        String[] options = isHand ? 
            new String[]{"Move to Field", "Remove", "Replace"} :
            new String[]{"Remove", "Replace"};
        
        builder.setItems(options, (dialog, which) -> {
            if (isHand) {
                switch (which) {
                    case 0: // Move to Field
                        moveCardToField(position);
                        break;
                    case 1: // Remove
                        hand.get(position).clear();
                        // Compact hand after manual removal (if enabled)
                        if (autoCompactHand) {
                            compactHand();
                        }
                        updateUI();
                        break;
                    case 2: // Replace
                        showCardSearchDialog(position, true);
                        break;
                }
            } else {
                switch (which) {
                    case 0: // Remove
                        field.get(position).clear();
                        updateUI();
                        break;
                    case 1: // Replace
                        showCardSearchDialog(position, false);
                        break;
                }
            }
        });
        
        builder.create().show();
    }
    
    private void moveCardToField(int handPosition) {
        Card cardToMove = hand.get(handPosition);
        
        // Find first empty field slot
        for (int i = 0; i < FIELD_SIZE; i++) {
            if (field.get(i).isEmpty()) {
                field.set(i, new Card(cardToMove.getId(), cardToMove.getName(), cardToMove.getType(), cardToMove.getAttack()));
                hand.get(handPosition).clear();
                // Compact hand after moving card to field (if enabled)
                if (autoCompactHand) {
                    compactHand();
                }
                updateUI();
                return;
            }
        }
        
        Toast.makeText(this, "Field is full!", Toast.LENGTH_SHORT).show();
    }
    
    private void setupFusionRecycler() {
        fusionAdapter = new FusionResultAdapter(currentFusions, this::performFusion);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        fusionResultsRecycler.setLayoutManager(layoutManager);
        fusionResultsRecycler.setAdapter(fusionAdapter);
        fusionResultsRecycler.setHasFixedSize(false);
        fusionResultsRecycler.setNestedScrollingEnabled(true);
    }
    
    private void updateFusionResults() {
        currentFusions.clear();
        
        // Create a sub-list of ALL current hand slots (not just visible ones)
        // This ensures fusion works with all cards user has, even if not all are visible on screen
        List<Card> currentHand = new ArrayList<>();
        for (int i = 0; i < currentHandSize; i++) {
            currentHand.add(hand.get(i));
        }
        
        // Create a list of current field cards
        List<Card> currentField = new ArrayList<>(field);
        
        List<FusionEngine.FusionResult> newFusions = fusionEngine.findPossibleFusions(currentHand, currentField);
        currentFusions.addAll(newFusions);
        
        // Apply current sorting
        applySorting();
        
        android.util.Log.d("MainActivity", "Found " + currentFusions.size() + " fusion results from " + currentHandSize + " hand slots and " + currentField.size() + " field slots");
        
        int directCount = 0;
        int chainedCount = 0;
        int fieldDirectCount = 0;
        int fieldChainedCount = 0;
        
        for (int i = 0; i < currentFusions.size(); i++) {
            FusionEngine.FusionResult fusion = currentFusions.get(i);
            if (fusion.getType() == FusionEngine.FusionType.DIRECT) {
                directCount++;
                android.util.Log.d("MainActivity", "Direct Fusion " + directCount + ": " + 
                    fusion.getMaterial1().getName() + " + " + fusion.getMaterial2().getName() + 
                    " = " + fusion.getResult().getName());
            } else if (fusion.getType() == FusionEngine.FusionType.CHAINED) {
                chainedCount++;
                FusionEngine.FusionResult prerequisite = fusion.getPrerequisiteFusion();
                android.util.Log.d("MainActivity", "Chained Fusion " + chainedCount + ": (" + 
                    prerequisite.getMaterial1().getName() + " + " + prerequisite.getMaterial2().getName() + 
                    " = " + prerequisite.getResult().getName() + ") + " + fusion.getAdditionalCard().getName() + 
                    " = " + fusion.getResult().getName());
            } else if (fusion.getType() == FusionEngine.FusionType.FIELD_DIRECT) {
                fieldDirectCount++;
                android.util.Log.d("MainActivity", "Field Direct Fusion " + fieldDirectCount + ": " + 
                    fusion.getMaterial1().getName() + " (Field) + " + fusion.getMaterial2().getName() + " (Hand)" +
                    " = " + fusion.getResult().getName());
            } else if (fusion.getType() == FusionEngine.FusionType.FIELD_CHAINED) {
                fieldChainedCount++;
                FusionEngine.FusionResult prerequisite = fusion.getPrerequisiteFusion();
                android.util.Log.d("MainActivity", "Field Chained Fusion " + fieldChainedCount + ": (" + 
                    prerequisite.getMaterial1().getName() + " (Field) + " + prerequisite.getMaterial2().getName() + " (Hand)" +
                    " = " + prerequisite.getResult().getName() + ") + " + fusion.getAdditionalCard().getName() + " (Hand)" +
                    " = " + fusion.getResult().getName());
            }
        }
        
        android.util.Log.d("MainActivity", "Summary: " + directCount + " direct fusions, " + chainedCount + " chained fusions, " + fieldDirectCount + " field direct fusions, " + fieldChainedCount + " field chained fusions");
        
        fusionAdapter.notifyDataSetChanged();
    }
    
    private void applySorting() {
        switch (currentSortType) {
            case ATTACK:
                sortByAttack();
                break;
            case CARD_COUNT:
                sortByCardCount();
                break;
            case NONE:
            default:
                // Keep original order - no sorting needed
                break;
        }
        if (fusionAdapter != null) {
            fusionAdapter.notifyDataSetChanged();
        }
    }
    
    private void sortByAttack() {
        currentFusions.sort((fusion1, fusion2) -> {
            int attack1 = fusion1.getResult().getAttack();
            int attack2 = fusion2.getResult().getAttack();
            return Integer.compare(attack2, attack1); // Descending order (highest first)
        });
    }
    
    private void sortByCardCount() {
        currentFusions.sort((fusion1, fusion2) -> {
            int cardCount1 = getCardCount(fusion1);
            int cardCount2 = getCardCount(fusion2);
            if (cardCount1 != cardCount2) {
                return Integer.compare(cardCount1, cardCount2); // Ascending order (fewest first)
            }
            // If same card count, sort by attack as secondary criteria
            return Integer.compare(fusion2.getResult().getAttack(), fusion1.getResult().getAttack());
        });
    }
    
    private int getCardCount(FusionEngine.FusionResult fusion) {
        if (fusion.getType() == FusionEngine.FusionType.DIRECT) {
            return 2; // 2 cards (a+b)
        } else if (fusion.getType() == FusionEngine.FusionType.CHAINED) {
            return 3; // 3 cards (a+b+c)
        } else if (fusion.getType() == FusionEngine.FusionType.FIELD_DIRECT) {
            return 2; // 1 field card + 1 hand card
        } else if (fusion.getType() == FusionEngine.FusionType.FIELD_CHAINED) {
            return 3; // 1 field card + 2 hand cards
        }
        return 0;
    }
    
    private void updateButtonStates() {
        // Reset all buttons to default state
        sortByAttackButton.setAlpha(currentSortType == SortType.ATTACK ? 1.0f : 0.6f);
        sortByCardCountButton.setAlpha(currentSortType == SortType.CARD_COUNT ? 1.0f : 0.6f);
        clearFiltersButton.setAlpha(currentSortType == SortType.NONE ? 1.0f : 0.6f);
    }
    
    private void updateMenuItems(Menu menu) {
        if (menu == null) {
            // Called from within activity, need to invalidate options menu
            invalidateOptionsMenu();
            return;
        }
        
        // Update the toggle menu item title
        MenuItem toggleItem = menu.findItem(R.id.action_toggle_reposition);
        if (toggleItem != null) {
            toggleItem.setTitle(autoCompactHand ? "Auto Reposition: ON" : "Auto Reposition: OFF");
        }
        MenuItem autoCloseItem = menu.findItem(R.id.action_toggle_auto_close);
        if (autoCloseItem != null) {
            autoCloseItem.setTitle(autoCloseDialog ? "Auto Close Dialog: ON" : "Auto Close Dialog: OFF");
        }
    }
    
    private void performFusion(FusionEngine.FusionResult fusionResult) {
        // Find empty field slot
        for (int i = 0; i < FIELD_SIZE; i++) {
            if (field.get(i).isEmpty()) {
                // Place fusion result on field
                Card result = fusionResult.getResult();
                field.set(i, new Card(result.getId(), result.getName(), result.getType(), result.getAttack()));
     
                // Remove materials from hand/field based on fusion type
                if (fusionResult.getType() == FusionEngine.FusionType.DIRECT) {
                    // Direct fusion: remove 2 hand cards
                    int pos1 = fusionResult.getPosition1();
                    int pos2 = fusionResult.getPosition2();
                    
                    if (pos1 < currentHandSize && pos2 < currentHandSize) {
                        hand.get(pos1).clear();
                        hand.get(pos2).clear();
                        if (autoCompactHand) {
                            compactHand();
                        }
                        Toast.makeText(this, "Direct fusion performed: " + result.getName(), Toast.LENGTH_SHORT).show();
                    }
                } else if (fusionResult.getType() == FusionEngine.FusionType.CHAINED) {
                    // Chained fusion: remove 3 hand cards
                    int pos1 = fusionResult.getPosition1();
                    int pos2 = fusionResult.getPosition2();
                    int pos3 = fusionResult.getAdditionalCardPosition();
                    
                    if (pos1 < currentHandSize && pos2 < currentHandSize && pos3 < currentHandSize) {
                        hand.get(pos1).clear();
                        hand.get(pos2).clear();
                        hand.get(pos3).clear();
                        if (autoCompactHand) {
                            compactHand();
                        }
                        
                        FusionEngine.FusionResult prerequisite = fusionResult.getPrerequisiteFusion();
                        String chainDescription = "Chained fusion: (" + prerequisite.getMaterial1().getName() + 
                                                " + " + prerequisite.getMaterial2().getName() + " → " + prerequisite.getResult().getName() + 
                                                ") + " + fusionResult.getAdditionalCard().getName() + " = " + result.getName();
                        Toast.makeText(this, chainDescription, Toast.LENGTH_LONG).show();
                    }
                } else if (fusionResult.getType() == FusionEngine.FusionType.FIELD_DIRECT) {
                    // Field direct fusion: remove 1 field card + 1 hand card
                    int fieldPos = fusionResult.getPosition1(); // Field position
                    int handPos = fusionResult.getPosition2(); // Hand position
                    
                    if (fieldPos < FIELD_SIZE && handPos < currentHandSize) {
                        field.get(fieldPos).clear();
                        hand.get(handPos).clear();
                        if (autoCompactHand) {
                            compactHand();
                        }
                        Toast.makeText(this, "Field fusion performed: " + fusionResult.getMaterial1().getName() + 
                                     " (Field) + " + fusionResult.getMaterial2().getName() + " (Hand) = " + result.getName(), Toast.LENGTH_LONG).show();
                    }
                } else if (fusionResult.getType() == FusionEngine.FusionType.FIELD_CHAINED) {
                    // Field chained fusion: remove 1 field card + 2 hand cards
                    int fieldPos = fusionResult.getPosition1(); // Field position from prerequisite
                    int handPos1 = fusionResult.getPosition2(); // Hand position from prerequisite  
                    int handPos2 = fusionResult.getAdditionalCardPosition(); // Additional hand position
                    
                    if (fieldPos < FIELD_SIZE && handPos1 < currentHandSize && handPos2 < currentHandSize) {
                        field.get(fieldPos).clear();
                        hand.get(handPos1).clear();
                        hand.get(handPos2).clear();
                        if (autoCompactHand) {
                            compactHand();
                        }
                        
                        FusionEngine.FusionResult prerequisite = fusionResult.getPrerequisiteFusion();
                        String chainDescription = "Field chained fusion: (" + prerequisite.getMaterial1().getName() + " (Field)" +
                                                " + " + prerequisite.getMaterial2().getName() + " (Hand) → " + prerequisite.getResult().getName() + 
                                                ") + " + fusionResult.getAdditionalCard().getName() + " (Hand) = " + result.getName();
                        Toast.makeText(this, chainDescription, Toast.LENGTH_LONG).show();
                    }
                }
                
                updateUI();
                return;
            }
        }
        
        Toast.makeText(this, "Field is full! Cannot perform fusion.", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Compacts the hand by moving all non-empty cards to the front positions.
     * This removes gaps left by removed cards and repositions remaining cards.
     * For example: [Card1, Empty, Empty, Card4, Card5] becomes [Card1, Card4, Card5, Empty, Empty]
     */
    private void compactHand() {
        List<Card> nonEmptyCards = new ArrayList<>();
        
        // Collect all non-empty cards from current hand size
        for (int i = 0; i < currentHandSize; i++) {
            Card card = hand.get(i);
            if (!card.isEmpty()) {
                // Create a copy to avoid reference issues
                nonEmptyCards.add(new Card(card.getId(), card.getName(), card.getType(), card.getAttack()));
            }
        }
        
        // Clear current hand positions
        for (int i = 0; i < currentHandSize; i++) {
            hand.get(i).clear();
        }
        
        // Place non-empty cards at the front positions
        for (int i = 0; i < nonEmptyCards.size() && i < currentHandSize; i++) {
            Card cardToCopy = nonEmptyCards.get(i);
            hand.set(i, new Card(cardToCopy.getId(), cardToCopy.getName(), cardToCopy.getType(), cardToCopy.getAttack()));
        }
        
        android.util.Log.d("MainActivity", "Hand compacted: " + nonEmptyCards.size() + " cards repositioned to front");
    }

    private void updateUI() {
        createHandSlots();
        createFieldSlots();
        updateFusionResults();
        updateStatusTexts();
    }
    
    private void updateStatusTexts() {
        int handCount = 0;
        int fieldCount = 0;
        
        // Count only visible hand slots
        for (int i = 0; i < currentHandSize; i++) {
            if (!hand.get(i).isEmpty()) handCount++;
        }
        
        for (Card card : field) {
            if (!card.isEmpty()) fieldCount++;
        }
        
        handStatusText.setText("Hand: " + handCount + "/" + currentHandSize); // + " (max " + MAX_HAND_SIZE + ")");
        fieldStatusText.setText("Field: " + fieldCount + "/" + FIELD_SIZE);
    }

    private void showInformationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_app_information, null);
        
        // Set up the dialog
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Handle close button
        Button closeButton = dialogView.findViewById(R.id.close_information_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
} 