package com.shoresdevelopment.ohmslawcalculator.app;

import lombok.Getter;

@Getter
public class DonateListItem {
    private String ITEM_SKU;
    private String title;
    private String description;
    private String price;

    public DonateListItem(String title, String description, String ITEM_SKU, String price) {
        this.title = title;
        this.description = description;
        this.ITEM_SKU = ITEM_SKU;
        this.price = price;
    }
}
