package com.semidigit.qrcodeplayarena;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.hoin.usbsdk.UsbController;
import com.semidigit.qrcodeplayarena.Utils.Constants;
import com.semidigit.qrcodeplayarena.Utils.HttpConnectionService;
import com.semidigit.qrcodeplayarena.Utils.UtilityMethods;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;
import java.util.HashMap;

import static com.semidigit.qrcodeplayarena.Utils.Constants.ALIGN_CENTER;
import static com.semidigit.qrcodeplayarena.Utils.Constants.CHECKIN_API_PATH;
import static com.semidigit.qrcodeplayarena.Utils.Constants.RESET_PRINTER;
import static com.semidigit.qrcodeplayarena.Utils.Constants.bb;
import static com.semidigit.qrcodeplayarena.Utils.Constants.bb2;
import static com.semidigit.qrcodeplayarena.Utils.Constants.cc;

/**
 * Created by rishabhk on 15/04/18.
 */

public class DisplayQRActivityUSB extends AppCompatActivity {


    private static final String ACTIVITY_LOG_TAG = ".DisplayQRActivityUSB";
    String vehicle_no="";
    UtilityMethods utilityMethods;
    private PrintBilltask printBillTask = null;
    private CheckinEntry checkinEntry = null;

    String current_timestamp;
    String qr_data;
    String username, company_id;

    UsbController usbCtrl = null;
    UsbDevice dev = null;

    UtilityMethods util;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_qr_usb);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        usbCtrl = new UsbController(this,mHandler);

        utilityMethods =  new UtilityMethods(this);
        username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
        company_id = PreferenceManager.getDefaultSharedPreferences(this).getString("company_id", "");

        util = new UtilityMethods(this);
        setupUSBPrinter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        usbCtrl.close();
        usbCtrl = null;
    }

    public boolean CheckUsbPermission(){
        if( dev != null ){
            if( usbCtrl.isHasPermission(dev)){
                return true;
            }
        }
        return false;
    }

    public void setupUSBPrinter(){

        byte isHasPaper;
        byte[] cmd = null;
        usbCtrl.close();
        int  i = 0;
        for( i = 0 ; i < 5 ; i++ ){
            dev = usbCtrl.getDev(Constants.u_infor[i][0],Constants.u_infor[i][1]);
            if(dev != null)
                break;
        }
        if( dev != null ){
            if( !(usbCtrl.isHasPermission(dev))){
                usbCtrl.getPermission(dev);
            }else{
                isHasPaper = usbCtrl.revByte(dev);
                if( isHasPaper == 0x38 ){
                    Toast.makeText(this, "The printer has no paper",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbController.USB_CONNECTED:
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_logout:
                PreferenceManager.getDefaultSharedPreferences(this).edit().remove("username").commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().remove("company_id").commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().remove("password").commit();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void printTicket(View v){
        setupUSBPrinter();
        generate_qr();
        if( CheckUsbPermission() == true ){
            checkinEntry = new CheckinEntry(this, qr_data,username, company_id , vehicle_no);
            checkinEntry.execute((Void) null);
        }else{
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Error connecting to USB printer. Try restarting the application", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    public void generate_qr() {
        Date dateCur = new Date();
        current_timestamp = util.displayDate(dateCur);
        qr_data = String.valueOf(dateCur.getTime());

    }

    public void playBeepSound(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class CheckinEntry extends AsyncTask<Void, Void, JSONObject> {

        Context context;
        private ProgressDialog pdia;
        String in_time, in_id, company_id, vehicle_no;

        CheckinEntry(Context context, String in_time, String in_id, String company_id, String vehicle_no) {
            this.context = context;
            this.in_time = in_time;
            this.in_id = in_id;
            this.company_id = company_id;
            this.vehicle_no=vehicle_no;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Syncing with server...");
            pdia.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            HashMap<String, String> postDataParams;
            postDataParams = new HashMap<String, String>();
            postDataParams.put("HTTP_ACCEPT", "application/json");
            postDataParams.put("in_time", in_time);
            postDataParams.put("in_id", in_id);
            postDataParams.put("company_id", company_id);
            postDataParams.put("vehicle_no", vehicle_no);

            HttpConnectionService service = new HttpConnectionService();
            String response = service.sendRequest(CHECKIN_API_PATH, postDataParams);
            JSONObject resultJsonObject = null;
            try {
                resultJsonObject = new JSONObject(response);
                return resultJsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultJsonObject;
        }
        // TODO: register the new account here

        @Override
        protected void onPostExecute(final JSONObject resultJsonObject) {
            checkinEntry = null;
            if(pdia!=null)
                pdia.dismiss();
            int responseCode=1;

            try {
                responseCode = utilityMethods.getValueOrDefaultInt(resultJsonObject.get("responseCode"),1);
            } catch (JSONException e) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            if (responseCode==1){
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Task Failed. Something went wrong. Try again!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            else{
                try {
                    String invoice_id = utilityMethods.getValueOrDefaultString(resultJsonObject.get("invoice_id"),"NA");
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Data synced with server", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    printBillTask = new PrintBilltask(context, invoice_id, vehicle_no);
                    printBillTask.execute((Void) null);
                } catch (JSONException e) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Unexpected response. Try again", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            printBillTask = null;
        }

    }


    class PrintBilltask extends AsyncTask<Void, Void, Boolean> {

        Context context;
        private ProgressDialog pdia;
        String vehicle_no, invoice_id;

        PrintBilltask(Context context, String invoice_id, String vehicle_no) {
            this.context = context;
            this.vehicle_no=vehicle_no;
            this.invoice_id=invoice_id;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pdia = new ProgressDialog(context);
            pdia.setMessage("Printing Ticket ...");
            pdia.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            usbCtrl.sendByte(RESET_PRINTER, dev);
            usbCtrl.sendByte(bb2, dev);
            usbCtrl.sendByte(ALIGN_CENTER, dev);
            usbCtrl.sendMsg("Play", "GBK", dev);
            usbCtrl.sendByte(cc, dev);
            usbCtrl.sendMsg("Sarjapur Road", "GBK", dev);
            usbCtrl.sendMsg("==============================", "GBK", dev);
            usbCtrl.sendByte(bb, dev);
            usbCtrl.sendMsg(invoice_id, "GBK", dev);
            usbCtrl.sendMsg(current_timestamp, "GBK", dev);

            byte[] cmd;
            cmd = new byte[7];
            cmd[0] = 0x1B;
            cmd[1] = 0x5A;
            cmd[2] = 0x00;
            cmd[3] = 0x02;
            cmd[4] = 0x07;
            cmd[5] = 0x0D;
            cmd[6] = 0x00;
            usbCtrl.sendByte(cmd, dev);
            usbCtrl.sendByte(qr_data.getBytes(), dev);
            usbCtrl.sendByte(cc, dev);
            usbCtrl.sendMsg(Constants.FOOTER_MSG_TICKET, "GBK", dev);
            usbCtrl.sendMsg("\n", "GBK", dev);
            usbCtrl.sendMsg("\n", "GBK", dev);
            usbCtrl.sendMsg("\n", "GBK", dev);
            usbCtrl.sendMsg("\n", "GBK", dev);
            usbCtrl.sendByte(RESET_PRINTER, dev);
            byte[] bb3 = new byte[]{0x1D, 0x56, 0x00, 0x48};
            usbCtrl.sendByte(bb3,dev);
            return null;
        }
        // TODO: register the new account here

        @Override
        protected void onPostExecute(final Boolean success) {
            printBillTask = null;
            pdia.dismiss();
            playBeepSound();
        }

        @Override
        protected void onCancelled() {
            printBillTask = null;
        }

    }
}
