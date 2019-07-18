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
    private static final String TAG = DeviceExpandableListAdapter.class.getSimpleName();

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
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_service_row, parent, false);
            viewHolder.serviceNameTextView = convertView.findViewById(R.id.service_textView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.serviceNameTextView.setText(getGroup(groupPosition).get(LIST_NAME));

        // 그룹이 열리고 닫힐 때 처리
//        if (isExpanded) {
//
//        } else {
//
//        }
        return convertView;
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
//        if (convertView == null) {
        convertView = inflater.inflate(R.layout.list_service_row, null);
        viewHolder.characteristicNameTextView = convertView.findViewById(R.id.characteristic_name_textView);
        viewHolder.characteristicUUIDTextView = convertView.findViewById(R.id.characteristic_uuid_textView);
        viewHolder.characteristicValueTextView = convertView.findViewById(R.id.characteristic_value_textView);
        convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }

        viewHolder.characteristicNameTextView.setText(getChild(groupPosition, childPosition).get(LIST_NAME));
        viewHolder.characteristicUUIDTextView.setText(getChild(groupPosition, childPosition).get(LIST_UUID));
        if (getChild(groupPosition, childPosition).containsKey(LIST_VALUE)) {
            viewHolder.characteristicValueTextView.setText(getChild(groupPosition, childPosition).get(LIST_VALUE));
            viewHolder.characteristicValueTextView.setVisibility(View.VISIBLE);
        } else
            viewHolder.characteristicValueTextView.setVisibility(View.GONE);

        return convertView;
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
