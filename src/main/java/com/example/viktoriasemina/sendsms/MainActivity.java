package com.example.viktoriasemina.sendsms;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int CONTACT_PICK_RESULT = 666;
    private static final String LOG_TAG = "my_tag";

    String mContactId;
    String mPhoneNumber;
    String mContactName;
    String mEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void pickPhone(View v) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICK_RESULT);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case CONTACT_PICK_RESULT:
                    Uri contactData = data.getData();
                    Cursor c =  getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToNext()) {
                        mContactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        mContactName = c.getString(c.getColumnIndexOrThrow(
                                ContactsContract.Contacts.DISPLAY_NAME));

                        String hasPhone = c.getString(c.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        Log.d(LOG_TAG, "name: " + mContactName);
                        Log.d(LOG_TAG, "hasPhone:" + hasPhone);
                        Log.d(LOG_TAG, "contactId:" + mContactId);

                        // если есть телефоны, получаем и выводим их
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ mContactId,
                                    null,
                                    null);

                            SmsManager smsManager = SmsManager.getDefault();

                            while (phones.moveToNext()) {
                                mPhoneNumber = phones.getString(phones.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                                Log.d(LOG_TAG, "телефон:" + mPhoneNumber);
                            }
                            try {
                                smsManager.sendTextMessage(mPhoneNumber, null, "Ваше сообщение здесь", null, null);
                                Toast.makeText(this, "SMS отправлено", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Ошибка при отправке!",
                                        Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                            phones.close();
                        }

                        // Достаем email-ы
                        Cursor emails = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + mContactId,
                                null,
                                null);
                        while (emails.moveToNext()) {
                            mEmail = emails.getString(
                                    emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            Log.d(LOG_TAG, "email:" + mEmail);
                        }
                        emails.close();
                    }
                    break;
            }

        } else {
            Log.d(LOG_TAG, "ERROR");
        }
    }
}