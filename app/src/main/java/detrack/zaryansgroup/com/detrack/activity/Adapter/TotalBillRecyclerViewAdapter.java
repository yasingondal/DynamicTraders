package detrack.zaryansgroup.com.detrack.activity.Adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import detrack.zaryansgroup.com.detrack.activity.Model.CompanyItemsModel.DeliveryItemModel;
import detrack.zaryansgroup.com.detrack.R;
import detrack.zaryansgroup.com.detrack.activity.SQLlite.ZEDTrackDB;
import detrack.zaryansgroup.com.detrack.activity.SharedPreferences.SharedPrefs;
import detrack.zaryansgroup.com.detrack.activity.activites.SecondView.SelectProductActivitySecond;
import timber.log.Timber;

public class TotalBillRecyclerViewAdapter extends RecyclerView.Adapter<TotalBillRecyclerViewAdapter.MyViewHolder> {

    ArrayList<DeliveryItemModel> selectedItemList;
    Context context;
    LayoutInflater inflater;
    SharedPrefs sharedPrefs;
    ZEDTrackDB db;


    public TotalBillRecyclerViewAdapter(Context context, ArrayList<DeliveryItemModel> selectedItemList) {
        this.context = context;
        this.selectedItemList = selectedItemList;
        inflater = LayoutInflater.from(context);
        sharedPrefs = new SharedPrefs(context);
        db = new ZEDTrackDB(context);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.total_bill_custom_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        DeliveryItemModel model = selectedItemList.get(position);
        holder.tvItemDescription.setText(model.getName().toLowerCase());



        float rSpecialPrice = db.checkIfSpecialPriceExists(SelectProductActivitySecond.rCustomerId,model.getItem_id());




        if(sharedPrefs.getView().equalsIgnoreCase("secondView")){

            if(rSpecialPrice==0.0f)
            {
                holder.tvPeritem.setText(String.valueOf(Math.abs(model.getWSCtnPrice())));

            }else{
                holder.tvPeritem.setText(String.valueOf(rSpecialPrice));
            }


            if(model.getTaxCode().equalsIgnoreCase("3rd")){

                float tam;

                if(rSpecialPrice==0.0f)
                {
                    tam = model.getWSCtnPrice() * model.getCtn_qty();
                    holder.tvItemValue.setText(String.valueOf(Math.abs(tam)));
                    holder.tvItemnettotal.setText(String.valueOf(Math.abs(tam)));

                }else{
                    tam = rSpecialPrice * model.getCtn_qty();
                    holder.tvItemValue.setText(String.valueOf(Math.abs(tam)));
                    holder.tvItemnettotal.setText(String.valueOf(Math.abs(tam)));
                }

            }else if(model.getTaxCode().equalsIgnoreCase("SR")){


                float tam;

                if(rSpecialPrice==0.0f)
                {
                    tam = model.getWSCtnPrice() * model.getCtn_qty();
                    holder.tvItemValue.setText(String.valueOf(Math.abs(tam)));
                    holder.tvItemnettotal.setText(String.valueOf(Math.abs(tam)));

                }else{
                    tam = rSpecialPrice * model.getCtn_qty();
                    holder.tvItemValue.setText(String.valueOf(Math.abs(tam)));
                    holder.tvItemnettotal.setText(String.valueOf(Math.abs(tam)));
                }



            } else {

                float tam;

                if(rSpecialPrice==0.0f)
                {
                    tam = model.getWSCtnPrice() * model.getCtn_qty();
                    holder.tvItemValue.setText(String.valueOf(Math.abs(tam)));
                    holder.tvItemnettotal.setText(String.valueOf(Math.abs(tam)));

                }else{
                    tam = rSpecialPrice * model.getCtn_qty();
                    holder.tvItemValue.setText(String.valueOf(Math.abs(tam)));
                    holder.tvItemnettotal.setText(String.valueOf(Math.abs(tam)));
                }
            }

        }
        else {

            holder.tvPeritem.setText(String.valueOf(Math.abs(model.getRetailCtnPrice())));
            holder.tvItemValue.setText(String.valueOf(Math.abs(model.getNetTotalRetailPrice())));
            holder.tvItemnettotal.setText(String.valueOf(Math.abs(model.getNetTotalRetailPrice())));
        }


        holder.tvItemPcs.setText(String.valueOf(Math.abs(model.getPcs_qty())));

        // Controlling visibility according to company terms
        if(sharedPrefs.getIsDiscountVisible().equalsIgnoreCase("false") &&
                sharedPrefs.getIsDiscountVisible().equalsIgnoreCase("false"))
        {
            holder.tvItemGst.setVisibility(View.GONE);
            holder.tvItemdiscount.setVisibility(View.GONE);

        }




        holder.tvItemGst.setText(String.valueOf(Math.abs(model.getItemGstValue())));
        holder.tvItemdiscount.setText(String.valueOf(Math.abs(model.getItem_discount())));


        if(model.getFocType()!= null) {
            if (model.getFocType().equals("Qty")) {

                //Coverting to positive Foc type
                float tempFOCtotal = model.getPcs_qty() + model.getFoc_qty();
                holder.tvItemPcs.setText(String.valueOf(Math.abs(tempFOCtotal)));
            }
        }


        holder.tvItemPck.setText(String.valueOf(Math.abs(model.getPac_qty())));
        holder.tvItemCtn.setText(String.valueOf(Math.abs(model.getCtn_qty())));

        if(sharedPrefs.getView().equals("secondView")){
            holder.tvItemPck.setVisibility(View.GONE);
            holder.tvItemPcs.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return selectedItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvItemDescription, tvItemPcs, tvItemPck, tvItemValue,
                tvItemCtn,tvPeritem,tvItemGst,tvItemnettotal,tvItemdiscount;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvItemDescription = itemView.findViewById(R.id.tvItemDescription);
            tvItemPcs = itemView.findViewById(R.id.tvItemPcs);
            tvItemPck = itemView.findViewById(R.id.tvItemPck);
            tvItemValue = itemView.findViewById(R.id.tvItemValue);
            tvItemCtn = itemView.findViewById(R.id.tvItemCtn);
            tvPeritem = itemView.findViewById(R.id.tvPeritem);
            tvItemGst = itemView.findViewById(R.id.tvItemGst);
            tvItemnettotal = itemView.findViewById(R.id.tvItemnettotal);
            tvItemdiscount = itemView.findViewById(R.id.tvItemdiscount);
        }
    }
}
