package com.kai.lktMode.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LTE {
    Context mContext;
    public LTE(Context context){
        mContext=context;
    }

    @TargetApi(22)
    public void SetVolteEnable(boolean enable){
        int value = enable ? 1 : 0;
        String volteStr = null ;
        Settings.Global Global = new Settings.Global();
        Class<?> GlobalClass = Global.getClass();
        try {
            Field field =GlobalClass.getDeclaredField("ENHANCED_4G_MODE_ENABLED");
            field.setAccessible(true);
            volteStr = (String) field.get(Global);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        //如果是你的App是预制为系统App的，那么直接改变即可


        try {
            SubscriptionManager subManager =(SubscriptionManager)mContext
                    .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

            Class<? extends SubscriptionManager> sunClass = subManager.getClass();
            Method method1 = sunClass.getDeclaredMethod("getDefaultVoicePhoneId");
            method1.setAccessible(true);
            int phoneid = (Integer) method1.invoke(subManager);

            Class<?> clazz = (Class<?>)Class.forName("com.android.ims.ImsManager");
            Constructor ct = clazz.getDeclaredConstructor(new Class[]{Context.class,int.class});
            ct.setAccessible(true);
            Object obj = ct.newInstance(new Object[]{mContext,phoneid});
            //setAdvanced4GMode
            Method method = clazz.getDeclaredMethod("setAdvanced4GMode", new Class[]{boolean.class});
            method.setAccessible(true);
            method.invoke(obj, enable);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }
    public  boolean isNonTtyOrTtyOnVolteEnabled() {
        if (getBooleanCarrierConfig(CarrierConfigManager.KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL)) {
            return true;
        }

        String preferred = null ;
        int mode = 0;
        try {
            Settings.Secure secure = new Settings.Secure();
            Class<?> secureClass = secure.getClass();
            Field field = secureClass.getDeclaredField("PREFERRED_TTY_MODE");
            field.setAccessible(true);
            preferred = (String) field.get(secure);

            TelecomManager telcom = (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
            Class telcomClass = telcom.getClass();
            Field field1 = telcomClass.getDeclaredField("TTY_MODE_OFF");
            field1.setAccessible(true);
            mode = (Integer) field1.get(telcom);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        //  public static final String PREFERRED_TTY_MODE =
//        "preferred_tty_mode";
        return Settings.Secure.getInt(mContext.getContentResolver(),preferred, mode)  == mode;
    }
    @TargetApi(23)
    private  boolean getBooleanCarrierConfig(String key) {
        CarrierConfigManager configManager = (CarrierConfigManager) mContext.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        Class configManagerClass = configManager.getClass();
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfig();
        }
        if (b != null) {
            return b.getBoolean(key);
        } else {
            try {
                Method method = configManagerClass.getDeclaredMethod("getDefaultConfig");
                method.setAccessible(true);
                PersistableBundle persistableBundle = (PersistableBundle) method.invoke(configManager);
                return persistableBundle.getBoolean(key);
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }



}
