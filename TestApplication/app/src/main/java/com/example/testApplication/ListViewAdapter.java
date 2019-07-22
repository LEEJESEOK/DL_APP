package com.example.testApplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter implements Filterable {

    Filter listFilter;
    private ArrayList<ListViewItem> listViewItemList;
    private ArrayList<ListViewItem> filteredItemList;

    public ListViewAdapter(ArrayList<ListViewItem> listViewItemList) {
        this.filteredItemList = this.listViewItemList = listViewItemList;
    }

    @Override
    public int getCount() {
        return filteredItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ListViewItem getItem(int position) {
        return filteredItemList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_device_row, parent, false);

        TextView titleTextView = convertView.findViewById(R.id.name);
        TextView descTextView = convertView.findViewById(R.id.address);

        ListViewItem listViewItem = filteredItemList.get(position);

        titleTextView.setText(listViewItem.getDeviceName());
        descTextView.setText(listViewItem.getDeviceAddress());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (listFilter == null)
            listFilter = new ListFilter();

        return listFilter;
    }

    private class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = listViewItemList;
                results.count = listViewItemList.size();
            } else {
                ArrayList<ListViewItem> itemList = new ArrayList<>();

                for (ListViewItem item : listViewItemList) {
                    if (item.getDeviceName().toUpperCase().contains(constraint.toString().toUpperCase()) || item.getDeviceAddress().contains(constraint.toString().toUpperCase())) {
                        itemList.add(item);
                    }
                }

                results.values = itemList;
                results.count = itemList.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItemList = (ArrayList<ListViewItem>) results.values;

            notifyDataSetChanged();
        }
    }
}
