package com.example.ygo;

public class Card {
    private int id;
    private String name;
    private String type;
    private int attack;
    private boolean isEmpty;

    public Card() {
        this.id = -1;
        this.name = "";
        this.type = "";
        this.attack = 0;
        this.isEmpty = true;
    }

    public Card(int id, String name, String type, int attack) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.attack = attack;
        this.isEmpty = false;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public void clear() {
        this.id = -1;
        this.name = "";
        this.type = "";
        this.attack = 0;
        this.isEmpty = true;
    }

    @Override
    public String toString() {
        if (isEmpty) {
            return "Empty Slot";
        }
        return name + " (" + attack + " ATK)";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return id == card.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
} 