package detrack.zaryansgroup.com.detrack.activity.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import detrack.zaryansgroup.com.detrack.activity.Model.CompanyItemsModel.DeliveryItemModel;
import detrack.zaryansgroup.com.detrack.R;
import detrack.zaryansgroup.com.detrack.activity.SharedPreferences.SharedPrefs;

/**
 * Created by 6520 on 2/3/2016.
 */
public class ItemsDilogListAdapter extends BaseAdapter {
    Context context;
    ArrayList<DeliveryItemModel> list;
    LayoutInflater inflater;
    DeliveryItemModel model;


    public ItemsDilogListAdapter(Context context, ArrayList<DeliveryItemModel> list) {
        this.context = context;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.items_search_list,null);
        TextView item_nameTV = view.findViewById(R.id.item_name);
        TextView tvItemCode = view.findViewById(R.id.tvItemCode);
        TextView tvItemDisplayPrice = view.findViewById(R.id.tvItemDisplayPrice);
        model = list.get(position);
        item_nameTV.setText(model.getName());
        tvItemCode.setText(model.getCode());

        if(new SharedPrefs(context).getView().equalsIgnoreCase("secondView")){
            tvItemDisplayPrice.setText(String.valueOf(model.getWSCtnPrice()));
        }else {
            tvItemDisplayPrice.setText(String.valueOf(model.getRetailPiecePrice()));
        }
        tvItemDisplayPrice.setText(String.valueOf(model.getRetailPiecePrice()));
        return view;
    }
}

