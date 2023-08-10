package detrack.zaryansgroup.com.detrack.activity.Adapter.SecondView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

import detrack.zaryansgroup.com.detrack.R;
import detrack.zaryansgroup.com.detrack.activity.Model.CompanyItemsModel.DeliveryItemModel;
import detrack.zaryansgroup.com.detrack.activity.Model.DeliveryInfo;
import detrack.zaryansgroup.com.detrack.activity.Model.OrderItemsListModel;
import detrack.zaryansgroup.com.detrack.activity.Model.RegisterCustomerModel.RegisterdCustomerModel;
import detrack.zaryansgroup.com.detrack.activity.SQLlite.ZEDTrackDB;
import detrack.zaryansgroup.com.detrack.activity.SharedPreferences.SharedPrefs;
import detrack.zaryansgroup.com.detrack.activity.activites.SecondView.SelectProductActivitySecond;
import detrack.zaryansgroup.com.detrack.activity.activites.SelectCustomerActivity;
import detrack.zaryansgroup.com.detrack.activity.utilites.Utility;
import timber.log.Timber;

public class OrderItemAdapterSecond extends RecyclerView.Adapter<OrderItemAdapterSecond.MyViewHolder> {

    Context context;
    ArrayList<DeliveryItemModel> orderList;
    String currency;

    ZEDTrackDB db;

    private OnItemClick onItemClick;
    private OnItemLongClick onItemLongClick;
    public interface OnItemClick{
        void onClick(int position, View view);
    }

    public interface OnItemLongClick{
        void onLongClick(int position, View view);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvProductName,
                tvCtvQty,
                tvCtnPrice,tvTotalAmount;
        public CardView maincard;



        public MyViewHolder(View view) {
            super(view);

            tvProductName = view.findViewById(R.id.tvProductName);
            tvCtvQty = view.findViewById(R.id.tvCtnQty);
            tvCtnPrice = view.findViewById(R.id.tvCtnPrice);
            tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
            maincard = view.findViewById(R.id.maincard);

        }
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setOnItemLongClick(OnItemLongClick onItemLongClick) {
        this.onItemLongClick = onItemLongClick;
    }

    public OrderItemAdapterSecond(Context context, ArrayList<DeliveryItemModel> orderlist ) {

        this.context = context;
        this.orderList = orderlist;
        currency =  new SharedPrefs(context).getCurrency();

        db = new ZEDTrackDB(context);

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_layout_second, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {


        DeliveryItemModel model = orderList.get(position);
        holder.tvProductName.setText(model.getName());


        holder.tvCtvQty.setText(String.valueOf(Math.abs(model.getCtn_qty())));

        holder.maincard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClick != null)
                    onItemClick.onClick(position,v);
            }
        });

        holder.maincard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onItemLongClick !=null)
                    onItemLongClick.onLongClick(position,v);
                return true;
            }
        });


        float totalAmount = 0;
        float pckqty = Float.parseFloat(String.valueOf(model.getPac_qty()));
        float pcsqty = Float.parseFloat(String.valueOf(model.getPcs_qty()));
        float ctnqty = Float.parseFloat(String.valueOf(model.getCtn_qty()));
        float pcsTotal = pcsqty * (float) model.getRetailCtnPrice();
        float pckTotal = pckqty *  model.getRetailPackPrice();
        float ctnTotal = 0;


// Work is Continued on prices...... I have changed Two Few Things You will get in History

//        if(model.getTaxCode().equalsIgnoreCase("3rd") || model.getTaxCode().equalsIgnoreCase("SR")){

       float rSpecialPrice = db.checkIfSpecialPriceExists(SelectProductActivitySecond.rCustomerId,model.getItem_id());

       if(rSpecialPrice==0.0f)
       {

           ctnTotal =  ctnqty *  model.getWSCtnPrice();
           holder.tvCtnPrice.setText(model.getWSCtnPrice()+"");

       }else {

           ctnTotal =  ctnqty *  rSpecialPrice;
           holder.tvCtnPrice.setText(rSpecialPrice+"");
       }





//        }else {
//            holder.tvCtnPrice.setText(String.valueOf((int) model.getRetailCtnPrice()));
//        }


        //This Block is running inside Add Sales Return Order...
        if (SelectCustomerActivity.addOrder.equals("false")) {

            totalAmount = (pckTotal + pcsTotal + ctnTotal) + Float.parseFloat(String.valueOf(model.getFoc_value()));

        } else {

            //This Block is running inside Add Sales Order...
            totalAmount = (pckTotal + pcsTotal + ctnTotal) - Float.parseFloat(String.valueOf(model.getFoc_value()));



        }


        model.setNetTotalRetailPrice((float) totalAmount);
        holder.tvTotalAmount.setText(Math.abs(totalAmount)+"");



    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
