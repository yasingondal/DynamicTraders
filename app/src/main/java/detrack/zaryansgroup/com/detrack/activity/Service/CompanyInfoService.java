package detrack.zaryansgroup.com.detrack.activity.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import detrack.zaryansgroup.com.detrack.activity.Model.BankModels.CompanyWiseBanksModel;
import detrack.zaryansgroup.com.detrack.activity.Model.CompanyRouteModel.RouteModel;
import detrack.zaryansgroup.com.detrack.activity.Model.CompanyItemsModel.DeliveryItemModel;
import detrack.zaryansgroup.com.detrack.activity.Model.Params;
import detrack.zaryansgroup.com.detrack.activity.Model.RegisterCustomerModel.RegisterdCustomerModel;
import detrack.zaryansgroup.com.detrack.activity.SQLlite.ZEDTrackDB;
import detrack.zaryansgroup.com.detrack.activity.SharedPreferences.SharedPrefs;
import detrack.zaryansgroup.com.detrack.activity.utilites.Utility;
import timber.log.Timber;

/**
 * Created by 6520 on 4/5/2016.
 */
public class CompanyInfoService extends Service {
    ZEDTrackDB db;

    SharedPrefs prefs;
    ArrayList<RegisterdCustomerModel> feedRegCustomerList;
    ArrayList<DeliveryItemModel> feedCompanyItemList;
    ArrayList<Params> paramsArrayList;
    ArrayList<RouteModel> routelist;



    //Assignment work By M.Y
    ArrayList<CompanyWiseBanksModel> feedCompanywiseBanksList;


    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Utility.logCatMsg("Syncing start..");
        db=new ZEDTrackDB(this);
        prefs = new SharedPrefs(this);

        Utility.logCatMsg("Service Started");
        paramsArrayList = new ArrayList<>();
        feedRegCustomerList=new ArrayList<RegisterdCustomerModel>();
        feedCompanyItemList=new ArrayList<DeliveryItemModel>();
        routelist=new ArrayList<>();

        //Assignment work by yaseen
        feedCompanywiseBanksList = new ArrayList<CompanyWiseBanksModel>();

        prefs = new SharedPrefs(this);
        getRegisterCustomerInfoMethod(Utility.BASE_LIVE_URL+"api/Customer/GetRegisterCompanyCustomer?Company_id=" + prefs.getCompanyID() + "&CompanySiteID=" + prefs.getCompanySiteID()
        +"&UserId="+prefs.getUserId());



        return super.onStartCommand(intent, flags, startId);

    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private void getRegisterCustomerInfoMethod(String url) {

        Timber.d("getRegisterCustomerInfoMethod is running "+url);


        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject parentObject = new JSONObject(response);
                    JSONArray Table = parentObject.getJSONArray("Table");
                    if (Table.length() > 0) {
                        parseJsonFeedCustomers(Table);
                        getCompanyRoutesMethod(Utility.BASE_LIVE_URL+"api/Company/GetRegisterCompanyRoutes?Company_id=" + prefs.getCompanyID() + "&CompanySiteId=" + prefs.getCompanySiteID()
                        +"&UserId="+prefs.getUserId());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Timber.d("Customer error is "+e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }



    public void getCompanyRoutesMethod(String url) {
        Timber.d(" getCompanyRoutesMethod : "+url);

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject parent = new JSONObject(response);
                    JSONArray tableArray = parent.getJSONArray("Table");
                    if (tableArray.length() > 0) {
                        parseJsonFeedCompanyRoute(tableArray);

                        getCompanyItemsMethod(Utility.BASE_LIVE_URL+"api/Company/GetCompanyItems?Company_id=" + prefs.getCompanyID()
                                +"&companysiteID="+prefs.getCompanySiteID());

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }



    private void getCompanyItemsMethod(String url) {

        Timber.d("getCompanyItemsMethod : "+url);

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject parentObject = new JSONObject(response);
                    JSONArray tableArray = parentObject.getJSONArray("Table");
                    if (tableArray.length() > 0) {
                        parseJsonFeedCompanyItems(tableArray);
                        getRegisterBanksInfo(Utility.BASE_LIVE_URL+ "api/company/BankDetails?Company_id=" + prefs.getCompanyID() + "&Compnay_siteId=" + prefs.getCompanySiteID());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

    }



    //Assignment Work By Muhammad Yaseen
    private void getRegisterBanksInfo(String url) {

        Timber.d(" getRegisterBanksInfo : "+url);

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject parentObject = new JSONObject(response);
                    JSONArray tableArray = parentObject.getJSONArray("Table");
                    if (tableArray.length() > 0) {
                        parseJsonFeedCompanyWiseBanks(tableArray);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

    }

    //Workk By Yaseen
    private void parseJsonFeedCompanyWiseBanks(JSONArray feedArray) {
        try {
            feedCompanywiseBanksList.clear();
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                CompanyWiseBanksModel model = new CompanyWiseBanksModel();
                model.setBankID(feedObj.getInt("BankID"));
                model.setBankName(feedObj.getString("BankName"));
                model.setBankAccountNbr(feedObj.getString("BankAccountNbr"));
                model.setBankAccountType(feedObj.getString("BankAccountType"));
                model.setAddress(feedObj.getString("Address"));
                feedCompanywiseBanksList.add(model);

            }

            if(feedCompanywiseBanksList.size()>0) {
                db.insertCompanyWiseBankDetails(feedCompanywiseBanksList);
            }

        } catch (JSONException e) {

            e.printStackTrace();
            Utility.logCatMsg("Feed Register Customer error" + e);
        }

    }


    private void parseJsonFeedCustomers(JSONArray feedArray) {
        feedRegCustomerList.clear();
        try {
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                RegisterdCustomerModel model = new RegisterdCustomerModel();

                model.setCustomer_id(feedObj.getInt("ContactId"));
                model.setName(feedObj.getString("Name"));
                model.setAddress(feedObj.getString("Address"));
                model.setAddress1(feedObj.getString("Address1"));
                model.setCell(feedObj.getString("Phone"));
                model.setPhone(feedObj.getString("Phone2"));
                model.setCity(feedObj.getString("City"));
                model.setCountry(feedObj.getString("Country"));
                model.setLat(feedObj.getString("Latitude").trim());
                model.setLng(feedObj.getString("Longitude").trim());
                model.setRoute(feedObj.getInt("RouteId"));
                model.setSalesMode(feedObj.getString("SalesMode").trim());
                model.setImageName(feedObj.getString("ImageName"));
                feedRegCustomerList.add(model);

            }

        } catch (JSONException e) {

            e.printStackTrace();
            Timber.d("Error in customer is "+e.getMessage());
            Utility.logCatMsg("Feed Register Customer error" + e);
        }

    }




    private void parseJsonFeedCompanyItems(JSONArray feedArray) {


        try {
            feedCompanyItemList.clear();
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);
                //  Utility.logCatMsg("feed array :" + feedArray);
                DeliveryItemModel model = new DeliveryItemModel();

                model.setItem_id(feedObj.getInt("Id"));
                model.setTitle(feedObj.getString("Title"));
                model.setCode(feedObj.getString("Code"));
                model.setName(feedObj.getString("Name"));
                model.setItemDetail(feedObj.getString("ItemDetail"));

                model.setTaxCode(feedObj.getString("TaxCode"));
                model.setImageName(feedObj.getString("ImageName"));

                model.setCostCtnPrice(Float.parseFloat(feedObj.getString("CostCtnPrice")));
                model.setCostPackPrice(Float.parseFloat(feedObj.getString("CostPackPrice")));
                model.setCostPiecePrice(Float.parseFloat(feedObj.getString("CostPiecePrice")));
                model.setWSCtnPrice(Float.parseFloat(feedObj.getString("WSCtnPrice")));
                model.setWSPackPrice(Float.parseFloat(feedObj.getString("WSPackPrice")));
                model.setWSPiecePrice(Float.parseFloat(feedObj.getString("WSPiecePrice")));
                model.setRetailCtnPrice(Float.parseFloat(feedObj.getString("RetailCtnPrice")));
                model.setRetailPackPrice(Float.parseFloat(feedObj.getString("RetailPackPrice")));
                model.setRetailPiecePrice(Float.parseFloat(feedObj.getString("RetailPiecePrice")));
                model.setDisplayPrice(Float.parseFloat(feedObj.getString("DisplayPrice")));
                model.setCtnSize(feedObj.getInt("CtnSize"));
                model.setPackSize(feedObj.getInt("PackSize"));

                feedCompanyItemList.add(model);

            }

        } catch (JSONException e) {

            e.printStackTrace();
            Utility.logCatMsg("Feed CompanyItems error" + e);
        }

        //offline Work for all other data if api returning more than 0...
        if (feedRegCustomerList.size() > 0) {

            db.dropRunTimeOrderTables();
            db.createRunTimeOrderTables();


            db.insertCompanyCustomer(feedRegCustomerList, "False");
            if(routelist.size()>0)
            {
                db.insertCompanyRoute(routelist);
            }


            if(feedCompanyItemList.size()>0){
                db.insertCompanyItem(feedCompanyItemList);
                Intent intent = new Intent("dataUpdated");
                Timber.d("Data Updated Successfully");
                Toast.makeText(getApplicationContext(), "Data Updated Successfully", Toast.LENGTH_SHORT).show();
                sendBroadcast(intent);
            }
        }

    }



    private void parseJsonFeedCompanyRoute(JSONArray feedArray) {
        try {
            routelist.clear();
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);
                RouteModel model = new RouteModel();
                model.setRoute_id(feedObj.getInt("Route_Id"));
                model.setRoute_code(feedObj.getString("Code"));
                model.setRoute_name(feedObj.getString("Title"));
                model.setRoute_description(feedObj.getString("Descript"));
                routelist.add(model);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Utility.logCatMsg("Feed CompanyRoute error" + e);
        }

    }
}
