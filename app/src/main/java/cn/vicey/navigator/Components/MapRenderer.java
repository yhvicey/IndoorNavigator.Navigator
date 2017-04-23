package cn.vicey.navigator.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import cn.vicey.navigator.Managers.NavigateManager;
import cn.vicey.navigator.Managers.SettingsManager;
import cn.vicey.navigator.Models.Floor;
import cn.vicey.navigator.Models.Nodes.GuideNode;
import cn.vicey.navigator.Models.Nodes.NodeBase;
import cn.vicey.navigator.Models.Nodes.NodeType;
import cn.vicey.navigator.Models.Nodes.WallNode;
import cn.vicey.navigator.Navigator;
import cn.vicey.navigator.Utils.Logger;

/**
 * Map renderer component, provides support for drawing, scrolling and zooming map
 */
public class MapRenderer
        extends View
{
    //region Constants

    private static final String LOGGER_TAG = "MapRenderer";

    private static final int GUIDE_COLOR    = Color.GREEN;  // Guide color
    private static final int LINE_WIDTH     = 8;            // Line width
    private static final int NODE_RADIUS    = 4;            // Node radius
    private static final int WALL_COLOR     = Color.DKGRAY; // Wall color
    private static final int ZOOM_LEVEL_MAX = 5;            // Max zoom level
    private static final int ZOOM_LEVEL_MIN = 1;            // Min zoom level
    private static final int ZOOM_SPEED     = 200;          // Zoom speed

    //endregion

    //region Fields

    private Paint   mGuidePaint;         // Paint for guide nodes and lines
    private int     mHalfHeight;         // Half of the component height
    private int     mHalfWidth;          // Half of the component width
    private boolean mIsZooming;          // Whether the component is zooming
    private Point   mLookAt;             // The center point of the view window in map
    private float   mPrevTouchX;         // Previous touch point x axis
    private float   mPrevTouchY;         // Previous touch point y axis
    private float   mTouchPointDistance; // Distance between two touch points
    private int     mTouchedPointCount;  // Current touch point count
    private Paint   mWallPaint;          // Paint for wall nodes and lines

    private int   mCurrentDisplayingFloorIndex = NavigateManager.NO_SELECTED_FLOOR; // Current displaying floor's index
    private float mCurrentZoomLevel            = 3;                                 // Current zoom level

    //endregion

    //region Constructors

    /**
     * Initialize new instance of class {@link MapRenderer}
     *
     * @param context Related context
     */
    public MapRenderer(Context context)
    {
        this(context, null);
    }

    /**
     * Initialize new instance of class {@link MapRenderer}
     *
     * @param context Related context
     * @param attrs   Xml file attributes
     */
    public MapRenderer(Context context, AttributeSet attrs)
    {
        super(context, attrs, 0);
        init(attrs);
    }

    //endregion

    //region Accessors

    /**
     * Gets current displaying floor index
     *
     * @return Current displaying floor index
     */
    public int getCurrentDisplayingFloorIndex()
    {
        return mCurrentDisplayingFloorIndex;
    }

    //endregion

    //region Methods

    /**
     * Calculate distance between two touch points
     *
     * @param event Touch event
     * @return Distance between two touch points
     */
    private float calcTouchPointDistance(MotionEvent event)
    {
        if (mTouchedPointCount != 2) return mTouchPointDistance;
        float firstX = event.getX(0);
        float firstY = event.getY(0);
        float secondX = event.getX(1);
        float secondY = event.getY(1);
        return (float) Math.sqrt(Math.pow(firstX - secondX, 2) + Math.pow(firstY - secondY, 2));
    }

    /**
     * Draw a link between two nodes
     *
     * @param canvas Canvas to draw
     * @param start  Start node
     * @param end    End node
     */
    private void drawLink(final @NonNull Canvas canvas, final @NonNull NodeBase start, final @NonNull NodeBase end)
    {
        mGuidePaint.setStrokeWidth(LINE_WIDTH * mCurrentZoomLevel);
        mWallPaint.setStrokeWidth(LINE_WIDTH * mCurrentZoomLevel);
        float startX = getRelativeX(start.getX());
        float startY = getRelativeY(start.getY());
        float endX = getRelativeX(end.getX());
        float endY = getRelativeY(end.getY());
        if (start.getType() == NodeType.WALL_NODE && end.getType() == NodeType.WALL_NODE)
            canvas.drawLine(startX, startY, endX, endY, mWallPaint);
        else canvas.drawLine(startX, startY, endX, endY, mGuidePaint);
    }

    /**
     * Draw target floor's links
     *
     * @param canvas Canvas to draw
     * @param floor  Target floor
     */
    private void drawLinks(final @NonNull Canvas canvas, final @NonNull Floor floor)
    {
        for (WallNode wallNode : floor.getWallNodes())
        {
            for (NodeBase.Link link : wallNode.getLinks())
            {
                drawLink(canvas, wallNode, link.getTarget());
            }
        }
        if (!SettingsManager.isDebugModeEnabled()) return;
        for (GuideNode guideNode : floor.getGuideNodes())
        {
            for (NodeBase.Link link : guideNode.getLinks())
            {
                drawLink(canvas, guideNode, link.getTarget());
            }
        }
    }

    /**
     * Draw a node
     *
     * @param canvas Canvas to draw
     * @param node   Target node
     */
    private void drawNode(final @NonNull Canvas canvas, final @NonNull NodeBase node)
    {
        float x = getRelativeX(node.getX());
        float y = getRelativeY(node.getY());
        switch (node.getType())
        {
            case GUIDE_NODE:
            {
                canvas.drawCircle(x, y, NODE_RADIUS * mCurrentZoomLevel, mGuidePaint);
                break;
            }
            case WALL_NODE:
            {
                canvas.drawCircle(x, y, NODE_RADIUS * mCurrentZoomLevel, mWallPaint);
                break;
            }
        }
    }

    /**
     * Draw target floor's nodes
     *
     * @param canvas Canvas to draw
     * @param floor  Target floor
     */
    private void drawNodes(final @NonNull Canvas canvas, final @NonNull Floor floor)
    {
        for (WallNode wallNode : floor.getWallNodes())
        {
            drawNode(canvas, wallNode);
        }
        if (!SettingsManager.isDebugModeEnabled()) return;
        for (GuideNode guideNode : floor.getGuideNodes())
        {
            drawNode(canvas, guideNode);
        }
    }

    /**
     * Gets displaying floor
     *
     * @return Displaying floor, or null if no floor is displaying
     */
    private Floor getDisplayingFloor()
    {
        Floor floor = NavigateManager.getFloor(mCurrentDisplayingFloorIndex);
        if (floor == null) mCurrentDisplayingFloorIndex = NavigateManager.NO_SELECTED_FLOOR;
        return floor;
    }

    /**
     * Convert x axis from floor coordinate to view coordinate
     *
     * @param x X axis in floor coordinate
     * @return X axis in view coordinate
     */
    private float getRelativeX(int x)
    {
        return (x - mLookAt.x) * mCurrentZoomLevel + mHalfWidth;
    }

    /**
     * Convert y axis from floor coordinate to view coordinate
     *
     * @param y Y axis in floor coordinate
     * @return Y axis in view coordinate
     */
    private float getRelativeY(int y)
    {
        return (y - mLookAt.y) * mCurrentZoomLevel + mHalfHeight;
    }

    /**
     * Initialize component
     *
     * @param attrs Xml file attribute
     */
    private void init(AttributeSet attrs)
    {
        try
        {
            // mGuidePaint
            mGuidePaint = new Paint();
            mGuidePaint.setColor(GUIDE_COLOR);

            // mWallPaint
            mWallPaint = new Paint();
            mWallPaint.setColor(WALL_COLOR);

            // mLookAt
            mLookAt = new Point();
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to init map renderer.", t);
            Navigator.exitWithError(Navigator.ERR_INIT);
        }
    }

    /**
     * Move "eyes" to specified location
     *
     * @param x X axis
     * @param y Y axis
     */
    private void lookAt(int x, int y)
    {
        mLookAt.set(x, y);
        invalidate();
    }

    /**
     * Zoom the floor view
     *
     * @param offset Zoom offset
     */
    private void zoom(float offset)
    {
        mCurrentZoomLevel += offset / ZOOM_SPEED;

        if (mCurrentZoomLevel < ZOOM_LEVEL_MIN) mCurrentZoomLevel = ZOOM_LEVEL_MIN;
        if (mCurrentZoomLevel > ZOOM_LEVEL_MAX) mCurrentZoomLevel = ZOOM_LEVEL_MAX;

        invalidate();
    }

    /**
     * Try display downstairs
     *
     * @return True if didn't reach the ground floor, otherwise false
     */
    public boolean displayDownstairs()
    {
        if (NavigateManager.getCurrentMap() == null) return false;
        if (mCurrentDisplayingFloorIndex <= 0) return false;
        mCurrentDisplayingFloorIndex--;

        invalidate();
        return true;
    }

    /**
     * Try display upstairs
     *
     * @return True if didn't reach the top floor, otherwise false
     */
    public boolean displayUpstairs()
    {
        if (NavigateManager.getCurrentMap() == null) return false;
        if (mCurrentDisplayingFloorIndex >= NavigateManager.getCurrentMap().getFloors().size() - 1) return false;
        mCurrentDisplayingFloorIndex++;

        invalidate();
        return true;
    }

    /**
     * Clip the offsets and move "eyes" to specified location
     *
     * @param xOffset X-axis offset
     * @param yOffset Y-axis offset
     */
    public void moveEye(float xOffset, float yOffset)
    {
        Floor floor;
        if ((floor = getDisplayingFloor()) == null) return;

        int newX = mLookAt.x += xOffset / mCurrentZoomLevel;
        int newY = mLookAt.y += yOffset / mCurrentZoomLevel;

        if (newX < 0) newX = 0;
        if (newX > floor.getWidth() * mCurrentZoomLevel) newX = (int) (floor.getWidth() * mCurrentZoomLevel);
        if (newY < 0) newY = 0;
        if (newY > floor.getHeight() * mCurrentZoomLevel) newY = (int) (floor.getHeight() * mCurrentZoomLevel);

        lookAt(newX, newY);
    }

    //endregion

    //region Override methods

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        try
        {
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                {
                    mTouchedPointCount = 1;
                    mPrevTouchX = event.getX();
                    mPrevTouchY = event.getY();
                    break;
                }
                case MotionEvent.ACTION_UP:
                {
                    mIsZooming = false;
                    mTouchedPointCount = 0;
                    break;
                }
                case MotionEvent.ACTION_POINTER_DOWN:
                {
                    mTouchedPointCount++;
                    mIsZooming = true;
                    mTouchPointDistance = calcTouchPointDistance(event);
                    break;
                }
                case MotionEvent.ACTION_POINTER_UP:
                {
                    mTouchedPointCount--;
                    mPrevTouchX = event.getX();
                    mPrevTouchY = event.getY();
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                {
                    if (mIsZooming)
                    {
                        float newDistance = calcTouchPointDistance(event);
                        float offset = newDistance - mTouchPointDistance;
                        zoom(offset);
                        mTouchPointDistance = newDistance;
                    }
                    else
                    {
                        float xOffset = mPrevTouchX - event.getX();
                        float yOffset = mPrevTouchY - event.getY();
                        moveEye(xOffset, yOffset);
                        mPrevTouchX = event.getX();
                        mPrevTouchY = event.getY();
                    }
                }
            }
            return true;
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to handle touch event.", t);
            Navigator.exitWithError(Navigator.ERR_INIT);
            return false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH)
    {
        try
        {
            mHalfWidth = w / 2;
            mHalfHeight = h / 2;
            mLookAt.set(mHalfWidth, mHalfHeight);
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to handle size change event.", t);
            Navigator.exitWithError(Navigator.ERR_INIT);
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        try
        {
            Floor floor = getDisplayingFloor();
            if (floor == null) return;

            drawNodes(canvas, floor);
            drawLinks(canvas, floor);
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to handle draw event.", t);
            Navigator.exitWithError(Navigator.ERR_INIT);
        }
    }

    //endregion
}
