package cn.vicey.navigator.Managers;

import android.support.annotation.NonNull;
import cn.vicey.navigator.Map.MapParser;
import cn.vicey.navigator.Map.TagParser;
import cn.vicey.navigator.Map.TagSaver;
import cn.vicey.navigator.Models.Floor;
import cn.vicey.navigator.Models.Map;
import cn.vicey.navigator.Models.Tag;
import cn.vicey.navigator.Navigator;
import cn.vicey.navigator.Utils.Logger;
import cn.vicey.navigator.Utils.Tools;

import java.io.File;
import java.util.List;

public final class MapManager
{
    private static final String LOGGER_TAG = "MapManager";
    private static final String MAP_DIR = "/maps";
    private static final String TAG_DIR = "/tags";

    public static final int NO_SELECTED_FLOOR = -1;

    private static int mCurrentFloorIndex = NO_SELECTED_FLOOR;
    private static Map mCurrentMap;
    private static File mMapDir;
    private static File mTagDir;

    public static boolean init()
    {
        try
        {
            mMapDir = new File(Navigator.getFilesDirPath() + MAP_DIR);
            mTagDir = new File(Navigator.getFilesDirPath() + TAG_DIR);
            if (!(mMapDir.exists() || mMapDir.mkdir()))
            {
                Logger.error(LOGGER_TAG, "Failed to init map manager. Can not init map directory.");
                return false;
            }
            if (!(mTagDir.exists() || mTagDir.mkdir()))
            {
                Logger.error(LOGGER_TAG, "Failed to init map manager. Can not init tag directory.");
                return false;
            }
            return true;
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to init map manager. Can not init map manager.", t);
            return false;
        }
    }

    public static boolean deleteMapFile(final @NonNull String mapFileName)
    {
        File map = new File(mMapDir + "/" + mapFileName);
        return !map.exists() || map.delete();
    }

    public static boolean deleteTagFile(final @NonNull String tagFileName)
    {
        File tag = new File(mMapDir + "/" + tagFileName);
        return !tag.exists() || tag.delete();
    }

    public static List<File> getAllMapFiles()
    {
        try
        {
            List<File> files = Tools.getFiles(mMapDir);
            if (files == null)
            {
                Logger.error(LOGGER_TAG, "Failed to get all map files.");
                return null;
            }
            return files;
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to get all map files.", t);
            return null;
        }
    }

    public static String getAvailableDefaultMapFileName()
    {
        return Tools.getAvailableDefaultName(mMapDir, ".xml");
    }

    public static Floor getCurrentFloor()
    {
        if (mCurrentMap == null) return null;
        if (mCurrentFloorIndex == NO_SELECTED_FLOOR || mCurrentFloorIndex >= mCurrentMap.getFloors().size())
            return null;
        return mCurrentMap.getFloors().get(mCurrentFloorIndex);
    }

    public static int getCurrentFloorIndex()
    {
        return mCurrentFloorIndex;
    }

    public static Map getCurrentMap()
    {
        return mCurrentMap;
    }

    public static Floor getFloor(int floorIndex)
    {
        if (mCurrentMap == null) return null;
        if (floorIndex < 0) return null;
        if (floorIndex > mCurrentMap.getFloors().size() - 1) return null;
        return mCurrentMap.getFloors().get(floorIndex);
    }

    public static File getMapDir()
    {
        return mMapDir;
    }

    public static File getTagDir()
    {
        return mTagDir;
    }

    public static boolean goDownstairs()
    {
        if (mCurrentMap == null) return false;
        if (mCurrentFloorIndex == NO_SELECTED_FLOOR) return false;
        mCurrentFloorIndex--;
        return true;
    }

    public static boolean goUpstairs()
    {
        if (mCurrentMap == null) return false;
        if (mCurrentFloorIndex >= mCurrentMap.getFloors().size() - 1) return false;
        mCurrentFloorIndex++;
        return true;
    }

    public static boolean hasMapFile(final @NonNull String mapName)
    {
        return new File(mMapDir + "/" + mapName).exists();
    }

    public static boolean hasTags(final @NonNull String mapName)
    {
        return new File(mTagDir + "/" + mapName).exists();
    }

    public static Map loadMap(final @NonNull String mapName)
    {
        return MapParser.parse(new File(mMapDir + "/" + mapName));
    }

    public static List<Tag> loadTags(final @NonNull String mapName)
    {
        if (!hasTags(mapName)) return null;
        return TagParser.parse(new File(mTagDir + "/" + mapName));
    }

    public static boolean renameMapFile(final @NonNull String mapName, final @NonNull String newMapName)
    {
        if (!hasMapFile(mapName)) return false;
        if (hasMapFile(newMapName)) return false;
        File map = new File(mMapDir + "/" + mapName);
        return map.renameTo(new File(mMapDir + "/" + newMapName));
    }

    public static void setCurrentMap(final @NonNull Map map)
    {
        mCurrentFloorIndex = NO_SELECTED_FLOOR;
        mCurrentMap = map;
        goUpstairs();
    }

    public static boolean saveMapFile(final @NonNull File src, boolean overwrite)
    {
        String fileName = src.getName();
        if (!overwrite && hasMapFile(fileName)) fileName = getAvailableDefaultMapFileName();
        return Tools.copyFile(src, new File(mMapDir + "/" + fileName), overwrite);
    }

    public static boolean saveTags(final @NonNull String mapName, List<Tag> tags)
    {
        File file = TagSaver.save(mapName, tags);
        if (file == null) return false;
        return Tools.copyFile(file, new File(mTagDir + "/" + mapName), true);
    }

    private MapManager()
    {
        // no-op
    }
}