package com.example.budgetappattempt2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;


import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class EarningsListAdapter extends BaseExpandableListAdapter {
    private final DecimalFormat DECI_FORMAT = new DecimalFormat("0.00");
    Context context;
    List<String> header;
    Map<String, List<EarningsListObject>> map; // Header is key.

    public EarningsListAdapter(Context context, List<String> header, Map<String,
            List<EarningsListObject>> map){
        this.context = context;
        this.header = header;
        this.map = map;
    }

    @Override
    public int getGroupCount() {
        return 1; // Only one string for this table
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return map.get(header.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return header.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return map.get(header.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String title = (String) getGroup(groupPosition);
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.earnings_table_header, null);
        }
        TextView titleTextView = (TextView) convertView.findViewById(R.id.textParent);
        titleTextView.setText(title);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        EarningsListObject child = (EarningsListObject) getChild(groupPosition, childPosition);
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.table_values, null);
        }
        TextView typeTextView = (TextView) convertView.findViewById(R.id.typeTextView);
        typeTextView.setText(child.getType());
        TextView amountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
        amountTextView.setText("$" + DECI_FORMAT.format(child.getAmount()[0]));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


}
