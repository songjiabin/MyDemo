package com.example.searchview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * author : 宋佳
 * time   : 2018/06/20
 * desc   :自定义搜索功能
 * version: 1.0.0
 */

public class SearchView extends LinearLayout {


    private ICallBack iCallBack;


    private float textSizeSearch;
    private int textColorSearch;
    private String textHintSearch;
    private int searchBlockHeight;
    private int searchBlockColor;
    private Context context;
    private EditText et_search;
    private LinearLayout search_block;
    private ListViewScrollView listView;
    private TextView tv_clear;
    private ImageView searchBack;
    private RecordSQLiteOpenHelper dbhelper;
    private SimpleCursorAdapter adapter;
    private SQLiteDatabase db;

    public SearchView(Context context) {
        super(context);
        initAttrs(context, null);
        init();
    }


    /**
     * 构造函数
     * 作用：对搜索框进行初始化
     */
    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init();
    }

    public SearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    /**
     * 作用：初始化自定义属性
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        this.context = context;
        // 控件资源名称
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Search_View);
        // 搜索框字体大小（dp）
        textSizeSearch = typedArray.getDimension(R.styleable.Search_View_textSizeSearch, 20);


        // 搜索框字体颜色（使用十六进制代码，如#333、#8e8e8e）
        int defaultColor = context.getResources().getColor(R.color.colorText); // 默认颜色 = 灰色
        textColorSearch = typedArray.getColor(R.styleable.Search_View_textColorSearch, defaultColor);


        // 搜索框提示内容（String）
        textHintSearch = typedArray.getString(R.styleable.Search_View_textHintSearch);


        // 搜索框高度
        searchBlockHeight = typedArray.getInteger(R.styleable.Search_View_searchBlockHeight, 150);

        // 搜索框颜色
        int defaultColor2 = context.getResources().getColor(R.color.colorDefault); // 默认颜色 = 白色
        searchBlockColor = typedArray.getColor(R.styleable.Search_View_searchBlockColor, defaultColor2);

        // 释放资源
        typedArray.recycle();


    }


    //    初始化搜索框
    private void init() {
        //初始化 View
        initView();

        //初始化数据库
        initDB();


        /**
         * "清空搜索历史"按钮
         */
        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 清空数据库->>关注2
                deleteData();
                // 模糊搜索空字符 = 显示所有的搜索历史（此时是没有搜索记录的）
                queryData("");
            }
        });


        /**
         * 监听输入键盘更换后的搜索按键
         * 调用时刻：点击键盘上的搜索键时
         */
        et_search.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

                    // 1. 点击搜索按键后，根据输入的搜索字段进行查询
                    // 注：由于此处需求会根据自身情况不同而不同，所以具体逻辑由开发者自己实现，此处仅留出接口
                    if (!(iCallBack == null)) {
                        iCallBack.searchAciton(et_search.getText().toString());
                    }
                    Toast.makeText(context, "需要搜索的是" + et_search.getText(), Toast.LENGTH_SHORT).show();

                    // 2. 点击搜索键后，对该搜索字段在数据库是否存在进行检查（查询）->> 关注1
                    boolean hasData = hasData(et_search.getText().toString().trim());
                    // 3. 若存在，则不保存；若不存在，则将该搜索字段保存（插入）到数据库，并作为历史搜索记录
                    if (!hasData) {
                        insertData(et_search.getText().toString().trim());
                        queryData("");
                    }
                }
                return false;
            }
        });


        /**
         * 搜索框的文本变化实时监听
         */
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            // 输入文本后调用该方法
            @Override
            public void afterTextChanged(Editable s) {
                // 每次输入后，模糊查询数据库 & 显示
                // 注：若搜索框为空,则模糊搜索空字符 = 显示所有的搜索历史
                String tempName = et_search.getText().toString();
                queryData(tempName); // ->>关注1

            }
        });
        /**
         * 搜索记录列表（ListView）监听
         * 即当用户点击搜索历史里的字段后,会直接将结果当作搜索字段进行搜索
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // 获取用户点击列表里的文字,并自动填充到搜索框内
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                String name = textView.getText().toString();
                et_search.setText(name);
                Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * 点击返回按键后的事件
         */
        searchBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 注：由于返回需求会根据自身情况不同而不同，所以具体逻辑由开发者自己实现，此处仅留出接口
                if (!(iCallBack == null)){
                    iCallBack.backAciton();
                }

                //根据输入的内容模糊查询商品，并跳转到另一个界面，这个根据需求实现
                Toast.makeText(context, "返回到上一页", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView() {
        // 1. 绑定R.layout.search_layout 作为搜索框的xml文件
        LayoutInflater.from(context).inflate(R.layout.layout_search, this);
        // 2. 绑定搜索框EditText
        et_search = (EditText) findViewById(R.id.et_search);
        et_search.setTextSize(textSizeSearch);
        et_search.setTextColor(textColorSearch);
        et_search.setHint(textHintSearch);
        // 3. 搜索框背景颜色
        search_block = (LinearLayout) findViewById(R.id.search_block);
        LayoutParams params = (LayoutParams) search_block.getLayoutParams();
        params.height = searchBlockHeight;
        search_block.setBackgroundColor(searchBlockColor);
        search_block.setLayoutParams(params);


        // 4. 历史搜索记录 = ListView显示
        listView = (ListViewScrollView) findViewById(R.id.listView);

        // 5. 删除历史搜索记录 按钮
        tv_clear = (TextView) findViewById(R.id.tv_clear);
        tv_clear.setVisibility(INVISIBLE);

        // 6. 返回按键
        searchBack = (ImageView) findViewById(R.id.search_back);
    }

    private void initDB() {
        // 2. 实例化数据库SQLiteOpenHelper子类对象
        dbhelper = new RecordSQLiteOpenHelper(context);
        //每当初始化的时候都要查询历史记录(所有的历史记录)
        queryData("");

    }


    /**
     * 关注1
     * 模糊查询数据 & 显示到ListView列表上
     */
    private void queryData(String tempName) {

        // 1. 模糊搜索
        Cursor cursor = dbhelper.getReadableDatabase().rawQuery(
                "select id as _id,name from records where name like '%" + tempName + "%' order by id desc ", null);
        // 2. 创建adapter适配器对象 & 装入模糊搜索的结果
        adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursor, new String[]{"name"},
                new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // 3. 设置适配器
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        // 当输入框为空 & 数据库中有搜索记录时，显示 "删除搜索记录"按钮
        if (tempName.equals("") && cursor.getCount() != 0) {
            tv_clear.setVisibility(VISIBLE);
        } else {
            tv_clear.setVisibility(INVISIBLE);
        }


    }

    /**
     * 关注2：清空数据库
     */
    private void deleteData() {

        db = dbhelper.getWritableDatabase();
        db.execSQL("delete from records");
        db.close();
        tv_clear.setVisibility(INVISIBLE);
    }


    /**
     * 关注3
     * 检查数据库中是否已经有该搜索记录
     */
    private boolean hasData(String tempName) {
        // 从数据库中Record表里找到name=tempName的id
        Cursor cursor = dbhelper.getReadableDatabase().rawQuery(
                "select id as _id,name from records where name =?", new String[]{tempName});
        //  判断是否有下一个
        return cursor.moveToNext();
    }


    /**
     * 关注4
     * 插入数据到数据库，即写入搜索字段到历史搜索记录
     */
    private void insertData(String tempName) {
        db = dbhelper.getWritableDatabase();
        db.execSQL("insert into records(name) values('" + tempName + "')");
        db.close();
    }


    public void setiCallBack(ICallBack iCallBack) {
        this.iCallBack = iCallBack;
    }


}
