package com.shoresdevelopment.ohmslawcalculator.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.shoresdevelopment.util.IabHelper;
import com.shoresdevelopment.util.IabResult;
import com.shoresdevelopment.util.Inventory;
import com.shoresdevelopment.util.Purchase;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private EditText volts, amps, ohms, watts;
    private List<EditText> editTexts;
    private Dialog dialog;
    private static final String TAG = "com.shodev.ohmslawcalc";
    IabHelper mHelper;
    static final String ITEM_SKU = "com.shoresdevelopment.donate";

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
            }

        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {

            if (result.isSuccess()) {
                dialog.setTitle(getResources().getText(R.string.thanks_title));
                TextView description = (TextView) dialog.findViewById(R.id.alert_description);
                description.setText(getResources().getText(R.string.thanks_description));
                Button close = (Button) dialog.findViewById(R.id.alert_close);
                close.setText(getResources().getText(R.string.thanks_close));
                dialog.show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeDialog();
        initializeEditTexts();
        initializeButtons();

        LicenseKey licenseKey = new LicenseKey();
        String base64EncodedPublicKey = licenseKey.getLicenseKey();

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");
                }
            }
        });

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
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
                mHelper.launchPurchaseFlow(MainActivity.this, ITEM_SKU, 10001,
                        mPurchaseFinishedListener, "mypurchasetoken");
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

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener  = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),  mConsumeFinishedListener);
            }
        }
    };
}