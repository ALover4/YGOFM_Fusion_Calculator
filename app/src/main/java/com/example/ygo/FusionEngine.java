package com.example.ygo;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class FusionEngine {
    private static final String TAG = "FusionEngine";
    private static FusionEngine instance;
    private List<Card> allCards;
    private Map<Integer, List<FusionData>> fusionsByCard;
    private Map<String, Integer> cardNameToId;
    private boolean isLoaded = false;

    private FusionEngine() {
        allCards = new ArrayList<>();
        fusionsByCard = new HashMap<>();
        cardNameToId = new HashMap<>();
    }

    public static FusionEngine getInstance() {
        if (instance == null) {
            instance = new FusionEngine();
        }
        return instance;
    }

    public void loadData(Context context) {
        if (isLoaded) return;

        try {
            loadCardsFromJson(context);
            isLoaded = true;
            Log.d(TAG, "Data loaded successfully. Cards: " + allCards.size() + ", Fusion entries: " + fusionsByCard.size());
        } catch (Exception e) {
            Log.e(TAG, "Error loading data", e);
            e.printStackTrace();
        }
    }

    private void loadCardsFromJson(Context context) throws IOException, JSONException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("Cards.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        
        StringBuilder jsonString = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        reader.close();

        JSONArray cardsArray = new JSONArray(jsonString.toString());
        Log.d(TAG, "Loading " + cardsArray.length() + " cards from Cards.json");

        for (int i = 0; i < cardsArray.length(); i++) {
            JSONObject cardObj = cardsArray.getJSONObject(i);
            
            // Parse basic card info
            int id = cardObj.getInt("Id");
            String name = cardObj.getString("Name");
            int type = cardObj.getInt("Type");
            int attack = cardObj.getInt("Attack");
            
            // Convert type number to string (you might need to adjust this mapping)
            String typeStr = getTypeString(type);
            
            Card card = new Card(id, name, typeStr, attack);
            allCards.add(card);
            cardNameToId.put(name.toLowerCase().replaceAll("\\s+", ""), id);
            
            // Parse fusions
            JSONArray fusions = cardObj.getJSONArray("Fusions");
            List<FusionData> cardFusions = new ArrayList<>();
            
            for (int j = 0; j < fusions.length(); j++) {
                JSONObject fusion = fusions.getJSONObject(j);
                int card1 = fusion.getInt("_card1");
                int card2 = fusion.getInt("_card2");
                int result = fusion.getInt("_result");
                
                // Only add if this card is card1 (to avoid duplicates)
                if (card1 == id) {
                    cardFusions.add(new FusionData(card1, card2, result));
                }
            }
            
            if (!cardFusions.isEmpty()) {
                fusionsByCard.put(id, cardFusions);
                Log.d(TAG, "Card " + name + " (ID: " + id + ") has " + cardFusions.size() + " fusions");
            }
        }
        
        Log.d(TAG, "Loaded " + allCards.size() + " cards");
        
        // Debug specific cards
        Card airMarmot = findCardById(202);
        Card griggle = findCardById(547);
        if (airMarmot != null && griggle != null) {
            Log.d(TAG, "Air Marmot: " + airMarmot.getName() + " (ID: " + airMarmot.getId() + ")");
            Log.d(TAG, "Griggle: " + griggle.getName() + " (ID: " + griggle.getId() + ")");
            
            // Check if Air Marmot has fusion with Griggle
            if (fusionsByCard.containsKey(202)) {
                for (FusionData fusion : fusionsByCard.get(202)) {
                    if (fusion.card2 == 547) {
                        Card result = findCardById(fusion.result);
                        Log.d(TAG, "Found fusion: Air Marmot + Griggle = " + (result != null ? result.getName() : "Unknown") + " (ID: " + fusion.result + ")");
                    }
                }
            }
        }
    }

    private String getTypeString(int type) {
        // This mapping might need adjustment based on the actual type values in the game
        switch (type) {
            case 1: return "spellcaster";
            case 2: return "dragon";
            case 3: return "zombie";
            case 4: return "warrior";
            case 5: return "beast";
            case 6: return "beast-warrior";
            case 7: return "winged-beast";
            case 8: return "fiend";
            case 9: return "fairy";
            case 10: return "insect";
            case 11: return "dinosaur";
            case 12: return "reptile";
            case 13: return "fish";
            case 14: return "sea-serpent";
            case 15: return "machine";
            case 16: return "thunder";
            case 17: return "aqua";
            case 18: return "pyro";
            case 19: return "rock";
            case 20: return "plant";
            default: return "unknown";
        }
    }

    public List<FusionResult> findPossibleFusions(List<Card> hand) {
        List<FusionResult> results = new ArrayList<>();
        
        Log.d(TAG, "Finding fusions for hand with " + hand.size() + " cards");
        
        // Find all direct fusions first (hand only)
        results.addAll(findDirectFusions(hand));
        
        // Find chained fusions (fusion results that can fuse with remaining cards)
        results.addAll(findChainedFusions(hand));
        
        Log.d(TAG, "Found " + results.size() + " possible fusions (direct + chained)");
        return results;
    }

    public List<FusionResult> findPossibleFusions(List<Card> hand, List<Card> field) {
        List<FusionResult> results = new ArrayList<>();
        
        Log.d(TAG, "Finding fusions for hand with " + hand.size() + " cards and field with " + field.size() + " cards");
        
        // Find all direct fusions (hand only)
        results.addAll(findDirectFusions(hand));
        
        // Find direct fusions with field cards (field + hand)
        results.addAll(findFieldDirectFusions(field, hand));
        
        // Find chained fusions (hand only)
        results.addAll(findChainedFusions(hand));
        
        // Find chained fusions with field cards (field card must be in prerequisite fusion)
        results.addAll(findFieldChainedFusions(field, hand));
        
        Log.d(TAG, "Found " + results.size() + " possible fusions (direct + chained + field)");
        return results;
    }
    
    private List<FusionResult> findDirectFusions(List<Card> hand) {
        List<FusionResult> results = new ArrayList<>();
        
        for (int i = 0; i < hand.size(); i++) {
            Card card1 = hand.get(i);
            if (card1.isEmpty()) continue;
            
            for (int j = i + 1; j < hand.size(); j++) {
                Card card2 = hand.get(j);
                if (card2.isEmpty()) continue;
                
                Log.d(TAG, "Checking direct fusion: " + card1.getName() + " (ID: " + card1.getId() + ") + " + card2.getName() + " (ID: " + card2.getId() + ")");
                
                Integer resultId = checkFusion(card1.getId(), card2.getId());
                if (resultId != null) {
                    Card resultCard = findCardById(resultId);
                    if (resultCard != null) {
                        Log.d(TAG, "Found direct fusion: " + card1.getName() + " + " + card2.getName() + " = " + resultCard.getName());
                        results.add(new FusionResult(card1, card2, resultCard, i, j, FusionType.DIRECT));
                    }
                }
            }
        }
        
        return results;
    }
    
    private List<FusionResult> findFieldDirectFusions(List<Card> field, List<Card> hand) {
        List<FusionResult> results = new ArrayList<>();
        
        // Check each field card against each hand card
        for (int fieldPos = 0; fieldPos < field.size(); fieldPos++) {
            Card fieldCard = field.get(fieldPos);
            if (fieldCard.isEmpty()) continue;
            
            for (int handPos = 0; handPos < hand.size(); handPos++) {
                Card handCard = hand.get(handPos);
                if (handCard.isEmpty()) continue;
                
                Log.d(TAG, "Checking field fusion: " + fieldCard.getName() + " (Field ID: " + fieldCard.getId() + ") + " + handCard.getName() + " (Hand ID: " + handCard.getId() + ")");
                
                Integer resultId = checkFusion(fieldCard.getId(), handCard.getId());
                if (resultId != null) {
                    Card resultCard = findCardById(resultId);
                    if (resultCard != null) {
                        Log.d(TAG, "Found field fusion: " + fieldCard.getName() + " + " + handCard.getName() + " = " + resultCard.getName());
                        results.add(new FusionResult(fieldCard, handCard, resultCard, fieldPos, handPos, FusionType.FIELD_DIRECT));
                    }
                }
            }
        }
        
        return results;
    }
    
    private List<FusionResult> findFieldChainedFusions(List<Card> field, List<Card> hand) {
        List<FusionResult> results = new ArrayList<>();
        
        // Get all field direct fusions first (these will be our prerequisite fusions)
        List<FusionResult> fieldDirectFusions = findFieldDirectFusions(field, hand);
        
        // For each field direct fusion, check if the result can fuse with remaining hand cards
        for (FusionResult fieldDirectFusion : fieldDirectFusions) {
            Card fusionResult = fieldDirectFusion.getResult();
            int usedFieldPos = fieldDirectFusion.getPosition1(); // Field position
            int usedHandPos = fieldDirectFusion.getPosition2(); // Hand position
            
            // Check if fusion result can combine with any remaining hand cards
            for (int handPos = 0; handPos < hand.size(); handPos++) {
                Card remainingHandCard = hand.get(handPos);
                if (remainingHandCard.isEmpty()) continue;
                if (handPos == usedHandPos) continue; // Skip already used hand card
                
                Log.d(TAG, "Checking field chained fusion: " + fusionResult.getName() + " (from " + 
                      fieldDirectFusion.getMaterial1().getName() + "+" + fieldDirectFusion.getMaterial2().getName() + 
                      ") + " + remainingHandCard.getName());
                
                Integer chainedResultId = checkFusion(fusionResult.getId(), remainingHandCard.getId());
                if (chainedResultId != null) {
                    Card chainedResultCard = findCardById(chainedResultId);
                    if (chainedResultCard != null) {
                        Log.d(TAG, "Found field chained fusion: (" + fieldDirectFusion.getMaterial1().getName() + "+" + 
                              fieldDirectFusion.getMaterial2().getName() + "=" + fusionResult.getName() + ") + " + 
                              remainingHandCard.getName() + " = " + chainedResultCard.getName());
                        
                        // Create field chained fusion result
                        results.add(new FusionResult(
                            fieldDirectFusion, // The prerequisite fusion (field + hand)
                            remainingHandCard, // The additional hand card
                            chainedResultCard, // The final result
                            handPos, // Position of the additional hand card
                            FusionType.FIELD_CHAINED
                        ));
                    }
                }
            }
        }
        
        return results;
    }

    private List<FusionResult> findChainedFusions(List<Card> hand) {
        List<FusionResult> results = new ArrayList<>();
        
        // Get all direct fusions first
        List<FusionResult> directFusions = findDirectFusions(hand);
        
        // For each direct fusion, check if the result can fuse with remaining cards
        for (FusionResult directFusion : directFusions) {
            Card fusionResult = directFusion.getResult();
            int usedPos1 = directFusion.getPosition1();
            int usedPos2 = directFusion.getPosition2();
            
            // Check if fusion result can combine with any remaining cards
            for (int i = 0; i < hand.size(); i++) {
                Card remainingCard = hand.get(i);
                if (remainingCard.isEmpty()) continue;
                if (i == usedPos1 || i == usedPos2) continue; // Skip already used cards
                
                Log.d(TAG, "Checking chained fusion: " + fusionResult.getName() + " (from " + 
                      directFusion.getMaterial1().getName() + "+" + directFusion.getMaterial2().getName() + 
                      ") + " + remainingCard.getName());
                
                Integer chainedResultId = checkFusion(fusionResult.getId(), remainingCard.getId());
                if (chainedResultId != null) {
                    Card chainedResultCard = findCardById(chainedResultId);
                    if (chainedResultCard != null) {
                        Log.d(TAG, "Found chained fusion: (" + directFusion.getMaterial1().getName() + "+" + 
                              directFusion.getMaterial2().getName() + "=" + fusionResult.getName() + ") + " + 
                              remainingCard.getName() + " = " + chainedResultCard.getName());
                        
                        // Create chained fusion result
                        results.add(new FusionResult(
                            directFusion, // The prerequisite fusion
                            remainingCard, // The additional card
                            chainedResultCard, // The final result
                            i, // Position of the additional card
                            FusionType.CHAINED
                        ));
                    }
                }
            }
        }
        
        return results;
    }

    private Integer checkFusion(int card1Id, int card2Id) {
        // Check if card1 has a fusion with card2
        if (fusionsByCard.containsKey(card1Id)) {
            for (FusionData fusion : fusionsByCard.get(card1Id)) {
                if (fusion.card2 == card2Id) {
                    return fusion.result;
                }
            }
        }
        
        // Check if card2 has a fusion with card1 (bidirectional)
        if (fusionsByCard.containsKey(card2Id)) {
            for (FusionData fusion : fusionsByCard.get(card2Id)) {
                if (fusion.card2 == card1Id) {
                    return fusion.result;
                }
            }
        }
        
        return null;
    }

    public Card findCardByName(String name) {
        String normalizedName = name.toLowerCase().replaceAll("\\s+", "");
        Integer id = cardNameToId.get(normalizedName);
        if (id != null) {
            return findCardById(id);
        }
        
        // Fallback: search by partial name match
        for (Card card : allCards) {
            if (card.getName().toLowerCase().contains(name.toLowerCase())) {
                return card;
            }
        }
        return null;
    }

    public Card findCardById(int id) {
        for (Card card : allCards) {
            if (card.getId() == id) {
                return card;
            }
        }
        return null;
    }

    public List<Card> searchCards(String query) {
        List<Card> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Card card : allCards) {
            if (card.getName().toLowerCase().contains(lowerQuery)) {
                results.add(card);
                if (results.size() >= 20) break; // Limit results
            }
        }
        
        return results;
    }

    // New methods for library functionality
    public List<Card> getAllCards() {
        return new ArrayList<>(allCards);
    }
    
    public List<Card> getDirectFusionsForCard(int cardId) {
        List<Card> partners = new ArrayList<>();
        
        // Check fusions where this card is card1
        if (fusionsByCard.containsKey(cardId)) {
            for (FusionData fusion : fusionsByCard.get(cardId)) {
                Card partner = findCardById(fusion.card2);
                if (partner != null) {
                    partners.add(partner);
                }
            }
        }
        
        // Check fusions where this card is card2
        for (Map.Entry<Integer, List<FusionData>> entry : fusionsByCard.entrySet()) {
            for (FusionData fusion : entry.getValue()) {
                if (fusion.card2 == cardId) {
                    Card partner = findCardById(fusion.card1);
                    if (partner != null && !partners.contains(partner)) {
                        partners.add(partner);
                    }
                }
            }
        }
        
        return partners;
    }
    
    public Card getFusionResult(int card1Id, int card2Id) {
        Integer resultId = checkFusion(card1Id, card2Id);
        if (resultId != null) {
            return findCardById(resultId);
        }
        return null;
    }
    
    public List<CardFusionInfo> getFusionsUsingCard(int cardId) {
        List<CardFusionInfo> fusions = new ArrayList<>();
        
        // Find all fusions where this card is used as material
        for (Map.Entry<Integer, List<FusionData>> entry : fusionsByCard.entrySet()) {
            for (FusionData fusion : entry.getValue()) {
                if (fusion.card1 == cardId || fusion.card2 == cardId) {
                    Card card1 = findCardById(fusion.card1);
                    Card card2 = findCardById(fusion.card2);
                    Card result = findCardById(fusion.result);
                    
                    if (card1 != null && card2 != null && result != null) {
                        String description = card1.getName() + " + " + card2.getName() + " = " + result.getName();
                        fusions.add(new CardFusionInfo(
                            CardFusionInfo.FusionType.AS_MATERIAL,
                            description,
                            card1, card2, result
                        ));
                    }
                }
            }
        }
        
        return fusions;
    }
    
    public List<CardFusionInfo> getFusionsResultingIn(int cardId) {
        List<CardFusionInfo> fusions = new ArrayList<>();
        
        // Find all fusions where this card is the result
        for (Map.Entry<Integer, List<FusionData>> entry : fusionsByCard.entrySet()) {
            for (FusionData fusion : entry.getValue()) {
                if (fusion.result == cardId) {
                    Card card1 = findCardById(fusion.card1);
                    Card card2 = findCardById(fusion.card2);
                    Card result = findCardById(fusion.result);
                    
                    if (card1 != null && card2 != null && result != null) {
                        String description = card1.getName() + " + " + card2.getName() + " = " + result.getName();
                        fusions.add(new CardFusionInfo(
                            CardFusionInfo.FusionType.AS_RESULT,
                            description,
                            card1, card2, result
                        ));
                    }
                }
            }
        }
        
        return fusions;
    }

    // Simple data class to hold fusion information
    private static class FusionData {
        public int card1;
        public int card2;
        public int result;

        public FusionData(int card1, int card2, int result) {
            this.card1 = card1;
            this.card2 = card2;
            this.result = result;
        }
    }

    public static class FusionResult {
        private Card material1;
        private Card material2;
        private Card result;
        private int position1;
        private int position2;
        private FusionType type;
        
        // Additional fields for chained fusions
        private FusionResult prerequisiteFusion;
        private Card additionalCard;
        private int additionalCardPosition;

        // Constructor for direct fusions
        public FusionResult(Card material1, Card material2, Card result, int position1, int position2, FusionType type) {
            this.material1 = material1;
            this.material2 = material2;
            this.result = result;
            this.position1 = position1;
            this.position2 = position2;
            this.type = type;
        }

        // Constructor for chained fusions
        public FusionResult(FusionResult prerequisiteFusion, Card additionalCard, Card finalResult, int additionalCardPosition, FusionType type) {
            this.prerequisiteFusion = prerequisiteFusion;
            this.additionalCard = additionalCard;
            this.result = finalResult;
            this.additionalCardPosition = additionalCardPosition;
            this.type = type;
            
            // For chained fusions, material1 and material2 represent the prerequisite fusion
            this.material1 = prerequisiteFusion.material1;
            this.material2 = prerequisiteFusion.material2;
            this.position1 = prerequisiteFusion.position1;
            this.position2 = prerequisiteFusion.position2;
        }

        public Card getMaterial1() { return material1; }
        public Card getMaterial2() { return material2; }
        public Card getResult() { return result; }
        public int getPosition1() { return position1; }
        public int getPosition2() { return position2; }
        public FusionType getType() { return type; }
        
        // Additional getters for chained fusions
        public FusionResult getPrerequisiteFusion() { return prerequisiteFusion; }
        public Card getAdditionalCard() { return additionalCard; }
        public int getAdditionalCardPosition() { return additionalCardPosition; }
        
        // Helper method to get all involved card positions
        public List<Integer> getAllInvolvedPositions() {
            List<Integer> positions = new ArrayList<>();
            if (type == FusionType.DIRECT) {
                positions.add(position1);
                positions.add(position2);
            } else if (type == FusionType.CHAINED) {
                positions.add(position1);
                positions.add(position2);
                positions.add(additionalCardPosition);
            }
            return positions;
        }
        
        // Helper method to get all involved hand positions only
        public List<Integer> getAllInvolvedHandPositions() {
            List<Integer> positions = new ArrayList<>();
            if (type == FusionType.DIRECT) {
                positions.add(position1);
                positions.add(position2);
            } else if (type == FusionType.CHAINED) {
                positions.add(position1);
                positions.add(position2);
                positions.add(additionalCardPosition);
            } else if (type == FusionType.FIELD_DIRECT) {
                positions.add(position2); // Only hand position
            } else if (type == FusionType.FIELD_CHAINED) {
                positions.add(position2); // Hand position from prerequisite
                positions.add(additionalCardPosition); // Additional hand position
            }
            return positions;
        }
        
        // Helper method to get field position (if any)
        public Integer getFieldPosition() {
            if (type == FusionType.FIELD_DIRECT || type == FusionType.FIELD_CHAINED) {
                return position1; // Field position
            }
            return null;
        }
        
        // Helper method to check if this fusion involves field cards
        public boolean involvesFieldCard() {
            return type == FusionType.FIELD_DIRECT || type == FusionType.FIELD_CHAINED;
        }
    }

    // Enum to distinguish between direct and chained fusions
    public enum FusionType {
        DIRECT,
        CHAINED,
        FIELD_DIRECT,
        FIELD_CHAINED
    }
} 