package cn.vicey.navigator.Views;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.vicey.navigator.Activities.MainActivity;
import cn.vicey.navigator.Managers.AlertManager;
import cn.vicey.navigator.Managers.MapManager;
import cn.vicey.navigator.Managers.NavigateManager;
import cn.vicey.navigator.Managers.TagManager;
import cn.vicey.navigator.Models.Tag;
import cn.vicey.navigator.Navigator;
import cn.vicey.navigator.R;
import cn.vicey.navigator.Share.ListViewAdapter;
import cn.vicey.navigator.Utils.Logger;

import java.util.List;

/**
 * Tags view, provides a view to manage tags
 */
public class TagsView
        extends RelativeLayout
{
    //region Inner classes

    /**
     * Tag list adapter class
     */
    private class TagListAdapter
            extends ListViewAdapter<Tag>
    {
        //region Constructors

        /**
         * Initialize new instance of class {@link TagListAdapter}
         *
         * @param context Related context
         */
        public TagListAdapter(Context context)
        {
            super(context);
        }

        //endregion

        //region Methods

        /**
         * Get all tags from list
         *
         * @return Tag list
         */
        public List<Tag> getAll()
        {
            return mItems;
        }

        //endregion

        //region Override methods

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            if (view == null)
            {
                view = mInflater.inflate(R.layout.cmpt_tag_list_item, null);
            }
            Tag tag = mItems.get(i);
            TextView textView = (TextView) view.findViewById(R.id.tli_text);
            TextView subTextView = (TextView) view.findViewById(R.id.tli_sub_text);
            if (textView != null) textView.setText(tag.getValue());
            if (subTextView != null)
                subTextView.setText(mParent.getString(R.string.tag_metadata, tag.getFloorIndex(), tag.getNodeType(), tag
                        .getNodeIndex()));
            return view;
        }

        //endregion
    }

    //endregion

    //region Constants

    private static final String LOGGER_TAG = "TagsView";

    //endregion

    //region Fields

    private MainActivity   mParent;         // Parent activity
    private TagListAdapter mTagListAdapter; // Tag list adapter

    //endregion

    //region Listeners

    private OnClickListener              mOnLoadTagButtonClick       = new OnClickListener()              // Load tag button click
    {
        @Override
        public void onClick(View view)
        {
            try
            {
                if (view.getId() != R.id.tv_load_tags) return;
                //region Load tags
                if (NavigateManager.getCurrentMap() == null)
                {
                    AlertManager.alert(R.string.no_loaded_map);
                    return;
                }
                String mapName = NavigateManager.getCurrentMap().getName();
                List<Tag> tags = TagManager.loadTags(mapName);
                if (tags == null) AlertManager.alert(R.string.no_tag);
                else
                {
                    if (NavigateManager.getCurrentMap().setTags(tags)) AlertManager.alert(R.string.load_succeed);
                    else AlertManager.alert(R.string.load_failed);
                }
                //endregion
            }
            catch (Throwable t)
            {
                Logger.error(LOGGER_TAG, "Failed to load tags.", t);
            }
        }
    };
    private OnClickListener              mOnSaveTagButtonClick       = new OnClickListener()              // Save tag button click listener
    {
        @Override
        public void onClick(View view)
        {
            try
            {
                //region Save tags
                if (NavigateManager.getCurrentMap() == null)
                {
                    AlertManager.alert(R.string.no_loaded_map);
                    return;
                }
                List<Tag> tags = mTagListAdapter.getAll();
                if (tags.size() == 0)
                {
                    AlertManager.alert(R.string.no_tag);
                    return;
                }
                if (TagManager.saveTags(NavigateManager.getCurrentMap().getName(), tags))
                    AlertManager.alert(R.string.save_succeed);
                else AlertManager.alert(R.string.save_failed);
                //endregion
            }
            catch (Throwable t)
            {
                Logger.error(LOGGER_TAG, "Failed to save tags.", t);
            }
        }
    };
    private ListView.OnItemClickListener mOnTagListItemClickListener = new ListView.OnItemClickListener() // Tag list item click listener
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            try
            {
                final TextView textView = (TextView) view.findViewById(R.id.fli_text);
                new AlertDialog.Builder(mParent).setTitle(R.string.manage).setItems(new String[]{
                        mParent.getString(R.string.modify),
                        mParent.getString(R.string.delete)
                }, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.dismiss();
                        switch (i)
                        {
                            //region Modify
                            case 0:
                            {
                                final String tagValue = textView.getText().toString();
                                final EditText editor = new EditText(mParent);
                                editor.setText(tagValue);
                                editor.selectAll();
                                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface childDialogInterface, int i)
                                    {
                                        childDialogInterface.dismiss();
                                        switch (i)
                                        {
                                            case AlertDialog.BUTTON_POSITIVE:
                                            {
                                                String newTagValue = editor.getText().toString();

                                                if (MapManager.renameMapFile(tagValue, newTagValue))
                                                    AlertManager.alert(R.string.rename_succeed);
                                                else AlertManager.alert(R.string.rename_failed);
                                                flush();
                                            }
                                        }
                                    }
                                };
                                new AlertDialog.Builder(mParent).setTitle(R.string.rename)
                                                                .setView(editor)
                                                                .setPositiveButton(R.string.confirm, listener)
                                                                .setNegativeButton(R.string.cancel, listener)
                                                                .show();
                                break;
                            }
                            //endregion
                            //region Delete
                            case 1:
                            {
                                new AlertDialog.Builder(mParent).setTitle(R.string.alert)
                                                                .setMessage(R.string.confirm_to_delete)
                                                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
                                                                {
                                                                    @Override
                                                                    public void onClick(DialogInterface childDialogInterface, int i)
                                                                    {
                                                                        childDialogInterface.dismiss();
                                                                        switch (i)
                                                                        {
                                                                            case AlertDialog.BUTTON_POSITIVE:
                                                                            {
                                                                                if (textView == null) return;
                                                                                String tagFileName = textView.getText()
                                                                                                             .toString();
                                                                                if (TagManager.deleteTagFile(tagFileName))
                                                                                    AlertManager.alert(R.string.delete_succeed);
                                                                                else
                                                                                    AlertManager.alert(R.string.delete_failed);
                                                                                flush();
                                                                            }
                                                                        }
                                                                    }
                                                                })
                                                                .setNegativeButton(R.string.cancel, null)
                                                                .show();
                                break;
                            }
                            //endregion
                        }
                    }
                }).show();
            }
            catch (Throwable t)
            {
                Logger.error(LOGGER_TAG, "Failed to choose tag.", t);
            }
        }
    };

    //endregion

    //region Constructors

    /**
     * Initialize new instance of class {@link TagsView}
     *
     * @param parent Parent activity
     */
    public TagsView(final @NonNull MainActivity parent)
    {
        super(parent);
        mParent = parent;
        init();
    }

    //endregion

    //region Methods

    /**
     * Initialize view
     */
    private void init()
    {
        try
        {
            // Inflate layout
            LayoutInflater.from(mParent).inflate(R.layout.view_tags, this, true);

            // mTagListAdapter
            mTagListAdapter = new TagListAdapter(mParent);

            // tagList
            ListView tagList = (ListView) findViewById(R.id.tv_list_view);
            tagList.setOnItemClickListener(mOnTagListItemClickListener);
            tagList.setAdapter(mTagListAdapter);

            // loadTagButton
            Button loadTagButton = (Button) findViewById(R.id.tv_load_tags);
            loadTagButton.setOnClickListener(mOnLoadTagButtonClick);

            // saveTagButton
            Button saveTagButton = (Button) findViewById(R.id.tv_save_tags);
            saveTagButton.setOnClickListener(mOnSaveTagButtonClick);
        }
        catch (Throwable t)
        {
            Logger.error(LOGGER_TAG, "Failed to init tags view.", t);
            Navigator.exitWithError(Navigator.ERR_INIT);
        }

    }

    /**
     * Flush view
     */
    public void flush()
    {
        mParent.setTitleText(R.string.tags);
        if (NavigateManager.getCurrentMap() != null)
        {
            List<Tag> tags = NavigateManager.getCurrentMap().getTags();
            mTagListAdapter.replace(tags);
        }
        else
        {
            AlertManager.alert(R.string.no_loaded_map);
        }
    }

    //endregion
}
