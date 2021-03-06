package cn.vicey.navigator.Debug;

import cn.vicey.navigator.Navigate.NavigateManager;
import cn.vicey.navigator.Share.SettingsManager;
import cn.vicey.navigator.Utils.Logger;

/**
 * Debug manager, provides a set of methods to help debug
 */
public final class DebugManager
{
    //region Constants

    private static final String LOGGER_TAG = "DebugFileManager";

    //endregion

    //region Static fields

    private static boolean mDisplayAllGuidePaths;     // Whether the renderer should display all guide paths
    private static boolean mTrackPathEnabled;         // Whether the application should record user's path
    private static boolean mUseDebugPathEnabled;      // Whether the user node should use debug path
    private static boolean mUseFakeLocationEnabled;   // Whether the user node should use fake location
    private static boolean mUseRandomLocationEnabled; // Whether the user node should use random location

    //endregion

    //region Static accessors

    /**
     * Gets whether the renderer should display all guide paths
     *
     * @return Whether the renderer should display all guide paths
     */
    public static boolean isDisplayAllGuidePaths()
    {
        return mDisplayAllGuidePaths;
    }

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
     * Gets whether the user node should use debug path
     *
     * @return Whether the user node should use debug path
     */
    public static boolean isUseDebugPathEnabled()
    {
        return mUseDebugPathEnabled;
    }

    /**
     * Gets whether the user node should use fake location
     *
     * @return Whether the user node should use fake location
     */
    public static boolean isUseFakeLocationEnabled()
    {
        return mUseFakeLocationEnabled;
    }

    /**
     * Gets whether the user node should use random location
     *
     * @return Whether the user node should use random location
     */
    public static boolean isUseRandomLocationEnabled()
    {
        return mUseRandomLocationEnabled;
    }

    /**
     * Sets whether the renderer should display all guide paths
     *
     * @param value Whether the renderer should display all guide paths
     */
    public static void setDisplayAllGuidePaths(boolean value)
    {
        mDisplayAllGuidePaths = value;
        if (value) Logger.debug(LOGGER_TAG, "Display all guide path enabled.");
        else Logger.debug(LOGGER_TAG, "Display all guide path disabled.");
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
        else
        {
            NavigateManager.clearUserPath();
            Logger.debug(LOGGER_TAG, "Track path disabled.");
        }
    }

    /**
     * Sets whether the user node should use debug path
     *
     * @param value Whether the user node should use debug path
     */
    public static void setUseDebugPathEnabled(boolean value)
    {
        mUseDebugPathEnabled = value;
        if (value)
        {
            Logger.debug(LOGGER_TAG, "Use debug path enabled.");
            if (mUseRandomLocationEnabled) setUseRandomLocationEnabled(false);
        }
        else Logger.debug(LOGGER_TAG, "Use debug path disabled.");
    }

    /**
     * Sets whether the user node should use fake location
     *
     * @param value Whether the user node should use fake location
     */
    public static void setUseFakeLocationEnabled(boolean value)
    {
        if (!SettingsManager.isDebugModeEnabled()) return;
        mUseFakeLocationEnabled = value;
        if (value) Logger.debug(LOGGER_TAG, "Use fake location enabled.");
        else Logger.debug(LOGGER_TAG, "Use fake location disabled.");
    }

    /**
     * Sets whether the user node should use random location
     *
     * @param value Whether the user node should use random location
     */
    public static void setUseRandomLocationEnabled(boolean value)
    {
        if (!SettingsManager.isDebugModeEnabled()) return;
        mUseRandomLocationEnabled = value;
        if (value)
        {
            Logger.debug(LOGGER_TAG, "Use random location enabled.");
            if (mUseDebugPathEnabled) setUseDebugPathEnabled(false);
        }
        else Logger.debug(LOGGER_TAG, "Use random location disabled.");
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
