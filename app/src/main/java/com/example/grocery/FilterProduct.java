package com.example.grocery;

import android.widget.Filter;
import android.widget.Toast;

import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.models.ModelProduct;

import java.util.ArrayList;

public class FilterProduct extends Filter {

    private AdapterProductSeller adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProduct(AdapterProductSeller adapter, ArrayList<ModelProduct> productList) {
        this.adapter = adapter;
        this.filterList = productList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // validate data for search query
        if (constraint != null && constraint.length() > 0){
            // search field not empty, searching something, perform search

            // change to upper case, to make case insensitive
            constraint = constraint.toString().toUpperCase();
            // store our filtered list
            ArrayList<ModelProduct> filterModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                // check, search by title and category
                if (filterList.get(i).getPrTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getPrCat().toUpperCase().contains(constraint)) {
                    // add filtered data to list
                    filterModels.add(filterList.get(i));
                }
            }
            results.count = filterModels.size();
            results.values = filterModels;
        } else {
            // search field empty, not searching, return original/all/complete list
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.productList = (ArrayList<ModelProduct>) results.values;
        // refresh adapter
        adapter.notifyDataSetChanged();
    }
}
