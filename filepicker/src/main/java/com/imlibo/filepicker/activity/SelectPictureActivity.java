package com.imlibo.filepicker.activity;

import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.imlibo.filepicker.R;
import com.imlibo.filepicker.adapter.BuketAdapter;
import com.imlibo.filepicker.adapter.EssMediaAdapter;
import com.imlibo.filepicker.loader.EssAlbumCollection;
import com.imlibo.filepicker.loader.EssMediaCollection;
import com.imlibo.filepicker.model.Album;
import com.imlibo.filepicker.model.EssFile;
import com.imlibo.filepicker.util.FileUtils;
import com.imlibo.filepicker.util.UiUtils;
import com.imlibo.filepicker.widget.MediaItemDecoration;
import com.imlibo.filepicker.widget.ToolbarSpinner;

import java.util.ArrayList;
import java.util.List;


/**
 * 选择图片界面
 */
public class SelectPictureActivity extends AppCompatActivity implements EssAlbumCollection.EssAlbumCallbacks, AdapterView.OnItemSelectedListener, EssMediaCollection.EssMediaCallbacks, BaseQuickAdapter.OnItemChildClickListener {

    /*1. 只展示指定文件名后缀的文件*/
    private String[] mFileTypes;
    /*2. 文件列表排序类型，默认按文件名升序排列*/
    private int mSortType = FileUtils.BY_NAME_ASC;
    /*3. 是否是多选，默认否*/
    private boolean mIsMultiSelect = true;
    /*4. 最多可选择个数，默认10*/
    private int mMaxCount;
    /*5. 是否需要压缩，默认返回压缩之后的图片*/
    private boolean mNeedCompress = true;
    /*6. 是否需要裁剪/旋转等，默认不裁剪*/
    private boolean mNeedClip = false;
    /*7. 是否需要显示照相机*/
    private boolean mNeedCamera = true;
    /*8. 相机为自定义相机还是系统相机，仅当mNeedCamera=true时有效*/
    private boolean mUseCustomCamera = true;
    /*9. 是否可预览图片，默认可预览*/
    private boolean mCanPreview = true;
    /*10. 主题*/
    private int mTheme = R.style.Matisse_Zhihu;

    private RecyclerView mRecyclerView;
    private TextView mTvSelectedFolder;
    private BuketAdapter mBuketAdapter;
    private EssMediaAdapter mMediaAdapter;

    private final EssAlbumCollection mAlbumCollection = new EssAlbumCollection();
    private final EssMediaCollection mMediaCollection = new EssMediaCollection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(mTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_picture);
//        EventBus.getDefault().register(this);
        mRecyclerView = findViewById(R.id.rcv_file_picture_list);
        mTvSelectedFolder = findViewById(R.id.selected_folder);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        Drawable navigationIcon = toolbar.getNavigationIcon();
        TypedArray ta = getTheme().obtainStyledAttributes(new int[]{R.attr.album_element_color});
        int color = ta.getColor(0, 0);
        ta.recycle();
        navigationIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        mBuketAdapter = new BuketAdapter(this,null,false);
        ToolbarSpinner spinner = new ToolbarSpinner(this);
        spinner.setSelectedTextView((TextView) findViewById(R.id.selected_folder));
        spinner.setPopupAnchorView(findViewById(R.id.toolbar));
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(mBuketAdapter);

        mAlbumCollection.onCreate(this,this);
        mAlbumCollection.load();

        mMediaCollection.onCreate(this,this);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.addItemDecoration(new MediaItemDecoration());
        mMediaAdapter = new EssMediaAdapter(new ArrayList<EssFile>());
        mMediaAdapter.setmImageResize(UiUtils.getImageResize(this,mRecyclerView));
        mRecyclerView.setAdapter(mMediaAdapter);
        mMediaAdapter.bindToRecyclerView(mRecyclerView);
        mMediaAdapter.setOnItemChildClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlbumCollection.onDestroy();
    }

    @Override
    public void onAlbumMediaLoad(Cursor cursor) {
        mBuketAdapter.swapCursor(cursor);
    }

    @Override
    public void onAlbumMediaReset() {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mBuketAdapter.getCursor().moveToPosition(position);
        Album album = Album.valueOf(mBuketAdapter.getCursor());
        mMediaCollection.load(album,mNeedCamera);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onMediaLoad(List<EssFile> essFileList) {
        mMediaAdapter.setNewData(essFileList);
        if(essFileList == null || essFileList.isEmpty()){
            mMediaAdapter.setEmptyView(R.layout.empty_file_list);
        }
    }

    @Override
    public void onmMediaReset() {

    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if(adapter.equals(mMediaAdapter)){
            if(view.getId() == R.id.check_view){
                mMediaAdapter.getData().get(position).setChecked(!mMediaAdapter.getData().get(position).isChecked());
                mMediaAdapter.notifyItemChanged(position,"");
            }else if(view.getId() == R.id.media_thumbnail){

            }
        }
    }
}
