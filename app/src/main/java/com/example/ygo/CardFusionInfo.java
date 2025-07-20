package com.example.ygo;

public class CardFusionInfo {
    public enum FusionType {
        DIRECT_FUSION,    // This card can fuse directly with another
        AS_MATERIAL,      // This card is used as material in a fusion
        AS_RESULT         // This card is the result of a fusion
    }
    
    private FusionType type;
    private String description;
    private Card card1;
    private Card card2;
    private Card result;
    private Card material3; // For chained fusions
    
    // Constructor for direct fusions (card1 + card2 = result)
    public CardFusionInfo(FusionType type, String description, Card card1, Card card2, Card result) {
        this.type = type;
        this.description = description;
        this.card1 = card1;
        this.card2 = card2;
        this.result = result;
    }
    
    // Constructor for chained fusions (card1 + card2 + card3 = result)
    public CardFusionInfo(FusionType type, String description, Card card1, Card card2, Card material3, Card result) {
        this.type = type;
        this.description = description;
        this.card1 = card1;
        this.card2 = card2;
        this.material3 = material3;
        this.result = result;
    }
    
    // Getters
    public FusionType getType() { return type; }
    public String getDescription() { return description; }
    public Card getCard1() { return card1; }
    public Card getCard2() { return card2; }
    public Card getResult() { return result; }
    public Card getMaterial3() { return material3; }
    
    public boolean isChained() { return material3 != null; }
} 