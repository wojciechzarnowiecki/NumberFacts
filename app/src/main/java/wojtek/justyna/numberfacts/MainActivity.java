package wojtek.justyna.numberfacts;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private final String REST_API_NUMBERS = "http://numbersapi.com/random/";
    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS FACT(TEXT VARCHAR);";
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = openOrCreateDatabase("numbersDB", MODE_PRIVATE, null);
        db.execSQL(CREATE_TABLE);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Button button_get = findViewById(R.id.getFacts_button);
        final TextView result_text = findViewById(R.id.text_result);
        result_text.setMovementMethod((new ScrollingMovementMethod()));

        button_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newText = getResponse(REST_API_NUMBERS);
                result_text.setText(newText);
            }
        });

        final EditText fromUser = findViewById(R.id.editNum);
        Button getByNumber = findViewById(R.id.getFactsByNumber_button);

        getByNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String answer =  fromUser.getText().toString();
                String newText = getResponse("http://numbersapi.com/"+answer);
                result_text.setText(newText);
            }
        });

        Button button_save = findViewById(R.id.saveToDB_buton);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayed_text = result_text.getText().toString()+"\n";
                db.execSQL("INSERT INTO FACT VALUES('"+displayed_text+"');");
                result_text.setText("DEAR USER, A NEW RECORD HAS BEEN ADDED TO YOUR LIST");
            }
        });

        Button button_showDB = findViewById(R.id.showDB_button);
        button_showDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result_text.setText("");
                StringBuilder resultOfDB = new StringBuilder();
                int i = 1;

                Cursor resultSet = db.rawQuery("SELECT * from FACT", null);
                resultSet.moveToFirst();

                if(resultSet.getCount()==0){
                    resultOfDB.append("DEAR USER, THE LIST IS EMPTY.\nADD NEW RECORDS WHEN READY");
                }

                do {
                    if(!(resultSet.getCount()==0)) {
                        resultOfDB.append(i + ". ");
                        resultOfDB.append(resultSet.getString(0) + "\n");
                        i++;
                    }
                }while (resultSet.moveToNext());

                result_text.setText(resultOfDB.toString());
            }
        });

        Button button_cleardb = findViewById(R.id.clearDB_button);

        button_cleardb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.execSQL("DELETE FROM FACT");
                result_text.setText("DEAR USER, THE LIST IS EMPTY.\nADD NEW RECORDS.");
            }
        });


    }
    public static String getResponse(String url) {
        InputStream inputStream = null;
        String result = "";

        try {
            HttpClient httpClient = new DefaultHttpClient();
            URI uri = new URL(url).toURI();
            HttpResponse httpResponse = httpClient.execute(new HttpGet(uri));

            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "DID NOT WORK!";

        } catch (Exception e) {
            Log.d("InputStream", e.getMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
