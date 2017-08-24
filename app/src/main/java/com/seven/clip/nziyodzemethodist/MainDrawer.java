package com.seven.clip.nziyodzemethodist;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;

public class MainDrawer extends AppCompatActivity {
    Intent toHymnNums,toSettings,toClearData;
    private FlowingDrawer mDrawer;
    MenuListFragment mMenuFragment;
    ViewSwitcher mTitleSwitcher;
    AutoCompleteTextView mainSearchEditText;
    View startSearch,startSearch_close;
    InputMethodManager imm;
    boolean request = false;
    Zvinokosha moreFeatures;
    int fromPrevActivity=0;
    RelativeLayout.LayoutParams layoutParams1;
    int verCode;

    public static final int RequestPermissionCode = 1;
    private static final int RECORD_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final String CURRENT_VERSION_CODE = "current_version_code";

    private FirebaseRemoteConfig remoteConfig;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_drawer);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    request = true;
                } else {
                    request = false;
                }
            }
        };


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("firstTime", false)) {
            Intent intro = new Intent(MainDrawer.this,IntroActivity.class);
            startActivity(intro);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.apply();
        }

        toHymnNums = new Intent(this, pickNum2.class);
        toHymnNums.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        final Intent toHymnList = new Intent(this, hymnList2.class);
        toHymnNums.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        final Intent toFavList = new Intent(this, FavList.class);
        final Intent toRecList = new Intent(this, RecList.class);
        final Intent toCaptionList = new Intent(this,CaptionList.class);
        final Intent toLogin = new Intent(this,ManageLogin.class);
        final Intent toTest = new Intent(this, ReadingList.class);
        moreFeatures = new Zvinokosha(this);
        toSettings = new Intent(this, Settings.class);
        toClearData = new Intent(this, ClearData.class);
        Hymns hymns = new Hymns(this);



        mDrawer = (FlowingDrawer) findViewById(R.id.activity_main_drawer);
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
        checkPermissions();
        setupMenu();


        Button hymnNumBut = (Button) findViewById(R.id.hymnNumberBut);
        Button hymnListBut = (Button) findViewById(R.id.hymnListBut);
        final Button updateHymnBut = (Button) findViewById(R.id.updateHymnBut);
        final TextView updateHymnButText = (TextView) findViewById(R.id.updateHymnButText);
        final TextView mainAppTitle = (TextView) findViewById(R.id.mainAppTitle);
        Button favBut = (Button) findViewById(R.id.favBut);
        Button recentBut = (Button) findViewById(R.id.recentBut);
        Button captionsBut = (Button) findViewById(R.id.captionsListBut);
        Button readingBut = (Button) findViewById(R.id.readingsBut);
        startSearch =  findViewById(R.id.searchButton);
        startSearch_close =  findViewById(R.id.searchButton_on);
        Button test = (Button) findViewById(R.id.test);
        final View opDrawer =findViewById(R.id.openMainDrawer);
        final View opDrawer_on =findViewById(R.id.openMainDrawer_on);
        mTitleSwitcher = (ViewSwitcher) findViewById(R.id.mainDrawerTitleSwitcher);
        mainSearchEditText = (AutoCompleteTextView) findViewById(R.id.mainSearchBar);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        final String[] hymnStrings = hymns.getAllHymns();
        ArrayAdapter<String> hymnArray = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,hymnStrings);
        mainSearchEditText.setAdapter(hymnArray);

        FirebaseRemoteConfigSettings remoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettings(remoteConfigSettings);
        remoteConfig.setDefaults(R.xml.remote_config_defaults);
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            verCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            verCode = 0;
            e.printStackTrace();
        }
        Runnable get = new Runnable() {
            @Override
            public void run() {
                long cacheExpiration = 3600;
                if (remoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
                    cacheExpiration = 0;
                }

                remoteConfig.fetch(cacheExpiration)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Once the config is successfully fetched it must be activated before newly fetched
                                    // values are returned.
                                    remoteConfig.activateFetched();
                                }
                                int onlineVersion = Integer.valueOf(remoteConfig.getString(CURRENT_VERSION_CODE));
                                if(onlineVersion>verCode){
                                    updateHymnBut.setVisibility(View.VISIBLE);
                                    updateHymnButText.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            }
        };


        mainSearchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    startSearching(mainSearchEditText.getText().toString());
                    return true;
                }
                else if((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_BACK)){
                    colapseTopBar();
                    return true;
                }
                return false;
            }
        });

        mainSearchEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                int pos = -1;
                Intent toHymnFromSearchBar = new Intent(MainDrawer.this,hymnDisplay.class);

                for (int i = 0; i < hymnStrings.length; i++) {
                    if (hymnStrings[i].equals(selection)) {
                        pos = i;
                        break;
                    }
                }
                pos++;
                if(pos>=1&&pos<=321){
                    toHymnFromSearchBar.putExtra("hymnNum",String.valueOf(pos));
                    startActivity(toHymnFromSearchBar);
                    colapseTopBar();
                }

            }
        });


        startSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandTopBar();
            }
        });

        startSearch_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainSearchEditText.getText().toString().isEmpty())
               colapseTopBar();
                else
                mainSearchEditText.setText("");
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(toTest);
            }
        });

        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/bh.ttf");
        mainAppTitle.setTypeface(custom_font);
        mainSearchEditText.setTypeface(custom_font);




        mDrawer.setOnDrawerStateChangeListener(new ElasticDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {

            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
                if(openRatio==0){
                    vis(opDrawer);
                    invis(opDrawer_on);
                } else if(openRatio==1) {
                    invis(opDrawer);
                    vis(opDrawer_on);
                }
            }
        });

        opDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vis(opDrawer_on);
                invis(opDrawer);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawer.toggleMenu();
                    }
                },600);
            }
        });

        hymnNumBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { startActivity(toHymnNums);
            }
        });

        hymnListBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {startActivity(toHymnList);
            }
        });

        updateHymnBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.seven.clip.nziyodzemethodist")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.seven.clip.nziyodzemethodist")));
                }
            }
        });

        favBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toFavList.putExtra("push","no");
                startActivity(toFavList);
            }
        });

        readingBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(request){
                   startActivity(toTest);
               } else {
                   QuickToast("not logged in");
                   startActivity(toLogin);
               }
            }
        });

        recentBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(toRecList);
            }
        });

        captionsBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(toCaptionList);
            }
        });

        fromPrevActivity = getIntent().getIntExtra("option",0);
        Options(fromPrevActivity);
        get.run();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener);
        //updateUI(currentUser);
    }

    public void QuickToast(String s){
        Toast.makeText(this, s,
                Toast.LENGTH_SHORT).show();
    }
    public void vis(View v){
        v.setAlpha(0f);
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(1f);
    }
    public void invis(final View v) {
        v.animate().alpha(0f).withEndAction(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(View.INVISIBLE);
            }
        });
    }
    public void openSettings(MenuItem item) {
        startActivity(toSettings);
    }
    public void openClearData(MenuItem item) {
        startActivity(toClearData);
    }
    public void shareApp(MenuItem item) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Nziyo DzeMethodist");
            String sAux = "Methodist Church in Zimbabwe\n" +
                    "Nziyo dzeMethodist\n" +
                    "HymnBook\n" +
                    "Available on Google Play\n" +
                    "\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=com.seven.clip.nziyodzemethodist \n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Share the Gospel:"));
        } catch(Exception e) {
            //e.toString();
        }
    }
    public void openCredits(MenuItem menuItem){
        Intent intent = new Intent(this,Credits.class);
        startActivity(intent);
    }
    public  void expandTopBar(){
        vis(startSearch_close);
        invis(startSearch);
        mTitleSwitcher.showNext();
        mTitleSwitcher.animate().scaleX(1.1f).scaleY(1.1f);
        mainSearchEditText.requestFocus();
        mainSearchEditText.setHint("any phrase in hymn...");
        imm.showSoftInput(mainSearchEditText, InputMethodManager.SHOW_IMPLICIT);
    }
    public void colapseTopBar(){
        invis(startSearch_close);
        vis(startSearch);
        mTitleSwitcher.animate().scaleX(1f).scaleY(1f);
        mainSearchEditText.setText("");
        mTitleSwitcher.showNext();
        imm.hideSoftInputFromWindow(mainSearchEditText.getWindowToken(), 0);
    }
    public void openFB(MenuItem menuItem){
        Intent intent = null;
        try {

            getPackageManager().getPackageInfo("com.facebook.katana", 0);
            String url = "https://www.facebook.com/nziyodzemethodistapp/";

            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href="+url));
            startActivity(intent);
        }

        catch (Exception e) {

            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/nziyodzemethodistapp/"));
            startActivity(intent);

            e.printStackTrace();
        }
    }
    public void openNotifications(MenuItem menuItem){
        Intent intent = new Intent(this,Notifications.class);
        startActivity(intent);
    }

    public void openReadings (MenuItem menuItem){

    }

    public void Options(int o){
        switch (o){
            case 1:
                mDrawer.toggleMenu();
                break;
            default:
                break;
        }
    }

    public void startSearching(String s){
        Intent toSearch = new Intent(this,Search.class);
        if(s.equals("")){
            QuickToast("Nothing to search...");
        }
        else {
            toSearch.putExtra("search",s);
            startActivity(toSearch);
        }

    }

    private void setupMenu() {
        FragmentManager fm = getSupportFragmentManager();
        mMenuFragment = (MenuListFragment) fm.findFragmentById(R.id.id_container_menu);
        if (mMenuFragment == null) {
            mMenuFragment = new MenuListFragment();
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment).commit();
        }
    }

    public void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,},
                        RequestPermissionCode);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,},
                        RequestPermissionCode);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_REQUEST_CODE);
        }
    }
}
