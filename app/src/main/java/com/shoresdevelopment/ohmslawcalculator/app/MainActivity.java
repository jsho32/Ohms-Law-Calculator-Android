package com.shoresdevelopment.ohmslawcalculator.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.vending.billing.IInAppBillingService;
import com.shoresdevelopment.util.IabHelper;
import com.shoresdevelopment.util.IabResult;
import com.shoresdevelopment.util.Inventory;
import com.shoresdevelopment.util.Purchase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private EditText volts, amps, ohms, watts;
    private List<EditText> editTexts;
    private Dialog dialog;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private LicenseKey licenseKey;
    private static final String TAG = "com.shodev.ohmslawcalc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeDialog();
        initializeEditTexts();
        initializeButtons();

        licenseKey = new LicenseKey();

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    /** When activity is destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == Activity.RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    String token = jo.getString("purchaseToken");
                    dialog.setTitle(getResources().getText(R.string.thanks_title));
                    TextView description = (TextView) dialog.findViewById(R.id.alert_description);
                    description.setText(getResources().getText(R.string.thanks_description));
                    Button close = (Button) dialog.findViewById(R.id.alert_close);
                    close.setText(getResources().getText(R.string.thanks_close));
                    dialog.show();
                    findViewById(R.id.donate_container).setVisibility(View.GONE);
                    try {
                        mService.consumePurchase(3, MainActivity.this.getPackageName(), token);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                catch (JSONException e) {
                    Toast.makeText(this, "Failed to parse purchase data.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    /** Initializes alert dialog */
    private void initializeDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.alert_pop_up);
        dialog.setTitle(getResources().getText(R.string.alert_title));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialog.setTitle(getResources().getText(R.string.alert_title));
                TextView description = (TextView) dialog.findViewById(R.id.alert_description);
                description.setText(getResources().getText(R.string.alert_description));
                Button close = (Button) dialog.findViewById(R.id.alert_close);
                close.setText(getResources().getText(R.string.alert_close));
            }
        });

        Button alertClose = (Button) dialog.findViewById(R.id.alert_close);
        alertClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

            }
        });
    }

    /** Initializes the edit text fields */
    private void initializeEditTexts() {
        editTexts = new ArrayList<>();

        volts = (EditText) findViewById(R.id.volts_value);
        amps = (EditText) findViewById(R.id.amps_value);
        ohms = (EditText) findViewById(R.id.ohms_value);
        watts = (EditText) findViewById(R.id.watts_value);

        editTexts.add(volts);
        editTexts.add(amps);
        editTexts.add(ohms);
        editTexts.add(watts);
    }

    /** Initializes all buttons */
    private void initializeButtons() {
        Button calculate = (Button) findViewById(R.id.calculate);
        Button reset = (Button) findViewById(R.id.reset);
        Button help = (Button) findViewById(R.id.help);
        Button developer = (Button) findViewById(R.id.developer);
        Button donate = (Button) findViewById(R.id.donate);

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidEntries()) {
                    calculate();
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (EditText text : editTexts) {
                    text.setText("0");
                }
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setTitle(getResources().getText(R.string.help_title));
                TextView description = (TextView) dialog.findViewById(R.id.alert_description);
                description.setText(getResources().getText(R.string.help_description));
                dialog.show();
            }
        });

        developer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/developer?id=J.+Shores+Development");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.donate_container).setVisibility(View.VISIBLE);
                Button close = (Button) findViewById(R.id.close_donation);
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        findViewById(R.id.donate_container).setVisibility(View.GONE);
                    }
                });
                populateListView();
            }
        });
    }

    /** Checks that the proper number of fields have been entered and displays error messages */
    private boolean isValidEntries() {
        int enteredCount = 0;
        for (EditText text : editTexts) {

            if (!text.getText().toString().equals("") && !text.getText().toString().equals("0")) {
                enteredCount += 1;
            }

        }

        if (enteredCount < 2 || enteredCount > 2) {

            if (!dialog.isShowing()) {
                dialog.show();
            }

            return false;
        }

        return true;
    }

    /** Calculates all fields */
    private void calculate() {
        double value1 = 0;
        double value2 = 0;
        int index1 = -1;
        int index2 = -1;
        int count = 0;

        for (EditText text : editTexts) {
            if (!text.getText().toString().equals("") && !text.getText().toString().equals("0")) {
                if (value1 == 0) {
                    value1 = Double.parseDouble(text.getText().toString());
                    index1 = count;
                } else {
                    value2 = Double.parseDouble(text.getText().toString());
                    index2 = count;
                }
            }
            count++;
        }

        if (index1 == 0 && index2 == 1) {
            double ohmsValue = value1 / value2;
            double wattsValue = value1 * value2;
            ohms.setText(String.valueOf(ohmsValue));
            watts.setText(String.valueOf(wattsValue));
        } else if (index1 == 0 && index2 == 2) {
            double ampsValue = value1 / value2;
            double wattsValue = (Math.pow(value1, 2)) / value2;
            amps.setText(String.valueOf(ampsValue));
            watts.setText(String.valueOf(wattsValue));
        } else if (index1 == 0 && index2 == 3) {
            double ampsValue = value2 / value1;
            double ohmsValue = (Math.pow(value1, 2)) / value2;
            amps.setText(String.valueOf(ampsValue));
            ohms.setText(String.valueOf(ohmsValue));
        } else if (index1 == 1 && index2 == 2) {
            double voltsValue = value1 * value2;
            double wattsValue = (Math.pow(value1, 2)) * value2;
            volts.setText(String.valueOf(voltsValue));
            watts.setText(String.valueOf(wattsValue));
        } else if (index1 == 1 && index2 == 3) {
            double voltsValue = value2 / value1;
            double ohmsValue =  value2 / (Math.pow(value1, 2));
            volts.setText(String.valueOf(voltsValue));
            ohms.setText(String.valueOf(ohmsValue));
        } else if (index1 == 2 && index2 == 3) {
            double voltsValue = Math.sqrt(value1 * value2);
            double ampsValue = Math.sqrt(value2 / value1);
            volts.setText(String.valueOf(voltsValue));
            amps.setText(String.valueOf(ampsValue));
        }
    }

    /** Populates the donate listview. */
    public void populateListView() {
        ListView listView = (ListView) findViewById(R.id.donateList);

        List<DonateListItem> listItems = getListItems();
        DonateListItem listContents[] = new DonateListItem[listItems.size()];

        listItems.toArray(listContents);
        listView.setAdapter(getListAdapter(listContents));
    }

    /** Creates the list of store list items */
    private List<DonateListItem> getListItems() {
        List<DonateListItem> listItems = new ArrayList<>();
        ArrayList<String> skuList = new ArrayList<>();
        skuList.add(licenseKey.getSmallDonation());
        skuList.add(licenseKey.getMediumDonation());
        skuList.add(licenseKey.getLargeDonation());
        //skuList.add("android.test.purchased");
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        try {
            Bundle skuDetails = mService.getSkuDetails(3, MainActivity.this.getPackageName(), "inapp", querySkus);

            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                for (String thisResponse : responseList) {
                    JSONObject object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    String price = object.getString("price");
                    String title = object.getString("title").replace("(Ohms Law Calculator)", "");
                    String description = object.getString("description");

                    DonateListItem item = new DonateListItem(title, description, sku, price);
                    listItems.add(item);

                }
            }
        } catch (RemoteException | JSONException e) {
            e.printStackTrace();
        }

        return listItems;

    }

    /** Returns adapter for store list view */
    private ArrayAdapter<DonateListItem> getListAdapter(final DonateListItem item[]) {

        return new ArrayAdapter<DonateListItem>(MainActivity.this, R.layout.donate_list_item, item) {

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.donate_list_item, null);
                }

                final TextView description = (TextView) convertView.findViewById(R.id.description);
                description.setText(item[position].getDescription());
                final TextView title = (TextView) convertView.findViewById(R.id.title);
                title.setText(item[position].getTitle());

                Button purchase = (Button) convertView.findViewById(R.id.purchase);
                purchase.setText(item[position].getPrice());
                purchase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Bundle buyIntentBundle = mService.getBuyIntent(3, MainActivity.this.getPackageName(),
                                    item[position].getITEM_SKU(), "inapp", "");

                            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                            MainActivity.this.startIntentSenderForResult(pendingIntent.getIntentSender(),
                                    1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                    Integer.valueOf(0));
                        } catch (RemoteException | IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                });

                return convertView;
            }
        };
    }
}