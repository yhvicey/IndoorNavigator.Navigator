package cn.vicey.navigator.Managers;

import cn.vicey.navigator.Utils.Logger;

/**
 * Debug manager, provides a set of methods to help debug
 */
public final class DebugManager
{
    //region Constants

    private static final String LOGGER_TAG = "DebugManager";

    //endregion

    //region Static fields

    private static boolean mUseFakeLocationEnabled; // Whether the user node should use fake location
    private static boolean mTrackPathEnabled;       // Whether the application should record user's path

    //endregion

    //region Static accessors

    /**
     * Gets whether the application should record user's path
     *
     * @return Whether the application should record user's path
     */
    public static boolean isTrackPathEnabled()
    {
        return mTrackPathEnabled;
    }

    /**
     * Gets whether the user node should use fake location
     *
     * @return Whether the user node should use fake location
     */
    public static boolean isUseFakeLocation()
    {
        return mUseFakeLocationEnabled;
    }

    /**
     * Sets whether the application should record user's path
     *
     * @param value Whether the application should record user's path
     */
    public static void setTrackPathEnabled(boolean value)
    {
        if (!SettingsManager.isDebugModeEnabled()) return;
        mTrackPathEnabled = value;
        if (value) Logger.debug(LOGGER_TAG, "Track path enabled.");
        else Logger.debug(LOGGER_TAG, "Track path disabled.");
    }

    /**
     * Sets whether the user node should use fake location
     *
     * @param value Whether the user node should use fake location
     */
    public static void setUseFakeLocation(boolean value)
    {
        if (!SettingsManager.isDebugModeEnabled()) return;
        mUseFakeLocationEnabled = value;
        if (value) Logger.debug(LOGGER_TAG, "Use fake location enabled.");
        else Logger.debug(LOGGER_TAG, "Use fake location disabled.");
    }

    //endregion

    //region Constructors

    /**
     * Hidden for static class design pattern
     */
    private DebugManager()
    {
        // no-op
    }

    //endregion
}
