package cn.vicey.navigator.Navigate;

import android.support.annotation.NonNull;
import cn.vicey.navigator.Models.Nodes.NodeBase;
import cn.vicey.navigator.Models.Nodes.PathNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Path class, represents a path in map
 */
public class Path
{
    //region Constants

    private static final String LOGGER_TAG = "Path";

    //endregion

    //region Fields

    private double mLength; // Path length

    private List<PathNode> mNodes = new ArrayList<>(); // Path nodes

    //endregion

    //region Constructors

    /**
     * Initialize new instance of class {@link Path}
     *
     * @param x X axis
     * @param y Y axis
     */
    public Path(int x, int y)
    {
        appendTail(new PathNode(x, y));
    }

    /**
     * Initialize new instance of class {@link Path}
     *
     * @param startNode Path's start node
     */
    public Path(NodeBase startNode)
    {
        if (startNode != null) appendTail(startNode);
    }

    //endregion

    //region Accessors

    /**
     * Gets path's end node
     *
     * @return End node, or null if the path is empty
     */
    public PathNode getEnd()
    {
        if (mNodes.isEmpty()) return null;
        return mNodes.get(mNodes.size() - 1);
    }

    /**
     * Gets path length
     *
     * @return Path length
     */
    public double getLength()
    {
        return mLength;
    }

    /**
     * Gets path's nodes
     *
     * @return Path's nodes
     */
    public List<PathNode> getNodes()
    {
        return mNodes;
    }

    /**
     * Gets path's node size
     *
     * @return Path's node size
     */
    public int getSize()
    {
        return mNodes.size();
    }

    /**
     * Gets path's start node
     *
     * @return Start node, or null if the path is empty
     */
    public PathNode getStart()
    {
        if (mNodes.isEmpty()) return null;
        return mNodes.get(0);
    }

    //endregion

    //region Methods

    /**
     * Append a node to path's head
     *
     * @param node Node to append
     * @return This path
     */
    public Path appendHead(final @NonNull NodeBase node)
    {
        if (mNodes.isEmpty())
        {
            mNodes.add(new PathNode(node));
            return this;
        }
        NodeBase head = mNodes.get(0);
        for (NodeBase.Link link : head.getLinks())
        {
            if (link.getTarget() == node)
            {
                mNodes.add(0, new PathNode(node));
                mLength += link.getDistance();
                break;
            }
        }
        mNodes.add(0, new PathNode(node));
        mLength += head.calcDistance(node);
        return this;
    }

    /**
     * Append nodes to path's head
     *
     * @param nodes Nodes to append
     * @return This path
     */
    public Path appendHead(final @NonNull List<NodeBase> nodes)
    {
        for (NodeBase node : nodes) appendHead(node);
        return this;
    }

    /**
     * Append a node to path's tail
     *
     * @param node Node to append
     * @return This path
     */
    public Path appendTail(final @NonNull NodeBase node)
    {
        if (mNodes.isEmpty())
        {
            mNodes.add(new PathNode(node));
            return this;
        }
        NodeBase tail = mNodes.get(mNodes.size() - 1);
        for (NodeBase.Link link : tail.getLinks())
        {
            if (link.getTarget() == node)
            {
                mNodes.add(new PathNode(node));
                mLength += link.getDistance();
                break;
            }
        }
        mNodes.add(new PathNode(node));
        mLength += tail.calcDistance(node);
        return this;
    }

    /**
     * Append nodes to path's tail
     *
     * @param nodes Nodes to append
     * @return This path
     */
    public Path appendTail(final @NonNull List<NodeBase> nodes)
    {
        for (NodeBase node : nodes) appendTail(node);
        return this;
    }

    /**
     * Indicate whether the path contains specified node
     *
     * @param node Specified node
     * @return Whether the path contains specified node
     */
    public boolean contains(final @NonNull NodeBase node)
    {
        return mNodes.contains(node);
    }

    /**
     * Fork a path from this path
     *
     * @return Forked path
     */
    public Path fork()
    {
        Path newPath = new Path(null);
        newPath.mLength = mLength;
        for (PathNode node : mNodes) newPath.mNodes.add(new PathNode(node));
        return newPath;
    }

    /**
     * Gets the nearest node to target node in this path
     *
     * @param target Target node
     * @return The nearest node
     */
    public NodeBase getNearestNode(final @NonNull NodeBase target)
    {
        double distance = Double.MAX_VALUE;
        NodeBase result = null;
        for (NodeBase node : mNodes)
        {
            double newDistance = node.calcDistance(target);
            if (newDistance < distance)
            {
                result = node;
                distance = newDistance;
            }
        }
        return result;
    }

    /**
     * Gets the index of the specified node
     *
     * @param node Specified node
     * @return Index of the node
     */
    public int indexOf(final @NonNull NodeBase node)
    {
        return mNodes.indexOf(node);
    }

    /**
     * Check whether target node is the tail node of this path
     *
     * @param target Target node
     * @return Whether target node is the tail node of this path
     */
    public boolean isEnd(final @NonNull NodeBase target)
    {
        return !mNodes.isEmpty() && mNodes.get(mNodes.size() - 1) == target;
    }

    /**
     * Remove path's tail
     *
     * @return This path
     */
    public Path removeTail()
    {
        if (mNodes.isEmpty()) return this;
        PathNode oldTail = mNodes.get(mNodes.size() - 1);
        if (mNodes.size() > 1)
        {
            PathNode newTail = mNodes.get(mNodes.size() - 2);
            mLength -= newTail.calcDistance(oldTail);
        }
        mNodes.remove(oldTail);
        return this;
    }

    public Path reverse()
    {
        Path newPath = new Path(null);
        newPath.mLength = mLength;
        for (int i = mNodes.size() - 1; i >= 0; i--) newPath.mNodes.add(mNodes.get(i));
        return newPath;
    }

    //endregion
}
