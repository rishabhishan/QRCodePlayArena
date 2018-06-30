package com.semidigit.qrcodeplayarena.Utils;

public class Constants {

    private static final String ROOT_DOMAIN = "http://semidigit.com/boom/android/";

    public static final String LOG_BASE_TAG = "QR Code Gen";
    public static final String LOGIN_API_PATH = ROOT_DOMAIN + "logincheck.php";
    public static final String CHECKOUT_API_PATH = ROOT_DOMAIN + "checkout_entry.php";
    public static final String LOST_TICKET_ENTRY_API_PATH = ROOT_DOMAIN + "lost_tikcet_entry.php";
    public static final String CALCULATE_BILL_API_PATH = ROOT_DOMAIN + "%s/calculate_bill.php";
    public static final String TOTAL_COLLECTION = ROOT_DOMAIN + "daily_collection.php";
    public static final String LOST_TICKET_API_PATH = ROOT_DOMAIN + "%s/lost_ticket_bill_calculate.php";
    public static final String BLUETOOTH_PRINTER_NAME = "BlueTooth Printer";
    public static final String CHECKIN_API_PATH = ROOT_DOMAIN + "checkin_entry.php";


    public static final byte[] cc = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
    public static final byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
    public static final byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
    public static final byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
    public static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0};
    public static final byte[] ALIGN_CENTER = {0x1B, 0x61, 1};
    public static final byte[] ALIGN_RIGHT = new byte[]{27, 97, 2};
    public static final byte[] RESET_PRINTER = new byte[]{0x1B, 0x40};


    public static final int REQUEST_ENABLE_BT = 3;
    public static final String DATE_FORMAT="dd MMMM HH:mm";
    public static final String RATE_PER_HOUR_FORMAT="Rs %d/hr";
    public static final String TOTAL_TIME_FORMAT="%d Hrs %d min";
    public static final String TOTAL_AMOUNT_FORMAT="Rs %s";
    public static final String DISCOUNT_FORMAT="Rs %d";

    public static final String FOOTER_MSG_TICKET="We will not be responsible for any loss/damage to your vehicle/belongings";
    public static final String FOOTER_MSG_RECEIPT="THANK YOU AND DRIVE SAFELY!";

    public static final int[][] u_infor = {
            {0x1CBE, 0x0003},
            {0x1CB0, 0x0003},
            {0x0483, 0x5740},
            {0x0493, 0x8760},
            {0x0471, 0x0055}
    };

    public static final boolean BLUETOOTH_PRINTER_AT_ENTRY = false;


}

