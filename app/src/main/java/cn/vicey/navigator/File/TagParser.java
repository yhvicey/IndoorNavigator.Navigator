package cn.vicey.navigator.File;

import android.support.annotation.NonNull;
import android.util.Xml;
import cn.vicey.navigator.Models.Nodes.NodeType;
import cn.vicey.navigator.Models.Tag;
import cn.vicey.navigator.Utils.Logger;
import cn.vicey.navigator.Utils.Tools;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tag parser class, provides a set of methods to parse tag file
 */
public final class TagParser
{
    //region Constants

    private static final String LOGGER_TAG = "TagParser";

    private static final String ATTR_FLOOR_INDEX  = "FloorIndex"; // Floor index attribute name
    private static final String ATTR_NODE_INDEX   = "NodeIndex";  // Node index attribute name
    private static final String ATTR_NODE_TYPE    = "NodeType";   // Node type attribute name
    private static final String ATTR_VALUE        = "Value";      // Value attribute name
    private static final String ATTR_VERSION      = "Version";    // Version attribute name
    private static final String ELEMENT_TAG       = "Tag";        // Tag element name
    private static final String ELEMENT_TAGS      = "Tags";       // Tags element name
    private static final String SUPPORTED_VERSION = "1.1";        // Supported version of this parser

    //endregion

    //region Static methods

    /**
     * Generate tag object from xml parser
     *
     * @param parser Xml parser
     * @return New tag object, or null if error occurred
     */
    private static Tag generateTag(final @NonNull XmlPullParser parser)
    {
        int floorIndex;
        int nodeIndex;
        try
        {
            floorIndex = Integer.parseInt(parser.getAttributeValue(null, ATTR_FLOOR_INDEX));
            nodeIndex = Integer.parseInt(parser.getAttributeValue(null, ATTR_NODE_INDEX));
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Tag element must have valid floor index or node index attribute. Line: " + parser.getLineNumber(), t);
            return null;
        }
        String nodeTypeText = parser.getAttributeValue(null, ATTR_NODE_TYPE);
        if (Tools.isStringEmpty(nodeTypeText, true))
        {
            Logger.error(LOGGER_TAG, "Tag element must have valid node type attribute. Line: " + parser.getLineNumber());
            return null;
        }
        NodeType nodeType;
        try
        {
            nodeType = NodeType.parse(nodeTypeText);
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Tag element must have valid node type attribute. Line: " + parser.getLineNumber(), t);
            return null;
        }
        String value = parser.getAttributeValue(null, ATTR_VALUE);
        if (value == null)
        {
            Logger.error(LOGGER_TAG, "Tag element must have valid value attribute. Line: " + parser.getLineNumber());
            return null;
        }
        return new Tag(floorIndex, nodeIndex, nodeType, value);
    }

    /**
     * Parse tags from InputStream
     *
     * @param stream InputStream to parse
     * @return New tags list object, or null if error occurred
     */
    private static List<Tag> parseStream(final @NonNull InputStream stream)
    {
        try
        {
            List<Tag> tagList = new ArrayList<>();

            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, Tools.FILE_ENCODING);
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT)
            {
                switch (event)
                {
                    //region Start tag
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = parser.getName();
                        switch (elementName)
                        {
                            // Meet tags element, parse name and version, then check it
                            //region Tags element
                            case ELEMENT_TAGS:
                            {
                                String version = parser.getAttributeValue(null, ATTR_VERSION);
                                if (!SUPPORTED_VERSION.equals(version))
                                {
                                    Logger.error(LOGGER_TAG, "Unsupported tag file version. Version: " + version);
                                    return null;
                                }
                                break;
                            }
                            //endregion
                            // Meet tag element, add the tag to the list
                            //region Tag element
                            case ELEMENT_TAG:
                            {
                                Tag tag = generateTag(parser);
                                if (tag == null)
                                {
                                    Logger.error(LOGGER_TAG, "Failed in building tag. Line:" + parser.getLineNumber());
                                    return null;
                                }
                                tagList.add(tag);
                                break;
                            }
                            //endregion
                        }
                        break;
                    }
                    //endregion
                }
                event = parser.next();
            }
            return tagList;
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to parse input stream.", t);
            return null;
        }
    }

    /**
     * Parse tags from file
     *
     * @param file File to parse
     * @return New tags list object, or null if error occurred
     */
    public static List<Tag> parse(final @NonNull File file)
    {
        try
        {
            Logger.info(LOGGER_TAG, "Start parsing file: " + file.getPath());
            if (!file.exists() || !file.isFile())
            {
                Logger.error(LOGGER_TAG, "Can't find tag file. File path: " + file.getPath());
                return null;
            }
            List<Tag> tagList = parseStream(new FileInputStream(file));
            Logger.info(LOGGER_TAG, "Finished parsing file: " + file.getPath());
            return tagList;
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to parse tag file. File path:" + file.getPath(), t);
            return null;
        }
    }

    //endregion

    //region Constructors

    /**
     * Hidden for static class design pattern
     */
    private TagParser()
    {
        // no-op
    }

    //endregion
}