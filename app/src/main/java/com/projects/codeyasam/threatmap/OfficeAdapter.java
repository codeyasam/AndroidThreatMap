package com.projects.codeyasam.threatmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by codeyasam on 11/15/16.
 */
public class OfficeAdapter extends ArrayAdapter<Office_TM> {

    Context context;
    List<Office_TM> officeList;

    public OfficeAdapter(Context context, List<Office_TM> officeList) {
        super(context,android.R.layout.simple_list_item_1, officeList);
        this. context = context;
        this.officeList = officeList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.list_office, null);

        try {
            Office_TM officeObj = officeList.get(position);
            CYM_UTILITY.displayText(view, R.id.officeName, officeObj.getName());
            CYM_UTILITY.displayText(view, R.id.officeAddress, officeObj.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

}
