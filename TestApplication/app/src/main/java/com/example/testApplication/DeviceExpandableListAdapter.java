package com.example.testApplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceExpandableListAdapter extends BaseExpandableListAdapter {

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private final String LIST_VALUE = "VALUE";

    private ArrayList<HashMap<String, String>> serviceList;
    private ArrayList<ArrayList<HashMap<String, String>>> characteristicList;
    private LayoutInflater inflater;
    private ViewHolder viewHolder;

    public DeviceExpandableListAdapter(Context context, ArrayList<HashMap<String, String>> serviceList,
                                       ArrayList<ArrayList<HashMap<String, String>>> characteristicList) {
        super();

        this.inflater = LayoutInflater.from(context);
        this.serviceList = serviceList;
        this.characteristicList = characteristicList;
    }

    @Override
    public HashMap<String, String> getGroup(int groupPosition) {
        return serviceList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return serviceList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.list_service_row, parent, false);
            viewHolder.serviceNameTextView = view.findViewById(R.id.service_textView);
            view.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) view.getTag();

        viewHolder.serviceNameTextView.setText(getGroup(groupPosition).get(LIST_NAME));

        // 그룹이 열리고 닫힐 때 처리
//        if (isExpanded) {
//
//        } else {
//
//        }
        return view;
    }

    @Override
    public HashMap<String, String> getChild(int groupPosition, int childPosition) {
        return characteristicList.get(groupPosition).get(childPosition);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return characteristicList.get(groupPosition).size();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.list_service_row, null);
            viewHolder.characteristicNameTextView = view.findViewById(R.id.characteristic_name_textView);
            viewHolder.characteristicUUIDTextView = view.findViewById(R.id.characteristic_uuid_textView);
            viewHolder.characteristicValueTextView = view.findViewById(R.id.characteristic_value_textView);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.characteristicNameTextView.setText(getChild(groupPosition, childPosition).get(LIST_NAME));
        viewHolder.characteristicUUIDTextView.setText(getChild(groupPosition, childPosition).get(LIST_UUID));
        if (getChild(groupPosition, childPosition).containsKey(LIST_VALUE)) {
            viewHolder.characteristicValueTextView.setText(getChild(groupPosition, childPosition).get(LIST_VALUE));
            viewHolder.characteristicValueTextView.setVisibility(View.VISIBLE);
        } else
            viewHolder.characteristicValueTextView.setVisibility(View.GONE);

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class ViewHolder {
        public TextView serviceNameTextView;
        public TextView characteristicNameTextView;
        public TextView characteristicUUIDTextView;
        public TextView characteristicValueTextView;
    }
}
