//package com.atsoft.screenrecord.ui.utils;
//
//import android.content.Context;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Bundle;
//import android.os.UserManager;
//import android.telephony.TelephonyManager;
//
//import com.atsoft.screenrecord.App;
//
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.util.Collections;
//import java.util.List;
//
///**
// * Created by ThaoDV on 6/13/14.
// */
//public class NetworkHelper {
//    private static final String TAG = NetworkHelper.class.getSimpleName();
//    public static final int TYPE_WIFI = 1;
//    public static final int TYPE_MOBILE = 0;
//    public static final int TYPE_NOT_CONNECTED = 2;
//    private int connectedType = -1;
//    private static boolean mIsNetworkAvailable = false;
////    private static NetworkConnectivityChangeListener mNetworkConnectivityChangeListeners;
//    private static NetworkChangedCallback networkChangedCallback;
//    private static NetworkHelper instance;
//
//    private NetworkHelper() {
//
//    }
//
//    public static synchronized NetworkHelper getInstance() {
//        if (instance == null) {
//            instance = new NetworkHelper();
//        }
//        return instance;
//    }
//
//    public static synchronized void setNetworkChangedCallback(NetworkChangedCallback callback) {
//        networkChangedCallback = callback;
//    }
//
//    public static synchronized void addNetworkConnectivityChangeListener(
//            NetworkConnectivityChangeListener listener) {
//        mNetworkConnectivityChangeListeners = listener;
//    }
//
//    private static boolean isNetworkAvailable() {
//        return mIsNetworkAvailable;
//    }
//
//    private static void setNetworkAvailable(boolean isNetworkAvailable) {
//        mIsNetworkAvailable = isNetworkAvailable;
//    }
//
//    /**
//     * check connect internet
//     *
//     * @param context
//     * @return boolean
//     * @author toanvk2
//     */
//    public static boolean isConnectInternet(Context context) {
//        NetworkInfo i = null;
//        try {
//            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            i = conMgr.getActiveNetworkInfo();
//            /*final android.net.NetworkInfo wifi = conMgr
//                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//            final android.net.NetworkInfo mobile = conMgr
//                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);*/
//        } catch (Exception e) {
//            Log.e(TAG, "isConnectInternet", e);
//        }
//        if (i != null) {
//            Log.d(TAG, "isConnectInternet: " + i.isConnected() + " --- type: " + i.getType()
//                    + " *** " + i.getTypeName());
//        }
//        return i != null && i.isConnected() && i.isAvailable();
//    }
//
//    private void startLoginServiceOnNetworkChanged(Context context) {
////        if (IMService.isReady()) {
////            IMService.getInstance().connectByToken();
////        } else {
////            ApplicationController applicationController = (ApplicationController) context.getApplicationContext();
////            applicationController.startIMService();
////        }
//        App applicationController = (App) context.getApplicationContext();
//        if (applicationController.isReady()) {
//            applicationController.connectByToken();
//        } else {
//            applicationController.startIMService();
//        }
//    }
//
//    public void onNetworkConnectivityChanged(Context context) {
//        ConnectivityManager cm = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        int oldConnectedType = connectedType;
//        String state = activeNetwork == null ? "null" : activeNetwork.getState().toString();
//        if (null != activeNetwork && activeNetwork.isConnectedOrConnecting()) {
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                connectedType = TYPE_WIFI;
//                setNetworkAvailable(true);
//            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                connectedType = TYPE_MOBILE;
//                setNetworkAvailable(true);
//            } else {
//                connectedType = activeNetwork.getType();
//                setNetworkAvailable(true);
//            }
//        } else {
//            Log.i(TAG, "no activeNetwork found");
//            setNetworkAvailable(false);
//            connectedType = TYPE_NOT_CONNECTED;
//        }
//        Log.f(TAG, "onNetworkConnectivityChanged: " + connectedType + " isAvailable: " + mIsNetworkAvailable + " state: " + state);
//        if (networkChangedCallback != null) {
//            networkChangedCallback.onNetworkChanged(mIsNetworkAvailable);
//        }
//        //notify to all listeners
//        if (mNetworkConnectivityChangeListeners != null) {
//            //sau khi sua thi ko thay chay vao ham nay khi disable network
//            try {
//                mNetworkConnectivityChangeListeners.onConnectivityChanged(isNetworkAvailable(), connectedType);
//            } catch (NullPointerException ex) {
//                Log.f(TAG, "NullPointerException mNetworkConnectivityChangeListeners");
//            }
//
//        } else {
//            Log.i(TAG, "no listener for connectivity changed : " + oldConnectedType + " new = " + connectedType);
//            if (isNetworkAvailable()) {
//                startLoginServiceOnNetworkChanged(context);
//            }
//        }
//    }
//
//    public static int checkTypeConnection(Context mContext) {
//        ConnectivityManager cm = (ConnectivityManager) mContext
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        if (null != activeNetwork) {
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
//                return TYPE_WIFI;
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
//                return TYPE_MOBILE;
//        }
//        return TYPE_NOT_CONNECTED;
//    }
//
//    public static String getTextTypeConnectionForLog(Context mContext) {
//        ConnectivityManager cm = (ConnectivityManager) mContext
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        if (null != activeNetwork) {
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
//                return "wifi";
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
//                return "3G";
//        }
//        return "";
//    }
//
//    public static String getNetworkSubType(Context context) {
//        String networkType = "";
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        if (null != activeNetwork) {
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                networkType = "WIFI";
//            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                networkType = activeNetwork.getSubtypeName();
//            }
//        }
//        return networkType;
//    }
//
//    public static int getTypeConnectionForLog(Context mContext) {
//        ConnectivityManager cm = (ConnectivityManager) mContext
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        if (null != activeNetwork) {
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
//                return 2;
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
//                return 1;
//        }
//        return 0;
//    }
//
//    public String getConnectedType() {
//        switch (connectedType) {
//            case TYPE_WIFI:
//                return "WIFI";
//            case TYPE_MOBILE:
//                return "MOBILE";
//            default:
//                return "DEFAULT";
//        }
//    }
//
//    public static String getNetWorkInfo(Context mContext) {
//        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        if (null != activeNetwork) {
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
//                return "WIFI";
//            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//                switch (activeNetwork.getSubtype()) {
//                    case TelephonyManager.NETWORK_TYPE_GPRS:
//                        return "MOBILE GPRS 100kbps";
//                    case TelephonyManager.NETWORK_TYPE_EDGE:
//                        return "MOBILE EDGE 50-100kbps";
//                    case TelephonyManager.NETWORK_TYPE_UMTS:
//                        return "MOBILE UMTS 400-7000kbps";
//                    case TelephonyManager.NETWORK_TYPE_CDMA:
//                        return "MOBILE CDMA 14-64kbps";
//                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
//                        return "MOBILE EVDO 400-1000kbps";
//                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
//                        return "MOBILE EVDO_A 600-1400kbps";
//                    case TelephonyManager.NETWORK_TYPE_1xRTT:
//                        return "MOBILE 1xRTT 50-100kbps";
//                    case TelephonyManager.NETWORK_TYPE_HSDPA:
//                        return "MOBILE HSDPA 2-4Mbps";
//                    case TelephonyManager.NETWORK_TYPE_HSUPA:
//                        return "MOBILE HSUPA 1-23Mbps";
//                    case TelephonyManager.NETWORK_TYPE_HSPA:
//                        return "MOBILE HSPA 700-1700kbps";
//                    case TelephonyManager.NETWORK_TYPE_IDEN:
//                        return "MOBILE IDEN 25kbps";
//                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
//                        return "MOBILE EVDO_B 5Mbps";
//                    //
//                    case 13:// TelephonyManager.NETWORK_TYPE_LTE: // API level 11
//                        return "MOBILE LTE 10Mbps";
//                    case 14: //TelephonyManager.NETWORK_TYPE_EHRPD // API level 11
//                        return "MOBILE EHRPD 1-2Mbps";
//                    case 15: //TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
//                        return "MOBILE HSPAP 10-20Mbps";
//                    // Unknown
//                    default:
//                        return "MOBILE unknown";
//                }
//            } else {
//                return "unknown";
//            }
//        } else {
//            return "Null";
//        }
//    }
//
//    public static boolean isConnectionFast(int type, int subType) {
//        if (type == ConnectivityManager.TYPE_WIFI) {
//            return true;
//        } else if (type == ConnectivityManager.TYPE_MOBILE) {
//            switch (subType) {
//                case TelephonyManager.NETWORK_TYPE_1xRTT:
//                    return false; // ~ 50-100 kbps
//                case TelephonyManager.NETWORK_TYPE_CDMA:
//                    return false; // ~ 14-64 kbps
//                case TelephonyManager.NETWORK_TYPE_EDGE:
//                    return false; // ~ 50-100 kbps
//                case TelephonyManager.NETWORK_TYPE_EVDO_0:
//                    return true; // ~ 400-1000 kbps
//                case TelephonyManager.NETWORK_TYPE_EVDO_A:
//                    return true; // ~ 600-1400 kbps
//                case TelephonyManager.NETWORK_TYPE_GPRS:
//                    return false; // ~ 100 kbps
//                case TelephonyManager.NETWORK_TYPE_HSDPA:
//                    return true; // ~ 2-14 Mbps
//                case TelephonyManager.NETWORK_TYPE_HSPA:
//                    return true; // ~ 700-1700 kbps
//                case TelephonyManager.NETWORK_TYPE_HSUPA:
//                    return true; // ~ 1-23 Mbps
//                case TelephonyManager.NETWORK_TYPE_UMTS:
//                    return true; // ~ 400-7000 kbps
//                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
//                    return true; // ~ 1-2 Mbps
//                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
//                    return true; // ~ 5 Mbps
//                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
//                    return true; // ~ 10-20 Mbps
//                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
//                    return false; // ~25 kbps
//                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
//                    return true; // ~ 10+ Mbps
//                // Unknown
//                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
//                default:
//                    return false;
//            }
//        } else {
//            return false;
//        }
//    }
//
//    public static boolean isEnableRestriction(Context context) {
//        boolean enableRestrict;
//        if (Version.hasJellyBeanMR2()) {
//            UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
//            Bundle restrictions = um.getUserRestrictions();
//            enableRestrict = restrictions.getBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS, false);
//        } else {
//            ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            enableRestrict = mgr.getBackgroundDataSetting();
//        }
//        Log.i(TAG, "enableRestrict: " + enableRestrict);
//        return enableRestrict;
//    }
//
//    public static String getIPAddress(boolean useIPv4) {
//        try {
//            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
//            for (NetworkInterface intf : interfaces) {
//                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
//                for (InetAddress addr : addrs) {
//                    if (!addr.isLoopbackAddress()) {
//                        String sAddr = addr.getHostAddress();
//                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
//                        boolean isIPv4 = sAddr.indexOf(':') < 0;
//
//                        if (useIPv4) {
//                            if (isIPv4)
//                                return sAddr;
//                        } else {
//                            if (!isIPv4) {
//                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
//                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//        } // for now eat exceptions
//        return "";
//    }
//
//    public interface NetworkChangedCallback {
//        void onNetworkChanged(boolean isNetworkAvailable);
//    }
//}