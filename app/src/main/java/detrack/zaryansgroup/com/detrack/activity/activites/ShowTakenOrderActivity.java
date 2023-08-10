package detrack.zaryansgroup.com.detrack.activity.activites;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import detrack.zaryansgroup.com.detrack.activity.Adapter.NewOrderListAdapter;
import detrack.zaryansgroup.com.detrack.activity.Map.GPSTracker;
import detrack.zaryansgroup.com.detrack.activity.Model.DeliveryInfo;
import detrack.zaryansgroup.com.detrack.activity.SQLlite.ZEDTrackDB;
import detrack.zaryansgroup.com.detrack.activity.Service.CompanyInfoService;
import detrack.zaryansgroup.com.detrack.activity.Service.GPSService;
import detrack.zaryansgroup.com.detrack.activity.SharedPreferences.SharedPrefs;
import detrack.zaryansgroup.com.detrack.activity.utilites.ConnectionDetector;
import detrack.zaryansgroup.com.detrack.activity.utilites.SendPod;
import detrack.zaryansgroup.com.detrack.activity.utilites.Utility;
import detrack.zaryansgroup.com.detrack.R;
import detrack.zaryansgroup.com.detrack.activity.viewmodels.Response_Login_VM;
import detrack.zaryansgroup.com.detrack.activity.viewmodels.SelectProduct_viewModel;
import detrack.zaryansgroup.com.detrack.activity.viewmodels.ShownTakenOrder_ViewModel;
import timber.log.Timber;

public class ShowTakenOrderActivity extends AppCompatActivity implements TabActivity.SendMultipleSelectionEventToRuntimeActivity {

    ImageButton btnMenu;
    SharedPrefs prefs;
    TextView demotextTV, selectAllTV;
    ListView delirveryJobListView;
    ProgressBar pb;
    EditText mSearchEt;
//    ZEDTrackDB db;
    TextView actionbar;
    String query;
    static ArrayList<DeliveryInfo> list;
    private static NewOrderListAdapter listAdapter;
    ArrayList<DeliveryInfo> filteredList;
    LinearLayout multipleSelectionLL;
    ImageView cancelIV, deleteIV, sendIV;
    Handler updateBarHandler;
    CharSequence optionlist[] = {"POB/POD", "Select Multiple","Delivered", "Cancel"};
    String getTitle = "" , syncedOrder = "";
    AlarmManager alarm;
    PendingIntent pintent;
    GPSTracker gps;
    ShownTakenOrder_ViewModel shownTakenOrder_viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_taken_order);


        setUpActionBar(getSupportActionBar());
        gps = new GPSTracker(this);
        demotextTV = findViewById(R.id.demotextTV);
        InitilizeAlaram();
        selectAllTV = findViewById(R.id.selectAllTV);
        mSearchEt = findViewById(R.id.serachET1);
        pb = findViewById(R.id.progressBar);
        delirveryJobListView = findViewById(R.id.divery_job_list);
        prefs = new SharedPrefs(this);
//        db = new ZEDTrackDB(ShowTakenOrderActivity.this);
        multipleSelectionLL = findViewById(R.id.multipleSelectionLL);
        cancelIV = findViewById(R.id.cancelIV);
        deleteIV = findViewById(R.id.deleteIV);
        sendIV = findViewById(R.id.sendIV);
        updateBarHandler = new Handler();



        //init viewmodel
        shownTakenOrder_viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(ShownTakenOrder_ViewModel.class);


        RegisterObserver();

        getTitle = getIntent().getStringExtra("title");
        syncedOrder = getIntent().getStringExtra("syncedOrder");
        actionbar.setText(getTitle);
        TabActivity.setSendMultipleSelectionEventToRuntimeActivity(this);
        selectAllTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectAllTV.getText().toString().equals("Select All")) {
                    selectAllTV.setText("UnSelect All");
                    boolean isOrderSent = false;
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setIsCBChecked(true);
                        if (list.get(i).getIsPod_sync().equals("2"))
                            isOrderSent = true;
                    }
                    if (isOrderSent)
                        sendIV.setVisibility(View.GONE);
                    else
                        sendIV.setVisibility(View.VISIBLE);
                } else {

                    selectAllTV.setText("Select All");
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setIsCBChecked(false);
                    }
                    sendIV.setVisibility(View.VISIBLE);
                }
                listAdapter.notifyDataSetChanged();

            }
        });

        delirveryJobListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckBox cb = parent.findViewById(R.id.checkBox);

                if (cb.getVisibility() == View.VISIBLE) {
                    if (list.get(position).isCBChecked()) {
                        list.get(position).setIsCBChecked(false);
                    } else {
                        list.get(position).setIsCBChecked(true);
                    }
                    boolean isOrderSent = false;
                    for (int i = 0; i < list.size(); i++)
                        if (list.get(i).getIsPod_sync().equals("2") && list.get(i).isCBChecked())
                            isOrderSent = true;

                    if (isOrderSent)
                        sendIV.setVisibility(View.GONE);
                    else
                        sendIV.setVisibility(View.VISIBLE);
                    listAdapter.notifyDataSetChanged();
                } else {

                    //here getting the position of each itemview
                    DeliveryInfo model = (DeliveryInfo) parent.getItemAtPosition(position);

                    // GrabBankIdAccording to the DeliveryId;
                    int bankId =  model.getCashDespositedBankId();

                    Log.d("Bakidishere", "=>"+bankId);


                    //sending position to POD_Dashboard Activity
                    Intent intent = new Intent(ShowTakenOrderActivity.this, POD_DashBoard.class);

                    //Sending Delievery id as well to  POD Dashboard..
                    intent.putExtra("Delivery_id", model.getDelivery_id() + "");
                    //send Bank id from here to POD_DashBoard for grabing bank reated details
                    intent.putExtra("BankId",bankId);

                    //sending Status of current item to other activity...
                    intent.putExtra("IsNew", "True");
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
            }
        });


        delirveryJobListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Select_Action_diloag(pos);
                return true;
            }
        });


        cancelIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multipleSelectionLL.setVisibility(View.GONE);
                mSearchEt.setVisibility(View.VISIBLE);
                listAdapter.isChecked(false);
                listAdapter.notifyDataSetChanged();
            }
        });
        deleteIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOrederDiloag();
            }
        });

        sendIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelect = false;
                for (int i = 0; i < list.size(); i++)
                    if (list.get(i).isCBChecked()) {
                        isSelect = true;
                        break;
                    }
                if (isSelect)
                    saveOrdertoServerDiloag();
                else
                    Utility.Toast(ShowTakenOrderActivity.this, "Select order first");


            }
        });
        String searchText = mSearchEt.getText().toString();
        mSearchEt.setText(searchText);
        openSearchBar();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.actionRegNewCustomer:{
                Intent intent = new Intent(this, NewUserActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            }
            case R.id.actionSyncCompanyCustomerInfo:{
                if (ConnectionDetector.isConnectingToInternet(this)) {
                    startService(new Intent(this, CompanyInfoService.class));
                    Utility.Toast(this, "Syncing Started...");
                } else {
                    Utility.Toast(this, "Check network connection and try again");
                }
                break;
            }
            case R.id.actionAddSalesOrder:{
                Intent intent = new Intent(this, TakeOrder.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            }
            case R.id.actionSettings:{
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            }
            case R.id.actionAddSalesReturn:{

                Intent intent = new Intent(this, ReturnOrderSearchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;
            }
            case R.id.enableLocation:{
                if(item.getTitle().toString().equals("Enable Location")){
                    SpannableString spanString = new SpannableString("Disable Location");
                    spanString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, spanString.length(), 0); //fix the color to white
                    item.setTitle(spanString);

                    item.setTitle("Disable Location");
                    GPSTracker gps = new GPSTracker(this);
                    if (ConnectionDetector.isConnectingToInternet(this)) {
                        if (gps.canGetLocation()) {
                            Utility.Toast(this, "Location Enable Successfully");
                            startservice();
                        } else {
                            Utility.Toast(this, "Enable your GPS first and try again..");
                            //gps.showSettingsAlert();
                        }
                    } else
                        Utility.Toast(this, "Check network connection and try again");
                    break;
                }
                else{
                    SpannableString spanString = new SpannableString("Enable Location");
                    spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
                    item.setTitle(spanString);

                    Utility.Toast(this, "Location Disable Successfully");
                    stopservice();
                    break;
                }
            }
            case R.id.actionAboutUs:{
                Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.aboutus_custom_dialog);
                TextView tvAppVersion = dialog.findViewById(R.id.tvAppVersion);
                tvAppVersion.setText("version"+WelcomeActivity.versionName);
                dialog.show();
                break;
            }
            case R.id.actionUserInfo:{
                Utility.userInfoDialog(this);
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    private void startservice() {
        Calendar cal = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1 * 10 * 1000, pintent);
    }

    private void stopservice() {
        alarm.cancel(pintent);
    }
    private void InitilizeAlaram() {
        Intent intent = new Intent(ShowTakenOrderActivity.this, GPSService.class);
        pintent = PendingIntent.getService(ShowTakenOrderActivity.this, 0, intent, 0);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }
    @Override
    protected void onResume() {
        mSearchEt.setVisibility(View.VISIBLE);
        multipleSelectionLL.setVisibility(View.GONE);
       /* String searchText = mSearchEt.getText().toString();
        if (!searchText.matches("")) {
            mSearchEt.setText(searchText);
            openSearchBar();
        }*/

        if (syncedOrder.equals("yes")) {
            getSyncOrderExecute();
        } else {
            Fill_listtView_Execute();
        }
        super.onResume();
    }

    private void getSyncOrderExecute() {
        String date = Utility.getCurrentDate();
        if(!WelcomeActivity.showHistory){
//            list = db.getOrderDeliveryStatusDeliveredAndSynced("");
            shownTakenOrder_viewModel.getOrderDeliveryStatusDeliveredAndSynced("");
        }
        else{
//            list = db.getOrderDeliveryStatusDeliveredAndSynced(date);
            shownTakenOrder_viewModel.getOrderDeliveryStatusDeliveredAndSynced(date);
        }
    }

    private void Fill_listtView_Execute() {
        list = new ArrayList<>();
        if (getTitle.equals("Returned Order")){
//            list = db.getOrderDelivery("Returned","");
            shownTakenOrder_viewModel.getOrderDelivery("Returned","");
        }
        else if(WelcomeActivity.bookingFlag && !WelcomeActivity.showHistory){
//            list = db.getOrderDelivery("Booking","");
            shownTakenOrder_viewModel.getOrderDelivery("Booking","");
        }
        else if (WelcomeActivity.bookingFlag && WelcomeActivity.showHistory){
//            list = db.getOrderDelivery("Booking",Utility.getCurrentDate());
            shownTakenOrder_viewModel.getOrderDelivery("Booking",Utility.getCurrentDate());
        }

        else if (!WelcomeActivity.bookingFlag && !WelcomeActivity.showHistory){
//            list = db.getOrderDelivery("Delivered","");
            shownTakenOrder_viewModel.getOrderDelivery("Delivered","");
        }
        else if (!WelcomeActivity.bookingFlag && WelcomeActivity.showHistory){
//            list = db.getOrderDelivery("Delivered",Utility.getCurrentDate());
            shownTakenOrder_viewModel.getOrderDelivery("Delivered",Utility.getCurrentDate());
        }
    }

    private void RegisterObserver() {

        shownTakenOrder_viewModel.getOrderDelivery().observe(this, new Observer<ArrayList<DeliveryInfo>>() {
            @Override
            public void onChanged(ArrayList<DeliveryInfo> deliveryInfos) {
                list = deliveryInfos;
                if (syncedOrder.equals("yes")){
                    getSyncedOrderFromSqliteDB();
                }else {

                    Fill_listtView_From_SqliteDB();
                }

            }
        });
        shownTakenOrder_viewModel.deleteOrder().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer i) {
                if(i != -1){
                    Utility.logCatMsg(list.get(i).getDelivery_id() + "deliver id deleted");
                }else {
                    Utility.Toast(ShowTakenOrderActivity.this, "Not Deleted");
                }

            }
        });

        shownTakenOrder_viewModel.UpdateOrderStatus().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer position) {

                listAdapter.notifyDataSetChanged();

                Log.d("updatedValues", list.get(position).getDelivery_status() + "\n" +
                        list.get(position).getIsSave() + "\n" + list.get(position).getDelivery_date()+"\n"+list.get(position).getPod_lat()
                        +"\n"+list.get(position).getPod_lng());
            }
        });
    }

    private void openSearchBar() {
        mSearchEt.addTextChangedListener(new SearchWatcher());
        mSearchEt.setHint("Search....");
        mSearchEt.setHintTextColor(Color.WHITE);
    }

    @Override
    public void MultipleSelectionEventCapturedInRuntimeActivity() {
        if (list.size() > 0) {
            multipleSelectionLL.setVisibility(View.VISIBLE);
            mSearchEt.setVisibility(View.GONE);
            listAdapter.isChecked(true);
            listAdapter.notifyDataSetChanged();
        }
    }

    private class SearchWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence c, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            query = mSearchEt.getText().toString();
            Utility.logCatMsg("Text Change Listener " + query);
            filteredList = performSearch(list, query);
            listAdapter = new NewOrderListAdapter(ShowTakenOrderActivity.this, filteredList);
            delirveryJobListView.setAdapter(listAdapter);
        }
    }

    private ArrayList<DeliveryInfo> performSearch(ArrayList<DeliveryInfo> modal, String query) {
        String[] queryByWords = query.toLowerCase().split("\\s+");
        ArrayList<DeliveryInfo> filter = new ArrayList<>();
        if(modal == null) return filter;
        for (int i = 0; i < modal.size(); i++) {
            DeliveryInfo data = modal.get(i);
            String name = data.getDeliver_to_name().toLowerCase();
            Utility.logCatMsg("Search query :" + name);
            for (String word : queryByWords) {
                int numberOfMatches = queryByWords.length;
                if (name.contains(word)) {
                    numberOfMatches--;
                    Utility.logCatMsg("Match " + name + " " + word);
                } else {
                    break;
                }
                if (numberOfMatches == 0) {
                    filter.add(data);
                }
            }
        }
        return filter;
    }

    private void Fill_listtView_From_SqliteDB() {

        pb.setVisibility(View.GONE);
        Utility.logCatMsg("Feed items from DB Size " + list.size());
        if (list.size() > 0) {
            demotextTV.setVisibility(View.GONE);

            listAdapter = new NewOrderListAdapter(this, list);
            delirveryJobListView.setAdapter(listAdapter);
            delirveryJobListView.setVisibility(View.VISIBLE);
        } else {
            demotextTV.setVisibility(View.VISIBLE);
            listAdapter = new NewOrderListAdapter(this, list);
            delirveryJobListView.setAdapter(listAdapter);

        }
    }

    private void getSyncedOrderFromSqliteDB() {

//        list = new ArrayList<>();
//        String date = Utility.getCurrentDate();
//        if(!WelcomeActivity.showHistory){
//            list = db.getOrderDeliveryStatusDeliveredAndSynced("");
//        }
//        else{
//            list = db.getOrderDeliveryStatusDeliveredAndSynced(date);
//        }
        pb.setVisibility(View.GONE);
        Utility.logCatMsg("Feed items from DB Size on sync " + list.size());
        if (list.size() > 0) {
            demotextTV.setVisibility(View.GONE);

            listAdapter = new NewOrderListAdapter(this, list);
            delirveryJobListView.setAdapter(listAdapter);
            delirveryJobListView.setVisibility(View.VISIBLE);
        } else {
            demotextTV.setVisibility(View.VISIBLE);
            listAdapter = new NewOrderListAdapter(this, list);
            delirveryJobListView.setAdapter(listAdapter);

        }

    }


    private void deleteOrederDiloag() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view1= getLayoutInflater().inflate(R.layout.dialog_custom_title, null);
        TextView tvCustomTitle = view1.findViewById(R.id.tvCustomTitle);
        tvCustomTitle.setText("Delete");
        builder.setCustomTitle(view1);
        builder.setMessage("Are you sure to delete this?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isCBChecked()) {
                                shownTakenOrder_viewModel.deleteOrder(list.get(i).getDelivery_id() + "", "True",i);
//                                if (db.deleteOrder(list.get(i).getDelivery_id() + "", "True")) {
//                                    Utility.logCatMsg(list.get(i).getDelivery_id() + "deliver id deleted");
//
//                                }
//                                else {
//                                    Utility.Toast(ShowTakenOrderActivity.this, "Not Deleted");
//                                }
                            }
                        }
                        onResume();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveOrdertoServerDiloag() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowTakenOrderActivity.this);
        View view1= getLayoutInflater().inflate(R.layout.dialog_custom_title, null);
        TextView tvCustomTitle = view1.findViewById(R.id.tvCustomTitle);
        tvCustomTitle.setText("Save Order?");
        builder.setCustomTitle(view1);
        String massage = "Are you sure to Save these Orders on server..!";
        builder.setMessage(massage)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ConnectionDetector.isConnectingToInternet(ShowTakenOrderActivity.this)) {
                            DeliveryInfo deliverInfo = new DeliveryInfo();
                            ArrayList<String> deliveryIdList = new ArrayList<String>();
                            Utility.logCatMsg("List Size " + list.size());
                            for (int i = 0; i < list.size(); i++) {
                                deliverInfo = list.get(i);
                                if (deliverInfo.isCBChecked()) {
                                    deliverInfo.setRejected_Reason("");
                                    deliveryIdList.add(deliverInfo.getDelivery_id() + "");
                                    Utility.logCatMsg("Delivery ID " + deliverInfo.getDelivery_id());
                                }
                            }
                            Utility.logCatMsg("Delivery Id List Size " + deliveryIdList.size());
                            if (deliveryIdList.size() > 0) {
                                SendPod sp = new SendPod(ShowTakenOrderActivity.this, "True", deliveryIdList);
                                sp.SaveChangesToDataBase();
                            }
                            dialog.cancel();
                            onResume();
                        } else {
                            Utility.Toast(ShowTakenOrderActivity.this, "Check network connection and try again");
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void updateTakenOrderListview(DeliveryInfo deliveryInfo) {
        for (int i = 0; i < list.size(); i++) {
            if (deliveryInfo.getDelivery_id() == list.get(i).getDelivery_id()) {
                list.get(i).setDelivery_status(deliveryInfo.getDelivery_status());
                list.get(i).setIsPod_sync(deliveryInfo.getIsPod_sync());
                break;
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    private void Select_Action_diloag(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view1= getLayoutInflater().inflate(R.layout.dialog_custom_title, null);
        TextView tvCustomTitle = view1.findViewById(R.id.tvCustomTitle);
        tvCustomTitle.setText("Select Option");
        builder.setCustomTitle(view1);
        builder.setItems(optionlist, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (optionlist[item].equals("Cancel")) {
                    dialog.dismiss();
                } else if (optionlist[item].equals("POB/POD")) {
                    Intent in = new Intent(ShowTakenOrderActivity.this, AddImages.class);
                    in.putExtra("OrderId", list.get(position).getDelivery_id() + "");
                    in.putExtra("imgType", "POB");
                    startActivity(in);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    dialog.dismiss();
                } else if (optionlist[item].equals("Select Multiple")) {
                    mSearchEt.setVisibility(View.GONE);
                    multipleSelectionLL.setVisibility(View.VISIBLE);
                    listAdapter.isChecked(true);
                    listAdapter.notifyDataSetChanged();
                }
                else if(optionlist[item].equals("Delivered")){
                    if (list.get(position).getIsPod_sync().toString().equals("2")) {
                        Utility.Toast(ShowTakenOrderActivity.this, "Cannot do this action Order is sent to server");
                    } else {
                        if (list.get(position).getDelivery_status().equals("Delivered")) {
                            Utility.Toast(ShowTakenOrderActivity.this, "Order status is already delivered");
                        } else {
                            if (gps.canGetLocation()) {
                                double latitude = gps.getLatitude();
                                double longitude = gps.getLongitude();
                                if (latitude > 0.0) {

                                    list.get(position).setDelivery_status("Delivered");
                                    list.get(position).setIsSave("1");
                                    list.get(position).setRejected_Reason("");
                                    list.get(position).setRefused_Reason("");
                                    list.get(position).setCancelledReason("");
                                    list.get(position).setPod_lat(String.valueOf(latitude));
                                    list.get(position).setPod_lng(String.valueOf(longitude));
//                                    db.UpdateOrderStatus(list.get(position));
                                    shownTakenOrder_viewModel.UpdateOrderStatus(list.get(position),position);
//                                    listAdapter.notifyDataSetChanged();

                                    Log.d("updatedValues", list.get(position).getDelivery_status() + "\n" +
                                            list.get(position).getIsSave() + "\n" + list.get(position).getDelivery_date()+"\n"+list.get(position).getPod_lat()
                                            +"\n"+list.get(position).getPod_lng());

                                }
                                else {

                                    Toast.makeText(ShowTakenOrderActivity.this, "Location not found...try again", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                gps.showSettingsAlert();
                            }
                        }
                    }
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setUpActionBar(ActionBar actionBar) {

        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        View v = getLayoutInflater().inflate(R.layout.actionbar_view, mainLayout, false);
        actionbar= v.findViewById(R.id.actionBarTextView);
        btnMenu = v.findViewById(R.id.btnMenu);
        btnMenu.setVisibility(View.GONE);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.appbluegrey)));
        actionBar.setCustomView(v);
    }
}
