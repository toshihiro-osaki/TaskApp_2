package jp.techacademy.osaki.toshihiro.taskapp_2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.EditText;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

import android.text.format.Time;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.osaki.toshihiro.taskapp_2.TASK";

    EditText mEditText1;

    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };
    private ListView mListView;
    private TaskAdapter mTaskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [絞込]ボタンクリック
        mEditText1 = (EditText) findViewById(R.id.category_search_text);
        Button button1 = (Button) findViewById(R.id.category_search_button);
        button1.setFocusable(true);
        button1.setFocusableInTouchMode(true);
        button1.requestFocus();

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText1.getText().toString().equals("")) {

                    String ttl = "<注意>";
                    String msg = "ｶﾃｺﾞﾘ絞込ｷｰﾜｰﾄﾞを入力";
                    //???????????????????????????????????????????????????
                    //Time time = new Time("Asia/Tokyo");
                    //time.setToNow();
                    //String date_now = time.year + "-" + (time.month + 1) + "-" + time.monthDay + " " + time.hour + ":" + time.minute + "'" + time.second + "\"";
                    //msg = msg + "\n" + date_now;
                    //???????????????????????????????????????????????????
                    showAlertDialog(ttl, msg);

                    reloadListView();

                    return;
                } else {
                    //???????????????????????????????????????????????????
                    //String ttl = "<キーワード>";
                    //String msg = "[" + mEditText1.getText().toString() + "]";
                    //showAlertDialog(ttl, msg);
                    //???????????????????????????????????????????????????

                    category_reloadListView(mEditText1.getText().toString());

                    mEditText1.setText("");

                    return;

                }
            }
        });

        // [＋]ボタンクリック
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                final Task task = (Task) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        reloadListView();
    }

    private void showAlertDialog(String ttl, String msg) {
        // AlertDialog.Builderクラスを使ってAlertDialogの準備をする
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(ttl);
        alertDialogBuilder.setMessage(msg);

        // 肯定ボタンに表示される文字列、押したときのリスナーを設定する
        alertDialogBuilder.setPositiveButton("OK", null);

        // AlertDialog1を作成して表示する
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void reloadListView() {
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING);
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    private void category_reloadListView(String category_KW) {

        //String ttl = "<category_KW>";
        //String msg = category_KW;
        //showAlertDialog(ttl, msg);

        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).equalTo("category", category_KW).findAll().sort("date", Sort.ASCENDING);
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }
}
