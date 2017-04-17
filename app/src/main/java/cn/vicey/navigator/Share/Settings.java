package cn.vicey.navigator.Share;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class Settings
{
    private static final String LOGGER_TAG = "Settings";
    private static final String DEFAULT_INT = "0";
    private static final String DEFAULT_STRING = "";
    private static final String PREFERENCE_NAME = "Settings";
    private static final String LINE_WIDTH = "LineWidth";

    private static boolean mIsDebugModeEnabled;
    private static int mLineWidth;
    private static SharedPreferences mSharedPreference;

    private static String getSettingValue(final @NonNull String name, final @NonNull String defaultValue)
    {
        try
        {
            String result = mSharedPreference.getString(name, null);
            return result == null ? defaultValue : result;
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to get setting value.", t);
            return null;
        }
    }

    public static boolean init(Context context)
    {
        try
        {
            mSharedPreference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

            mLineWidth = Integer.parseInt(getSettingValue(LINE_WIDTH, DEFAULT_INT));

            return true;
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to get shared preferences.", t);
            return false;
        }
    }

    private static boolean setSettingValue(final @NonNull String name, final @NonNull String value)
    {
        try
        {
            if (mSharedPreference == null) return false;
            SharedPreferences.Editor editor = mSharedPreference.edit();
            editor.putString(name, value);
            editor.apply();
            return true;
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to set setting value.", t);
            return false;
        }
    }

    public static boolean getIsDebugModeEnabled()
    {
        return mIsDebugModeEnabled;
    }

    public static int getLineWidth()
    {
        return mLineWidth;
    }

    public static boolean setLineWidth(int value)
    {
        try
        {
            mLineWidth = value;
            return setSettingValue(LINE_WIDTH, Integer.toString(mLineWidth));
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to save settings.", t);
            return false;
        }
    }

    public static void enableDebugMode()
    {
        mIsDebugModeEnabled = true;
    }

    private Settings()
    {
        // no-op
    }
}