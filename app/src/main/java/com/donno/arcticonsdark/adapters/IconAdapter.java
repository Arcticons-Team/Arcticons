package com.donno.arcticonsdark.adapters;

import java.util.ArrayList;

public class IconAdapter extends ViewHolderListAdapter<String, IconViewHolder> {
    public IconAdapter(int listItemLayout) {
        super(IconViewHolder.class, listItemLayout, new ArrayList<>(0));
    }
}
