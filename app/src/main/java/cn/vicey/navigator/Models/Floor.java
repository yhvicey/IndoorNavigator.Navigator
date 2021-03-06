package cn.vicey.navigator.Models;

import android.support.annotation.NonNull;
import cn.vicey.navigator.Models.Nodes.GuideNode;
import cn.vicey.navigator.Models.Nodes.NodeBase;
import cn.vicey.navigator.Models.Nodes.NodeType;
import cn.vicey.navigator.Models.Nodes.WallNode;
import cn.vicey.navigator.Utils.Logger;
import cn.vicey.navigator.Utils.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Floor class
 */
public class Floor
{
    //region Constants

    private static final String LOGGER_TAG = "Floor";

    private static final int MAP_PADDING = 50; // Map's right and bottom padding for displaying the whole area

    //endregion

    //region Fields

    private int mHeight; // Floor's height
    private int mWidth;  // Floor's width

    private List<GuideNode> mGuideNodes     = new ArrayList<>(); // Floor's guide nodes
    private List<GuideNode> mNextEntryNodes = new ArrayList<>(); // Floor's next floor's entry nodes
    private List<GuideNode> mPrevEntryNodes = new ArrayList<>(); // Floor's previous floor's entry nodes
    private List<WallNode>  mWallNodes      = new ArrayList<>(); // Floor's wall nodes

    //endregion

    //region Accessors

    /**
     * Gets node's guide nodes
     *
     * @return Node's guide nodes
     */
    public List<GuideNode> getGuideNodes()
    {
        return mGuideNodes;
    }

    /**
     * Gets floor's height
     *
     * @return Floor's height
     */
    public int getHeight()
    {
        return mHeight;
    }

    /**
     * Gets floor's next floor's entry nodes
     *
     * @return Next floor's entry nodes
     */
    public List<GuideNode> getNextEntryNodes()
    {
        return mNextEntryNodes;
    }

    /**
     * Gets floor's previous floor's entry nodes
     *
     * @return Previous floor's entry nodes
     */
    public List<GuideNode> getPrevEntryNodes()
    {
        return mPrevEntryNodes;
    }

    /**
     * Gets floor's wall nodes
     *
     * @return Floor's wall nodes
     */
    public List<WallNode> getWallNodes()
    {
        return mWallNodes;
    }

    /**
     * Gets floor's width
     *
     * @return Floor's width
     */
    public int getWidth()
    {
        return mWidth;
    }

    //endregion

    // region Methods


    /**
     * Find the nearest node to specified location
     *
     * @param x     X axis
     * @param y     Y axis
     * @param nodes Nodes to find
     * @return The nearest node to specified location, or null if there is no node in nodes
     */
    private GuideNode findNearestGuideNode(int x, int y, List<GuideNode> nodes)
    {
        double distance = Double.MAX_VALUE;
        GuideNode result = null;
        for (GuideNode node : nodes)
        {
            double newDistance = node.calcDistance(x, y);
            if (newDistance > distance) continue;
            distance = newDistance;
            result = node;
        }
        return result;
    }

    /**
     * Add and convert link to {@link NodeBase.Link}
     *
     * @param link Link to add
     */
    public void addLink(final @NonNull Link link)
    {
        NodeBase start = getNode(link.getType(), link.getStartIndex());
        NodeBase end = getNode(link.getType(), link.getEndIndex());
        start.link(end);
        end.link(start);
    }

    /**
     * Add and convert links to {@link NodeBase.Link}
     *
     * @param links Links to add
     */
    public void addLinks(final @NonNull List<Link> links)
    {
        for (Link link : links)
        {
            addLink(link);
        }
    }

    /**
     * Add node to floor
     *
     * @param node Node to add
     */
    public void addNode(final @NonNull NodeBase node)
    {
        if (node.getX() > mWidth) mWidth = node.getX() + MAP_PADDING;
        if (node.getY() > mHeight) mHeight = node.getY() + MAP_PADDING;
        switch (node.getType())
        {
            case GUIDE_NODE:
            {
                GuideNode guideNode = (GuideNode) node;
                mGuideNodes.add(guideNode);
                if (guideNode.getPrev() != null) mPrevEntryNodes.add(guideNode);
                if (guideNode.getNext() != null) mNextEntryNodes.add(guideNode);
                return;
            }
            case WALL_NODE:
            {
                mWallNodes.add((WallNode) node);
                return;
            }
            default:
            {
                Logger.error(LOGGER_TAG, "Unexpected node type.");
            }
        }
    }

    /**
     * Find guide nodes by pattern
     *
     * @param pattern Search pattern
     * @return Found nodes
     */
    public List<GuideNode> findGuideNodes(final @NonNull String pattern)
    {
        if (Tools.isStringEmpty(pattern, true)) return new ArrayList<>();
        List<GuideNode> result = new ArrayList<>();
        for (GuideNode node : mGuideNodes)
        {
            if (node.getName() == null) continue;
            if (node.getName().toLowerCase().contains(pattern.toLowerCase())) result.add(node);
        }
        return result;
    }

    /**
     * Find the nearest guide node to specified location
     *
     * @param x X axis
     * @param y Y axis
     * @return The nearest guide node
     */
    public GuideNode findNearestGuideNode(int x, int y)
    {
        return findNearestGuideNode(x, y, mGuideNodes);
    }

    /**
     * Find the nearest entry node to next floor to specified location
     *
     * @param x X axis
     * @param y Y axis
     * @return The nearest entry node to next floor
     */
    public GuideNode findNearestNextEntryNode(int x, int y)
    {
        return findNearestGuideNode(x, y, mNextEntryNodes);
    }

    /**
     * Find the nearest entry node to previous floor to specified location
     *
     * @param x X axis
     * @param y Y axis
     * @return The nearest entry node to previous floor
     */
    public GuideNode findNearestPrevEntryNode(int x, int y)
    {
        return findNearestGuideNode(x, y, mPrevEntryNodes);
    }

    /**
     * Gets guide node by index
     *
     * @param index Guide node index
     * @return Specified guide node
     */
    public GuideNode getGuideNode(int index)
    {
        return mGuideNodes.get(index);
    }

    /**
     * Gets guide node's index
     *
     * @param node Specified guide node
     * @return Specified guide node's index
     */
    public int getGuideNodeIndex(@NonNull GuideNode node)
    {
        return mGuideNodes.indexOf(node);
    }

    /**
     * Gets node by type and index
     *
     * @param type  Node type
     * @param index Node index
     * @return Specified node
     */
    public NodeBase getNode(NodeType type, int index)
    {
        switch (type)
        {
            case GUIDE_NODE:
            {
                return getGuideNode(index);
            }
            case WALL_NODE:
            {
                return getWallNode(index);
            }
            default:
            {
                Logger.error(LOGGER_TAG, "Unexpected node type.");
                return null;
            }
        }
    }

    /**
     * Gets node's index
     *
     * @param node Specified node
     * @return Specified node's index
     */
    public int getNodeIndex(NodeBase node)
    {
        switch (node.getType())
        {
            case GUIDE_NODE:
            {
                return getGuideNodeIndex((GuideNode) node);
            }
            case WALL_NODE:
            {
                return getWallNodeIndex((WallNode) node);
            }
            default:
            {
                Logger.error(LOGGER_TAG, "Unexpected node type.");
                return -1;
            }
        }
    }

    /**
     * Gets wall node by index
     *
     * @param index Wall node's index
     * @return Specified wall node
     */
    public WallNode getWallNode(int index)
    {
        return mWallNodes.get(index);
    }

    /**
     * Gets wall node's index
     *
     * @param node Specified wall node
     * @return Specified wall node's index
     */
    public int getWallNodeIndex(@NonNull WallNode node)
    {
        return mWallNodes.indexOf(node);
    }

    //endregion
}
